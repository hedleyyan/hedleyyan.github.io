---
layout: post
title: "负责任再谈 Callback 与 Delegation"
date: 2015-07-18 15:03:11 +0800
comments: true
categories: 技术
tags: [Java, Pattern, Framework]
toc: true
---

# 引出

话接上回[不负责瞎说说 Patterns 与 Frameworks][]，还谈引出问题：*Wrapper classes are not suited for use in callback frameworks*。虽然最后给出了一个可以验证的 *Demo Code*，但是个中缘由仍影影绰绰，真相仿佛隔着一层薄纱在向我招手。心急难耐，遂从引出 *SELF problem* 的论文出发，顺藤摸瓜按图索骥，也许是我的真心打动了上天，也许是我太帅，幸得 [yqj2065][] 老师指点，才把真相抱了个满怀。

幸福来的太快，虚幻又不真实，也许我看到的只是背影，又或许我看到的是庐山真面，放空思绪，容我一点点回忆。不知道待我从求知路上走的更远，回头再看今天的“负责任”是不是够炫酷呢。

先甩大招上结论

*	Java 不支持 *delegation*
*	C# 的 *delegate* 不是真正的 *delegate*
*	<a name="想法">*delegation* ：我只需要你的原型方法，但我不要依赖于你 </a>

<!--more-->

# 线索

写完上次的“不负责任”，感觉不爽，因为 *SELF problem* 论文 [[Using Prototypical Objects to Implement Shared Behavior in Object Oriented Systems][Lieberman86]] 里还有理解不了的点，*Wrapper classes are not suited for use in callback frameworks* 这个问题感觉理解的还可以，但是相应的，论文里提到的 *delegation* 为什么就 OK 呢？论文里说的这个 *delegation* 又是如何实现的呢？

顺着论文里提到的 *delegation appearing several in Lisp-based object oriented systems..* 调查了一下 *Lisp* 语言的背景。

看到了 *Lisp* 的语法以后，忽感和 *Java8 lambda* 表达式长的很像。于是再查 *Java8 lambda* 是不是就是我困惑的那个 *delegation*。

了解了 *Java8 lambda* 的机制，并查到了较可信的[证据][Java Delegates?]说 *Java8 lambda* 不是 *delegation*，并且以后 Java 都不会支持 *delegation*。

但是上面的信息中提到 C# 是实现了 *delegate* 的。然后了解了 C# 的 *delegate*、*event* 关键字，写了些代码来验证我的[想法][]。

但是很不幸，没有验证成功。我发现 C# 的 *delegate* 其实和 *Java8 lambda* 是一样的原理，不过是一种更方便的语法加上更灵活的实现。

所以又回头开始理 *SELF problem* 论文，到底是哪里不一致。开始怀疑 C# 的 *delegate* 和论文里说的 *delegation* 不是一个频道。

太帅了，又找到了证据。

但是更帅的是，我搞懂了 *Callback*。

# 真· *Callback*

{% img right /images/Snip20150719_6.png 400 300 %}

上回给 *callback* 下的一句话定义：*If you call me, i will call back.*  Well，丢掉忘掉扔到垃圾桶。灵感来自[这里][《编程导论（Java）·9.3.1Java回调》1]，诚如我下面的留言：拨云见日醍醐灌顶，痛快！

首先， *callback* 是名词，不是动词，不是场景，它就是一个方法，是回调函数的简称。

其次， *callback* 强调上下文，有一个分层的概念在。如果是同一层中的调用，就变成了普通的依赖关系，多态的概念。 *callback* 是特殊场景的多态应用。

了解了这两点之后，理解起来简直是爽，一张类图搞定。

{% blockquote %}
一个回调函数 / 方法（简称回调 / callback）是上层模块实现的，将被下层模块（反过来）执行的方法。
{% endblockquote %}

可见所谓 *framework*，根本就是建立在 *callback* 的基础上的。我们要想做填空题，那么 *framework* 封装的细节是如何调用我们的代码的呢？ *callback*！*[Hollywood principle][]* 强调的正是 *callback* 的使用情景：你（上层 *Client*）不需要来询问我（底层 *Server*）事件是不是发生了，发生了我会通知你（通过 *callback*）。

# 伪· *Delegation*

{% blockquote %}
Worse still, almost every reference you’ll find for "delegation" shows you examples of just object collaboration with message forwarding. They are examples of plain old method calls and not delegation.
{% endblockquote %}

