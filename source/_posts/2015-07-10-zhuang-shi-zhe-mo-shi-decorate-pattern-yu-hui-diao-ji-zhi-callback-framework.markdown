---
layout: post
title: "不负责瞎说说 Patterns 与 Frameworks"
date: 2015-07-10 17:11:00 +0800
comments: true
categories: 技术
tags: [Java, Pattern, Framework]
toc: true
---

# 引出

[Effective Java, Item 16][EJ] 中说 *Wrapper classes are not suited for use in callback frameworks*，想不明白，一顿 Google 依旧看个似懂非懂。我想把这些凌乱的线索都整理出来，抽抽剪剪排列组合一下，也许就看的透彻点了呢。

关于 *Wrapper class* 的引出问题，个人认为比较[炫酷的解答][]。

{% blockquote %}
Because a wrapped object doesn't know of its wrapper, it passes a reference to itself(this) and callbacks elude the wrapper. This is known as the SELF problem [Lieberman86]. This leads to subtle bugs, like the wrapper missing the event. Or the wrapper and wrapped object both registering for the same events - leading to duplicate processing and potential concurrency issues as well. If you don't know (don't have the source code) where callbacks are registered, it may be impossible to work around this problem.
{% endblockquote %}

# Panttern? Framework?

引出中的 *Wrapper class* 是 *Decorator pattern* 的引出概念，*callback frameworks* 是一种 *framework*。从翻译上看，*pattern* 译为 **模式**，*framework* 译为 **框架**。那么这个 *pattern* 与 *framework* 有毛的区别呢？

<!--more-->

[Introduction to Patterns and Frameworks][] 和 [Design patterns vs Frameworks][] 对这个问题都有比较系统的分析，各位看官可以细细品看一下。一言以蔽之，*pattern* 是解决某个具体问题的方案，*framework* 是一套系统架构可重用的组件（通常包括若干个 *pattern*），他们经常共同为软件系统的可重用性、可扩展性提供支持。

{% blockquote %}
Patterns support reuse of software architecture and design: Patterns capture the static and dynamic structures and collaborations of successful solutions to problems that arise when building applications in a particular domain. 
Frameworks support reuse of detailed design and code: A framework is an integrated set of components that collaborate to provide a reusable architecture for a family of related applications.
{% endblockquote %}

# Framework

## Inversion of Control

打个比方：*Frameworks* 就是挖好了一堆坑，让你往里面填萝卜，填肥料。不同的肥料怎么找对应的萝卜 *Frameworks* 来接手，这个过程就是**控制反转**（[Inversion of Control][]）啦。

{% blockquote Hollywood principle %}
Don't call us, we'll call you.
{% endblockquote %}

### Dependency injection

从代码上看，*IoC* 的实现主要包括**依赖注入**（[Dependency injection][]），以及其他的一些 *Patterns*。所以准确的来说，*Dependency injection* 是实现 *IoC* 的一种途径，但是现在很多的技术鸡汤文都把它们划上了等于号，这里了解一下概念即可，无伤大雅。

*So*，*Frameworks* 的基础是 *IoC*，而 *IoC* 的基础又是 *Dependency injection*。不要被这个高大上的名词唬到，其实它（主要）就是 `set` 方法而已。

## Callback frameworks

如果说 *IoC* 的精髓一句话概括为：*Don't call us, we'll call you.*

那么**回调**（[callback][]）的一句话概括就是：*If you call me, i will call back.*

*callback frameworks* 其实并不是一种具体的 *framework*，而是代表**一类** *framework*。比如大多数的 *GUI frameworks*，比如 *SAX (Streaming XML) XML parsers*。 

### <a name="callback_code">Demo code</a>

```java
public class Context implements A.Callback {

    private A a;

    public void begin() {
        System.out.println("begin ...");
    }

    public void end() {
        System.out.println("end ...");
    }

    public Context() {
        this.a = new A(this);
    }

    public void doSomething() {
        this.a.doIt();
    }

    public static void main(String args[]) {
        new Context().doSomething();
    }
}

public class A {

    private final Callback callback;

    public static interface Callback {
        public void begin();
        public void end();
    }
    public A(Callback callback) {
        this.callback = callback;
    }
    public void doIt() {
        callback.begin();
        System.out.println("do something ...");
        callback.end();
    }
}
```

## Spring

如果要找一种具体的 *framework*，最典型的当然非 *Spring* 莫属。

网上介绍的文章一箩筐，比如这两篇，[戳这里][What is dependency injection]，[戳这里][什么是依赖注入]。可以简单的看一下，主要是加深对 *Dependency injection* 的理解。

# Pattern

## Decorator pattern

{% blockquote %}
Favor composition over inheritance. Inheritance propagates any flaws in the superclass’s API, while composition lets you design a new API that hides these flaws.
{% endblockquote %}

装饰者模式是*继承可实例化的类*的替代解决方案，[Effective Java, Item 16][EJ] 一节的主题正是 *Favor composition over inheritance*。B 继承可实例化的类 A 有很多问题，比如

*	B 依赖于 A 的实现细节，如果 A 的后续版本中更新了实现细节，可能会破坏 B
*	A 在后续版本中可能会加入新的方法，不满足 B 的 check
*	A 的后续版本中加入了新方法 m，不巧 B 提前声明了 m
*	B 不可能实现一个满意的 `equals` 方法（[Effective Java, Item 8][]）

### <a name="forward">Demo code</a>

```java
// Wrapper class - uses composition in place of inheritance
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}

// Reusable forwarding class
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) 		{ this.s = s; 			   }
    public void clear()                 { s.clear();               }
    public boolean contains(Object o)   { return s.contains(o);    }
    public boolean isEmpty()            { return s.isEmpty();      }
    public int size()                   { return s.size();         }
    public Iterator<E> iterator()       { return s.iterator();     }
    public boolean add(E e)             { return s.add(e);         }
    public boolean remove(Object o)     { return s.remove(o);      }
    public boolean containsAll(Collection<?> c) 
                                        { return s.containsAll(c); }
    public boolean addAll(Collection<? extends E> c)
                                        { return s.addAll(c);      }
    public boolean removeAll(Collection<?> c)
                                        { return s.removeAll(c);   }
    public boolean retainAll(Collection<?> c)
                                        { return s.retainAll(c);   }
    public Object[] toArray()           { return s.toArray();      }
    public <T> T[] toArray(T[] a)       { return s.toArray(a);     }
    @Override public boolean equals(Object o)
                                        { return s.equals(o);      }
    @Override public int hashCode()     { return s.hashCode();     }
    @Override public String toString()  { return s.toString();     }
}
```

# 再谈引出

*Wrapper classes are not suited for use in callback frameworks*，在了解了相关的各种基本概念后，现在可以给出示例代码了。结合之前介绍的 [callback framework demo code][] 进行说明，给 `Context` 一个 *Wrapper class*，观察效果。

```java
public class ContextB implements Callback {
    
    Callback real;
    
    public ContextB(Callback real) {
        this.real = real;
    }
    
    @Override
    public void begin() {
        System.out.println("B begin ...");
        real.begin();
    }

    @Override
    public void end() {
        System.out.println("B end ...");
        real.end();
    }
    
    public void ex() {
        System.out.println("B ex ...");
        real.ex();
    }
    
    public static void main(String args[]) {
        Callback real = new Context();
        new ContextB(real).ex();
    }
}
```

观察输出，有豁然开朗的感觉吗？有请点赞~

```java
B ex ...
begin ...
do something ...
end ...
```

# 扩展阅读

*	[Effective Java, Item 16][EJ]
*	[Effective Java, Item 8][]
*	[Why are wrapper classes not suited for use in callback frameworks][炫酷的解答]
*	[Introduction to Patterns and Frameworks][]
*	[Design patterns vs Frameworks][]
*	[浅析 Java Callback 回调模式][callback]
*	[Inversion of Control 维基百科][Inversion of Control]
*	[Dependency injection 维基百科][Dependency injection]
*	[What is dependency injection][]
*	[什么是依赖注入][]
*	[Using Prototypical Objects to Implement Shared Behavior in Object Oriented Systems][Lieberman86]


[EJ]:	https://books.google.com.hk/books?id=ka2VUBqHiWkC&pg=PA85&dq=The+disadvantage+of+wrapper+classes&hl=en&sa=X&ved=0CB0Q6AEwAGoVChMI5L-T5PnPxgIVCQuOCh1joQq6#v=onepage&q=The%20disadvantage%20of%20wrapper%20classes&f=false
[Effective Java, Item 8]:	https://books.google.com.hk/books?id=ka2VUBqHiWkC&pg=PA40&dq=While+there+is+no+satisfactory+way+to+extend+an+instantiable+class+and+add+a+value+component&hl=en&sa=X&ved=0CB0Q6AEwAGoVChMI08nF6OrVxgIVhe9yCh28bQWD#v=onepage&q=While%20there%20is%20no%20satisfactory%20way%20to%20extend%20an%20instantiable%20class%20and%20add%20a%20value%20component&f=false
[炫酷的解答]:	http://programmers.stackexchange.com/questions/117628/why-are-wrapper-classes-not-suited-for-use-in-callback-frameworks
[Design patterns vs Frameworks]:	http://stackoverflow.com/questions/320142/design-patterns-vs-frameworks
[Introduction to Patterns and Frameworks]:	https://www.dre.vanderbilt.edu/~schmidt/PDF/patterns-intro4.pdf
[Inversion of Control]:	https://en.wikipedia.org/wiki/Inversion_of_control
[Dependency injection]:	https://en.wikipedia.org/wiki/Dependency_injection
[What is dependency injection]:	http://programmers.stackexchange.com/questions/92393/what-does-the-spring-framework-do-should-i-use-it-why-or-why-not?rq=1

[callback]:	http://www.codeweblog.com/%E6%B5%85%E6%9E%90java-callback-%E5%9B%9E%E8%B0%83%E6%A8%A1%E5%BC%8F/
[什么是依赖注入]:	http://blog.csdn.net/taijianyu/article/details/2338311/

[nested callbacks]:	http://stackoverflow.com/questions/10695152/java-pattern-for-nested-callbacks
[gwt-tips-1-chain-of-responsibility]:	http://seewah.blogspot.sg/2009/02/gwt-tips-1-chain-of-responsibility.html
[callback framework demo code]: #callback_code
[Lieberman86]: https://static.aminer.org/pdf/PDF/000/522/451/using_prototypical_objects_to_implement_shared_behavior_in_object_oriented.pdf
