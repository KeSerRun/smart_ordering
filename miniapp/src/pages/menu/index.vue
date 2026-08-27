<template>
  <view class="menu-page">
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>

    <view class="top-card">
      <view class="store-visual" :style="{ paddingTop: (statusBarHeight + 8) + 'px', minHeight: (statusBarHeight + navBarHeight + 92) + 'px' }">
        <image v-if="menuHeroBanners.length > 0" class="store-visual__image" :src="menuHeroBanners[0].imageUrl" mode="aspectFill" />
        <view class="store-visual__wash"></view>
        <view class="store-visual__content">
          <view class="store-visual__eyebrow">堂食点单 · 现点现做</view>
          <view class="store-visual__title">{{ menuHeroTitle }}</view>
          <view class="store-visual__meta">
            <view class="store-visual__status-dot"></view>
            <text>{{ heroStatusText }}</text>
            <text class="store-visual__divider">·</text>
            <text>{{ table ? '桌台 ' + tableDisplayCode : '请先关联桌台' }}</text>
          </view>
        </view>
      </view>

      <view class="menu-hero">
        <view class="hero-banner-stage" v-if="banners.length > 0">
          <swiper class="hero-banner-swiper" circular autoplay interval="4200" duration="450"
            indicator-dots indicator-color="rgba(255, 245, 240, 0.24)" indicator-active-color="#fff7f2">
            <swiper-item v-for="b in banners" :key="b.id">
              <view class="hero-banner-card" @tap="openBanner(b)">
                <image class="hero-banner-image" :src="b.imageUrl" mode="aspectFill" lazy-load />
                <view class="hero-banner-overlay"></view>
                <view class="hero-banner-copy">
                  <view class="hero-banner-title">{{ b.title }}</view>
                  <view class="hero-banner-subtitle" v-if="b.subtitle">{{ b.subtitle }}</view>
                </view>
              </view>
            </swiper-item>
          </swiper>
        </view>

        <view class="filter-card compact">
          <view class="search-shell">
            <view class="search-icon">⌕</view>
            <input class="search-input" placeholder="搜索菜品、口味、配料" :value="keyword" @input="onKeywordInput" />
          </view>
          <view class="quick-filter-row">
            <view class="quick-filter" :class="{ active: quickFilter === 'all' }" @tap="selectQuickFilter('all')">全部</view>
            <view class="quick-filter" :class="{ active: quickFilter === 'recommend' }" @tap="selectQuickFilter('recommend')">推荐</view>
            <view class="quick-filter" :class="{ active: quickFilter === 'ordered' }" @tap="selectQuickFilter('ordered')">点过</view>
          </view>
        </view>

        <view class="table-entry-notice" v-if="!table">
          <view class="table-entry-copy">
            <view class="table-entry-title">还没有关联桌台</view>
            <view class="table-entry-sub">扫码或输入桌台编码后，才能加入购物车和支付。</view>
          </view>
          <button class="table-entry-btn" @tap="goTableEntry">关联桌台</button>
        </view>
      </view>
    </view>

    <view class="layout">
      <scroll-view class="left-side" scroll-y scroll-with-animation :scroll-into-view="categoryScrollIntoView" :show-scrollbar="false">
        <view
          v-for="item in categories"
          :key="item._idStr"
          :id="'category-' + item._idStr"
          class="cat-item"
          :class="{ active: activeCategoryId === item._idStr }"
          @tap="selectCategory(item._idStr)"
        >
          <view class="cat-item__icon-wrap">
            <view class="cat-item__icon">
              <image v-if="item.imageView" class="cat-item__image" :src="item.imageView" mode="aspectFill" lazy-load />
              <text v-else>{{ item.shortLabel }}</text>
            </view>
            <text class="cat-item__badge" v-if="item._cartCount > 0">{{ item._cartCountText }}</text>
          </view>
          <view class="cat-item__copy">
            <text class="cat-item__label">{{ item.name }}</text>
          </view>
          <text class="cat-item__active-dot" v-if="activeCategoryId === item._idStr"></text>
        </view>
        <view class="cat-scroll-tail">
          <view class="cat-scroll-tail__line"></view>
          <view class="cat-scroll-tail__text">继续下滑查看更多分类</view>
        </view>
      </scroll-view>

      <scroll-view class="right-side" scroll-y :show-scrollbar="false">
        <view class="right-surface">
          <view class="dish-card" v-for="item in dishList" :key="item.id" @tap="openDetail(item.id)">
            <view class="dish-card__media">
              <image class="thumb" :src="item.imageView" mode="aspectFill" lazy-load />
              <text class="dish-card__badge" v-if="item._recommended">推荐</text>
            </view>
            <view class="info">
              <view class="dish-meta-row">
                <text class="meta-pill spice" v-if="item.spiceLabel">{{ item.spiceLabel }}</text>
                <text class="meta-pill" v-if="item.preparationTime">{{ item.preparationTime }} 分钟</text>
              </view>

              <view class="dish-topline">
                <view class="dish-name">{{ item.name }}</view>
              </view>

              <view class="dish-sub">{{ item.briefText }}</view>

              <view class="dish-bottom">
                <view class="dish-bottom__main">
                  <view class="dish-tags">
                    <text class="mini-tag had" v-if="item._ordered">点过</text>
                    <text class="soldout-tag" v-if="item.soldOutFlag">售罄</text>
                    <text class="dish-sales">月售{{ item.monthlySales || 0 }}</text>
                  </view>
                  <view class="dish-price-row">
                    <view class="dish-price">¥{{ item.price }}</view>
                    <text class="dish-unit">/份起</text>
                  </view>
                </view>
                <button class="spec-btn" :class="{ disabled: item.soldOutFlag }" :disabled="item.soldOutFlag" @tap.stop="openDetail(item.id)">
                  <text class="spec-btn__text">{{ item.soldOutFlag ? '售罄' : '选规格' }}</text>
                  <text class="spec-btn__badge" v-if="item._cartCount > 0">{{ item._cartCountText }}</text>
                </button>
              </view>
            </view>
          </view>

          <empty-state v-if="visibleDishCount === 0" text="没有找到匹配的菜品，换个关键词试试" icon="/static/tabbar/menu.png" />
        </view>
      </scroll-view>
    </view>

    <view class="cart-feedback" :class="{ show: cartFeedbackVisible }" v-if="cartFeedbackVisible">{{ cartFeedbackText }}</view>

    <view class="cart-bar" v-if="table">
      <view class="bar-info" @tap="toggleCartSheet">
        <view class="bar-icon">
          <view class="bar-icon__handle"></view>
          <view class="bar-icon__bag"></view>
          <text class="bar-icon__badge" v-if="cartSummary.totalCount > 0">{{ cartSummary.totalCountText }}</text>
        </view>
        <view class="bar-copy">
          <view class="bar-sub">已点 {{ cartSummary.totalCount }} 份菜品</view>
          <view class="bar-price">¥{{ cartSummary.totalPrice }}</view>
        </view>
      </view>
      <view class="bar-btn-wrap">
        <button class="bar-btn" @tap="goCart">去结算</button>
      </view>
    </view>

    <view class="cart-sheet-mask" :class="{ show: cartSheetVisible }" v-if="cartSheetVisible" @tap="closeCartSheet"></view>
    <view class="cart-sheet" :class="{ show: cartSheetVisible }" v-if="cartSheetVisible">
      <view class="cart-sheet__head">
        <view class="cart-sheet__select">
          <view class="cart-sheet__check">✓</view>
          <text>已选菜品</text>
          <text class="cart-sheet__count">{{ cartSummary.totalCount }} 份</text>
        </view>
        <view class="cart-sheet__clear" @tap="clearMenuCart">
          <view class="trash-icon"></view>
          <text>清空</text>
        </view>
      </view>
      <scroll-view class="cart-sheet__list" scroll-y :show-scrollbar="false">
        <view class="cart-sheet__item" v-for="item in cartItems" :key="item.dishId">
          <view class="cart-sheet__check cart-sheet__check--small">✓</view>
          <image class="cart-sheet__image" :src="item.imageView" mode="aspectFill" />
          <view class="cart-sheet__info">
            <view class="cart-sheet__name">{{ item.dishName }}</view>
            <view class="cart-sheet__remark" v-if="item.remark">{{ item.remark }}</view>
            <view class="cart-sheet__price">¥{{ item.priceText }}</view>
          </view>
          <view class="cart-sheet__stepper">
            <view class="cart-sheet__step" :class="{ disabled: cartUpdatingDishId }" @tap="changeCartQuantity(item, -1)">−</view>
            <text class="cart-sheet__quantity">{{ item.quantity }}</text>
            <view class="cart-sheet__step cart-sheet__step--plus" :class="{ disabled: cartUpdatingDishId }" @tap="changeCartQuantity(item, 1)">＋</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="mask sheet-mask" v-if="detailVisible" @tap="closeDetail">
      <view class="popup sheet-popup" @tap.stop>
        <view class="popup-handle"></view>
        <view class="popup-scroll">
          <view class="popup-hero">
            <image class="popup-img" :src="detailDish.imageView" mode="aspectFill" />
            <view class="popup-copy">
              <view class="popup-tag-row">
                <text class="popup-tag">{{ detailDish.categoryName }}</text>
                <text class="popup-tag spice">{{ detailDish.spiceLabel }}</text>
                <text class="popup-tag" v-if="detailDish.preparationTime">{{ detailDish.preparationTime }} 分钟</text>
              </view>
              <view class="popup-title">{{ detailDish.name }}</view>
              <view class="popup-price">¥{{ detailDish.price }}</view>
              <view class="popup-meta" v-if="detailDish.description">{{ detailDish.description }}</view>
            </view>
          </view>

          <view class="popup-section" v-if="detailHasIngredients">
            <view class="section-head">
              <view class="section-title">配料参考</view>
              <view class="section-sub">下单前先看主要食材，避免重复确认。</view>
            </view>
            <view class="ingredient-row">
              <text class="ingredient-chip" v-for="(ig, i) in detailDish.ingredientsList" :key="i">{{ ig }}</text>
            </view>
          </view>

          <view class="popup-section">
            <view class="section-head">
              <view class="section-title">选数量</view>
              <view class="section-sub">确认份数后加入购物车，购物车里再统一下单。</view>
            </view>
            <view class="qty-panel">
              <view class="qty-btn qty-btn-minus" @tap="decreaseDetailQty">
                <text class="qty-icon">−</text>
              </view>
              <view class="qty-num">{{ detailQty }}</view>
              <view class="qty-btn qty-btn-plus" @tap="increaseDetailQty">
                <text class="qty-icon">＋</text>
              </view>
            </view>
          </view>

          <view class="popup-section">
            <view class="section-head">
              <view class="section-title">口味要求</view>
              <view class="section-sub">例如少辣、不要香菜、先上热菜。</view>
            </view>
            <textarea class="remark-input" maxlength="120" auto-height placeholder="请输入你的备注"
              :value="detailRemark" @input="onDetailRemarkInput" />
          </view>
        </view>

        <view class="popup-actions">
          <button class="act-add" @tap="confirmDetailAdd">加入购物车</button>
        </view>
      </view>
    </view>

    <view class="mask" v-if="showLoginPanel" @tap="closeLoginPanel">
      <view class="login-popup" @tap.stop>
        <view class="login-popup-icon">登录</view>
        <view class="login-popup-title">手机号登录</view>
        <view class="login-popup-desc">登录后可下单、加入购物车</view>
        <view class="agreement-row" @tap="toggleAgreeProtocol">
          <view class="agreement-check" :class="{ checked: agreeProtocol }">
            <text v-if="agreeProtocol">✓</text>
          </view>
          <view class="agreement-text">
            已阅读并同意
            <text class="agreement-link" @tap.stop="openUserAgreement">《用户协议》</text>
            和
            <text class="agreement-link" @tap.stop="openPrivacyPolicy">《隐私政策》</text>
          </view>
        </view>
        <button
          class="btn-primary login-popup-btn"
          :class="{ 'login-popup-btn--disabled': !agreeProtocol }"
          :open-type="agreeProtocol ? 'getPhoneNumber' : ''"
          @tap="handlePhoneLoginTap"
          @getphonenumber="handlePhoneLogin"
        >
          手机号快捷登录
        </button>
        <view class="login-popup-cancel" @tap="closeLoginPanel">暂不登录</view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 点餐页
