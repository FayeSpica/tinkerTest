# Tinker调研报告

## 背景
项目中可能会遇到一些小bug、小改动，这些改动很频繁或者可能急需修改。若频繁的发布版本更新就显得很繁琐，这类情况衍生出了一些热修复框架，希望在不重装应用的情况下更新资源&代码


## Tinker简介
**Tinker**是微信官方的Android热补丁解决方案，它支持动态下发代码、So库以及资源，让应用在无需重新安装的情况下实现更新。功能组件：
 
- **gradle编译插件** ：`tinker-patch-gradle-plugin`
- **核心sdk库** ：`tinker-android-lib`
- **命令行接入工具** ：`tinker-patch-cli.jar`

-------------------
## 接入过程
**Tinker**接入方式：

### 命令行接入
命令行工具`tinker-patch-cli.jar`提供了基准包与新安装包做差异，生成补丁包的功能。具体命令：
``` bash
java -jar tinker-patch-cli.jar -old old.apk -new new.apk -config tinker_config.xml -out output_path
```
相比gradle接入只需将TINKER_ID插入到AndroidManifest.xml中，例如
``` xml
<meta-data android:name="TINKER_ID" android:value="tinker_id_v1.0" />
```
另外需要一些额外配置文件
### Gradle接入
Gradle是推荐的接入方式，相比命令行接入要繁琐很多。
#### 添加Gradle依赖
- 在项目的build.gradle中，添加`tinker-patch-gradle-plugin`的依赖
- 接着在app的gradle文件app/build.gradle里需要添加tinker库的依赖以及apply tinker的gradle插件
**完整的[gradle](https://github.com/SteiensGate/tinkerTest/blob/master/app/build.gradle)文件，基于[tinker-sample-android的build.gradle](https://github.com/Tencent/tinker/blob/master/tinker-sample-android/app/build.gradle)修改**

补丁生成(Debug为例)：
> - 生成基准安装包，在gradle中设置oldApk为基准包
> - 在基准代码上添加改动
> - `gradle tinkerDebugPatch` 即可生成补丁包

补丁生成位置可通过gradle配置修改，gradle中有相当多的配置信息，默认Debug补丁生成位置是`.\app\build\outputs\tinkerPatch\debug\`

[(更多详细信息查看官方接入指南)](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)
> 注意：需要读写存储的权限

### 项目代码的修改
需要使用tinker规范的Application类，主要是因为要在Application类里初始化Tinker后才能进行补丁的操作，可通过继承DefaultApplicationLike&利用注解生成Apllication类
>在**onBaseContextAttached**时通过**TinkerInstaller.install()**方法初始化Tinker

使用: 
>- 安装：通过**TinkerInstaller.onReceiveUpgradePatch(context,url)**安装patch，可在初始化Tinker时添加自己的Service以获取patch安装结果信息
>- 修改：通过**Tinker.with(context)**获取Tinker单例，其提供rollbackPatch()、cleanPatch()等方法
 
## 测试案例
`compileSdkVersion 25`
`minSdkVersion 19`
 
| Android版本号 | 接入方式 |         场景         | 结果 |
| :----------: |:------:| :------------------: | :--: |
| 7.1          | Gradle | 修该xml布局(Textview) | 成功 |
## Tinker优缺点
### 优点

- 支持包括类、So库、资源的替换
- 补丁包较小
- 支持gradle方式打补丁
- 成功率较高(未测试失败情况)

### 缺点

- 补丁包需要重启之后生效
- 不支持修改AndroidManifest.xml
- 不支持新增四大组件
- 配置较麻烦

**调研人**：廖维明(Franklin)
**调研时间**：2017/06

-------------------

