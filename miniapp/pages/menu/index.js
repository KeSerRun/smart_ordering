const bannerApi = require('../../api/banner');
const dishApi = require('../../api/dish');
const cartApi = require('../../api/cart');
const orderApi = require('../../api/order');
const { KEYS, get } = require('../../utils/storage');
const { isLoggedIn, wxLogin, phoneLogin } = require('../../utils/auth');
const { formatPrice } = require('../../utils/format');
const { bindTableByCode, ensureCurrentUserTableBinding, normalizeTableCode } = require('../../utils/table-binding');

const SPICE_LABEL_MAP = {
  0: '不辣',
  1: '微辣',
  2: '中辣',
  3: '重辣'
};

function normalizeId(v) {
  if (v === null || v === undefined) return '';
  return String(v);
}

function normalizeCategoryId(...values) {
  for (const value of values) {
    const normalized = normalizeId(value);
    if (normalized && normalized !== '0' && normalized !== 'null' && normalized !== 'undefined') {
      return normalized;
    }
  }
  return '0';
}

function parseIngredients(raw) {
  if (!raw) return [];

  if (Array.isArray(raw)) {
    return raw.map(item => String(item).trim()).filter(Boolean);
  }

  if (typeof raw === 'string') {
    const text = raw.trim();
    if (!text) return [];

    if ((text.startsWith('[') && text.endsWith(']')) || (text.startsWith('"') && text.endsWith('"'))) {
      try {
        const parsed = JSON.parse(text);
        if (Array.isArray(parsed)) {
          return parsed.map(item => String(item).trim()).filter(Boolean);
        }
      } catch (err) {
        // ignore json parse error and fallback to plain text split
      }
    }

    return text
      .split(/[、,，/]/)
      .map(item => item.trim())
      .filter(Boolean);
  }

  return [];
}

function extractDishIdFromOrderItem(item) {
  if (!item || typeof item !== 'object') return '';
  return normalizeId(item.dishId ?? item.id ?? '');
}