import { reactive, toRefs } from 'vue';
import { onLoad, onShow, onUnload, onPullDownRefresh } from '@dcloudio/uni-app';
import { getBannerList } from '@/api/banner';
import { getCategoryList, getDishList } from '@/api/dish';
import { getCart, addCartItem, updateCartItem, removeCartItem, clearCart } from '@/api/cart';
import { getTableOrders } from '@/api/order';
import { KEYS, get } from '@/utils/storage';
import { isLoggedIn, wxLogin, phoneLogin } from '@/utils/auth';
import { formatPrice } from '@/utils/format';
import { bindTableByCode, ensureCurrentUserTableBinding, normalizeTableCode } from '@/utils/table-binding';
import { calcNavBar } from '@/utils/nav';

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
    return raw.map((item) => String(item).trim()).filter(Boolean);
  }
  if (typeof raw === 'string') {
    const text = raw.trim();
    if (!text) return [];
    if ((text.startsWith('[') && text.endsWith(']')) || (text.startsWith('"') && text.endsWith('"'))) {
      try {
        const parsed = JSON.parse(text);
        if (Array.isArray(parsed)) {
          return parsed.map((item) => String(item).trim()).filter(Boolean);
        }
      } catch (err) { /* ignore json parse error and fallback to plain text split */ }
    }
    return text.split(/[、,，/]/).map((item) => item.trim()).filter(Boolean);
  }
  return [];
}

function extractDishIdFromOrderItem(item) {
  if (!item || typeof item !== 'object') return '';
  return normalizeId(item.dishId ?? item.id ?? '');
}

