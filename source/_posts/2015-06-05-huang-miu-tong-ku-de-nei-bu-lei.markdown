---
layout: post
title: "荒谬痛苦的内部类"
date: 2015-06-05 11:25:38 +0800
comments: true
categories: 技术
tags: Java
toc: true
---

# <a name="about">关于</a>

此文简单总结嵌套类 *(Nested Class)* 的使用场景与一般建议：能使用**静态嵌套类** *(Static Nested Class)* 的时候就不用**内部类** *(Inner Class)*。

{% blockquote %}
A nested class is any class whose declaration occurs within the body of another class or interface. A top level class is a class that is not a nested class.
{% endblockquote %}

<!--more-->

# 分类定义

## 按位置

*	成员嵌套类 *(Member Nested Class)* ：作为外部类 *(Enclosing Class)* **成员**定义，成员嵌套类有 *Enclosing Class* 的属性。
	*	可以使用 `public`、 `private` 等访问控制符，也可以用 `static`、 `final` 关键字
*	局部嵌套类 *(Local Nested Class)* ：局部嵌套类定义在 *Enclosing Class* 的方法里面，局部嵌套类有 *Enclosing Class* 的属性和外部方法 *(Enclosing Method)* 的属性。
	*	可以使用 `final` 关键字，不能使用访问控制符
	*	局部类不能在外部进行创建，只能在方法调用的时候进行创建
	*	*Enclosing Method* 可以是静态方法，可以是实例方法，也可以是构造器方法或者静态初始化语句块
	*	在 `static` 上下文定义的局部类，没有指向父类实例变量的引用
*	匿名嵌套类 *(Anonymous Nested Class)* ：匿名嵌套类没有显示的定义一个类，直接通过 `new` 的方法创建类的实例。
	*	不使用任何关键字和访问控制符

## 按功能

大多数情况下，嵌套类都按功能分为：

*	静态嵌套类 *(Static Nested Class)* ：`static` 修饰的**成员嵌套类**。
*	内部类 *(Inner Class)* ：静态嵌套类之外所有的嵌套类的总称。
	*	内部类不能定义为 `static`，不能有 `static` 方法和 `static` 初始化语句块
	*	可以有 `static final` 常量属性，但不推荐这么用（放外部类就 *OK* ，为什么用在这？）

所以，局部嵌套类和匿名嵌套类肯定是内部类，成员嵌套类则分为静态嵌套类和内部类。

{% blockquote %}
An inner class is a nested class that is not explicitly or implicitly declared static. Inner classes may not declare static initializers or member interfaces.
{% endblockquote %}

``` java
// 嵌套类实例代码，略
```

# 痛苦场景

## *Puzzle 80*：反射

``` java
public class Outer {
    public static void main(String[] args) throws Exception {
        new Outer().greetWorld();
    }

    private void greetWorld() throws Exception {
        System.out.println(Inner.class.newInstance());
    }

    public class Inner {
        public String toString() {
            return "Hello world";
        }
    }
}
```

代码看上去就是一个花样 *Hello world*，但是当你执行时，*Bazinga*！

```
Exception in thread "main" java.lang.InstantiationException: test.Outer$Inner
	at java.lang.Class.newInstance0(Class.java:342)
	at java.lang.Class.newInstance(Class.java:310)
	at test.Outer.greetWorld(Outer.java:10)
	at test.Outer.main(Outer.java:6)
```

*OK* ，看来 *inner class* 持有 *enclosing class* 的一个实例变量 *(immediately enclosing instance)* 是真的！编译器没那么牛逼每次都可以悄悄替你做了所有的事，至少在使用反射初始化时， *enclosing class* 的 *instance* 不会传进来。

{% blockquote JLS 13.1 %}
The constructor of a non-static nested class is compiled such that it has as its first parameter an additional implicit parameter representing the immediately enclosing instance.
{% endblockquote %}

{% blockquote %}
This parameter is passed implicitly when you invoke the constructor from any point in the code where the compiler can find an appropriate enclosing instance. But this applies only when you invoke the constructor normally: nonreflectively.
{% endblockquote %}

用含参的构造方法测试一下：

``` java
private void greetWorld() throws Exception {
       Constructor c = Inner.class.getConstructor(Outer.class);
       System.out.println(c.newInstance(Outer.this));
}
```

