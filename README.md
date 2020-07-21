# ProgressNote
使用**Github Actions**自动检查提交、构建APK并发布到Releases  
![Android CI](https://github.com/0Kirby/ProgressNote/workflows/Android%20CI/badge.svg)
![Create APK Release](https://github.com/0Kirby/ProgressNote/workflows/Create%20APK%20Release/badge.svg)
# 简介
**《天天笔记》** 是我的毕业设计选题《基于Android的云笔记本设计与实现》。  
本课题要求以Intellij IDEA为集成开发环境，Java为主要编程语言，采用云服务器实现云同步功能，进行基于Android平台的移动APP云笔记本的开发。
# 主要内容
## 客户端
开屏动画：展示软件的logo和介绍语  
数据库管理：初次启动软件时建立数据库，每次执行保存、修改、删除等操作时对数据库中保存的笔记数据能正确进行操作  
界面构建：根据正在执行的操作，从数据库中读取笔记数据显示在UI中  
笔记管理：对笔记进行新增、查看、修改、保存、删除等操作  
侧滑菜单：用户登录、显示基本信息、更换头像、进行同步、查看帮助以及修改设置等  
个性化设置：可对一些定制功能进行设置，如更换界面颜色、修改笔记排列样式等
## 服务器
用户管理：新用户可进行注册，老用户可进行登录与修改  
数据库管理：对每个用户数据进行管理，包括登录认证与笔记数据等  
笔记同步：通过OKHttp3建立连接，对客户端和服务端的笔记数据进行双向同步  
头像存取：通过Java字节流方式进行头像的上传与下载，根据UUID生成唯一的文件名，确保同一用户多次上传的头像不会因重名而被覆盖，并将头像的路径存入数据库以便用户登录时读取头像
# 联系我
QQ：[623768813](http://wpa.qq.com/msgrd?v=3&uin=623768813&site=qq&menu=yes)  
E-mail：jty.1234567890@163.com  
个人主页：https://zerokirby.cn  
天天笔记主页：https://note.zerokirby.cn  
博客：https://blog.zerokirby.cn