function buildSearchText(dish) {
  return [dish.name, dish.categoryName, dish.description, dish.spiceLabel, dish.ingredientsText]
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
  return { totalCount: 0, totalPrice: '0.00', totalCountText: '' };
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

// ===== 响应式状态 =====
const state = reactive({
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
});

const {
  statusBarHeight, navBarHeight, loggedIn, tableCode, table, menuHeroBanners, banners,
  categories, categoryScrollIntoView, dishMap, allDishList, recommendDishIds, activeCategoryId,
  activeCategoryCount, visibleDishCount, dishList, keyword, quickFilter, orderedDishIds,
  cartItems, cartSummary, cartSheetVisible, cartUpdatingDishId, heroStatusText, menuHeroTitle,
  tableDisplayCode, detailVisible, detailDish, detailHasIngredients, detailQty, detailRemark,
  cartFeedbackVisible, cartFeedbackText, showLoginPanel, agreeProtocol, loginCallback
} = toRefs(state);

let cartFeedbackTimer = null;

// ===== 生命周期 =====
onLoad((options) => {
  initNavBar();
  const code = extractTableCode(options);
  if (code) {
    state.tableCode = code;
    state.showTableInput = false;
    state.sceneRefreshPending = true;
  }
});

onShow(() => {
  loadBanners();
  const login = isLoggedIn();
  const cachedTable = get(KEYS.TABLE);
  const sceneTableCode = normalizeTableCode(state.tableCode);
  const cachedTableCode = normalizeTableCode(cachedTable && cachedTable.code);
  const useCachedTable = !sceneTableCode || sceneTableCode === cachedTableCode;
  const activeTable = useCachedTable ? cachedTable : null;
  state.loggedIn = login;
  state.table = activeTable || null;
  state.heroStatusText = activeTable ? '桌台已就绪' : '等待绑定桌台';
  state.menuHeroTitle = activeTable ? `${activeTable.name || activeTable.code || '当前桌台'} 正在点餐` : '选好菜，再确认下单';
  state.tableDisplayCode = activeTable && activeTable.code ? activeTable.code : '未绑定';

  if (sceneTableCode && state.sceneRefreshPending) {
    Object.assign(state, { orderedDishIds: [], orderedDishCount: 0, ...createEmptyCartState() });
    updateDishListFromState();
    loadTable(state.tableCode);
    return;
  }

  if (activeTable) {
    state.tableCode = activeTable.code || '';
    state.showTableInput = false;
    loadMenu();
    loadOrderedDishIds();
    if (login) {
      loadCart();
    } else {
      Object.assign(state, { ...createEmptyCartState() });
      updateDishListFromState();
    }
  } else if (sceneTableCode) {
    loadTable(state.tableCode);
  } else {
    loadMenu();
    Object.assign(state, { orderedDishIds: [], orderedDishCount: 0, ...createEmptyCartState() });
    updateDishListFromState();
  }
});

onUnload(() => {
  if (cartFeedbackTimer) {
    clearTimeout(cartFeedbackTimer);
    cartFeedbackTimer = null;
  }
});

onPullDownRefresh(() => {
  const tasks = [loadMenu()];
  if (state.table) {
    tasks.push(loadOrderedDishIds());
    if (state.loggedIn) {
      tasks.push(loadCart());
    }
  }
  Promise.allSettled(tasks).finally(() => {
    uni.stopPullDownRefresh();
  });
});

// ===== 导航栏 =====
function initNavBar() {
  const { statusBarHeight, navBarHeight } = calcNavBar();
  state.statusBarHeight = statusBarHeight;
  state.navBarHeight = navBarHeight;
}

function extractTableCode(options) {
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
}

// ===== 桌台 =====
async function loadTable(code) {
  if (!code) {
    uni.showToast({ title: '请输入桌号编码', icon: 'none' });
    return;
  }
  const requestCode = normalizeTableCode(code);
  uni.showLoading({ title: '加载桌台' });
  try {
    const { table: boundTable } = await bindTableByCode(code);
    if (requestCode !== normalizeTableCode(state.tableCode)) return;
    state.table = boundTable;
    state.tableCode = boundTable.code || code;
    state.showTableInput = false;
    state.sceneRefreshPending = false;
    state.heroStatusText = '桌台已就绪';
    state.menuHeroTitle = `${boundTable.name || boundTable.code || '当前桌台'} 正在点餐`;
    state.tableDisplayCode = boundTable.code || '未绑定';
    await loadMenu();
    await loadOrderedDishIds();
    if (isLoggedIn()) {
      await loadCart();
    } else {
      Object.assign(state, { ...createEmptyCartState() });
      updateDishListFromState();
    }
  } catch (err) {
    state.sceneRefreshPending = false;
    uni.showToast({ title: err.message || '桌台不存在', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
}

// ===== 轮播 =====
async function loadBanners() {
  const [menuHeroBannersRes, menuBannersRes, homeBannersRes] = await Promise.all([
    getBannerList('MENU_HERO').catch(() => []),
    getBannerList('MENU_BANNER').catch(() => []),
    getBannerList('HOME').catch(() => [])
  ]);
  state.menuHeroBanners = Array.isArray(menuHeroBannersRes) ? menuHeroBannersRes : [];
  state.banners = Array.isArray(menuBannersRes) && menuBannersRes.length > 0
    ? menuBannersRes
    : (Array.isArray(homeBannersRes) ? homeBannersRes : []);
}

// ===== 菜单加载 =====
async function loadMenu() {
  try {
    const [categoryList, dishPayload] = await Promise.all([getCategoryList(), getDishList()]);
    const catList = Array.isArray(categoryList) ? categoryList : [];
    const categoryNameMap = catList.reduce((acc, item) => {
      acc[normalizeId(item.id)] = item.name;
      return acc;
    }, {});

    const groupedMap = {};
    const categoryCountMap = {};
    const allDishes = [];
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
        briefText: buildDishBrief({ ...dish, ingredientsText: ingredientsList.join(' ') }),
        soldOutFlag: Number(dish.status) === 0 || Number(dish.soldOut) === 1,
        _idStr: normalizeId(dish.id)
      };
      normalizedDish._searchText = buildSearchText(normalizedDish);

      if (!groupedMap[categoryKey]) groupedMap[categoryKey] = [];
      groupedMap[categoryKey].push(normalizedDish);
      categoryCountMap[categoryKey] = Number(categoryCountMap[categoryKey] || 0) + 1;
      allDishes.push(normalizedDish);
    };

    if (Array.isArray(dishPayload)) {
      dishPayload.forEach((item) => pushDish(item));
    } else if (sourceMap && typeof sourceMap === 'object') {
      Object.keys(sourceMap).forEach((key) => {
        const categoryId = normalizeCategoryId(key);
        (sourceMap[key] || []).forEach((item) => pushDish(item, categoryId));
      });
    }

    const recommendIds = buildRecommendDishIds(allDishes);
    const categoryViewList = catList.map((item) => {
      const id = normalizeId(item.id);
      return {
        ...item,
        _idStr: id,
        imageView: pickCategoryImage(item),
        shortLabel: buildCategoryShortLabel(item.name),
        dishCount: Number(categoryCountMap[id] || 0)
      };
    });
    const activeCatId = catList.length ? normalizeId(catList[0].id) : null;
    state.categories = categoryViewList;
    state.categoryScrollIntoView = activeCatId ? `category-${activeCatId}` : 'category-all';
    state.dishMap = groupedMap;
    state.allDishList = allDishes;
    state.recommendDishIds = recommendIds;
    state.activeCategoryId = activeCatId;
    state.activeCategoryName = catList.length ? catList[0].name : '全部菜品';
    state.totalDishCount = allDishes.length;
    state.recommendDishCount = recommendIds.length;
    updateDishListFromState();
  } catch (err) {
    console.error('加载菜单失败:', err);
    uni.showToast({ title: '菜单加载失败', icon: 'none' });
  }
}

async function loadOrderedDishIds() {
  const currentTableId = Number((state.table || {}).id || 0);
  if (!currentTableId) {
    state.orderedDishIds = [];
    state.orderedDishCount = 0;
    updateDishListFromState();
    return;
  }
  try {
    const list = await getTableOrders(currentTableId);
    if (currentTableId !== Number((state.table || {}).id || 0)) return;
    const orderedSet = new Set();
    (list || []).forEach((order) => {
      (order.items || []).forEach((item) => {
        const dishId = extractDishIdFromOrderItem(item);
        if (dishId) orderedSet.add(dishId);
      });
    });
    state.orderedDishIds = Array.from(orderedSet);
    state.orderedDishCount = orderedSet.size;
    updateDishListFromState();
  } catch (err) {
    state.orderedDishIds = [];
    state.orderedDishCount = 0;
    updateDishListFromState();
  }
}

async function loadCart() {
  const currentTableId = Number((state.table || {}).id || 0);
  if (!currentTableId) {
    Object.assign(state, { ...createEmptyCartState() });
    updateDishListFromState();
    return;
  }
  try {
    const cart = await getCart(currentTableId);
    if (currentTableId !== Number((state.table || {}).id || 0)) return;
    const totalCount = Number(cart.totalCount || 0);
    const items = enrichCartItems(Array.isArray(cart.items) ? cart.items : []);
    state.cartItems = items;
    state.cartSummary = {
      totalCount,
      totalPrice: formatPrice(cart.totalPrice),
      totalCountText: formatCountBadge(totalCount)
    };
    state.cartSheetVisible = totalCount > 0 && state.cartSheetVisible;
    updateDishListFromState();
  } catch (err) {
    Object.assign(state, { ...createEmptyCartState() });
    updateDishListFromState();
  }
}

// ===== 筛选/列表 =====
function selectCategory(id) {
  const selected = (state.categories || []).find((item) => normalizeId(item.id) === id);
  state.activeCategoryId = id;
  state.activeCategoryName = selected ? selected.name : '全部菜品';
  state.categoryScrollIntoView = `category-${id}`;
  updateDishListFromState();
}

function selectQuickFilter(filter) {
  if (!filter || filter === state.quickFilter) return;
  state.quickFilter = filter;
  updateDishListFromState();
}

function mergeDishList() {
  return state.allDishList || [];
}

function getBaseDishList() {
  const id = state.activeCategoryId;
  if (id === null) return mergeDishList();
  return state.dishMap[id] || [];
}

function isRecommended(dish) {
  if (!dish) return false;
  if (dish.recommend === 1 || dish.isRecommend === 1 || dish.recommended === 1) return true;
  const text = `${dish.tags || ''}${dish.tag || ''}${dish.label || ''}`;
  if (text.includes('推荐')) return true;
  return (state.recommendDishIds || []).includes(dish._idStr);
}

function buildRecommendDishIds(list) {
  const explicit = (list || [])
    .filter((dish) => dish.recommend === 1 || dish.isRecommend === 1 || dish.recommended === 1 ||
      `${dish.tags || ''}${dish.tag || ''}${dish.label || ''}`.includes('推荐'))
    .map((dish) => dish._idStr)
    .filter(Boolean);
  if (explicit.length) return explicit;
  return (list || [])
    .filter((dish) => !dish.soldOutFlag)
    .sort((left, right) => {
      const leftPrep = Number(left.preparationTime || 999);
      const rightPrep = Number(right.preparationTime || 999);
      if (leftPrep !== rightPrep) return leftPrep - rightPrep;
      return Number(right.price || 0) - Number(left.price || 0);
    })
    .slice(0, 8)
    .map((dish) => dish._idStr);
}

function applyQuickFilter(list) {
  const orderedSet = new Set((state.orderedDishIds || []).map(normalizeId));
  const markedList = (list || []).map((dish) => ({
    ...dish,
    _ordered: orderedSet.has(dish._idStr),
    _recommended: isRecommended(dish)
  }));
  if (state.quickFilter === 'recommend') {
    return markedList.filter((item) => item._recommended);
  }
  if (state.quickFilter === 'ordered') {
    return markedList.filter((item) => item._ordered);
  }
  return markedList;
}

function applyKeywordFilter(list) {
  const keywordText = (state.keyword || '').trim().toLowerCase();
  if (!keywordText) return list;
  return (list || []).filter((item) => item._searchText.includes(keywordText));
}

function enrichCartItems(items) {
  const dishNameMap = {};
  const dishMapLocal = (state.allDishList || []).reduce((acc, dish) => {
    if (dish && dish._idStr) acc[dish._idStr] = dish;
    if (dish && dish.name) dishNameMap[dish.name] = dish;
    return acc;
  }, {});
  return (items || []).map((item) => {
    const dishId = extractDishIdFromOrderItem(item);
    const dish = dishMapLocal[dishId] || dishNameMap[item.dishName] || {};
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
}

function buildCartCountState(cartItemsRef = state.cartItems) {
  const dishCategoryMap = (state.allDishList || []).reduce((acc, item) => {
    if (item && item._idStr) acc[item._idStr] = normalizeId(item.categoryId);
    return acc;
  }, {});
  const dishCountMap = {};
  const categoryCountMap = {};
  let totalCount = 0;
  (cartItemsRef || []).forEach((item) => {
    const dishId = extractDishIdFromOrderItem(item);
    const quantity = Number(item.quantity || 0);
    if (!dishId || quantity <= 0) return;
    dishCountMap[dishId] = Number(dishCountMap[dishId] || 0) + quantity;
    totalCount += quantity;
    const fallbackCategoryId = normalizeCategoryId(item.categoryId, item.category_id, '');
    const categoryKey = dishCategoryMap[dishId] || fallbackCategoryId;
    if (categoryKey) categoryCountMap[categoryKey] = Number(categoryCountMap[categoryKey] || 0) + quantity;
  });
  return { dishCountMap, categoryCountMap, totalCount };
}

function buildCategoryListWithCartCount(categoryCountMap) {
  return (state.categories || []).map((item) => {
    const cartCount = Number(categoryCountMap[item._idStr] || 0);
    return { ...item, _cartCount: cartCount, _cartCountText: formatCountBadge(cartCount) };
  });
}

function updateDishListFromState() {
  const refItems = enrichCartItems(state.cartItems || []);
  const { dishCountMap, categoryCountMap, totalCount } = buildCartCountState(refItems);
  const baseList = getBaseDishList();
  const filterList = applyQuickFilter(baseList);
  const list = applyKeywordFilter(filterList).map((item) => {
    const cartCount = Number(dishCountMap[item._idStr] || 0);
    return { ...item, _cartCount: cartCount, _cartCountText: formatCountBadge(cartCount) };
  });
  state.cartItems = refItems;
  state.categories = buildCategoryListWithCartCount(categoryCountMap);
  state.dishList = list;
  state.visibleDishCount = list.length;
  state.activeCategoryCount = baseList.length;
  state.allCategoryCartCount = totalCount;
  state.allCategoryCartCountText = formatCountBadge(totalCount);
}

function onKeywordInput(e) {
  state.keyword = (e.detail.value || '').trim();
  updateDishListFromState();
}

// ===== 详情弹窗 =====
function findDishById(dishId) {
  const normalizedId = normalizeId(dishId);
  if (!normalizedId) return null;
  const currentDish = (state.dishList || []).find((item) => normalizeId(item.id) === normalizedId);
  if (currentDish) return currentDish;
  return (state.allDishList || []).find((item) => normalizeId(item.id) === normalizedId) || null;
}

function openDetail(dishId) {
  const dish = findDishById(dishId);
  if (!dish || dish.soldOutFlag) return;
  state.detailVisible = true;
  state.detailDish = { ...dish, imageView: dish.imageView || pickDishImage(dish) };
  state.detailHasIngredients = !!(dish.ingredientsList && dish.ingredientsList.length);
  state.detailQty = 1;
  state.detailRemark = '';
}

function closeDetail() {
  state.detailVisible = false;
  state.detailDish = null;
  state.detailHasIngredients = false;
  state.detailQty = 1;
  state.detailRemark = '';
}

function showCartFeedback(text) {
  if (cartFeedbackTimer) clearTimeout(cartFeedbackTimer);
  state.cartFeedbackVisible = true;
  state.cartFeedbackText = text || '已加入购物车';
  cartFeedbackTimer = setTimeout(() => {
    state.cartFeedbackVisible = false;
    state.cartFeedbackText = '';
    cartFeedbackTimer = null;
  }, 1400);
}

function increaseDetailQty() {
  state.detailQty = Number(state.detailQty || 1) + 1;
}

function decreaseDetailQty() {
  const qty = Number(state.detailQty || 1);
  state.detailQty = Math.max(1, qty - 1);
}

function onDetailRemarkInput(e) {
  state.detailRemark = e.detail.value;
}

function requireLogin(callback) {
  state.showLoginPanel = true;
  state.loginCallback = callback;
  state.agreeProtocol = false;
}

function closeLoginPanel() {
  state.showLoginPanel = false;
  state.loginCallback = null;
  state.agreeProtocol = false;
}

function toggleAgreeProtocol() {
  state.agreeProtocol = !state.agreeProtocol;
}

function openUserAgreement() {
  uni.showModal({
    title: '用户协议',
    content: '登录前请阅读并同意《用户协议》。当前先使用说明弹窗占位，后续可接正式协议页。',
    showCancel: false
  });
}

function openPrivacyPolicy() {
  uni.showModal({
    title: '隐私政策',
    content: '登录前请阅读并同意《隐私政策》。当前先使用说明弹窗占位，后续可接正式隐私政策页。',
    showCancel: false
  });
}

async function handlePhoneLogin(e) {
  if (!state.agreeProtocol) {
    uni.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
    return;
  }
  if (!e.detail.code) {
    uni.showToast({ title: e.detail.errMsg || '请授权手机号', icon: 'none' });
    return;
  }
  uni.showLoading({ title: '登录中', mask: true });
  try {
    const code = await wxLogin();
    await phoneLogin(code, e.detail.code);
    const reboundTable = await ensureCurrentUserTableBinding(state.table || get(KEYS.TABLE));
    state.loggedIn = true;
    state.showLoginPanel = false;
    state.table = reboundTable || state.table || null;
    uni.showToast({ title: '登录成功', icon: 'none' });
    if (reboundTable) {
      state.heroStatusText = '桌台已就绪';
      state.menuHeroTitle = `${reboundTable.name || reboundTable.code || '当前桌台'} 正在点餐`;
      state.tableDisplayCode = reboundTable.code || '未绑定';
    }
    loadCart();
    const cb = state.loginCallback;
    if (cb) {
      state.loginCallback = null;
      setTimeout(cb, 300);
    }
  } catch (err) {
    uni.showToast({ title: err.message || '登录失败', icon: 'none', duration: 2000 });
  } finally {
    uni.hideLoading();
  }
}

function handlePhoneLoginTap() {
  if (state.agreeProtocol) return;
  uni.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
}

// ===== 加入购物车 =====
async function addDetailDishToCart() {
  const dish = state.detailDish;
  if (!dish || dish.soldOutFlag) throw new Error('该菜品已售罄');
  if (!state.table) throw new Error('请先关联桌台');
  await addCartItem(state.table.id, dish.id, Number(state.detailQty || 1), state.detailRemark || '');
  await loadCart();
}

async function confirmDetailAdd() {
  if (!isLoggedIn()) {
    state.detailVisible = false;
    requireLogin(async () => {
      state.detailVisible = true;
      try {
        await addDetailDishToCart();
        showCartFeedback('已加入购物车');
        closeDetail();
      } catch (err) {
        uni.showToast({ title: err.message || '加入失败', icon: 'none' });
      }
    });
    return;
  }
  try {
    await addDetailDishToCart();
    showCartFeedback('已加入购物车');
    closeDetail();
  } catch (err) {
    uni.showToast({ title: err.message || '加入失败', icon: 'none' });
  }
}

async function submitDetailOrder() {
  if (!isLoggedIn()) {
    state.detailVisible = false;
    requireLogin(async () => {
      state.detailVisible = true;
      try {
        await addDetailDishToCart();
        closeDetail();
        uni.navigateTo({ url: '/pages/cart/index' });
      } catch (err) {
        uni.showToast({ title: err.message || '操作失败', icon: 'none' });
      }
    });
    return;
  }
  try {
    await addDetailDishToCart();
    closeDetail();
    uni.navigateTo({ url: '/pages/cart/index' });
  } catch (err) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' });
  }
}

// ===== 购物车 =====
function toggleCartSheet() {
  if (!state.cartSummary.totalCount) {
    uni.showToast({ title: '还没有选择菜品', icon: 'none' });
    return;
  }
  state.cartSheetVisible = !state.cartSheetVisible;
}

function closeCartSheet() {
  state.cartSheetVisible = false;
}

async function changeCartQuantity(item, delta) {
  const dishId = extractDishIdFromOrderItem(item);
  const nextQuantity = Number(item.quantity || 0) + delta;
  if (!dishId || !delta || state.cartUpdatingDishId) return;
  state.cartUpdatingDishId = dishId;
  try {
    if (nextQuantity <= 0) {
      await removeCartItem(dishId, state.table.id);
    } else {
      await updateCartItem(dishId, state.table.id, nextQuantity);
    }
    await loadCart();
  } catch (err) {
    uni.showToast({ title: err.message || '更新购物车失败', icon: 'none' });
  } finally {
    state.cartUpdatingDishId = '';
  }
}

function clearMenuCart() {
  if (!state.table || !state.cartSummary.totalCount) return;
  uni.showModal({
    title: '清空已选菜品',
    content: '清空后需要重新选择，确定继续吗？',
    confirmText: '清空',
    confirmColor: '#4f6845',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await clearCart(state.table.id);
        state.cartSheetVisible = false;
        await loadCart();
        uni.showToast({ title: '已清空', icon: 'none' });
      } catch (err) {
        uni.showToast({ title: err.message || '清空失败', icon: 'none' });
      }
    }
  });
}

function goCart() {
  if (!isLoggedIn()) {
    requireLogin(() => {
      uni.navigateTo({ url: '/pages/cart/index' });
    });
    return;
  }
  uni.navigateTo({ url: '/pages/cart/index' });
}

function goTableEntry() {
  uni.navigateTo({ url: '/pages/table/index' });
}

function openBanner(banner) {
  const targetPath = banner.targetPath || '';
  const actionType = Number(banner.actionType || 0);
  if (!targetPath || actionType === 0) return;
  if (actionType === 2) {
    uni.switchTab({ url: targetPath });
    return;
  }
  uni.navigateTo({ url: targetPath });
}
</script>

<style scoped>
.menu-page {
  --menu-accent: #07c160;
  --menu-accent-deep: #057a3d;
  --menu-accent-soft: #e8f8ee;
  --menu-bg: #f5efe8;
  --menu-surface: rgba(255, 253, 249, 0.96);
  --menu-border: rgba(7, 193, 96, 0.10);
  --menu-text: #261718;
  --menu-subtext: #7f6e69;
  --menu-muted: #a1908c;

  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.10), transparent 24%),
    radial-gradient(circle at bottom left, rgba(236, 219, 191, 0.22), transparent 30%),
    linear-gradient(180deg, #fbf8f4 0%, var(--menu-bg) 58%, #ece3db 100%);
}