*bingo* ！观察一下，这个场景中为什么要使用 *inner class* 呢，明明 *static nested class* 就可以嘛！ *so* 更帅的解决办法是：

``` java
public static class Inner { ... }
```

**秘籍：能用静态嵌套类解决，就不用内部类。**

## *Puzzle 89*：泛型

``` java
public class LinkedList<E> {
    private Node<E> head = null;

    private class Node<E> {
        E value;

        Node<E> next;

        // Node constructor links the node as a new head
        Node(E value) {
            this.value = value;
            this.next = head;
            head = this;
        }
    }

    public void add(E e) {
        new Node<E>(e);		// Link node as new head
    }

    public void dump() {
        for (Node<E> n = head; n != null; n = n.next)
            System.out.print(n.value + " ");
    }

    public static void main(String[] args) {
        LinkedList<String> list = new LinkedList<String>();
        list.add("world");
        list.add("Hello");
        list.dump();
    }
}
```

代码看上去就是另一个花样 *Hello world*，但是当你执行时，*Double Bazinga* ！

```
Exception in thread "main" java.lang.Error: Unresolved compilation problems: 
	Type mismatch: cannot convert from LinkedList<E>.Node<E> to LinkedList<E>.Node<E>
	Type mismatch: cannot convert from LinkedList<E>.Node<E> to LinkedList<E>.Node<E>

	at test.LinkedList$Node.<init>(LinkedList.java:15)
	at test.LinkedList.add(LinkedList.java:21)
	at test.LinkedList.main(LinkedList.java:32)
```

`LinkedList<E>.Node<E>` 和 `LinkedList<E>.Node<E>` 类型不符？？？

原因是前面的 `Node<E>` 与后面的 `Node<E>` 虽然看上去一模一样，但它们压根就不是一个类型。具体点说，因为这段代码有两个 `<E>` 类型声明 *(type parameter)*，第一个是 `LinkedList` 的 `<E>` ，第二个是 `LinkedList.Node` 的 `<E>` 。后面的 *[Shadow][]* 了前面的。

如果这里把 *inner class* 的声明换一个 *type parameter* `Node<T>` ，可以看到更多一点的细节：

```
...
	Type mismatch: cannot convert from LinkedList<E>.Node<E> to LinkedList<E>.Node<T>
	Type mismatch: cannot convert from LinkedList<E>.Node<T> to LinkedList<E>.Node<E>
...
```

{% blockquote %}
An inner class of a generic class has access to the type parameters of its outer class.
{% endblockquote %}

*So*，*inner class* 可以访问 *enclosing class* 的各种属性，包括 *type parameter* 。

这段代码的初衷本来就是 `Node` 持有和 `LinkedList` 一样的  *type parameter* ，所以我们可以这样修正：

```java
public class LinkedList<E> {
    private Node head = null;

    private class Node {
        E value;

        Node next;

        // Node constructor links the node as a new head
        Node(E value) {
            this.value = value;
            this.next = head;
            head = this;
        }
    }

    public void add(E e) {
        new Node(e);		// Link node as new head
    }

    public void dump() {
        for (Node n = head; n != null; n = n.next)
            System.out.print(n.value + " ");
    }
}
```

但是这不是一个漂亮的修正。它的功能完全可以通过 *static nested class* 实现；并且 *inner class* 的构造方法里修改了 *enclosing class* 的属性 `head` ，**这不是好的实践**。

{% blockquote %}
Change instance fields of a class only in its own instance methods.
{% endblockquote %}

更帅的解决方法当然是用 *static nested class* 实现 `Node` ，并且把对 *enclosing class* 的属性修改抽离出来。

```java
class LinkedList<E> {
    private Node<E> head = null;

    private static class Node<T> {
        T value;

        Node<T> next;

        Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    public void add(E e) {
        head = new Node<E>(e, head);
    }

    public void dump() {
        for (Node<E> n = head; n != null; n = n.next)
            System.out.print(n.value + " ");
    }
}
```

这样 `Node<T>` 完全与他的 `LinkedList<E>` 是解耦合的，想象一下你不止可以声明一个 `Node<E>` ，你完全可以声明 `Node<Integer>` 、 `Node<String>` ，是不是爽多了。

