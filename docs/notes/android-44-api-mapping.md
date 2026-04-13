# Android 4.4 API 对照笔记

这份笔记只记录我们已经在 4.4 工程里确认过的接口骨架，目的是让 Day3 绑定流程接起来时不再到处翻 guide 和 5.0 快照。

## 当前统一约定

- 基础域名：`https://catenai.cn`
- 指纹公式：`macAddress|serial|board` 做 `SHA-256`
- 设备类型：固定先用 `ANDROID_TV`
- 设备厂商：先走 `BuildConfig.DEVICE_VENDOR`，当前默认值是 `daqian`
- 通用请求头至少带这些：
  - `x-device-token`
  - `x-device-type`
  - `x-device-vendor`
  - `x-device-model`
  - `x-app-version`
  - `Content-Type: application/json`

## Day2 已落地骨架

- `DeviceFingerprint`：负责生成设备指纹
- `DeviceInfoProvider`：集中拿设备型号、序列号、MAC、版本号、屏幕分辨率
- `ApiHeaders`：统一补请求头
- `ApiClient`：统一拼接 base URL、创建 JSON 请求、配置 OkHttp
- `Tls12SocketFactory`：给 Android 4.4 明确打开 TLS 1.2

## 下一步要接的接口

- `POST /api/device/bind/activate`
  - Day3 绑定码激活入口
- `GET /api/device/session`
  - Day4 启动后校验本地 token 是否仍有效
- `POST /api/device/capabilities/upsert`
  - Day4 上报设备能力
- `GET /api/v1/tv/config?hotelId={hotelId}`
  - Day5 拉取酒店 TV 配置

## 参考来源

- guide 里的 Android 4.4 流程说明
- 本地 5.0 快照仓库：`C:\Dean-codex\catenai-android-tv-legacy`

## 现在的结论

4.4 工程不去硬搬 5.0 的实现细节，只对齐三件事：

1. 接口路径
2. 请求头语义
3. 设备身份字段

这样后面做绑定、session、配置拉取时，复杂度会低很多。