.page-glow {
  position: absolute;
  border-radius: 999rpx;
  pointer-events: none;
  filter: blur(8rpx);
}

.page-glow-a {
  top: 40rpx;
  right: -88rpx;
  width: 260rpx;
  height: 260rpx;
  background: rgba(7, 193, 96, 0.14);
}

.page-glow-b {
  left: -90rpx;
  bottom: 180rpx;
  width: 300rpx;
  height: 300rpx;
  background: rgba(206, 175, 127, 0.18);
}

.top-card {
  position: sticky;
  top: 0;
  z-index: 30;
  flex-shrink: 0;
  padding: 0;
}

.store-visual {
  position: relative;
  min-height: 262rpx;
  overflow: hidden;
  padding-left: 24rpx;
  padding-right: 24rpx;
  padding-bottom: 22rpx;
  border-radius: 0;
  background: linear-gradient(135deg, #31482d 0%, #6f8c5f 55%, #c5d9ad 100%);
  box-shadow: none;
  box-sizing: border-box;
}

.store-visual__image,
.store-visual__wash {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.store-visual__image {
  display: block;
}

.store-visual__wash {
  background:
    linear-gradient(90deg, rgba(24, 35, 21, 0.76) 0%, rgba(29, 43, 25, 0.40) 54%, rgba(31, 48, 27, 0.12) 100%),
    linear-gradient(0deg, rgba(17, 27, 15, 0.52) 0%, transparent 64%);
}

.store-visual__content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  height: 100%;
  min-height: 150rpx;
}

.store-visual__eyebrow {
  width: fit-content;
  padding: 7rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(246, 251, 241, 0.18);
  color: rgba(250, 253, 247, 0.92);
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}

.store-visual__title {
  margin-top: 12rpx;
  max-width: 72%;
  color: #fbfdf8;
  font-size: 35rpx;
  line-height: 1.22;
  font-weight: 800;
  text-shadow: 0 4rpx 14rpx rgba(20, 31, 17, 0.24);
}

.store-visual__meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 10rpx;
  color: rgba(247, 252, 243, 0.82);
  font-size: 20rpx;
}

