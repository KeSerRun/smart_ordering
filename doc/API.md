# SmartOrdering 后端接口文档

> 由 **Knife4j / springdoc-openapi** 从正在运行的后端实例自动生成，来源 `GET /api/v3/api-docs`（OpenAPI 3.0.1）。
> 在线交互式文档：`http://localhost:8080/api/doc.html`

- 路径总数：**158**
- 接口方法总数：**178**（GET / POST / PUT / DELETE / PATCH）
- 上下文路径：`/api`（服务器端口 8080）

---

## `/admin/..`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/../system/config/theme-preset` | &nbsp; |

## `/admin/banner`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/banner` | Create banner |
| GET | `/admin/banner/page` | Paged banner list |
| PUT | `/admin/banner/{id}` | Update banner |
| PUT | `/admin/banner/{id}/status` | Update banner status |

## `/admin/coupon`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/coupon/grant` | Grant coupons |
| GET | `/admin/coupon/task/detail/page` | Paged grant task details |
| GET | `/admin/coupon/task/page` | Paged grant tasks |
| POST | `/admin/coupon/template` | Create coupon template |
| GET | `/admin/coupon/template/page` | Paged coupon templates |
| PUT | `/admin/coupon/template/{id}` | Update coupon template |
| PUT | `/admin/coupon/template/{id}/status` | Update coupon template status |
| GET | `/admin/coupon/user/page` | Paged user coupons |

## `/admin/dish`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/dish` | Create dish |
| POST | `/admin/dish/category` | Create category |
| GET | `/admin/dish/category/list` | List all categories with spec bindings (admin) |
| PUT | `/admin/dish/category/sort` | Batch update category sort |
| PUT | `/admin/dish/category/{id}` | Update category |
| DELETE | `/admin/dish/category/{id}` | Delete category |
| GET | `/admin/dish/list` | Paged dish list (admin) |
| POST | `/admin/dish/spec` | Create spec group |
| GET | `/admin/dish/spec/list` | List all spec groups with options (admin) |
| PUT | `/admin/dish/spec/{id}` | Update spec group |
| DELETE | `/admin/dish/spec/{id}` | Delete spec group |
| GET | `/admin/dish/{id}` | Dish detail (admin) |
| PUT | `/admin/dish/{id}` | Update dish |
| PUT | `/admin/dish/{id}/status` | Update dish on/off shelf status |

## `/admin/feedback`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/feedback/list` | Paged feedback list |
| PUT | `/admin/feedback/{feedbackId}/reply` | Reply to feedback |

## `/admin/file`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/file/upload/banner-image` | Upload a banner image |
| POST | `/admin/file/upload/dish-image` | Upload a dish image |

## `/admin/member`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/member/benefit-config` | Benefit config |
| PUT | `/admin/member/benefit-config` | Save benefit config |
| POST | `/admin/member/exchange` | Save exchange |
| GET | `/admin/member/exchange/list` | Exchange list |
| DELETE | `/admin/member/exchange/{id}` | Delete exchange |
| GET | `/admin/member/growth-record/page` | Paged growth records |
| POST | `/admin/member/level` | Create member level |
| GET | `/admin/member/level/list` | All member levels |
| PUT | `/admin/member/level/{id}` | Update member level |
| PUT | `/admin/member/level/{id}/status` | Update member level status |
| GET | `/admin/member/overview` | Member overview |
| GET | `/admin/member/page` | Paged member list |
| GET | `/admin/member/points-record/page` | Paged points records |
| GET | `/admin/member/{id}` | Member detail |
| POST | `/admin/member/{id}/points-adjust` | Adjust member points |

## `/admin/mq`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/mq/message/page` | Page messages |
| POST | `/admin/mq/message/{id}/retry` | Retry message |

## `/admin/order`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/order` | Create order |
| POST | `/admin/order/estimate` | Estimate order |
| PUT | `/admin/order/item/{itemId}/gift` | Gift item |
| PUT | `/admin/order/item/{itemId}/replace` | Replace item |
| PUT | `/admin/order/item/{itemId}/return` | Return item |
| GET | `/admin/order/list` | Paged order list |
| GET | `/admin/order/{id}` | Order detail |
| POST | `/admin/order/{id}/add-item` | Add item |
| PUT | `/admin/order/{id}/discount` | Discount order |
| POST | `/admin/order/{id}/rush/{itemId}` | Rush item |