function buildSearchText(dish) {
  return [
    dish.name,
    dish.categoryName,
    dish.description,
    dish.spiceLabel,
    dish.ingredientsText
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();
}

function buildCategoryShortLabel(name) {
  const text = String(name || '').trim();
  if (!text) return '分';
  return text.length <= 2 ? text : text.slice(0, 2);
}

function pickCategoryImage(category) {
  if (!category || typeof category !== 'object') return '';
  return category.image || category.imageUrl || category.icon || '';
}

function pickDishImage(dish) {
  if (!dish || typeof dish !== 'object') return '';
  return dish.image || dish.imageUrl || dish.thumbnail || '';
}

function buildDishBrief(dish) {
  if (!dish || typeof dish !== 'object') return '点击查看菜品详情';
  return dish.description || dish.ingredientsText || '点击查看菜品详情';
}

function formatCountBadge(count) {
  const value = Number(count || 0);
  if (value <= 0) return '';
  return value > 99 ? '99+' : String(value);
}

function createEmptyCartSummary() {
  return {
    totalCount: 0,
    totalPrice: '0.00',
    totalCountText: ''
  };
}

function createEmptyCartState() {
  return {
    cartItems: [],
    cartSummary: createEmptyCartSummary(),
    allCategoryCartCount: 0,
    allCategoryCartCountText: '',
    cartSheetVisible: false
  };
}

Page({
  data: {
    statusBarHeight: 0,
    navBarHeight: 44,
    loggedIn: false,
    tableCode: '',
    table: null,
    menuHeroBanners: [],
    banners: [],
    showTableInput: false,
    categories: [],
    categoryScrollIntoView: '',
    dishMap: {},
    allDishList: [],
    recommendDishIds: [],
    activeCategoryId: null,
    activeCategoryName: '全部菜品',
    activeCategoryCount: 0,
    totalDishCount: 0,
    recommendDishCount: 0,
    orderedDishCount: 0,
    visibleDishCount: 0,
    dishList: [],
    keyword: '',
    quickFilter: 'all',
    orderedDishIds: [],
    cartItems: [],
    cartSummary: createEmptyCartSummary(),
    allCategoryCartCount: 0,
    allCategoryCartCountText: '',
    cartSheetVisible: false,
    cartUpdatingDishId: '',
    heroStatusText: '等待绑定桌台',
    menuHeroTitle: '选好菜，再确认下单',
    tableDisplayCode: '未绑定',
    emptyDishText: '没有找到匹配的菜品，换个关键词试试',
    detailVisible: false,
    detailDish: null,
    detailHasIngredients: false,
    detailQty: 1,
    detailRemark: '',
    cartFeedbackVisible: false,
    cartFeedbackText: '',
    showLoginPanel: false,
    agreeProtocol: false,
    loginCallback: null,
    sceneRefreshPending: false
  },

  onLoad(options) {
    this.initNavBar();
    const code = this.extractTableCode(options);
    if (code) this.setData({ tableCode: code, showTableInput: false, sceneRefreshPending: true });
  },

  onShow() {
    this.loadBanners();
    const loggedIn = isLoggedIn();
    const table = get(KEYS.TABLE);
    const sceneTableCode = normalizeTableCode(this.data.tableCode);
    const cachedTableCode = normalizeTableCode(table && table.code);
    const useCachedTable = !sceneTableCode || sceneTableCode === cachedTableCode;
    const activeTable = useCachedTable ? table : null;
    this.setData({
      loggedIn,
      table: activeTable || null,
      heroStatusText: activeTable ? '桌台已就绪' : '等待绑定桌台',
      menuHeroTitle: activeTable ? `${activeTable.name || activeTable.code || '当前桌台'} 正在点餐` : '选好菜，再确认下单',
      tableDisplayCode: activeTable && activeTable.code ? activeTable.code : '未绑定'
    });

    if (sceneTableCode && this.data.sceneRefreshPending) {
      this.setData(
        {
          orderedDishIds: [],
          orderedDishCount: 0,
          ...createEmptyCartState()
        },
        () => {
          this.updateDishListFromState();
        }
      );
      this.loadTable(this.data.tableCode);
      return;
    }

    if (activeTable) {
      this.setData({ tableCode: activeTable.code || '', showTableInput: false });
      this.loadMenu();
      this.loadOrderedDishIds();
      if (loggedIn) {
        this.loadCart();
      } else {
        this.setData({ ...createEmptyCartState() }, () => this.updateDishListFromState());
      }
    } else if (sceneTableCode) {
      this.loadTable(this.data.tableCode);
    } else {
      this.loadMenu();
      this.setData({ orderedDishIds: [], orderedDishCount: 0, ...createEmptyCartState() }, () => {
        this.updateDishListFromState();
      });
    }
  },

  onUnload() {
    if (this.cartFeedbackTimer) {
      clearTimeout(this.cartFeedbackTimer);
      this.cartFeedbackTimer = null;
    }
  },

  initNavBar() {
    const { statusBarHeight } = wx.getWindowInfo();
    const menuBtn = wx.getMenuButtonBoundingClientRect();
    this.setData({
      statusBarHeight,
      navBarHeight: (menuBtn.top - statusBarHeight) * 2 + menuBtn.height
    });
  },

  extractTableCode(options) {
    if (!options) return '';
    if (options.scene) {
      const decoded = decodeURIComponent(options.scene);
      if (decoded.includes('code=')) return decoded.split('code=')[1].split('&')[0];
      return decoded;
    }
    if (options.code) return String(options.code);
    if (options.q) {
      const decoded = decodeURIComponent(options.q);
      if (decoded.includes('code=')) return decoded.split('code=')[1].split('&')[0];
      return decoded;
    }
    return '';
  },

  onTableCodeInput(e) {
    this.setData({ tableCode: e.detail.value.trim() });
  },

  async loadTable(code) {
    if (!code) {
      wx.showToast({ title: '请输入桌号编码', icon: 'none' });
      return;
    }
    const requestCode = normalizeTableCode(code);
    wx.showLoading({ title: '加载桌台' });
    try {
      const { table: boundTable } = await bindTableByCode(code);
      if (requestCode !== normalizeTableCode(this.data.tableCode)) {
        return;
      }
      this.setData({
        table: boundTable,
        tableCode: boundTable.code || code,
        showTableInput: false,
        sceneRefreshPending: false,
        heroStatusText: '桌台已就绪',
        menuHeroTitle: `${boundTable.name || boundTable.code || '当前桌台'} 正在点餐`,
        tableDisplayCode: boundTable.code || '未绑定'
      });
      await this.loadMenu();
      await this.loadOrderedDishIds();
      if (isLoggedIn()) {
        await this.loadCart();
      } else {
        this.setData({ ...createEmptyCartState() }, () => this.updateDishListFromState());
      }
    } catch (err) {
      this.setData({ sceneRefreshPending: false });
      wx.showToast({ title: err.message || '桌台不存在', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  async loadBanners() {
    const [menuHeroBanners, menuBanners, homeBanners] = await Promise.all([
      bannerApi.getBannerList('MENU_HERO').catch(() => []),
      bannerApi.getBannerList('MENU_BANNER').catch(() => []),
      bannerApi.getBannerList('HOME').catch(() => [])
    ]);
    this.setData({
      menuHeroBanners: Array.isArray(menuHeroBanners) ? menuHeroBanners : [],
      banners: Array.isArray(menuBanners) && menuBanners.length > 0
        ? menuBanners
        : (Array.isArray(homeBanners) ? homeBanners : [])
    });
  },

  toggleTableInput() {
    this.setData({ showTableInput: !this.data.showTableInput });
  },

  async loadMenu() {
    try {
      const [categoryList, dishPayload] = await Promise.all([dishApi.getCategoryList(), dishApi.getDishList()]);
      const catList = Array.isArray(categoryList) ? categoryList : [];
      const categoryNameMap = catList.reduce((acc, item) => {
        acc[normalizeId(item.id)] = item.name;
        return acc;
      }, {});

      const groupedMap = {};
      const categoryCountMap = {};
      const allDishList = [];
      const sourceMap = Array.isArray(dishPayload) ? null : dishPayload;

      const pushDish = (dish, fallbackCategoryId = '0') => {
        const categoryKey = normalizeCategoryId(dish.categoryId, dish.category_id, fallbackCategoryId);
        const ingredientsList = parseIngredients(dish.ingredients);
        const spiceLevel = Number(dish.spiceLevel || 0);
        const normalizedDish = {
          ...dish,
          categoryId: categoryKey,
          categoryKey,
          categoryName: dish.categoryName || categoryNameMap[categoryKey] || '未分类',
          spiceLabel: SPICE_LABEL_MAP[spiceLevel] || '口味',
          ingredientsList,
          ingredientsText: ingredientsList.join(' '),
          imageView: pickDishImage(dish),
          briefText: buildDishBrief({
            ...dish,
            ingredientsText: ingredientsList.join(' ')
          }),
          soldOutFlag: Number(dish.status) === 0 || Number(dish.soldOut) === 1,
          _idStr: normalizeId(dish.id)
        };
        normalizedDish._searchText = buildSearchText(normalizedDish);

        if (!groupedMap[categoryKey]) groupedMap[categoryKey] = [];
        groupedMap[categoryKey].push(normalizedDish);
        categoryCountMap[categoryKey] = Number(categoryCountMap[categoryKey] || 0) + 1;
        allDishList.push(normalizedDish);
      };

      if (Array.isArray(dishPayload)) {
        dishPayload.forEach(item => pushDish(item));
      } else if (sourceMap && typeof sourceMap === 'object') {
        Object.keys(sourceMap).forEach(key => {
          const categoryId = normalizeCategoryId(key);
          (sourceMap[key] || []).forEach(item => pushDish(item, categoryId));
        });
      }

      const recommendDishIds = this.buildRecommendDishIds(allDishList);
      const categoryViewList = catList.map(item => {
        const id = normalizeId(item.id);
        return {
          ...item,
          _idStr: id,
          imageView: pickCategoryImage(item),
          shortLabel: buildCategoryShortLabel(item.name),
          dishCount: Number(categoryCountMap[id] || 0)
        };
      });
      const activeCategoryId = catList.length ? normalizeId(catList[0].id) : null;
      this.setData(
        {
          categories: categoryViewList,
          categoryScrollIntoView: activeCategoryId ? `category-${activeCategoryId}` : 'category-all',
          dishMap: groupedMap,
          allDishList,
          recommendDishIds,
          activeCategoryId,
          activeCategoryName: catList.length ? catList[0].name : '全部菜品',
          totalDishCount: allDishList.length,
          recommendDishCount: recommendDishIds.length
        },
        () => {
          this.updateDishListFromState();
        }
      );
    } catch (err) {
      console.error('加载菜单失败:', err);
      wx.showToast({ title: '菜单加载失败', icon: 'none' });
    }
  },

  async loadOrderedDishIds() {
    const currentTableId = Number((this.data.table || {}).id || 0);
    if (!currentTableId) {
      this.setData({ orderedDishIds: [], orderedDishCount: 0 }, () => {
        this.updateDishListFromState();
      });
      return;
    }

    try {
      const list = await orderApi.getTableOrders(currentTableId);
      if (currentTableId !== Number((this.data.table || {}).id || 0)) {
        return;
      }

      const orderedSet = new Set();
      (list || []).forEach(order => {
        (order.items || []).forEach(item => {
          const dishId = extractDishIdFromOrderItem(item);
          if (dishId) {
            orderedSet.add(dishId);
          }
        });
      });

      this.setData({ orderedDishIds: Array.from(orderedSet), orderedDishCount: orderedSet.size }, () => {
        this.updateDishListFromState();
      });
    } catch (err) {
      this.setData({ orderedDishIds: [], orderedDishCount: 0 }, () => {
        this.updateDishListFromState();
      });
    }
  },

  async loadCart() {
    const currentTableId = Number((this.data.table || {}).id || 0);
    if (!currentTableId) {
      this.setData({ ...createEmptyCartState() }, () => this.updateDishListFromState());
      return;
    }
    try {
      const cart = await cartApi.getCart(currentTableId);
      if (currentTableId !== Number((this.data.table || {}).id || 0)) {
        return;
      }
      const totalCount = Number(cart.totalCount || 0);
      const cartItems = this.enrichCartItems(Array.isArray(cart.items) ? cart.items : []);

      this.setData({
        cartItems,
        cartSummary: {
          totalCount,
          totalPrice: formatPrice(cart.totalPrice),
          totalCountText: formatCountBadge(totalCount)
        },
        cartSheetVisible: totalCount > 0 && this.data.cartSheetVisible
      }, () => this.updateDishListFromState());
    } catch (err) {
      this.setData({ ...createEmptyCartState() }, () => this.updateDishListFromState());
    }
  },

  selectCategory(e) {
    const id = normalizeId(e.currentTarget.dataset.id);
    const selected = (this.data.categories || []).find(item => normalizeId(item.id) === id);
    this.setData(
      {
        activeCategoryId: id,
        activeCategoryName: selected ? selected.name : '全部菜品',
        categoryScrollIntoView: `category-${id}`
      },
      () => this.updateDishListFromState()
    );
  },

  selectQuickFilter(e) {
    const filter = e.currentTarget.dataset.filter;
    if (!filter || filter === this.data.quickFilter) return;
    this.setData({ quickFilter: filter }, () => this.updateDishListFromState());
  },

  mergeDishList() {
    return this.data.allDishList || [];
  },

  getBaseDishList() {
    const id = this.data.activeCategoryId;
    if (id === null) return this.mergeDishList();
    return this.data.dishMap[id] || [];
  },

  isRecommended(dish) {
    if (!dish) return false;
    if (dish.recommend === 1 || dish.isRecommend === 1 || dish.recommended === 1) return true;
    const text = `${dish.tags || ''}${dish.tag || ''}${dish.label || ''}`;
    if (text.includes('推荐')) return true;
    return (this.data.recommendDishIds || []).includes(dish._idStr);
  },

  buildRecommendDishIds(list) {
    const explicit = (list || [])
      .filter(dish => dish.recommend === 1 || dish.isRecommend === 1 || dish.recommended === 1 || `${dish.tags || ''}${dish.tag || ''}${dish.label || ''}`.includes('推荐'))
      .map(dish => dish._idStr)
      .filter(Boolean);

    if (explicit.length) {
      return explicit;
    }

    return (list || [])
      .filter(dish => !dish.soldOutFlag)
      .sort((left, right) => {
        const leftPrep = Number(left.preparationTime || 999);
        const rightPrep = Number(right.preparationTime || 999);
        if (leftPrep !== rightPrep) return leftPrep - rightPrep;
        return Number(right.price || 0) - Number(left.price || 0);
      })
      .slice(0, 8)
      .map(dish => dish._idStr);
  },

  applyQuickFilter(list) {
    const orderedSet = new Set((this.data.orderedDishIds || []).map(normalizeId));
    const markedList = (list || []).map(dish => ({
      ...dish,
      _ordered: orderedSet.has(dish._idStr),
      _recommended: this.isRecommended(dish)
    }));

    if (this.data.quickFilter === 'recommend') {
      return markedList.filter(item => item._recommended);
    }

    if (this.data.quickFilter === 'ordered') {
      return markedList.filter(item => item._ordered);
    }

    return markedList;
  },

  applyKeywordFilter(list) {
    const keyword = (this.data.keyword || '').trim().toLowerCase();
    if (!keyword) {
      return list;
    }

    return (list || []).filter(item => {
      return item._searchText.includes(keyword);
    });
  },

  enrichCartItems(items) {
    const dishNameMap = {};
    const dishMap = (this.data.allDishList || []).reduce((acc, dish) => {
      if (dish && dish._idStr) {
        acc[dish._idStr] = dish;
      }
      if (dish && dish.name) {
        dishNameMap[dish.name] = dish;
      }
      return acc;
    }, {});

    return (items || []).map(item => {
      const dishId = extractDishIdFromOrderItem(item);
      const dish = dishMap[dishId] || dishNameMap[item.dishName] || {};
      const itemImage = item.imageView || item.dishImage || item.imageUrl || '';
      const usableItemImage = /^(https?:\/\/|\/assets\/)/.test(itemImage) ? itemImage : '';
      return {
        ...item,
        dishId,
        dishName: item.dishName || dish.name || '菜品',
        imageView: dish.imageView || usableItemImage,
        priceText: formatPrice(item.price ?? dish.price ?? 0),
        amountText: formatPrice(item.amount ?? item.subtotal ?? (Number(item.price || dish.price || 0) * Number(item.quantity || 0)))
      };
    });
  },

  buildCartCountState(cartItems = this.data.cartItems) {
    const dishCategoryMap = (this.data.allDishList || []).reduce((acc, item) => {
      if (item && item._idStr) {
        acc[item._idStr] = normalizeId(item.categoryId);
      }
      return acc;
    }, {});
    const dishCountMap = {};
    const categoryCountMap = {};
    let totalCount = 0;

    (cartItems || []).forEach(item => {
      const dishId = extractDishIdFromOrderItem(item);
      const quantity = Number(item.quantity || 0);
      if (!dishId || quantity <= 0) {
        return;
      }

      dishCountMap[dishId] = Number(dishCountMap[dishId] || 0) + quantity;
      totalCount += quantity;

      const fallbackCategoryId = normalizeCategoryId(item.categoryId, item.category_id, '');
      const categoryKey = dishCategoryMap[dishId] || fallbackCategoryId;
      if (categoryKey) {
        categoryCountMap[categoryKey] = Number(categoryCountMap[categoryKey] || 0) + quantity;
      }
    });

    return { dishCountMap, categoryCountMap, totalCount };
  },

  buildCategoryListWithCartCount(categoryCountMap) {
    return (this.data.categories || []).map(item => {
      const cartCount = Number(categoryCountMap[item._idStr] || 0);
      return {
        ...item,
        _cartCount: cartCount,
        _cartCountText: formatCountBadge(cartCount)
      };
    });
  },

  updateDishListFromState() {
    const cartItems = this.enrichCartItems(this.data.cartItems || []);
    const { dishCountMap, categoryCountMap, totalCount } = this.buildCartCountState(cartItems);
    const baseList = this.getBaseDishList();
    const filterList = this.applyQuickFilter(baseList);
    const dishList = this.applyKeywordFilter(filterList).map(item => {
      const cartCount = Number(dishCountMap[item._idStr] || 0);
      return {
        ...item,
        _cartCount: cartCount,
        _cartCountText: formatCountBadge(cartCount)
      };
    });
    this.setData({
      cartItems,
      categories: this.buildCategoryListWithCartCount(categoryCountMap),
      dishList,
      visibleDishCount: dishList.length,
      activeCategoryCount: baseList.length,
      allCategoryCartCount: totalCount,
      allCategoryCartCountText: formatCountBadge(totalCount)
    });
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value.trim() }, () => {
      this.updateDishListFromState();
    });
  },

  findDishById(dishId) {
    const normalizedId = normalizeId(dishId);
    if (!normalizedId) return null;

    // 优先从当前列表中取，避免弹层数据和页面展示数据不一致
    const currentDish = (this.data.dishList || []).find(item => normalizeId(item.id) === normalizedId);
    if (currentDish) return currentDish;

    return (this.data.allDishList || []).find(item => normalizeId(item.id) === normalizedId) || null;
  },

  openDetail(e) {
    const dishId = e.currentTarget.dataset.id;
    const dish = this.findDishById(dishId);
    if (!dish || dish.soldOutFlag) return;
    this.setData({
      detailVisible: true,
      detailDish: {
        ...dish,
        imageView: dish.imageView || pickDishImage(dish)
      },
      detailHasIngredients: !!(dish.ingredientsList && dish.ingredientsList.length),
      detailQty: 1,
      detailRemark: ''
    });
  },

  closeDetail() {
    this.setData({ detailVisible: false, detailDish: null, detailHasIngredients: false, detailQty: 1, detailRemark: '' });
  },

  showCartFeedback(text) {
    if (this.cartFeedbackTimer) {
      clearTimeout(this.cartFeedbackTimer);
    }

    this.setData({
      cartFeedbackVisible: true,
      cartFeedbackText: text || '已加入购物车'
    });

    this.cartFeedbackTimer = setTimeout(() => {
      this.setData({ cartFeedbackVisible: false, cartFeedbackText: '' });
      this.cartFeedbackTimer = null;
    }, 1400);
  },

  increaseDetailQty() {
    this.setData({ detailQty: Number(this.data.detailQty || 1) + 1 });
  },

  decreaseDetailQty() {
    const qty = Number(this.data.detailQty || 1);
    this.setData({ detailQty: Math.max(1, qty - 1) });
  },

  onDetailRemarkInput(e) {
    this.setData({ detailRemark: e.detail.value });
  },

  requireLogin(callback) {
    this.setData({ showLoginPanel: true, loginCallback: callback, agreeProtocol: false });
  },

  closeLoginPanel() {
    this.setData({ showLoginPanel: false, loginCallback: null, agreeProtocol: false });
  },

  toggleAgreeProtocol() {
    this.setData({ agreeProtocol: !this.data.agreeProtocol });
  },

  openUserAgreement() {
    wx.showModal({
      title: '用户协议',
      content: '登录前请阅读并同意《用户协议》。当前先使用说明弹窗占位，后续可接正式协议页。',
      showCancel: false
    });
  },

  openPrivacyPolicy() {
    wx.showModal({
      title: '隐私政策',
      content: '登录前请阅读并同意《隐私政策》。当前先使用说明弹窗占位，后续可接正式隐私政策页。',
      showCancel: false
    });
  },

  async handlePhoneLogin(e) {
    if (!this.data.agreeProtocol) {
      wx.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
      return;
    }
    if (!e.detail.code) {
      wx.showToast({ title: e.detail.errMsg || '请授权手机号', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '登录中', mask: true });
    try {
      const code = await wxLogin();
      await phoneLogin(code, e.detail.code);
      const reboundTable = await ensureCurrentUserTableBinding(this.data.table || get(KEYS.TABLE));
      this.setData({
        loggedIn: true,
        showLoginPanel: false,
        table: reboundTable || this.data.table || null
      });
      wx.showToast({ title: '登录成功', icon: 'none' });
      if (reboundTable) {
        this.setData({
          heroStatusText: '桌台已就绪',
          menuHeroTitle: `${reboundTable.name || reboundTable.code || '当前桌台'} 正在点餐`,
          tableDisplayCode: reboundTable.code || '未绑定'
        });
      }
      this.loadCart();
      const cb = this.data.loginCallback;
      if (cb) {
        this.setData({ loginCallback: null });
        setTimeout(cb, 300);
      }
    } catch (err) {
      wx.showToast({ title: err.message || '登录失败', icon: 'none', duration: 2000 });
    } finally {
      wx.hideLoading();
    }
  },

  handlePhoneLoginTap() {
    if (this.data.agreeProtocol) {
      return;
    }
    wx.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
  },

  async confirmDetailAdd() {
    if (!isLoggedIn()) {
      this.setData({ detailVisible: false });
      this.requireLogin(async () => {
        this.setData({ detailVisible: true });
        try {
          await this.addDetailDishToCart();
          this.showCartFeedback('已加入购物车');
          this.closeDetail();
        } catch (err) {
          wx.showToast({ title: err.message || '加入失败', icon: 'none' });
        }
      });
      return;
    }

    try {
      await this.addDetailDishToCart();
      this.showCartFeedback('已加入购物车');
      this.closeDetail();
    } catch (err) {
      wx.showToast({ title: err.message || '加入失败', icon: 'none' });
    }
  },

  async submitDetailOrder() {
    if (!isLoggedIn()) {
      this.setData({ detailVisible: false });
      this.requireLogin(async () => {
        this.setData({ detailVisible: true });
        try {
          await this.addDetailDishToCart();
          this.closeDetail();
          wx.navigateTo({ url: '/pages/cart/index' });
        } catch (err) {
          wx.showToast({ title: err.message || '操作失败', icon: 'none' });
        }
      });
      return;
    }

    try {
      await this.addDetailDishToCart();
      this.closeDetail();
      wx.navigateTo({ url: '/pages/cart/index' });
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    }
  },

  /**
   * 将当前弹窗菜品加入购物车
   * @returns {Promise<void>}
   */
  async addDetailDishToCart() {
    const dish = this.data.detailDish;
    if (!dish || dish.soldOutFlag) {
      throw new Error('该菜品已售罄');
    }

    if (!this.data.table) {
      throw new Error('请先关联桌台');
    }

    await cartApi.addCartItem(this.data.table.id, dish.id, Number(this.data.detailQty || 1), this.data.detailRemark || '');
    await this.loadCart();
  },

  toggleCartSheet() {
    if (!this.data.cartSummary.totalCount) {
      wx.showToast({ title: '还没有选择菜品', icon: 'none' });
      return;
    }
    this.setData({ cartSheetVisible: !this.data.cartSheetVisible });
  },

  closeCartSheet() {
    this.setData({ cartSheetVisible: false });
  },

  async changeCartQuantity(e) {
    const item = e.currentTarget.dataset.item || {};
    const delta = Number(e.currentTarget.dataset.delta || 0);
    const dishId = extractDishIdFromOrderItem(item);
    const nextQuantity = Number(item.quantity || 0) + delta;
    if (!dishId || !delta || this.data.cartUpdatingDishId) return;

    this.setData({ cartUpdatingDishId: dishId });
    try {
      if (nextQuantity <= 0) {
        await cartApi.removeCartItem(dishId, this.data.table.id);
      } else {
        await cartApi.updateCartItem(dishId, this.data.table.id, nextQuantity);
      }
      await this.loadCart();
    } catch (err) {
      wx.showToast({ title: err.message || '更新购物车失败', icon: 'none' });
    } finally {
      this.setData({ cartUpdatingDishId: '' });
    }
  },

  clearMenuCart() {
    if (!this.data.table || !this.data.cartSummary.totalCount) return;
    wx.showModal({
      title: '清空已选菜品',
      content: '清空后需要重新选择，确定继续吗？',
      confirmText: '清空',
      confirmColor: '#4f6845',
      success: async res => {
        if (!res.confirm) return;
        try {
          await cartApi.clearCart(this.data.table.id);
          this.setData({ cartSheetVisible: false });
          await this.loadCart();
          wx.showToast({ title: '已清空', icon: 'none' });
        } catch (err) {
          wx.showToast({ title: err.message || '清空失败', icon: 'none' });
        }
      }
    });
  },

  goCart() {
    if (!isLoggedIn()) {
      this.requireLogin(() => {
        wx.navigateTo({ url: '/pages/cart/index' });
      });
      return;
    }
    wx.navigateTo({ url: '/pages/cart/index' });
  },

  goTableEntry() {
    wx.navigateTo({ url: '/pages/table/index' });
  },

  openBanner(e) {
    const banner = e.currentTarget.dataset.banner || {};
    const targetPath = banner.targetPath || '';
    const actionType = Number(banner.actionType || 0);

    if (!targetPath || actionType === 0) return;

    if (actionType === 2) {
      wx.switchTab({ url: targetPath });
      return;
    }

    wx.navigateTo({ url: targetPath });
  },

  noop() {},

  onPullDownRefresh() {
    const tasks = [this.loadMenu()];
    if (this.data.table) {
      tasks.push(this.loadOrderedDishIds());
      if (this.data.loggedIn) {
        tasks.push(this.loadCart());
      }
    }

    Promise.allSettled(tasks).finally(() => {
      wx.stopPullDownRefresh();
    });
  }
});