.store-visual__status-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 999rpx;
  background: #d7efac;
  box-shadow: 0 0 0 5rpx rgba(215, 239, 172, 0.15);
}

.store-visual__divider {
  opacity: 0.58;
}

.menu-hero {
  position: relative;
  margin: 8rpx 10rpx 0;
  padding: 0 4rpx;
}

.hero-banner-swiper {
  height: 146rpx;
}

.hero-banner-card {
  position: relative;
  height: 146rpx;
  overflow: hidden;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.16);
  box-shadow: 0 14rpx 28rpx rgba(70, 78, 57, 0.12);
}

.hero-banner-image {
  width: 100%;
  height: 100%;
  display: block;
}

.hero-banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(108deg, rgba(18, 34, 22, 0.76) 0%, rgba(18, 34, 22, 0.18) 86%);
}

.hero-banner-copy {
  position: absolute;
  left: 22rpx;
  right: 22rpx;
  bottom: 22rpx;
  z-index: 2;
}

.hero-banner-title {
  color: #fff7f2;
  font-size: 30rpx;
  line-height: 1.18;
  font-weight: 800;
}

.hero-banner-subtitle {
  margin-top: 8rpx;
  width: 72%;
  color: rgba(255, 247, 242, 0.84);
  font-size: 20rpx;
  line-height: 1.55;
}