## `/admin/payment`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/payment/cash` | Cash payment |
| GET | `/admin/payment/list` | Paged payment records |
| POST | `/admin/payment/order/{orderId}/refund` | Refund order |
| POST | `/admin/payment/qrcode` | Generate payment QR |
| POST | `/admin/payment/split-bill` | Split bill |
| GET | `/admin/payment/{id}/status` | Payment status |

## `/admin/report`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/report/dashboard-overview` | &nbsp; |
| GET | `/admin/report/dish-ranking` | Dish sales ranking |
| GET | `/admin/report/revenue` | Revenue statistics |
| GET | `/admin/report/table-turnover` | Table turnover rate |

## `/admin/review`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/review/list` | Paged review list |
| GET | `/admin/review/order/{orderId}` | Order review detail |

## `/admin/table`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/table` | Create table |
| POST | `/admin/table/area` | Create area |
| GET | `/admin/table/area/enabled-list` | List enabled areas |
| GET | `/admin/table/area/list` | List all areas |
| PUT | `/admin/table/area/{id}` | Update area |
| DELETE | `/admin/table/area/{id}` | Delete area |
| GET | `/admin/table/list` | List all tables (board view) |
| POST | `/admin/table/qrcode/download-all/task` | Async download-all QR codes task |
| POST | `/admin/table/qrcode/generate-all` | Generate all table QR codes |
| POST | `/admin/table/qrcode/generate-all/task` | Async generate all QR codes task |
| GET | `/admin/table/qrcode/task/{taskId}` | Query QR task status |
| GET | `/admin/table/qrcode/task/{taskId}/download` | Download QR task result file |
| PUT | `/admin/table/{id}` | Update table |
| DELETE | `/admin/table/{id}` | Delete table |
| PUT | `/admin/table/{id}/checkout` | Confirm checkout (occupied -> to-clean) |
| PUT | `/admin/table/{id}/clean` | Mark table clean |
| GET | `/admin/table/{id}/order` | Table current order (placeholder) |
| GET | `/admin/table/{id}/qrcode/download` | Download table QR code |
| PUT | `/admin/table/{id}/release` | Release table (free) |
| PUT | `/admin/table/{id}/to-clean` | Move table to to-clean |

## `/app/banner`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/banner/list` | List enabled banners |

## `/app/cart`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/cart` | Get cart |
| DELETE | `/app/cart` | Clear cart |
| POST | `/app/cart/item` | Add item to cart |
| PUT | `/app/cart/item/{dishId}` | Update item quantity |
| DELETE | `/app/cart/item/{dishId}` | Remove item from cart |

## `/app/coupon`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/coupon/my` | List my coupons |
| GET | `/app/coupon/templates` | List available coupon templates |
| POST | `/app/coupon/{templateId}/receive` | Receive coupon |

## `/app/dish`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/dish/category/list` | List enabled categories |
| GET | `/app/dish/list` | List on-sale dishes (optional categoryId) |
| GET | `/app/dish/{id}` | Get dish detail |
| PUT | `/app/dish/{id}/sold-out` | Set/clear dish sold-out flag (0=normal 1=sold out) |

## `/app/feedback`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/app/feedback` | Submit feedback |
| GET | `/app/feedback/my` | List my feedback |

## `/app/kitchen`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/kitchen/auto-accept` | Get auto-accept enabled flag |
| PUT | `/app/kitchen/auto-accept` | Update auto-accept enabled flag |
| PUT | `/app/kitchen/task/{itemId}/accept` | Accept task (pending -> cooking) |
| PUT | `/app/kitchen/task/{itemId}/complete` | Complete task (cooking -> done) |
| GET | `/app/kitchen/tasks` | Get task list (pending/cooking) |

## `/app/member`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/member/growth-record/page` | &nbsp; |
| GET | `/app/member/level/list` | &nbsp; |
| GET | `/app/member/me` | &nbsp; |
| GET | `/app/member/points-record/page` | &nbsp; |

## `/app/order`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/app/order` | Create order from cart |
| GET | `/app/order/{id}` | Get order detail |

