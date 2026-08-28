-- ============================================================
-- 智能点餐系统 - MySQL 数据库初始化脚本（7 张核心表）
-- 说明：
-- ============================================================
-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT NOT NULL COMMENT '角色ID',
    name        VARCHAR(100) NOT NULL COMMENT '角色名称',
    code        VARCHAR(100) NOT NULL COMMENT '角色编码',
    status      INT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    remark      VARCHAR(500) COMMENT '备注',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted     INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';


-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';


-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT NOT NULL COMMENT '用户ID',
    username    VARCHAR(50) NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(100) COMMENT '昵称',
    email       VARCHAR(100) COMMENT '邮箱',
    phone       VARCHAR(20) COMMENT '手机号',
    openid      VARCHAR(64) COMMENT '微信openid',
    user_type   VARCHAR(20) NOT NULL DEFAULT 'APP' COMMENT '用户类型：APP小程序/BACKEND后台',
    avatar      VARCHAR(255) COMMENT '头像URL',
    status      INT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted     INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';


-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';


-- 桌台表
CREATE TABLE IF NOT EXISTS dining_table (
    id                   BIGINT NOT NULL COMMENT '桌台ID',
    code                 VARCHAR(20) NOT NULL COMMENT '桌台编号',
    name                 VARCHAR(50) NOT NULL COMMENT '桌台名称',
    capacity             INT NOT NULL DEFAULT 4 COMMENT '容纳人数',
    status               INT NOT NULL DEFAULT 0 COMMENT '状态：0空闲 1就餐中 2已预订',
    qr_code_url          VARCHAR(255) COMMENT '二维码URL',
    area_id              BIGINT COMMENT '桌区ID',
    area_name            VARCHAR(50) COMMENT '桌区名称',
    current_session_code VARCHAR(64) COMMENT '当前就餐会话码',
    create_by            BIGINT COMMENT '创建人',
    update_by            BIGINT COMMENT '更新人',
    create_time          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_status (status),
    KEY idx_area_id (area_id),
    KEY idx_current_session_code (current_session_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='桌台表';


-- 菜品表
CREATE TABLE IF NOT EXISTS dish (
    id               BIGINT NOT NULL COMMENT '菜品ID',
    category_id      BIGINT NOT NULL COMMENT '分类ID',
    name             VARCHAR(100) NOT NULL COMMENT '菜品名称',
    price            DECIMAL(10,2) NOT NULL COMMENT '价格',
    image            VARCHAR(255) COMMENT '图片URL',
    thumbnail        VARCHAR(255) COMMENT '缩略图URL',
    spice_level      INT NOT NULL DEFAULT 0 COMMENT '辣度：0不辣 1微辣 2中辣 3重辣',
    spec_values      TEXT COMMENT '规格值（JSON）',
    ingredients      TEXT COMMENT '食材（JSON数组）',
    description      TEXT COMMENT '描述',
    status           INT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    sold_out         INT NOT NULL DEFAULT 0 COMMENT '是否售罄：1售罄 0正常',
    stock            INT NOT NULL DEFAULT -1 COMMENT '库存：-1不限',
    preparation_time INT COMMENT '制作时长（分钟）',
    create_by        BIGINT COMMENT '创建人',
    update_by        BIGINT COMMENT '更新人',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';


-- 订单表（order 是 MySQL 保留字，用反引号包裹）
CREATE TABLE IF NOT EXISTS `order` (
    id                      BIGINT NOT NULL COMMENT '订单ID',
    order_no                VARCHAR(32) NOT NULL COMMENT '订单号',
    table_id                BIGINT COMMENT '桌台ID',
    table_code              VARCHAR(20) COMMENT '桌台编号',
    table_session_code      VARCHAR(64) COMMENT '就餐会话码',
    original_amount         DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '原价',
    discount_rate           DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率',
    coupon_id               BIGINT COMMENT '优惠券ID',
    coupon_name             VARCHAR(100) COMMENT '优惠券名称',
    coupon_type             INT COMMENT '优惠券类型',
    coupon_threshold_amount DECIMAL(10,2) COMMENT '满减门槛金额',
    coupon_discount_amount  DECIMAL(10,2) COMMENT '优惠券抵扣金额',
    coupon_discount_rate    DECIMAL(10,2) COMMENT '优惠券折扣率',
    actual_amount           DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实际金额',
    points_used             INT NOT NULL DEFAULT 0 COMMENT '使用积分',
    points_discount_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
    paid_amount             DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已支付金额',
    status                  INT NOT NULL DEFAULT 0 COMMENT '状态',
    payment_mode            INT NOT NULL DEFAULT 1 COMMENT '支付方式',
    order_type              INT NOT NULL DEFAULT 0 COMMENT '订单类型',
    remark                  TEXT COMMENT '备注',
    customer_openid         VARCHAR(64) COMMENT '客户openid',
    create_by               BIGINT COMMENT '创建人',
    update_by               BIGINT COMMENT '更新人',
    create_time             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_table_id (table_id),
    KEY idx_table_session_status (table_id, table_session_code, status),
    KEY idx_status (status),
    KEY idx_customer_openid (customer_openid),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';


-- 菜品分类表
CREATE TABLE IF NOT EXISTS dish_category (
    id            BIGINT NOT NULL COMMENT '分类ID',
    name          VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort          INT NOT NULL DEFAULT 0 COMMENT '排序',
    status        INT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    image         VARCHAR(255) COMMENT '分类图片URL',
    spec_template INT NOT NULL DEFAULT 1 COMMENT '规格模板',
    create_by     BIGINT COMMENT '创建人',
    update_by     BIGINT COMMENT '更新人',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';

-- 桌区表
CREATE TABLE IF NOT EXISTS table_area (
    id          BIGINT NOT NULL COMMENT '桌区ID',
    name        VARCHAR(50) NOT NULL COMMENT '桌区名称',
    sort        INT NOT NULL DEFAULT 0 COMMENT '排序',
    status      INT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    remark      VARCHAR(500) COMMENT '备注',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='桌区表';


-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id             BIGINT NOT NULL COMMENT '明细ID',
    order_id       BIGINT NOT NULL COMMENT '订单ID',
    dish_id        BIGINT NOT NULL COMMENT '菜品ID',
    dish_name      VARCHAR(100) NOT NULL COMMENT '菜品名称（冗余）',
    dish_image     VARCHAR(255) COMMENT '菜品图片（冗余）',
    price          DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
    quantity       INT NOT NULL DEFAULT 1 COMMENT '数量',
    amount         DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    remark         VARCHAR(500) COMMENT '口味备注',
    status         INT NOT NULL DEFAULT 0 COMMENT '状态：0待制作 1制作中 2已完成',
    payment_status INT NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付 2已支付',
    is_gift        INT NOT NULL DEFAULT 0 COMMENT '是否赠送：0否 1是',
    added_at       DATETIME COMMENT '加入订单时间',
    create_by      BIGINT COMMENT '创建人',
    update_by      BIGINT COMMENT '更新人',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_dish_id (dish_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';


-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT NOT NULL COMMENT '菜单ID',
    parent_id   BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name        VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path        VARCHAR(200) COMMENT '路由地址',
    component   VARCHAR(200) COMMENT '组件路径',
    permission  VARCHAR(100) COMMENT '权限标识',
    type        INT NOT NULL DEFAULT 0 COMMENT '类型：0目录 1菜单 2按钮',
    icon        VARCHAR(100) COMMENT '图标',
    order_num   INT NOT NULL DEFAULT 0 COMMENT '排序',
    status      INT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted     INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';


-- 支付记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id              BIGINT NOT NULL COMMENT '支付记录ID',
    order_id        BIGINT NOT NULL COMMENT '订单ID',
    payment_no      VARCHAR(32) NOT NULL COMMENT '支付流水号',
    third_party_no  VARCHAR(64) COMMENT '第三方支付流水号',
    payment_method  INT NOT NULL COMMENT '支付方式：0微信 1支付宝 2现金',
    amount          DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status          INT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已退款 3支付失败',
    payer_openid    VARCHAR(64) COMMENT '支付人openid',
    callback_data   TEXT COMMENT '支付回调原始数据',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_id (order_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';


-- 首页轮播图表
CREATE TABLE IF NOT EXISTS home_banner (
    id          BIGINT NOT NULL COMMENT '轮播图ID',
    title       VARCHAR(100) NOT NULL COMMENT '主标题',
    subtitle    VARCHAR(255) COMMENT '副标题',
    image_url   VARCHAR(255) NOT NULL COMMENT '图片地址',
    action_type TINYINT NOT NULL DEFAULT 0 COMMENT '操作类型：0无动作 1页面跳转 2切换Tab',
    target_path VARCHAR(255) COMMENT '跳转路径',
    scene       VARCHAR(32) NOT NULL DEFAULT 'HOME' COMMENT '投放位置',
    sort        INT NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_home_banner_scene_status_sort (scene, status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页轮播图表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS user_feedback (
    id              BIGINT NOT NULL COMMENT '反馈ID',
    customer_openid VARCHAR(64) COMMENT '反馈用户openid',
    customer_phone  VARCHAR(32) COMMENT '用户手机号快照',
    contact_phone   VARCHAR(32) COMMENT '联系手机号',
    content         VARCHAR(500) NOT NULL COMMENT '反馈内容',
    reply_content   VARCHAR(500) COMMENT '回复内容',
    reply_time      DATETIME COMMENT '回复时间',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待回复 1已回复',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_user_feedback_openid (customer_openid),
    KEY idx_user_feedback_status_create_time (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 订单项评价表
CREATE TABLE IF NOT EXISTS order_item_review (
    id            BIGINT NOT NULL COMMENT '评价明细ID',
    review_id     BIGINT NOT NULL COMMENT '评价ID',
    order_item_id BIGINT NOT NULL COMMENT '订单项ID',
    rating        TINYINT NOT NULL COMMENT '评分',
    create_by     BIGINT COMMENT '创建人',
    update_by     BIGINT COMMENT '更新人',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_order_item_review_review_id (review_id),
    KEY idx_order_item_review_order_item_id (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项评价表';

-- 订单评价表
CREATE TABLE IF NOT EXISTS order_review (
    id              BIGINT NOT NULL COMMENT '评价ID',
    order_id        BIGINT NOT NULL COMMENT '订单ID',
    overall_rating  TINYINT NOT NULL COMMENT '总评分',
    content         VARCHAR(500) COMMENT '评价内容',
    customer_openid VARCHAR(100) COMMENT '客户openid',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_review_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';


-- 优惠券模板表
CREATE TABLE IF NOT EXISTS coupon_template (
    id                 BIGINT NOT NULL COMMENT '模板ID',
    name               VARCHAR(100) NOT NULL COMMENT '模板名称',
    type               INT NOT NULL COMMENT '类型：1满减 2折扣',
    threshold_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额',
    discount_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    discount_rate      DECIMAL(10,2) COMMENT '折扣比例',
    total_quantity     INT NOT NULL DEFAULT 0 COMMENT '发放总量：0不限量',
    issued_quantity    INT NOT NULL DEFAULT 0 COMMENT '已发放数量',
    per_user_limit     INT NOT NULL DEFAULT 0 COMMENT '每人限领：0不限',
    validity_type      INT NOT NULL COMMENT '有效期类型：1固定时间 2领券后N天',
    valid_from         DATETIME COMMENT '固定生效时间',
    valid_to           DATETIME COMMENT '固定失效时间',
    valid_days         INT COMMENT '领券后有效天数',
    status             INT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    description        TEXT COMMENT '说明',
    available_weekdays VARCHAR(20) COMMENT '可用星期，1-7逗号分隔，空表示每天',
    create_by          BIGINT COMMENT '创建人',
    update_by          BIGINT COMMENT '更新人',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_coupon_template_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupon (
    id                 BIGINT NOT NULL COMMENT '用户券ID',
    template_id        BIGINT NOT NULL COMMENT '模板ID',
    user_id            BIGINT NOT NULL COMMENT '用户ID',
    username           VARCHAR(50) COMMENT '用户名快照',
    nickname           VARCHAR(100) COMMENT '昵称快照',
    phone              VARCHAR(20) COMMENT '手机号快照',
    coupon_name        VARCHAR(100) NOT NULL COMMENT '券名称快照',
    coupon_type        INT NOT NULL COMMENT '类型：1满减 2折扣',
    threshold_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '门槛金额',
    discount_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    discount_rate      DECIMAL(10,2) COMMENT '折扣比例',
    source_type        INT NOT NULL DEFAULT 1 COMMENT '来源：1后台发放 2全员发放',
    status             INT NOT NULL DEFAULT 0 COMMENT '状态：0未使用 1已使用 2已过期 3已锁定',
    received_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    valid_from         DATETIME NOT NULL COMMENT '生效时间',
    valid_to           DATETIME NOT NULL COMMENT '失效时间',
    used_time          DATETIME COMMENT '使用时间',
    order_id           BIGINT COMMENT '使用订单ID',
    grant_task_id      BIGINT COMMENT '发券任务ID',
    available_weekdays VARCHAR(20) COMMENT '可用星期',
    create_by          BIGINT COMMENT '创建人',
    update_by          BIGINT COMMENT '更新人',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_user_coupon_user_status (user_id, status),
    KEY idx_user_coupon_template_user (template_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';


-- 会员等级表
CREATE TABLE IF NOT EXISTS member_level (
    id                         BIGINT NOT NULL COMMENT '等级ID',
    level_code                 VARCHAR(30) NOT NULL COMMENT '等级编码',
    level_name                 VARCHAR(50) NOT NULL COMMENT '等级名称',
    sort                       INT NOT NULL DEFAULT 0 COMMENT '排序',
    growth_threshold           INT NOT NULL DEFAULT 0 COMMENT '成长值门槛',
    points_rate                DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '积分倍率',
    discount_rate              DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率',
    benefit_config             TEXT COMMENT '权益配置',
    upgrade_coupon_template_id BIGINT COMMENT '升级礼包券模板ID',
    exclusive_coupon_template_id BIGINT COMMENT '专属券模板ID',
    status                     INT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    remark                     TEXT COMMENT '备注',
    create_by                  BIGINT COMMENT '创建人',
    update_by                  BIGINT COMMENT '更新人',
    create_time                DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time                DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                    INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_level_code (level_code),
    KEY idx_member_level_growth_threshold (growth_threshold)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- 会员档案表
CREATE TABLE IF NOT EXISTS member_profile (
    id                  BIGINT NOT NULL COMMENT '会员档案ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    member_no           VARCHAR(32) NOT NULL COMMENT '会员编号',
    level_id            BIGINT NOT NULL COMMENT '等级ID',
    growth_value        INT NOT NULL DEFAULT 0 COMMENT '成长值',
    points_balance      INT NOT NULL DEFAULT 0 COMMENT '积分余额',
    total_points_earned INT NOT NULL DEFAULT 0 COMMENT '累计获得积分',
    total_points_used   INT NOT NULL DEFAULT 0 COMMENT '累计使用积分',
    total_amount_consumed DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    birthday            DATE COMMENT '生日',
    register_source     VARCHAR(32) COMMENT '注册来源',
    status              INT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    last_consume_time   DATETIME COMMENT '最近消费时间',
    create_by           BIGINT COMMENT '创建人',
    update_by           BIGINT COMMENT '更新人',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_profile_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员档案表';


-- 会员积分流水表
CREATE TABLE IF NOT EXISTS member_points_record (
    id            BIGINT NOT NULL COMMENT '积分流水ID',
    member_id     BIGINT NOT NULL COMMENT '会员档案ID',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    change_type   INT NOT NULL COMMENT '变更类型',
    biz_type      VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_id        BIGINT NOT NULL COMMENT '业务ID',
    change_amount INT NOT NULL COMMENT '变更积分',
    balance_after INT NOT NULL COMMENT '变更后余额',
    expire_time   DATETIME COMMENT '过期时间',
    remark        VARCHAR(500) COMMENT '备注',
    create_by     BIGINT COMMENT '创建人',
    update_by     BIGINT COMMENT '更新人',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_points_record_biz (biz_type, biz_id),
    KEY idx_member_points_record_member_id (member_id),
    KEY idx_member_points_record_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员积分流水表';

-- 会员成长值流水表
CREATE TABLE IF NOT EXISTS member_growth_record (
    id            BIGINT NOT NULL COMMENT '成长值流水ID',
    member_id     BIGINT NOT NULL COMMENT '会员档案ID',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    biz_type      VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_id        BIGINT NOT NULL COMMENT '业务ID',
    change_amount INT NOT NULL COMMENT '变更成长值',
    growth_after  INT NOT NULL COMMENT '变更后成长值',
    remark        VARCHAR(500) COMMENT '备注',
    create_by     BIGINT COMMENT '创建人',
    update_by     BIGINT COMMENT '更新人',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_member_growth_record_member_id (member_id),
    KEY idx_member_growth_record_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员成长值流水表';

-- 订单操作日志表（审计日志）
CREATE TABLE IF NOT EXISTS order_operation_log (
    id             BIGINT NOT NULL COMMENT '日志ID',
    order_id       BIGINT NOT NULL COMMENT '订单ID',
    order_item_id  BIGINT COMMENT '订单项ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operator_id    BIGINT NOT NULL COMMENT '操作人ID',
    operator_name  VARCHAR(100) COMMENT '操作人姓名',
    reason         VARCHAR(500) COMMENT '操作原因',
    detail         TEXT COMMENT '操作详情',
    create_by      BIGINT COMMENT '创建人',
    update_by      BIGINT COMMENT '更新人',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_order_item_id (order_item_id),
    KEY idx_operator_id (operator_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';


-- MQ消息表
CREATE TABLE IF NOT EXISTS mq_message (
    id              BIGINT NOT NULL COMMENT '消息ID',
    message_key     VARCHAR(64) NOT NULL COMMENT '消息唯一键',
    topic           VARCHAR(100) NOT NULL COMMENT '主题',
    tag             VARCHAR(100) COMMENT '标签',
    biz_type        VARCHAR(50) NOT NULL COMMENT '业务类型',
    biz_key         VARCHAR(64) NOT NULL COMMENT '业务主键',
    payload         MEDIUMTEXT NOT NULL COMMENT '消息体',
    deliver_status  INT NOT NULL DEFAULT 0 COMMENT '投递状态',
    retry_count     INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    next_retry_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下次重试时间',
    last_error      TEXT COMMENT '最后错误信息',
    sent_time       DATETIME COMMENT '发送成功时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mq_message_key (message_key),
    KEY idx_mq_message_status_retry (deliver_status, next_retry_time),
    KEY idx_mq_message_biz (biz_type, biz_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可靠消息表';

-- MQ消费日志表
CREATE TABLE IF NOT EXISTS mq_consume_log (
    id              BIGINT NOT NULL COMMENT '日志ID',
    consumer_group  VARCHAR(100) NOT NULL COMMENT '消费者组',
    topic           VARCHAR(100) NOT NULL COMMENT '主题',
    tag             VARCHAR(100) COMMENT '标签',
    message_key     VARCHAR(64) NOT NULL COMMENT '消息唯一键',
    biz_key         VARCHAR(64) NOT NULL COMMENT '业务主键',
    consume_status  INT NOT NULL DEFAULT 0 COMMENT '消费状态',
    retry_count     INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    last_error      TEXT COMMENT '最后错误信息',
    finished_time   DATETIME COMMENT '完成时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
        UNIQUE KEY uk_mq_consume_message (consumer_group, message_key),
        KEY idx_mq_consume_biz (biz_key)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MQ消费日志表';


-- 菜品规格组表（管理端：小份/大份/辣度等规格组）
CREATE TABLE IF NOT EXISTS dish_spec_group (
    id          BIGINT NOT NULL COMMENT '规格组ID',
    name        VARCHAR(50) NOT NULL COMMENT '规格组名称',
    sort        INT NOT NULL DEFAULT 0 COMMENT '排序',
    status      INT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品规格组表';

-- 规格值表（规格组下的选项：小份/大份、微辣/重辣等，price 为加价/减价金额）
CREATE TABLE IF NOT EXISTS dish_spec_option (
    id          BIGINT NOT NULL COMMENT '规格值ID',
    group_id    BIGINT NOT NULL COMMENT '规格组ID',
    name        VARCHAR(50) NOT NULL COMMENT '规格值名称',
    sort        INT NOT NULL DEFAULT 0 COMMENT '排序',
    price       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '加价金额（正为加价，负为减价，0 不加价）',
    deleted     INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_spec_option_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品规格值表';

-- 分类-规格组关联表（一个分类可绑定多个规格组）
CREATE TABLE IF NOT EXISTS dish_category_spec (
    id            BIGINT NOT NULL COMMENT 'ID',
    category_id   BIGINT NOT NULL COMMENT '分类ID',
    spec_group_id BIGINT NOT NULL COMMENT '规格组ID',
    create_by     BIGINT COMMENT '创建人',
    update_by     BIGINT COMMENT '更新人',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_spec (category_id, spec_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类规格组关联表';


-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGINT NOT NULL COMMENT '配置ID',
    name         VARCHAR(100) COMMENT '配置名称',
    config_key   VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    remark       VARCHAR(500) COMMENT '备注',
    create_by    BIGINT COMMENT '创建人',
    update_by    BIGINT COMMENT '更新人',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

    -- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id          BIGINT NOT NULL COMMENT '日志ID',
    username    VARCHAR(100) COMMENT '用户名',
    ip          VARCHAR(50) COMMENT 'IP',
    location    VARCHAR(100) COMMENT '地点',
    browser     VARCHAR(100) COMMENT '浏览器',
    os          VARCHAR(100) COMMENT '操作系统',
    status      INT NOT NULL DEFAULT 0 COMMENT '状态：1成功 0失败',
    message     VARCHAR(500) COMMENT '消息',
    login_time  DATETIME COMMENT '登录时间',
            PRIMARY KEY (id),
            KEY idx_login_log_username (username)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id              BIGINT NOT NULL COMMENT '日志ID',
    module          VARCHAR(100) COMMENT '模块',
    operation       VARCHAR(100) COMMENT '操作',
    method          VARCHAR(200) COMMENT '方法',
    request_url     VARCHAR(500) COMMENT '请求URL',
    request_method  VARCHAR(20) COMMENT '请求方式',
    request_params  TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    user_id         BIGINT COMMENT '用户ID',
    username        VARCHAR(100) COMMENT '用户名',
    ip              VARCHAR(50) COMMENT 'IP',
    duration        BIGINT COMMENT '耗时(毫秒)',
    status          INT COMMENT '状态：1成功 0失败',
    error_msg       TEXT COMMENT '错误信息',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operation_log_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 角色-模块权限关联表（模块编码：core 点餐核心 / ops 运营管理 / sys 系统管理 / kitchen 后厨任务）
CREATE TABLE IF NOT EXISTS sys_role_module (
    role_id     BIGINT NOT NULL COMMENT '角色ID',
    module_code VARCHAR(20) NOT NULL COMMENT '模块编码：core/ops/sys/kitchen',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (role_id, module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色模块权限表';

-- 发券任务表（后台发放优惠券，MQ 异步任务）
CREATE TABLE IF NOT EXISTS coupon_grant_task (
    id              BIGINT NOT NULL COMMENT '任务ID',
    template_id     BIGINT NOT NULL COMMENT '模板ID',
    template_name   VARCHAR(100) COMMENT '模板名称快照',
    grant_mode      INT NOT NULL COMMENT '发放方式: 1指定用户 2全部用户 3按会员等级',
    target_count    INT NOT NULL DEFAULT 0 COMMENT '目标人数',
    success_count   INT NOT NULL DEFAULT 0 COMMENT '成功发放数',
    fail_count      INT NOT NULL DEFAULT 0 COMMENT '失败数',
    task_status     INT NOT NULL DEFAULT 0 COMMENT '状态: 0待处理 1处理中 2成功 3失败',
    batch_count     INT NOT NULL DEFAULT 1 COMMENT '总批次数',
    finished_batch  INT NOT NULL DEFAULT 0 COMMENT '已完成批次数',
    level_ids       VARCHAR(255) COMMENT '按等级发放时的等级ID(逗号分隔)',
    user_ids        VARCHAR(512) COMMENT '指定用户ID(逗号分隔, grantMode=1时)',
    remark          VARCHAR(255) COMMENT '备注',
    last_error      VARCHAR(500) COMMENT '最近错误',
    started_time    DATETIME COMMENT '开始时间',
    finished_time   DATETIME COMMENT '完成时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
    PRIMARY KEY (id),
    KEY idx_grant_task_template (template_id),
    KEY idx_grant_task_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发券任务表';
