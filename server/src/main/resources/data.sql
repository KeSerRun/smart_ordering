-- ============================================================
-- 智能点餐系统 - 种子数据（精简版）
-- 说明：
--   1. 全部使用 INSERT IGNORE：仅插入不存在的行，不覆盖已有数据
--   2. 保留最小可用数据集：登录账号、RBAC 菜单、示例菜品/桌台/配置
--   3. 菜品图片为 MinIO 对象键（bucket: smart-ordering），缺失时前端显示占位
-- ============================================================

-- ---------- 角色 ----------
INSERT IGNORE INTO sys_role (id, name, code, status, remark, create_by, update_by, create_time, update_time, deleted) VALUES (1, '超级管理员', 'admin', 1, '拥有所有权限', NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_role (id, name, code, status, remark, create_by, update_by, create_time, update_time, deleted) VALUES (2, '普通用户', 'user', 1, '默认角色', NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);

-- ---------- 用户（admin / 123456，BCrypt） ----------
INSERT IGNORE INTO sys_user (id, username, password, nickname, email, phone, openid, user_type, avatar, status, create_by, update_by, create_time, update_time, deleted) VALUES (1, 'admin', '$2a$10$D273KbUAFVgQ3IUx4LD8m.r9bowenLvwNgKXc4.e2MZxzoTK48T4W', '超级管理员', NULL, NULL, NULL, 'BACKEND', NULL, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-07-04 13:45:15', 0);

-- ---------- 用户-角色 ----------
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- ---------- 角色-模块（超级管理员角色拥有全部模块权限） ----------
INSERT IGNORE INTO sys_role_module (role_id, module_code) VALUES (1, 'core'), (1, 'ops'), (1, 'sys'), (1, 'kitchen');

-- ---------- 菜单（与前端静态菜单对应，权限字符串预留） ----------
-- 系统管理
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1, 0, '系统管理', '/system', '', '', 0, 'mdi:cog-outline', 9, 1, NULL, 1, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (100, 1, '用户管理', '/system/user', 'system/user/index', 'system:user:list', 1, 'mdi:account-outline', 1, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (200, 1, '角色管理', '/system/role', 'system/role/index', 'system:role:list', 1, 'mdi:shield-account-outline', 2, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
-- 日志
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (2, 0, '日志管理', '/log', '', '', 0, 'mdi:text-box-outline', 10, 1, NULL, 1, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (600, 2, '登录日志', '/log/login', 'log/login/index', 'log:login:list', 1, 'mdi:login', 1, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (700, 2, '操作日志', '/log/operation', 'log/operation/index', 'log:operation:list', 1, 'mdi:file-document-edit-outline', 2, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
-- 菜品
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1000, 0, '菜品管理', '/dish', '', '', 0, 'mdi:food', 1, 1, NULL, 1, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1002, 1000, '菜品列表', '/dish/list', 'view.dish_list', 'dish:list', 1, 'mdi:food-variant', 2, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
-- 桌台
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1010, 0, '桌台管理', '/table', '', '', 0, 'mdi:table-furniture', 2, 1, NULL, 1, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1011, 1010, '桌台列表', '/table/manage', 'view.table_manage', 'table:list', 1, 'mdi:table-chair', 1, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
-- 订单
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1020, 0, '订单中心', '/order', '', '', 0, 'mdi:clipboard-list-outline', 3, 1, NULL, 1, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1021, 1020, '订单列表', '/order/list', 'view.order_list', 'order:list', 1, 'mdi:format-list-bulleted', 1, 1, NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1045, 1020, '支付管理', '/device/payment', 'view.device_payment', 'payment:list', 1, 'mdi:cash-multiple', 2, 1, 1, 1, '2026-03-02 21:15:41', '2026-03-03 14:13:07', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1046, 1020, '评价管理', '/device/review', 'view.device_review', 'review:list', 1, 'mdi:star-outline', 3, 1, 1, 1, '2026-03-02 21:15:41', '2026-03-03 14:13:07', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1091, 1020, '反馈管理', '/device/feedback', 'view.device_feedback', 'feedback:list', 1, 'mdi:message-reply-text-outline', 4, 1, 1, 1, '2026-06-27 16:27:04', '2026-06-27 16:27:04', 0);
-- 营销
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1080, 0, '营销中心', '/marketing', '', '', 0, 'mdi:bullhorn-outline', 7, 1, 1, 1, '2026-06-27 00:39:46', '2026-07-02 17:42:05', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1060, 1080, '优惠券管理', '/marketing/coupon', 'view.marketing_coupon', NULL, 1, 'mdi:ticket-percent-outline', 1, 1, 1, 1, '2026-06-26 22:56:03', '2026-06-27 00:39:46', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1070, 1080, '轮播图管理', '/marketing/banner', 'view.marketing_banner', NULL, 1, 'mdi:image-multiple-outline', 2, 1, 1, 1, '2026-06-26 23:20:31', '2026-06-27 00:39:46', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1082, 1080, '会员列表', '/marketing/member', 'view.marketing_member', NULL, 1, 'mdi:account-group-outline', 3, 1, 1, 1, '2026-06-30 12:47:21', '2026-06-30 12:47:21', 0);
-- 服务收银/后厨
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1150, 0, '服务收银', '/service', 'layout.base', '', 0, 'mdi:cash-register', 6, 1, 1, 1, '2026-03-02 18:12:56', '2026-07-02 17:41:58', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1155, 1150, '后厨', '/service/kitchen', 'view.service_kitchen', 'kitchen:tasks', 1, 'mdi:chef-hat', 5, 1, 1, 1, '2026-03-03 15:00:20', '2026-03-03 15:00:20', 0);
-- MQ 消息
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (1071, 0, '消息监控', '/monitor', '', '', 0, 'mdi:message-alert-outline', 8, 1, 1, 1, '2026-06-27 00:39:46', '2026-07-02 17:42:09', 0);
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, permission, type, icon, order_num, status, create_by, update_by, create_time, update_time, deleted) VALUES (2270, 1071, '消息列表', NULL, NULL, 'mq:message:list', 2, NULL, 1, 1, 1, 1, '2026-06-27 00:21:05', '2026-06-27 00:21:05', 0);

-- ---------- 角色-菜单（超级管理员绑定全部菜单） ----------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 100), (1, 200), (1, 2), (1, 600), (1, 700), (1, 1000), (1, 1002), (1, 1010), (1, 1011), (1, 1020), (1, 1021), (1, 1045), (1, 1046), (1, 1091), (1, 1080), (1, 1060), (1, 1070), (1, 1082), (1, 1150), (1, 1155), (1, 1071), (1, 2270);

-- ---------- 桌台区域 ----------
INSERT IGNORE INTO table_area VALUES (31001, '包间', 0, 1, '', 1, 1, '2026-07-02 19:52:03', '2026-07-02 20:31:39', 0);
INSERT IGNORE INTO table_area VALUES (31002, '大厅', 10, 1, '', 1, 1, '2026-07-02 19:52:03', '2026-07-02 20:31:43', 0);

-- ---------- 桌台（大厅 A01-A05 + 包间 B01） ----------
INSERT IGNORE INTO dining_table VALUES (30001, 'A01', 'A1桌', 2, 0, 'table/qrcode/A01-fed19e4ebb49c5b2a4c01fd696a3db.png', 31002, '大厅', NULL, 1, NULL, '2025-12-01 10:00:00', '2026-07-03 18:50:17', 0);
INSERT IGNORE INTO dining_table VALUES (30002, 'A02', 'A2桌', 2, 0, 'table/qrcode/A02-01042d5415ca46eb8ec91ccb3ebee22e.png', 31002, '大厅', NULL, 1, NULL, '2025-12-01 10:00:00', '2026-07-03 00:44:28', 0);
INSERT IGNORE INTO dining_table VALUES (30003, 'A03', 'A3桌', 4, 0, 'table/qrcode/A03-2d3230e48eda41b4a064f4b9b129bbcb.png', 31002, '大厅', NULL, 1, NULL, '2025-12-01 10:00:00', '2026-07-03 18:50:23', 0);
INSERT IGNORE INTO dining_table VALUES (30004, 'A04', 'A4桌', 4, 0, 'table/qrcode/A04-2552179a23794fbba794c24645c1e385.png', 31002, '大厅', NULL, 1, NULL, '2025-12-01 10:00:00', '2026-07-04 15:56:00', 0);
INSERT IGNORE INTO dining_table VALUES (30005, 'A05', 'A5桌', 4, 0, 'table/qrcode/A05-79ad6b9a6ee64152bfbcf88ed330806d.png', 31002, '大厅', NULL, 1, NULL, '2025-12-01 10:00:00', '2026-07-04 16:14:20', 0);
INSERT IGNORE INTO dining_table VALUES (30013, 'B01', '牡丹厅', 8, 0, 'table/qrcode/B01-adf64d1d43504ec4b952d070905d3068.png', 31001, '包间', NULL, 1, NULL, '2025-12-01 10:00:00', '2026-07-04 16:19:58', 0);

-- ---------- 菜品分类（凉菜/热菜/主食/饮品） ----------
INSERT IGNORE INTO dish_category VALUES (10001, '凉菜', 1, 1, 'dish/da7af074b69e4d9db633ceb06cfdb63a.jpg', 1, 1, 1, '2025-12-01 10:00:00', '2026-07-03 19:43:18', 0);
INSERT IGNORE INTO dish_category VALUES (10002, '热菜', 2, 1, 'dish/c5bce648113e4c60a378b752422d5154.jpg', 1, 1, 1, '2025-12-01 10:00:00', '2026-07-03 19:19:51', 0);
INSERT IGNORE INTO dish_category VALUES (10004, '主食', 4, 1, 'dish/5009da4f2ca9490eb0528dea36ea0b78.jpg', 1, 1, 1, '2025-12-01 10:00:00', '2026-07-03 19:42:54', 0);
INSERT IGNORE INTO dish_category VALUES (10007, '饮品', 7, 1, 'dish/55e0dc3eb9774c7ebf728e47338843ad.jpg', 2, 1, 1, '2025-12-01 10:00:00', '2026-07-03 19:47:25', 0);

-- ---------- 菜品（每类 2 个示例） ----------
INSERT IGNORE INTO dish VALUES (20001, 10001, '老醋花生', 18.00, 'dish/00573c5882dd49048aa36451e30919c9.jpg', 'dish/00573c5882dd49048aa36451e30919c9.jpg', 0, NULL, '["花生米","香醋","香菜"]', '酥脆花生配陈醋，开胃爽口', 1, 0, 100, 5, 1, 1, '2025-12-01 10:00:00', '2026-07-02 23:22:05', 0);
INSERT IGNORE INTO dish VALUES (20002, 10001, '凉拌黄瓜', 16.00, 'dish/137a97acab3a462dbe1369a02a714e9d.jpg', 'dish/137a97acab3a462dbe1369a02a714e9d.jpg', 0, NULL, '["黄瓜","蒜末","辣椒油"]', '清脆爽口，微辣开胃', 1, 0, 100, 3, 1, 1, '2025-12-01 10:00:00', '2026-07-02 23:22:11', 0);
INSERT IGNORE INTO dish VALUES (20101, 10002, '宫保鸡丁', 38.00, 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 2, NULL, '["鸡胸肉","花生","干辣椒"]', '经典川菜，香辣酥脆', 1, 0, 100, 12, 1, NULL, '2025-12-01 10:00:00', '2026-03-02 21:47:18', 0);
INSERT IGNORE INTO dish VALUES (20103, 10002, '麻婆豆腐', 28.00, 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 3, NULL, '["豆腐","牛肉末","花椒"]', '麻辣烫鲜，川菜之魂', 1, 0, 100, 10, 1, NULL, '2025-12-01 10:00:00', '2026-03-02 21:47:18', 0);
INSERT IGNORE INTO dish VALUES (20301, 10004, '蛋炒饭', 16.00, 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 0, NULL, '["米饭","鸡蛋","葱花"]', '粒粒分明，蛋香浓郁', 1, 0, 100, 8, 1, NULL, '2025-12-01 10:00:00', '2026-03-02 21:47:18', 0);
INSERT IGNORE INTO dish VALUES (20304, 10004, '重庆小面', 16.00, 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 3, NULL, '["面条","花生","辣椒"]', '麻辣爽口，重庆味道', 1, 0, 100, 8, 1, NULL, '2025-12-01 10:00:00', '2026-03-02 21:47:18', 0);
INSERT IGNORE INTO dish VALUES (20601, 10007, '鲜榨橙汁', 18.00, 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 0, NULL, '["鲜橙"]', '现榨现饮，维C满满', 1, 0, 100, 3, 1, NULL, '2025-12-01 10:00:00', '2026-03-02 21:47:18', 0);
INSERT IGNORE INTO dish VALUES (20603, 10007, '酸梅汤', 10.00, 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 'dish/b7a59d8fc4f444d7ab566ea6685ec988.jpg', 0, NULL, '["乌梅","山楂","冰糖"]', '古法熬制，消暑解腻', 1, 0, 100, 2, 1, NULL, '2025-12-01 10:00:00', '2026-03-02 21:47:18', 0);

-- ---------- 优惠券模板（2 个示例） ----------
INSERT IGNORE INTO coupon_template VALUES (30001, '新客到店券', 1, 68.00, 8.00, NULL, 0, 0, 1, 1, '2026-06-26 23:33:32', '2026-07-26 23:33:32', NULL, 1, '满68减8，适合首次到店体验', NULL, 1, 1, '2026-06-26 23:33:32', '2026-06-27 14:04:34', 0);
INSERT IGNORE INTO coupon_template VALUES (30002, '人气热菜券', 1, 100.00, 12.00, NULL, 0, 0, 1, 1, '2026-06-26 23:33:32', '2026-07-26 23:33:32', NULL, 1, '满100减12，适合多人堂食点单', NULL, 1, 1, '2026-06-26 23:33:32', '2026-06-26 23:34:01', 0);

-- ---------- 会员等级 ----------
INSERT IGNORE INTO member_level VALUES (2001, 'NORMAL', '普通会员', 1, 0, 1.00, 1.00, NULL, NULL, NULL, 1, '默认会员等级', 1, 1, '2026-06-30 12:47:12', '2026-06-30 12:47:12', 0);
INSERT IGNORE INTO member_level VALUES (2002, 'SILVER', '银卡会员', 2, 1000, 1.10, 1.00, NULL, NULL, NULL, 1, '成长值满1000升级', 1, 1, '2026-06-30 12:47:12', '2026-06-30 12:47:12', 0);
INSERT IGNORE INTO member_level VALUES (2003, 'GOLD', '金卡会员', 3, 5000, 1.20, 0.98, NULL, NULL, NULL, 1, '成长值满5000升级', 1, 1, '2026-06-30 12:47:12', '2026-06-30 12:47:12', 0);
INSERT IGNORE INTO member_level VALUES (2004, 'BLACK_GOLD', '黑金会员', 4, 15000, 1.50, 0.95, NULL, NULL, NULL, 1, '成长值满15000升级', 1, 1, '2026-06-30 12:47:12', '2026-06-30 12:47:12', 0);

-- ---------- 系统配置（kitchen.autoAccept 为后厨自动接单开关，功能依赖） ----------
INSERT IGNORE INTO sys_config (id, name, config_key, config_value, remark, create_by, update_by, create_time, update_time, deleted) VALUES (1, '系统名称', 'sys.name', '智能点餐系统', '系统名称', NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_config (id, name, config_key, config_value, remark, create_by, update_by, create_time, update_time, deleted) VALUES (2, '系统版本', 'sys.version', '1.0.0', '系统版本号', NULL, NULL, '2026-03-02 18:12:56', '2026-03-02 18:12:56', 0);
INSERT IGNORE INTO sys_config (id, name, config_key, config_value, remark, create_by, update_by, create_time, update_time, deleted) VALUES (2070487983681265666, '后厨自动接单', 'kitchen.autoAccept', 'true', '控制新堂食订单是否自动接单', 1, 1, '2026-06-26 20:42:58', '2026-06-26 20:42:58', 0);