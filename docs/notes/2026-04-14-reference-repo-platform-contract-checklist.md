# 5.0 参考仓库提炼的平台契约清单

这份清单不是要把 `C:\Dean-codex\catenai-android-tv-legacy` 原样搬到 4.4 工程里，而是把里面已经能坐实的平台契约提炼出来，给 `catenai-android-tv-legacy-dean` 对接口用。

一句话原则：

- 5.0 快照可以拿来“对接口、对字段、对状态”
- 不适合直接拿来“照搬架构、照搬产品流程”
- 真实运行数据，例如测试绑定码、后台页面入口、当前酒店配置，不在参考仓库里

---

## 一、这份清单怎么用

适合直接拿来指导 4.4 工程的内容：

- 基础域名
- 接口路径
- 请求头语义
- session / config / license 这些返回字段
- 认证状态枚举
- 哪些地方是平台规则，哪些地方是产品规则

不适合直接下结论的内容：

- 平台后台现在到底给哪个测试绑定码
- 绑定码在哪个后台页面生成
- 当前项目最终要不要走 5.0 的双 APK 模式
- 平台线上接口现在是不是已经完全和 5.0 快照一致

---

## 二、已经能坐实的平台契约

### 1. 平台基准域名

- 参考仓库里的业务 APK 和服务模块默认都指向：`https://catenai.cn`
- 这说明 4.4 工程把 `BASE_URL` 先定成 `https://catenai.cn` 是合理的

参考来源：

- `catenai_os_native/app/build.gradle.kts`
- `catenai_service/app/build.gradle.kts`

### 2. 设备鉴权是“设备身份鉴权”，不是浏览器态登录

5.0 快照里反复出现这些请求头：

- `x-device-token`
- `x-device-id`
- `x-hotel-id`
- `X-Portal-Channel`

可以先把它理解成：

- `x-device-token`：当前设备会话的主身份
- `x-device-id`：设备唯一标识
- `x-hotel-id`：酒店维度
- `X-Portal-Channel`：渠道或分发环境维度

这意味着 4.4 工程后面接接口时，不能只想着“拿个 token 就完了”，`deviceId/hotelId` 很可能也是平台决策输入。

参考来源：

- `catenai_portal_launcher/docs/PORTAL_BACKEND_REQUIREMENTS_V05.md`
- `catenai_portal_launcher/docs/PORTAL_BACKEND_P0_IMPLEMENTATION_SLICE.md`

### 3. Session 接口已经比较清楚

5.0 业务代码里明确请求：

- `GET /api/device/session`

从测试和代码可以提炼出这批核心字段：

- `sessionId`
- `deviceId`
- `name`
- `hotelId`
- `hotelName`
- `bindStatus`
- `expiresAt`

另外还出现了：

- `adaptationPolicy`
- `policyVersion`
- `expiredNoticeIntervalSec`

这说明 `session` 不只是“token 还活不活”，它还顺带承载了一部分设备适配策略和提示策略。

对 4.4 当前阶段最重要的是：

- `bindStatus`
- `deviceId`
- `hotelId`
- `name`

这些就是我们现在 Day4 最小链路真正依赖的字段。

参考来源：

- `catenai_os_native/app/src/main/java/com/ylhy/catenai/os/TvConfigClient.kt`
- `catenai_os_native/app/src/test/java/com/ylhy/catenai/os/ApiParsingTest.kt`
- `catenai_os_native/app/src/test/java/com/ylhy/catenai/os/BusinessAuthNoticeAndPortalBridgeTest.kt`

### 4. TV 配置接口不是只有一个地址

5.0 代码里对 TV 配置接口做了优先级：

- 优先：`GET /api/tv-config?hotelId={hotelId}&resolution={resolution}`
- 回退：`GET /api/v1/tv/config?hotelId={hotelId}&resolution={resolution}`

这对 4.4 很有价值，因为它说明：

- 平台接口可能有新旧两套入口
- 我们后面做 Day5 时，最好也保留一个“优先新接口，失败再回退旧接口”的策略

参考来源：

- `catenai_os_native/app/src/main/java/com/ylhy/catenai/os/TvConfigClient.kt`

### 5. 还存在一批后续会用到的平台接口

从参考仓库能看到，后面业务链路很可能还会碰到这些接口：

- `POST /api/tv/license/check`
- `GET /api/tv/manifest?hotelId={hotelId}`
- `GET /api/tv/media/catalog?hotelId={hotelId}`
- `POST /api/device/engineer/verify-pin`
- `GET /api/device/adaptation/policy`
- `POST /api/device/capability/upsert` 或同类能力上报接口

对 4.4 来说，这些接口不一定要马上全做，但说明平台不是只有“绑定 + session + config”这三条线。

参考来源：

- `catenai_os_native/app/src/main/java/com/ylhy/catenai/os/TvConfigClient.kt`
- `catenai_service/app/src/main/java/com/catenai/service/hal/TvHalService.kt`

### 6. 认证状态不止 ACTIVE / 非 ACTIVE 两种

参考仓库里多次出现这些状态：

- `AUTHORIZED`
- `ACTIVE`
- `UNBOUND`
- `DEVICE_UNBOUND`
- `TOKEN_INVALID`
- `TOKEN_REVOKED`