[The Gang Of Four Is Wrong And You Don't Understand Delegation][] 是最让我感动的线索，之前各种凭空猜想的 *delegation* 到底是何实现终于可以尘埃落定。基本上所有的伪 *delegation* 都是 *composite* 加 *message forward*，包括 Java，包括 C# 和 C++。 *delegation* 不可能通过 *Design Pattern* 得到实现。

## C# *delegate*

[Delegates and Events in C# / .NET][] 例子很棒，简简单单说明白了 C# 中的 delegate 和 event 的原理，然后你会发现它和 Java 的各种充满 *Listeners* / *Handlers* 的代码意思大同小异。例子中的代码完全可以用 Java 实现，一个 *callback* 搞定。

{% include_code DelegationTest.java %}

但是 C# 的 delegate 机制比 Java 的更灵活，因为 delegate 可以被“委托”多个不同方法，而这些方法只需要有相同的 `signature` 和返回值，并不需要是同一个接口的声明，所以一个类中的多个方法都可以“委托”它。Java 并没有这种原生支持，[A Java Programmer Looks at C# Delegates][] 有如何用 Java 模拟 C# delegate 的实现，有兴趣的可以看看，那是相当费劲。

## *Java8 lambda* 表达式

{% blockquote %}
It is unlikely that the Java programming language will ever include this construct. Sun already carefully considered adopting it in 1996, to the extent of building and discarding working prototypes. Our conclusion was that bound method references are unnecessary and detrimental to the language. This decision was made in consultation with Borland International, who had previous experience with bound method references in Delphi Object Pascal.

We believe bound method references are unnecessary because another design alternative, inner classes, provides equal or superior functionality. In particular, inner classes fully support the requirements of user-interface event handling, and have been used to implement a user-interface API at least as comprehensive as the Windows Foundation Classes.

We believe bound method references are harmful because they detract from the simplicity of the Java programming language and the pervasively object-oriented character of the APIs. Bound method references also introduce irregularity into the language syntax and scoping rules. Finally, they dilute the investment in VM technologies because VMs are required to handle additional and disparate types of references and method linkage efficiently.
{% endblockquote %}

Java 官方很早就声明了其不会支持 *delegate*（这里指 C# 的 *delegate*）。Sun 相信可以通过内部类实现需要的功能，*method reference* 会破坏 Java 的 *simplicity*。对 Java 的简单特性表示粉赞👍。

*Java8 lambda* 的出现解决了内部类的臃肿写法，语法更清凉，口感更酸爽，更重要的是其可利用内部循环（`for` 是外部循环）充分利用多处理器。但其本质上就是一个匿名方法，并不是 *method reference*，不赘述更多细节。

### *Monad*

{% blockquote %}
In functional programming, a monad is a structure that represents computations defined as sequences of steps: a type with a monad structure defines what it means to chain operations, or nest functions of that type together.
{% endblockquote %}

*Monad* 本是函数编程语言中的高级特性，*Java8 lambda* 的出现却让人不由自主的把他们联想到一起。[这篇棒棒的引出教程][Functors, Applicatives, And Monads In Pictures]完美诠释了什么叫一张图胜过千言万语，虽然我没往细里看 *Monad*，但这篇文章里的图借来理解 *Java8 lambda* 的内部循环也是极好的。

{% img /images/monad/fmap_just.png %}
{% img /images/monad/fmap_list.png %}
{% img /images/monad/monad_chain.png %}

通过 *Java8 lambda* 也可以实现初级的 *Monad*，脑洞略大暂不做细表。

# 真· *Delegation*

{% blockquote Lieberman86 %}
Delegation removes the distinction between classes and instances. Any object can serve as a prototype. To create an object that shares knowledge with a prototype, you construct an extension object, which has a list containing its prototypes which may be shared with other objects, and personal behavior idiosyncratic to the object itself. When an extension object receives a message, it first attempts to respond to the message using the behavior stored in its personal part. If the object's personal characteristics are not relevant for answering the message, the object forwards the message on to the prototypes to see if one can respond to the message. This process of forwarding is called delegating the message.
{% endblockquote %}

*delegation* 是建立在 *prototype object* 的基础上的，以 *class* 为基础的 *inheritance system* 不能实现 *delegation*。

*delegation* 和 *message forward* 区别的唯一标准就是**最终执行的方法 M 内是否可以引用到最初发起请求的对象 O**，更简单粗暴一点：**M 内的 `this` 是不是指代 O**。[*SELF problem*]

## *Javascript*

了解的语言少是短板，如果早了解了 *Javascript* 的大概机制和语法，少绕多大一圈子啊。

```javascript
function Container(){};
Container.prototype = new Object();
Container.prototype.announce = function(){ alert("these are my things: " + this.things) };

function Bucket(things){this.things = things};
Bucket.prototype = new Container();

bucket = new Bucket("planes, trains, and automobiles")
bucket.announce() // alerts "these are my things: planes, trains, and automobiles"
```

# 乱入 *delegate* 的 *Patterns*

由于 GoF 在设计模式中大范围杀伤式的使用了 *delegation* 一词，各种 *pattern* 中的各种 *delegate* 乱入满天飞一直流传至今。当然它们都是 *message forward* 和 *composite* 的结合体。

## *Decorator Pattern* 和 *Proxy Pattern*

{% blockquote %}
Decorator Pattern focuses on dynamically adding functions to an object, while Proxy Pattern focuses on controlling access to an object.
{% endblockquote %}

综合 [Differences between Proxy and Decorator Pattern][] 所有回答的意见，可以得到一个比较全面的结论。

装饰者模式和代理模式的类图看上去没什么差，他们的区别主要在目的。代理模式中，代理类对被代理的对象有控制权，决定其执行或者不执行。而装饰模式中，装饰类对代理对象没有控制权，只能为其增加一层装饰，以加强被装饰对象的功能。

### *Dynamic Proxy*

设想一个被代理的对象有很多方法，那么它的代理类看上去可能就很像上回提到的[这段代码][forward]。而我们想要实现的一个代理其权限判断很集中，那么是不是把这段集中的权限控制的代码剥离出来，感觉世界会更美好呢。

`java.lang.reflect` 包下的 `InvocationHandler` 和 `Proxy` 类对动态代理提供支持，我们只需要提供一个 `InvocationHandler` 的实例来处理集中的权限控制逻辑就好。

# 总结

引用一段百度的委托和代理的区别作为总结，想来学会写代码考个律师应该也不是太难的事。

{% blockquote %}
所谓代理，就是指一方授予他方代理权，他方依代理权与第三方进行法律行为，其行为后果由一方承担的一种民事法律制度。委托是一方将一定的事务委诸于另一方实施的法律制度。

委托和代理的区别在于：第一，委托规范的是委托人和受托人双方之间的关系；而代理规范的是本人、代理人和第三人的关系。第二，代理关系中代理人代理的对象是进行意思表示和接受意思表示的行为；而委托中受托人代为实施的行为可以是法律行为，也可以是事实行为。第三，代理包括对内和对外两种关系，对内是代理人和被代理人之间的关系，而对外是代理人和第三人之间的关系；而委托只是委托人和受托人之间的关系。
{% endblockquote %}

# 扩展阅读

*	[Using Prototypical Objects to Implement Shared Behavior in Object Oriented Systems][Lieberman86]
*	[Java Delegates?][]
*	[Is there delegate in Java 8 (JDK8)?][]
*	[C# does not implement delegates][]
*	[The Gang Of Four Is Wrong And You Don't Understand Delegation][]
*	[《编程导论（Java）·9.3.1Java回调》1][]
*	[《编程导论（Java）·9.3.1Java回调》2][]
*	[A Java Programmer Looks at C# Delegates][]
*	[Delegates and Events in C# / .NET][]
*	[Differences between Proxy and Decorator Pattern][]
*	[What is a monad?][]
	*	[Functors, Applicatives, And Monads In Pictures][]
	*	[You Could Have Invented Monads! (And Maybe You Already Have.)][]
	*	[Java8 的 Monad][]


[yqj2065]:	http://blog.csdn.net/yqj2065
[想法]:	#想法
[不负责瞎说说 Patterns 与 Frameworks]:	/blog/2015/07/10/zhuang-shi-zhe-mo-shi-decorate-pattern-yu-hui-diao-ji-zhi-callback-framework/
[Hollywood principle]:	/blog/2015/07/10/zhuang-shi-zhe-mo-shi-decorate-pattern-yu-hui-diao-ji-zhi-callback-framework/#inversion-of-control
[forward]:	/blog/2015/07/10/zhuang-shi-zhe-mo-shi-decorate-pattern-yu-hui-diao-ji-zhi-callback-framework/#forward

[Lieberman86]: https://static.aminer.org/pdf/PDF/000/522/451/using_prototypical_objects_to_implement_shared_behavior_in_object_oriented.pdf
[Java Delegates?]:	http://stackoverflow.com/questions/44912/java-delegates#comment13546516_44916

[C# does not implement delegates]:	http://stackoverflow.com/questions/1746332/delegates-and-callbacks
[The Gang Of Four Is Wrong And You Don't Understand Delegation]:	http://www.saturnflyer.com/blog/jim/2012/07/06/the-gang-of-four-is-wrong-and-you-dont-understand-delegation/
[Is there delegate in Java 8 (JDK8)?]:	http://stackoverflow.com/questions/20311779/is-there-delegate-in-java-8-jdk8/30925223#30925223

[《编程导论（Java）·9.3.1Java回调》1]:	http://blog.csdn.net/yqj2065/article/details/39481255
[《编程导论（Java）·9.3.1Java回调》2]:	http://blog.csdn.net/yqj2065/article/details/31441221
[A Java Programmer Looks at C# Delegates]:	http://www.onjava.com/pub/a/onjava/2003/05/21/delegates.html
[Delegates and Events in C# / .NET]:	http://www.akadia.com/services/dotnet_delegates_and_events.html#The%20Second%20Change%20Event%20Example

[You Could Have Invented Monads! (And Maybe You Already Have.)]:	http://blog.sigfpe.com/2006/08/you-could-have-invented-monads-and.html
[Functors, Applicatives, And Monads In Pictures]:	http://adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html
[What is a monad?]:	http://stackoverflow.com/questions/44965/what-is-a-monad
[Java8 的 Monad]:	http://www.jdon.com/idea/java8-monad.html
[Differences between Proxy and Decorator Pattern]:	http://stackoverflow.com/questions/18618779/differences-between-proxy-and-decorator-pattern