## `/app/payment`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/app/payment/cash` | Cash payment |
| GET | `/app/payment/{id}/status` | Get payment status |

## `/app/review`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/app/review` | Submit review |
| GET | `/app/review/order/{orderId}` | Get order review |

## `/app/table`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/table/list` | List tables (optional areaId) |
| GET | `/app/table/{code}` | Get table by code (scan QR to enter) |

## `/auth/error`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/error` | Report an auth error (frontend client-side logging) |

## `/auth/info`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/auth/info` | Get current user info |

## `/auth/login`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/login` | Login |

## `/auth/logout`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/logout` | Logout |

## `/auth/refreshToken`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/auth/refreshToken` | Refresh token (returns current valid token; renews timeout) |

## `/auth/register`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/register` | Register |

## `/health/error`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health/error` | &nbsp; |

## `/health/ok`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health/ok` | &nbsp; |

## `/route/getConstantRoutes`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/route/getConstantRoutes` | Get constant routes (no login required) |

## `/route/getUserRoutes`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/route/getUserRoutes` | Get user routes (login required) |

## `/route/isRouteExist`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/route/isRouteExist` | Check whether a route exists |

## `/system/config`

| 方法 | 路径 | 说明 |
|---|---|---|
| PUT | `/system/config` | Update config |
| POST | `/system/config` | Create config |
| GET | `/system/config/key/{configKey}` | Config value by key |
| GET | `/system/config/page` | Paged config list |
| GET | `/system/config/theme-preset` | Get admin theme preset |
| PUT | `/system/config/theme-preset/{presetId}` | Save admin theme preset |
| GET | `/system/config/{configId}` | Config detail |
| DELETE | `/system/config/{configId}` | Delete config |

## `/system/dict`

| 方法 | 路径 | 说明 |
|---|---|---|
| PUT | `/system/dict/data` | Update dict data |
| POST | `/system/dict/data` | Create dict data |
| GET | `/system/dict/data/code/{typeCode}` | Dict data by type code |
| GET | `/system/dict/data/type/{typeId}` | Dict data by type id |
| GET | `/system/dict/data/{dictDataId}` | Dict data detail |
| DELETE | `/system/dict/data/{dictDataId}` | Delete dict data |
| PUT | `/system/dict/type` | Update dict type |
| POST | `/system/dict/type` | Create dict type |
| GET | `/system/dict/type/list` | All dict types |
| GET | `/system/dict/type/page` | Paged dict type list |
| GET | `/system/dict/type/{dictTypeId}` | Dict type detail |
| DELETE | `/system/dict/type/{dictTypeId}` | Delete dict type |

## `/system/log`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/system/log/login/page` | Paged login logs |
| GET | `/system/log/operation/page` | Paged operation logs |

## `/system/menu`

| 方法 | 路径 | 说明 |
|---|---|---|
| PUT | `/system/menu` | Update menu |
| POST | `/system/menu` | Create menu |
| GET | `/system/menu/list` | Paged-free flat menu list |
| GET | `/system/menu/permission/tree` | Permission tree |
| GET | `/system/menu/tree` | Menu tree |
| GET | `/system/menu/user/tree` | Current user's menu tree |
| DELETE | `/system/menu/{menuId}` | Delete menu |

## `/system/role`

| 方法 | 路径 | 说明 |
|---|---|---|
| PUT | `/system/role` | Update role |
| POST | `/system/role` | Create role |
| GET | `/system/role/list` | All roles |
| GET | `/system/role/page` | Paged role list |
| GET | `/system/role/{roleId}` | Role detail |
| DELETE | `/system/role/{roleId}` | Delete role |
| GET | `/system/role/{roleId}/menus` | Role's menu ids |
| POST | `/system/role/{roleId}/menus` | Assign menus to role |
| PUT | `/system/role/{roleId}/status/{status}` | Update role status |
| GET | `/system/role/{roleId}/users` | Role's user ids |
| POST | `/system/role/{roleId}/users` | Assign users to role |

## `/system/user`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/system/user/page` | Paged user list |
| PUT | `/system/user/password` | Change own password |
| PUT | `/system/user/profile` | Update current user's profile |
| PUT | `/system/user/{userId}/password/reset` | Admin reset a user's password |
| PUT | `/system/user/{userId}/status/{status}` | Enable/disable a user |