.filter-card {
  margin-top: 8rpx;
  padding: 10rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.78);
  border: 1rpx solid rgba(95, 127, 82, 0.10);
}

.search-caption {
  color: var(--menu-text);
  font-size: 22rpx;
  font-weight: 800;
}

.search-caption-sub {
  margin-top: 6rpx;
  color: var(--menu-subtext);
  font-size: 21rpx;
}

.search-shell {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
  border-radius: 14rpx;
  background: #ffffff;
  border: 1rpx solid rgba(95, 127, 82, 0.16);
  padding: 0 14rpx;
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.88);
}

.search-icon {
  margin-right: 10rpx;
  color: #07a857;
  font-size: 28rpx;
}

.search-input {
  flex: 1;
  height: 58rpx;
  font-size: 22rpx;
  color: var(--menu-text);
}

.quick-filter-row {
  display: flex;
  gap: 6rpx;
  margin-top: 6rpx;
}

.quick-filter {
  flex: 1;
  min-height: 38rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2e8;
  color: #687462;
  font-size: 17rpx;
  font-weight: 700;
  border: 1rpx solid transparent;
}

.quick-filter.active {
  color: #f7fcf3;
  background: linear-gradient(135deg, #07c160 0%, #057a3d 100%);
  border-color: rgba(113, 25, 29, 0.62);
  box-shadow: 0 10rpx 20rpx rgba(113, 25, 29, 0.18);
}

.table-entry-notice {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-top: 10rpx;
  padding: 12rpx;
  border-radius: 18rpx;
  background:
    linear-gradient(135deg, rgba(245, 252, 247, 0.96) 0%, rgba(235, 248, 239, 0.92) 100%);
  border: 1rpx solid rgba(7, 193, 96, 0.18);
  box-shadow: 0 12rpx 24rpx rgba(7, 193, 96, 0.08);
  box-sizing: border-box;
}

.table-entry-copy {
  flex: 1;
  min-width: 0;
}

.table-entry-title {
  color: #1f3a28;
  font-size: 22rpx;
  line-height: 1.3;
  font-weight: 800;
}

.table-entry-sub {
  margin-top: 4rpx;
  color: #6f8478;
  font-size: 18rpx;
  line-height: 1.35;
}

.table-entry-btn {
  flex-shrink: 0;
  width: 132rpx;
  height: 52rpx;
  line-height: 52rpx;
  margin: 0;
  padding: 0;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.98);
  color: #07a857;
  font-size: 21rpx;
  font-weight: 800;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  box-shadow: 0 8rpx 18rpx rgba(7, 193, 96, 0.08);
}

.layout {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 6rpx;
  padding: 6rpx 8rpx 0;
  box-sizing: border-box;
}

.left-side {
  position: relative;
  width: 166rpx;
  min-width: 166rpx;
  max-width: 166rpx;
  height: 100%;
  min-height: 0;
  border-radius: 20rpx;
  background: rgba(255, 252, 247, 0.7);
  border: 1rpx solid rgba(255, 255, 255, 0.52);
  box-shadow: 0 14rpx 28rpx rgba(70, 78, 57, 0.06);
  box-sizing: border-box;
}

.cat-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  align-items: center;
  justify-content: center;
  margin: 6rpx 8rpx 0;
  padding: 10rpx 6rpx;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
}

.cat-item__icon-wrap {
  position: relative;
}

.cat-item__icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7efeb;
  color: #07c160;
  font-size: 17rpx;
  font-weight: 800;
  overflow: hidden;
}

.cat-item__badge {
  position: absolute;
  top: -6rpx;
  right: -10rpx;
  min-width: 26rpx;
  height: 26rpx;
  padding: 0 6rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #2b3d26 0%, #435b3b 100%);
  color: #ffffff;
  font-size: 16rpx;
  line-height: 26rpx;
  font-weight: 800;
  text-align: center;
  box-shadow: 0 6rpx 14rpx rgba(43, 61, 38, 0.16);
  box-sizing: border-box;
}

.cat-item__image {
  width: 100%;
  height: 100%;
  display: block;
}

.cat-item__copy {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-width: 0;
}