**秘籍1：类 A 的成员变量，只在 A 的成员方法里改。** <br>
**秘籍2：能用静态嵌套类解决，就不用内部类！！**

## *Puzzle 90*：嵌套继承

```java
public class Outer {
       class Inner1 extends Outer {}
       class Inner2 extends Inner1 {}
}
```

为什么这么简单的代码就是不给编译过呢？搞咩？不过这次的错误提示好像有点帮助。。

```
	No enclosing instance of type Outer is available due to some intermediate constructor invocation.
	at test.Outer$Inner2.<init>(Outer.java:8)
	...
```

{% blockquote JLS 8.8.7 %}
The instantiation of an inner class, requires an enclosing instance to be supplied to the constructor. Normally, it is supplied implicitly, but it can also be supplied explicitly with a superclass constructor invocation of the form expression.super(args).
{% endblockquote %}

原来 *inner class* 可以显示调用 *enclosing instance* ，编译器帮不了还得自己来。和[反射][]很像嘛

```java
class Inner2 extends Inner1 {
	// Plan A
    Inner2(Outer outer) {
        outer.super();
    }
}
```

也可以

```java
class Inner2 extends Inner1 {
	// Plan B
    Inner2() {
        Outer.this.super();
    }
}
```

*《Thinking in Java》* 推荐 *Plan A* 的写法，它更通用一些。如果 `Inner2` 不是 `Outer` 的 *inner class* ，它只能用 *Plan A* 的写法来继承 `Inner1`， 无论是否 `Inner1 extends Outer` 。

```java
public class Outer {
    class Inner1 {};
}

class Inner2 extends Outer.Inner1 {
	// Outer.this doesn't work here
    public Inner2(Outer outer) {
        outer.super();
    }
}
```

*OK* ，是不是有点晕？晕就对了。无论何时，当你要写一个 *inner class* 的时候，问问自己，真的不可以用 *static nested class* 来代替吗？真的有必要吗？ *inner class* 还要继承 *enclosing class* ？真的真的有必要吗？ 

**秘籍1：尽量避免继承内部类。** <br>
**秘籍2：能用静态嵌套类解决，就不用内部类！！！！**

##  *Puzzle 92*：另类匿名类

```java
public class Twisted {
    private final String name;

    Twisted(String name) {
        this.name = name;
    }

    private String name() {
        return name;
    }

    private void reproduce() {
        new Twisted("reproduce") {
            void printName() {
                System.out.println(name());
            }
        }.printName();
    }

    public static void main(String[] args) {
        new Twisted("main").reproduce();
    }
}
```

肤浅的分析：无法编译！因为 `reproduce` 里面的 *anonymous class* 尝试访问 `Twisted` 的 `private`方法。而 `private` 的属性应该只有 	`Twisted` 可以访问。 <br>

{% blockquote JLS 6.6.1 %}
Within a top-level type, all the local, inner, nested, and anonymous classes can access one another’s members without any restrictions.
{% endblockquote %}

其实 *nested class* 和 *enclosing class* 就是一家子， *enclosing class* 的所有成员变量都可以被 *nested class* 无限制访问。

知道了这个振奋人心的消息后，你的答案可能是：输出 `reproduce` 。显而易见，我使用了 `reproduce` 作为参数传入构造函数产生实例 `new Twisted("reproduce")` ，调用 `printName()` 方法，再调用 `name()` 方法，输出。但很遗憾，它的输出是 `main` 。

{% blockquote JLS 8.2 %}
Private members are never inherited.
{% endblockquote %}

**私有方法无法被继承！** *This is the key*！ *So* ，`new Twisted("reproduce")` 实例没有 `name()` 方法，而 `printName()` 方法会依最小作用域找到 `enclosing instance` 的 `name()` 方法并调用。

**秘籍：避免一个内部类继承它的外部类。**

# 总结

说到口都干了：能用静态嵌套类解决，就不用内部类。
{% blockquote EJ Item 18 %}
Unless you have a compelling need for an enclosing instance, prefer static member classes over nonstatic.
{% endblockquote %}

[Shadow]:	http://hedleyyan.github.io/blog/2015/05/16/a-glossary-of-name-reuse/#shadowing
[反射]:		#puzzle-80

<!--## 引用-->
