---
layout: post
title: "Octopress 搭建记录"
date: 2015-05-17 22:29:18 +0800
comments: true
categories: 技术
tags: 配置
toc: true
---

本文是小站搭建的流水账记录，收集了各种用到的链接地址。

# 搭建
*	参照 [octopress 官方搭建手册][]，一步一步来。到 `gem install bundler` 时候卡壳，网络不通
*	修改 [RubyGems 镜像][]，连接通了。但是一直报缺少依赖包的异常，按照提示手动一个个安装完依赖包
*	安装完毕，修改 `_config.yml` 配置文件

# 留言系统
*	参照[这篇文章][多说评论]添加多说评论
*	[修改多说样式][多说评论样式]，样式代码添加在多说系统的管理后台的基本设置里。代码位于 `source/downloads/code/duoshuo.css`

# 访问速度
参照[这篇文章][访问速度]替换若干 *google* 源。

<!--more-->

# *ST* 书写插件
*	*Octopress* ： 可以在 *ST* 中快捷新建 *Post* ，快捷生成、部署
*	*Octopress Snippets* ： `CMD + SHIFT + P` 接输入 `Snippet` ，调用语法模板
*	*Markdown Extended* ： `CMD + SHIFT + P` 接输入 `Set Syntax` ，设为 `Markdown Extended`。代码片段彩色显示
*	*Markdown Preview* ： 主要用到 `Preview in browser` 来进行静态预览，以及 `cheat sheet` 来查看语法
*	*Monokai Neue* ： `Color Scheme` 修改， Markdown 颜色显示。修改 `Packages/Monokai Neue/Monokai-Neue.tmTheme` 文件，在 `settings -> settings` 代码段下修改 `<key>selection</key> <string>#00B2EE</string>`，更改选中背景颜色

效果如下
{% img /images/Snip20150525_38.png %}

# *TOC (Table of Contents)*
参照[这篇文章][TOC]搭建 *TOC* 解决方案。遇到几个问题

*	如果 *TOC* 的左面有 `blockquote` ， *TOC* 的 `link` 无法点击
	*	在 `sass/custom/_screen.scss` 中加入代码 `position:relative; z-index: 99999;` ，将 *TOC* 的 `<div>` 置顶
*	自动生成的锚点只对英文标题有效
	*	编辑 `source/javascripts/jquery.tableofcontents.min.js`	，搜索 `text.toLowerCase().replace` ，去掉 `.replace(/[^a-z0-9 -]/gi,'')` 这部分代码，这句代码过滤了中文字符
*	此方案的 *TOC* 只能生成 `h1 ~ h3` ，它的配置可以在 `source/javascripts/jquery.tableofcontents.min.js` 中修改。但是为了方便，应用此默认配置，修改标题风格

# 风格
生成网页的风格个性化都在 `sass/custom` 中进行定义，参考了 https://github.com/bmc/brizzled ，主要对标题的字体大小进行了调整，其他一些微调待定。标题的字体颜色主要在 `source/_includes/custom/header.html` ， `sass/custom/_fonts.scss` ， `sass/custom/_styles.scss` 几个地方相应修改。其他样式对应位置[参考这里][样式修改]。

# 其他
*	链接在新窗口打开、列表排版、*404ERROR* 公益页面，参考[这篇文章][连接在新窗口打开、列表的排版、404ERROR页面]
*	文章分类侧边栏、导航栏设置，参考[这篇文章][分类]
*	[其他配置][]很全，重点参考

## 原文链接
参照[这篇文章][添加原文链接]，在每篇文章下面自动生成作者信息，原文信息。

*	注意：每篇文章最后要有留白行，否者生成的原文信息会有问题。

## 云标签
*	参照 [Tag Cloud 搭建][]，*clone* 几个文件到本地目录
*	使用方法：在文件头添加标识 `tags: xxx` ，但此时无法添加多个标签
*	[这篇文章][Tag Cloud2]给出了解决方案，修改 `tags.html` 文件去掉 `limit` 属性，多标签写法 `tags: [xx1, xx2]` 

## 回到顶部
参考[Octopress添加回到顶部功能][]。

## 数学公式
参考[kramdown配置][]。


[octopress 官方搭建手册]:	http://octopress.org/docs/setup/
[RubyGems 镜像]:	http://ruby.taobao.org/
[Tag Cloud 搭建]: http://codemacro.com/2012/07/18/add-tag-to-octopress/
[Tag Cloud2]: http://my.oschina.net/pangyi/blog/379606
[添加原文链接]: http://my.oschina.net/pangyi/blog/379620
[多说评论]:  http://droidyue.com/blog/2014/07/29/integrate-duoshuo-in-octopress/
[多说评论样式]:	http://shenchaofei.cn/duoshuo-comment-box-css-custom/328.html
[访问速度]:	http://droidyue.com/blog/2014/06/22/fix-octopress-slow-loading-speed-issue-in-china-mainland/
[连接在新窗口打开、列表的排版、404ERROR页面]: http://blog.csdn.net/biaobiaoqi/article/details/9289563
[分类]:	http://www.jianshu.com/p/0ac2ac1a8e45
[TOC]:	http://brizzled.clapper.org/blog/2012/02/04/generating-a-table-of-contents-in-octopress/
[样式修改]:	http://www.360doc.com/content/12/0215/22/1016783_186940749.shtml
[Octopress添加回到顶部功能]:	http://www.tuicool.com/articles/qu6ZfiV
[kramdown配置]:	http://www.tuicool.com/articles/miaUR3
[其他配置]:	http://shengmingzhiqing.com/blog/octopress-lean-modification-4.html/#fontawesome