大白话理解：

- `AUTHORIZED / ACTIVE`：可以正常跑业务
- `UNBOUND / DEVICE_UNBOUND`：设备没绑定或已解绑
- `TOKEN_INVALID / TOKEN_REVOKED`：token 不可用或被平台收回

这件事非常重要，因为它决定了 4.4 后面不能只写“成功 / 失败”两种分支，而要开始准备更细的认证状态机。

参考来源：

- `docs/superpowers/plans/2026-03-20-android-3-0-unbound-pass-through.md`
- `catenai_service/app/src/main/java/com/catenai/service/ServiceBootstrapStore.kt`
- `catenai_portal_launcher/app/src/main/java/com/catenai/portal/PortalAuthBridge.kt`

### 7. 平台会下发“提示间隔”之类的策略

参考仓库说明，后台不是只返回授权结果，还会附带类似下面的策略字段：

- `expiredNoticeIntervalSec`
- 或者嵌套在 `policy` 对象里

这说明将来 4.4 如果做“未绑定提醒 / 授权失效提醒 / 周期提示”，最好不要先写死成本地常量，而是预留成可被后台刷新。

参考来源：

- `docs/superpowers/plans/2026-03-20-android-3-0-unbound-pass-through.md`
- `catenai_os_native/app/src/test/java/com/ylhy/catenai/os/BusinessAuthNoticeAndPortalBridgeTest.kt`

### 8. 5.0 的整体业务边界是双 APK

从联调指南和规划文档看，5.0 快照里平台分工大致是：

- `Portal`：绑定入口、token 持有、鉴权预检、OTA、拉起业务 APK
- `业务 APK`：前台业务体验、内容展示、缓存和降级体验

这个边界很有参考意义，但 4.4 当前不能直接照搬，原因是你们已经明确了：

- 这次 4.4 是从零搭工程
- 先按 guide 和 5.0 快照对接口
- 不等于先做完整双 APK 体系

所以这条信息更适合拿来理解平台整体方向，不适合现在就当强制实现方案。

参考来源：

- `联调前_双APK流程图文指南.md`
- `docs/superpowers/plans/2026-03-20-android-3-0-unbound-pass-through.md`

---

## 三、现在还不能从参考仓库里直接拿到的东西

### 1. 真实可用的 6 位绑定码

拿不到。

原因很简单：

- 绑定码是后台运行时生成的数据
- 不会硬编码在 5.0 参考仓库里

所以这件事必须问平台方。

### 2. 绑定码后台入口

现在还不能从参考仓库里直接确定：

- 到底在哪个后台页面生成
- 谁来生成
- 有效期多久
- 一次性还是可复用

这也必须问平台方。

### 3. `POST /api/device/bind/activate` 的完整正式契约

我们在 4.4 工程里已经按 guide 和当前推断接了这条线，但参考仓库里对这条接口的完整说明并不像 `session` 和 `tv-config` 那么清楚。

也就是说：

- 路径大概率没问题
- 但成功态、失败态、错误码、返回字段，仍然建议找平台再确认一次

### 4. 最终产品流程

比如下面这些问题，参考仓库只能给方向，不能给最终答案：

- 绑定成功后到底先进首页，还是先进某个引导页
- 绑定失败时页面该怎么提示
- 已绑定设备再次输入绑定码时，产品想让用户看到什么
- 未绑定状态是留在本地业务页，还是回 Portal

这些属于“产品决策”，不是纯接口决策。

---

## 四、对 4.4 当前开发最有用的落地结论

### 1. 当前 4.4 已经做对的地方

- `BASE_URL` 先对齐 `https://catenai.cn`
- 先接 `bind -> session -> config` 这条最小全链路
- 会话里重点抓 `hotelId/deviceId/name/bindStatus`
- 把真机可装的 debug 包先跑通

### 2. 下一步接口策略建议

- `bind`：继续沿用当前最小实现，但尽快补平台错误码确认
- `session`：后面开始准备支持更多状态，不只看 `ACTIVE`
- `tv config`：建议按 5.0 逻辑做“双地址兜底”
- `policy`：对提醒间隔、适配策略字段先留扩展位

### 3. 现在最该问平台和产品的问题

平台侧：

- 6 位绑定码在哪个后台页面生成
- 测试环境有没有现成可用绑定码
- `bind/activate` 的成功返回和失败错误码有哪些
- `session` 现在正式支持哪些 `bindStatus`
- `tv-config` 和 `/api/v1/tv/config` 当前哪个是正式口径

产品侧：

- 绑定成功后该进入哪里
- 绑定失败分别怎么提示
- token 失效后是回绑定页，还是进本地降级页
- 未绑定提醒文案和频率要不要跟后台策略走

---

## 五、建议你怎么理解这份参考仓库

最实用的理解方式是：

- 它不是 4.4 的现成答案
- 它是“平台契约样本库”
- 它最大的价值是帮我们少猜接口、多问对问题

所以后面开发时，优先级建议一直保持为：

1. 先看 4.4 guide
2. 再看 5.0 参考仓库有没有现成字段和状态样本
3. 样本都不够时，再去问平台和产品

这样能最大限度减少返工。