.cat-item__active-dot {
  width: 8rpx;
  height: 8rpx;
  margin-left: 0;
  border-radius: 999rpx;
  flex-shrink: 0;
  background: linear-gradient(135deg, #6b8c5d 0%, #46643e 100%);
  box-shadow: 0 0 0 4rpx rgba(95, 127, 82, 0.08);
}

.cat-item__label {
  color: #33402f;
  font-size: 18rpx;
  font-weight: 700;
  line-height: 1.3;
  text-align: center;
  white-space: normal;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.cat-item__count {
  color: var(--menu-muted);
  font-size: 20rpx;
}

.cat-item.active {
  background: linear-gradient(180deg, rgba(244, 251, 246, 0.98) 0%, rgba(234, 247, 238, 0.96) 100%);
  border-color: rgba(7, 193, 96, 0.16);
  box-shadow: 0 10rpx 18rpx rgba(5, 122, 61, 0.08);
  transform: translateX(2rpx);
}

.cat-item.active::before {
  content: '';
  position: absolute;
  left: -2rpx;
  top: 9rpx;
  bottom: 9rpx;
  width: 4rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #18d071 0%, #057a3d 100%);
}

.cat-item.active .cat-item__icon {
  color: #fff6f1;
  background: linear-gradient(135deg, #07c160 0%, #057a3d 100%);
}

.cat-item.active .cat-item__label {
  color: #21301d;
}

.cat-scroll-tail {
  position: relative;
  margin: 4rpx 8rpx 0;
  padding: 12rpx 6rpx 88rpx;
}

.cat-scroll-tail__line {
  width: 28rpx;
  height: 6rpx;
  margin: 0 auto 10rpx;
  border-radius: 999rpx;
  background: rgba(95, 127, 82, 0.22);
}

.cat-scroll-tail__text {
  text-align: center;
  color: #8f9a89;
  font-size: 15rpx;
  line-height: 1.5;
}

.right-side {
  flex: 1;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
  padding-bottom: 0;
}

.right-surface {
  min-height: 100%;
  padding: 6rpx 10rpx calc(180rpx + env(safe-area-inset-bottom));
  border-radius: 20rpx;
  background: rgba(255, 252, 247, 0.74);
  border: 1rpx solid rgba(255, 255, 255, 0.56);
  box-shadow: 0 16rpx 30rpx rgba(67, 72, 57, 0.06);
  box-sizing: border-box;
}

.dish-card {
  display: flex;
  align-items: stretch;
  gap: 10rpx;
  margin-top: 10rpx;
  padding: 10rpx;
  min-height: 0;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid rgba(95, 127, 82, 0.08);
  box-shadow: 0 16rpx 30rpx rgba(79, 86, 62, 0.06);
  box-sizing: border-box;
}

.dish-card__media {
  position: relative;
  width: 144rpx;
  min-width: 144rpx;
  flex-shrink: 0;
}

.dish-card__badge {
  position: absolute;
  left: 10rpx;
  top: 10rpx;
  min-width: 52rpx;
  height: 28rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  background: rgba(31, 43, 29, 0.84);
  color: #ffffff;
  font-size: 16rpx;
  line-height: 28rpx;
  font-weight: 800;
  text-align: center;
  box-sizing: border-box;
}

.thumb {
  width: 144rpx;
  min-width: 144rpx;
  max-width: 144rpx;
  height: 144rpx;
  border-radius: 18rpx;
  background: #eef1ea;
  display: block;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding: 2rpx 0;
  justify-content: space-between;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.dish-topline {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
}

.dish-name {
  flex: 1;
  min-width: 0;
  color: #1d271b;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-sales {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26rpx;
  padding: 0 8rpx;
  border-radius: 999rpx;
  background: #f3f1ea;
  color: #8d9389;
  font-size: 15rpx;
  line-height: 1.2;
}

.dish-price {
  flex-shrink: 0;
  color: var(--price);
  font-size: 26rpx;
  font-weight: 800;
  line-height: 1.2;
}

.dish-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6rpx;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26rpx;
  padding: 0 7rpx;
  border-radius: 999rpx;
  background: #f1f8ef;
  color: #886c64;
  font-size: 15rpx;
}

.meta-pill.spice {
  background: #f9eedf;
  color: #a0732b;
}

.dish-sub {
  min-height: 0;
  color: #7c8678;
  font-size: 18rpx;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 6rpx;
  margin-top: auto;
  padding-top: 4rpx;
  min-width: 0;
}

.dish-bottom__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.dish-tags {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
  min-width: 0;
}

.dish-price-row {
  display: flex;
  align-items: flex-end;
  gap: 4rpx;
}

.dish-unit {
  color: #9ca48f;
  font-size: 18rpx;
  line-height: 1.4;
}

.mini-tag,
.soldout-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26rpx;
  padding: 0 7rpx;
  border-radius: 999rpx;
  font-size: 15rpx;
  font-weight: 800;
}

.mini-tag.rec {
  color: #f7fcf3;
  background: linear-gradient(135deg, #07c160 0%, #057a3d 100%);
}

.mini-tag.had {
  color: #07a857;
  background: #e8f8ee;
}

.soldout-tag {
  color: #fff;
  background: #98a2b3;
}

.spec-btn {
  position: relative;
  flex-shrink: 0;
  width: 96rpx !important;
  min-width: 96rpx !important;
  height: 50rpx !important;
  line-height: 50rpx !important;
  padding: 0 10rpx !important;
  margin: 0 !important;
  margin-left: 8rpx !important;
  border-radius: 999rpx;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
  font-size: 18rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
  box-sizing: border-box;
}

.spec-btn__text {
  color: inherit;
  font-size: inherit;
  font-weight: inherit;
  line-height: inherit;
}

.spec-btn__badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 6rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #2b3d26 0%, #435b3b 100%);
  color: #ffffff;
  font-size: 16rpx;
  line-height: 28rpx;
  font-weight: 800;
  text-align: center;
  box-shadow: 0 6rpx 14rpx rgba(43, 61, 38, 0.18);
  box-sizing: border-box;
}

.spec-btn.disabled {
  color: #98a2b3;
  border-color: rgba(152, 162, 179, 0.36);
  background: #f2f4f7;
}

.cart-feedback {
  position: fixed;
  right: 42rpx;
  bottom: 148rpx;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(5, 122, 61, 0.94);
  color: #f7fcf3;
  font-size: 22rpx;
  font-weight: 700;
  box-shadow: 0 16rpx 30rpx rgba(49, 74, 45, 0.18);
  opacity: 0;
  transform: translateY(14rpx) scale(0.96);
}

.cart-feedback.show {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.cart-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 8rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(255, 251, 246, 0.96);
  border: 1rpx solid rgba(255, 255, 255, 0.82);
  box-shadow: 0 18rpx 34rpx rgba(55, 66, 48, 0.12);
  backdrop-filter: blur(18rpx);
  box-sizing: border-box;
  z-index: 100;
}

.cart-sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(17, 21, 15, 0.48);
}

.cart-sheet {
  position: fixed;
  left: 10rpx;
  right: 10rpx;
  bottom: 108rpx;
  z-index: 90;
  overflow: hidden;
  border-radius: 28rpx 28rpx 18rpx 18rpx;
  background: #fbfcf8;
  box-shadow: 0 -22rpx 60rpx rgba(22, 31, 19, 0.22);
  transform-origin: bottom center;
  animation: cart-sheet-in 220ms ease-out both;
}

@keyframes cart-sheet-in {
  from {
    opacity: 0;
    transform: translateY(24rpx) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.cart-sheet__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 78rpx;
  padding: 0 24rpx;
  background: #f1f3ed;
  border-bottom: 1rpx solid rgba(77, 96, 69, 0.10);
  box-sizing: border-box;
}

.cart-sheet__select,
.cart-sheet__clear {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.cart-sheet__select {
  color: #273224;
  font-size: 24rpx;
  font-weight: 800;
}

.cart-sheet__count {
  color: #82907d;
  font-size: 19rpx;
  font-weight: 600;
}

.cart-sheet__check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30rpx;
  height: 30rpx;
  border-radius: 999rpx;
  background: #59734d;
  color: #ffffff;
  font-size: 18rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.cart-sheet__check--small {
  width: 26rpx;
  height: 26rpx;
  font-size: 15rpx;
}

.cart-sheet__clear {
  min-height: 56rpx;
  color: #7d8878;
  font-size: 21rpx;
}

.trash-icon {
  position: relative;
  width: 18rpx;
  height: 20rpx;
  border: 2rpx solid currentColor;
  border-top: 0;
  border-radius: 0 0 4rpx 4rpx;
  box-sizing: border-box;
}

.trash-icon::before {
  content: '';
  position: absolute;
  left: -4rpx;
  top: -6rpx;
  width: 22rpx;
  height: 2rpx;
  border-radius: 999rpx;
  background: currentColor;
}

.cart-sheet__list {
  max-height: 500rpx;
}

.cart-sheet__item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 126rpx;
  padding: 16rpx 22rpx;
  border-bottom: 1rpx solid rgba(77, 96, 69, 0.08);
  box-sizing: border-box;
}

.cart-sheet__item:last-child {
  border-bottom: 0;
}

.cart-sheet__image {
  width: 82rpx;
  height: 82rpx;
  border-radius: 14rpx;
  background: #e8ece4;
  flex-shrink: 0;
}

.cart-sheet__info {
  flex: 1;
  min-width: 0;
}

.cart-sheet__name {
  color: #263123;
  font-size: 24rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-sheet__remark {
  margin-top: 3rpx;
  color: #899484;
  font-size: 18rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-sheet__price {
  margin-top: 6rpx;
  color: #283724;
  font-size: 25rpx;
  font-weight: 800;
}

.cart-sheet__stepper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.cart-sheet__step {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  border: 2rpx solid #607656;
  color: #43543d;
  font-size: 28rpx;
  line-height: 1;
  box-sizing: border-box;
}

.cart-sheet__step--plus {
  border-color: #4f6845;
  background: #4f6845;
  color: #ffffff;
}

.cart-sheet__step.disabled {
  opacity: 0.46;
}

.cart-sheet__quantity {
  min-width: 28rpx;
  color: #344130;
  font-size: 23rpx;
  font-weight: 700;
  text-align: center;
}

.bar-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 8rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(248, 248, 244, 0.96);
}

.bar-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(180deg, #eef9f2 0%, #dff4e7 100%);
  flex-shrink: 0;
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  overflow: visible;
}

.bar-icon__handle {
  position: absolute;
  top: 12rpx;
  width: 22rpx;
  height: 12rpx;
  border: 3rpx solid #0a9f4a;
  border-bottom: 0;
  border-radius: 16rpx 16rpx 0 0;
  box-sizing: border-box;
}

.bar-icon__bag {
  position: absolute;
  top: 20rpx;
  width: 24rpx;
  height: 19rpx;
  border-radius: 8rpx 8rpx 10rpx 10rpx;
  background: linear-gradient(180deg, #07c160 0%, #059b4d 100%);
  box-shadow: 0 5rpx 10rpx rgba(7, 193, 96, 0.16);
}

.bar-icon__bag::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 5rpx;
  width: 10rpx;
  height: 2rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(-50%);
}

.bar-icon__badge {
  position: absolute;
  top: -6rpx;
  right: -8rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 6rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #2b3d26 0%, #435b3b 100%);
  color: #ffffff;
  font-size: 16rpx;
  line-height: 28rpx;
  font-weight: 800;
  text-align: center;
  box-shadow: 0 6rpx 14rpx rgba(43, 61, 38, 0.18);
  box-sizing: border-box;
}

.bar-copy {
  flex: 1;
  min-width: 0;
}

.bar-btn-wrap {
  flex: 0 0 auto;
  width: 132rpx;
  display: flex;
  align-items: stretch;
}

.bar-sub {
  color: #64725f;
  font-size: 20rpx;
  line-height: 1.4;
}

.bar-price {
  margin-top: 2rpx;
  color: var(--price);
  font-size: 28rpx;
  font-weight: 800;
}

.bar-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-width: 0;
  height: 68rpx;
  line-height: 1;
  border-radius: 999rpx;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  padding: 0 20rpx;
  margin: 0;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 18rpx rgba(7, 193, 96, 0.18);
}

.mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(17, 21, 15, 0.42);
  z-index: 300;
}

.sheet-mask {
  display: flex;
  align-items: flex-end;
}

.sheet-popup {
  width: 100%;
  border-radius: 32rpx 32rpx 0 0;
  background: linear-gradient(180deg, #fbfcf8 0%, #f7f4ec 100%);
  box-shadow: 0 -24rpx 64rpx rgba(23, 31, 20, 0.18);
  max-height: 88vh;
  overflow: hidden;
}

.popup {
  padding: 18rpx 18rpx 22rpx;
  box-sizing: border-box;
}

.popup-scroll {
  max-height: calc(88vh - 132rpx);
  overflow-y: auto;
}

.popup-handle {
  width: 86rpx;
  height: 8rpx;
  margin: 2rpx auto 16rpx;
  border-radius: 999rpx;
  background: rgba(100, 114, 95, 0.24);
}

.popup-hero {
  display: flex;
  gap: 18rpx;
}

.popup-img {
  width: 212rpx;
  height: 212rpx;
  border-radius: 22rpx;
  background: #eef1ea;
  flex-shrink: 0;
}

.popup-copy {
  flex: 1;
  min-width: 0;
}

.popup-tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.popup-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: #e8f8ee;
  color: #07c160;
  font-size: 20rpx;
  font-weight: 700;
}

.popup-tag.spice {
  background: #f9eedf;
  color: #a0732b;
}

.popup-title {
  margin-top: 14rpx;
  color: #1f2b1d;
  font-size: 34rpx;
  line-height: 1.3;
  font-weight: 800;
}

.popup-price {
  margin-top: 8rpx;
  color: var(--price);
  font-size: 38rpx;
  font-weight: 800;
}

.popup-meta {
  margin-top: 12rpx;
  color: #687362;
  font-size: 23rpx;
  line-height: 1.7;
}

.popup-section {
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.74);
  border: 1rpx solid rgba(95, 127, 82, 0.10);
}

