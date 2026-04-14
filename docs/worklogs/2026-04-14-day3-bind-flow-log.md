# 2026-04-14 Day3 绑定链路工作日志

- 日期：2026-04-14
- 项目：`catenai-android-tv-legacy-dean`
- 当前分支：`feature/day2-network-foundation`
- 今日目标：把“绑定码激活 + 本地持久化 + 启动跳转绑定页”这条最小业务链真正接起来。

## 一句话结论

今天已经完成 Day3 的最小全链路，当前工程在无本地 token 时会进入绑定页，输入 6 位绑定码后可以调用 `/api/device/bind/activate`，成功后把会话信息保存到本地，并继续回到启动页。

---

## 一、今天完成的内容

## 1. 本地会话存储层完成

新增了 `PrefsStore`，当前已经支持：

- 保存 `device_token`
- 保存 `hotel_id`
- 保存 `device_id`
- 保存 `device_name`
- 保存 `config_hash`
- 读取当前本地会话
- 清空当前本地会话
- 判断本地是否已有 token

这部分已经通过单测验证。

## 2. 绑定激活仓库层完成

新增了绑定流程相关的数据模型和仓库逻辑：

- `DeviceInfo`
- `BindActivateRequest`
- `BindActivateResponse`
- `AuthRepository`

当前 `AuthRepository.bindActivate` 已经具备这些能力：

- 组装 guide 要求的请求体
- 携带设备指纹和设备信息
- 调用 `POST /api/device/bind/activate`
- 正常处理 `BIND_ACTIVATED`
- 把 `ALREADY_ACTIVATED` 也当成成功处理
- 成功后把 token、hotelId、deviceId、deviceName 落到本地

这部分也已经通过单测验证。

## 3. 最小绑定页面完成

新增了：

- `BindActivity`
- `activity_bind.xml`
- `BindCodeValidator`

当前绑定页已经具备：

- 6 位绑定码输入框
- 提交按钮
- 状态提示文本
- 绑定码格式校验
- 调用绑定仓库逻辑
- 成功后返回启动页

## 4. 启动页路由接通

修改了 `SplashActivity`。

当前行为：

- 如果本地没有 token，直接跳转绑定页
- 如果本地已有 token，先留在启动页

这为下一阶段的 session 校验预留了位置。

---

## 二、今天采用的实现方法

今天仍然按 TDD 推进，核心顺序是：

1. 先写失败测试
2. 在编译服务器确认它确实失败
3. 再补最小实现
4. 再回编译服务器确认测试变绿

今天实际跑过的三轮红绿循环：

1. `PrefsStoreTest`
2. `AuthRepositoryTest`
3. `BindCodeValidatorTest`

---

## 三、今天的验证结果

编译服务器验证通过：

- `PrefsStoreTest` 通过
- `AuthRepositoryTest` 通过
- `BindCodeValidatorTest` 通过
- Day3 相关组合测试通过
- `assembleRelease` 通过

当前 release 产物：

- `/opt/android/catenai_os_legacy/app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 四、今天新增或修改的重点文件

## 新增

- `app/src/main/java/com/catenai/hotelos/legacy/data/local/PrefsStore.java`
- `app/src/main/java/com/catenai/hotelos/legacy/data/model/DeviceInfo.java`
- `app/src/main/java/com/catenai/hotelos/legacy/data/model/BindActivateRequest.java`
- `app/src/main/java/com/catenai/hotelos/legacy/data/model/BindActivateResponse.java`
- `app/src/main/java/com/catenai/hotelos/legacy/data/repo/AuthRepository.java`
- `app/src/main/java/com/catenai/hotelos/legacy/bind/BindCodeValidator.java`
- `app/src/main/java/com/catenai/hotelos/legacy/bind/BindActivity.java`
- `app/src/main/res/layout/activity_bind.xml`
- `app/src/test/java/com/catenai/hotelos/legacy/data/local/PrefsStoreTest.java`
- `app/src/test/java/com/catenai/hotelos/legacy/data/repo/AuthRepositoryTest.java`
- `app/src/test/java/com/catenai/hotelos/legacy/bind/BindCodeValidatorTest.java`

## 修改

- `app/src/main/java/com/catenai/hotelos/legacy/SplashActivity.java`
- `app/src/main/java/com/catenai/hotelos/legacy/device/DeviceInfoProvider.java`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`

---

## 五、今天遇到的小问题

## 1. Git 短暂出现 `index.lock`

现象：

- 一次 `git add` 时提示仓库存在 `index.lock`

处理：

- 先检查是否有持续占用
- 确认锁文件已自行消失后，再重新执行 git 操作

结论：

- 只是瞬时残留，不是仓库损坏

## 2. 本机依然没有 Java

现象：

- 仍然不能在本机直接跑 Gradle

处理：

- 继续通过编译服务器跑红绿测试和 release 构建

结论：

- 当前开发节奏不受影响

---

## 六、当前项目状态

当前已经完成：

- Day1：项目启动和 4.4 工程骨架
- Day2：设备与网络基础层
- Day3：最小绑定全链路

当前项目已经具备：

- 4.4 可编译工程
- 设备指纹能力
- 统一请求头
- 统一网络客户端
- 本地会话存储
- 绑定激活仓库
- 绑定页面
- 启动时无 token 自动跳绑定页

---

## 七、下一步计划

下一步进入 Day4，目标是把“有 token 后的启动验证”接起来。

优先顺序建议：

1. `GET /api/device/session`
2. `bindStatus` 判定
3. token 无效时清空本地并回绑定页
4. 成功后上报设备能力
5. 为 Day5 的 TV 配置拉取做准备

---

## 八、阶段提醒

今天可以视为一个明确阶段完成：

- Day3：绑定码激活 + 本地持久化 + 启动跳转绑定页，已完成

这意味着项目已经从“只有基础底座”进入“第一条真实业务流程已打通”的状态。

下一阶段是：

- Day4：session 校验和启动认证链路