.section-head {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.section-title {
  color: #243121;
  font-size: 26rpx;
  font-weight: 800;
}

.section-sub {
  color: #788373;
  font-size: 22rpx;
  line-height: 1.5;
}

.ingredient-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.ingredient-chip {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e8f8ee;
  color: #07a857;
  font-size: 22rpx;
}

.qty-panel {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 16rpx;
  padding: 10rpx 12rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #ffffff 0%, #f0f4ea 100%);
  border: 1rpx solid rgba(95, 127, 82, 0.12);
  box-sizing: border-box;
}

.qty-btn {
  width: 68rpx;
  min-width: 68rpx;
  height: 68rpx;
  line-height: 68rpx;
  margin: 0;
  padding: 0;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid rgba(95, 127, 82, 0.12);
  box-sizing: border-box;
}

.qty-btn::after {
  border: none;
}

.qty-btn-minus {
  background: #fbfcf9;
  color: #6b7667;
}

.qty-btn-plus {
  background: linear-gradient(135deg, #07c160 0%, #057a3d 100%);
  color: #f7fcf3;
  border-color: transparent;
  box-shadow: 0 10rpx 18rpx rgba(5, 122, 61, 0.20);
}

.qty-icon {
  line-height: 1;
  font-size: 34rpx;
  font-weight: 800;
}

.qty-num {
  min-width: 72rpx;
  text-align: center;
  color: #233021;
  font-size: 30rpx;
  font-weight: 800;
}

.remark-input {
  width: 100%;
  min-height: 132rpx;
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  box-sizing: border-box;
  background: #ffffff;
  color: #1f2b1d;
  font-size: 24rpx;
  line-height: 1.7;
  border: 1rpx solid rgba(95, 127, 82, 0.12);
}

.popup-actions {
  display: block;
  margin-top: 20rpx;
  padding-top: 12rpx;
  padding-bottom: max(env(safe-area-inset-bottom), 4rpx);
  background: linear-gradient(180deg, rgba(247, 244, 236, 0) 0%, rgba(247, 244, 236, 0.92) 28%, #f7f4ec 100%);
}

.popup-actions button {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  margin: 0;
  border-radius: 20rpx;
  font-size: 26rpx;
  font-weight: 800;
}

.act-add {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18);
}

.login-popup {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  padding: 40rpx 34rpx 34rpx;
  border-radius: 30rpx;
  background: #fbfcf8;
  text-align: center;
}

.login-popup-icon {
  width: 92rpx;
  height: 92rpx;
  margin: 0 auto 18rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #07c160 0%, #057a3d 100%);
  color: #f7fcf3;
  font-size: 26rpx;
  font-weight: 800;
}

.login-popup-title {
  color: #243121;
  font-size: 34rpx;
  font-weight: 800;
}

.login-popup-desc {
  margin-top: 10rpx;
  color: #7a8574;
  font-size: 25rpx;
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 26rpx;
  text-align: left;
}

.agreement-check {
  width: 28rpx;
  height: 28rpx;
  margin-top: 4rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(7, 193, 96, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f7fcf3;
  font-size: 18rpx;
  font-weight: 800;
  box-sizing: border-box;
  flex-shrink: 0;
}

.agreement-check.checked {
  border-color: #07c160;
  background: #07c160;
}

.agreement-text {
  color: #7a8574;
  font-size: 22rpx;
  line-height: 1.6;
}

.agreement-link {
  color: #07c160;
  font-weight: 700;
}

.login-popup-btn {
  width: 100%;
  margin-top: 34rpx;
}

.login-popup-btn--disabled {
  background: rgba(255, 255, 255, 0.96) !important;
  color: #07c160 !important;
  border-color: rgba(7, 193, 96, 0.42) !important;
  box-shadow: 0 8rpx 18rpx rgba(7, 193, 96, 0.08) !important;
  opacity: 1;
}

.login-popup-cancel {
  margin-top: 18rpx;
  color: #93a08f;
  font-size: 26rpx;
}
</style>