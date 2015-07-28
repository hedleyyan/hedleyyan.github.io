---
layout: post
title: "A Glossary of Name Reuse"
date: 2015-05-16 17:07:57 +0800
comments: true
categories: 技术
tags: [Java, 规约]
---

此文大概就是《Java Puzzlers》中 *Classier Pazzlers* 一节的简单翻译和整理，粗略总结了 *Java* 有关**命名重复**的场景和代码示例。

# Overriding


{% blockquote JLS 8.4.8.1%}
An instance method overrides all accessible instance methods with the same signature in superclasses.
{% endblockquote %}

重写的规范定义包括：

*  Context 包括两个类，一个子类，一个父类。
*  载体必须是实例方法 *(instance methods)*，而非静态方法。
*  必须是同样签名 *(signature)* 和返回类型。
*  重写方法不能缩小可见范围。

重写是面向对象编程的核心概念，是**唯一**鼓励使用的 *Name Reuse* 场景。

``` java
    class Base {
        protected void f() { }
	}

    class Derived extends Base {
        public void f() { } // overrrides Base.f()
	}
```

<!--more-->

# Overloading


{% blockquote JLS 8.4.9%}
Methods in a class overload one another if they have the same name and different signatures.
{% endblockquote %}

重载和重写是 *Name Reuse* 出现最多的场景。相对于重写，重载规范包括：

*  Context 只有一个类。
*  载体可以是实例方法，也可以是静态方法。
*  拥有不同的签名。

``` java
    class CircuitBreaker {
        public void f(int i)    { }  // int overloading
        public void f(String s) { }  // String overloading
	} 
```


# Hiding


{% blockquote JLS 8.3-8.5%}
A field, static method, or member type hides all accessible fields, static methods, or member types, respectively, with the same name (or, for methods, signature) in supertypes. Hiding a member prevents it from being inherited.
{% endblockquote %}

*Hiding* 和 *Overriding* 场景比较像，*Overriding* 的载体是实例方法，而 *Hiding* 的载体是除去实例方法以外的其他所有元素。

*  Context 包括子类和父类。
*  载体包括属性、静态方法、成员类。
*  同样签名。
*  可见范围无约束。

``` java
    class Base {
        public String className = "Base";
	}

    class Derived extends Base {
        private String className = "Derived";
	}

    public class PrivateMatter {
        public static void main(String[] args) {
            System.out.println(new Derived().className);
        }
	}
```

️️️代码结果

```
PrivateMatter.java:11: className has private access in Derived
           System.out.println(new Derived().className);
```

很明显，*Hiding* 的代码是晦涩难懂的，更重要的是它破坏了 *Liskov* 替换原则。应极力避免这种 *Name Reuse* 场景。

{% blockquote Liskov Substitution Principle%}
Everything you can do with a base class, you can also do with a derived class.
{% endblockquote %}


# <a name='shadowing'>Shadowing</a>


{% blockquote JLS 6.3.1%}
A variable, method, or type shadows all variables, methods, or types, respectively, with the same name in a textually enclosing scope.
{% endblockquote %}

*  Context 可能只有一个类，也可能包括别的函数库的类。
*  载体包括变量、方法、类、类型声明 *(<T>)*。

``` java
	class WhoKnows {
        static String sentence = "I don’t know.";
        public static void main(String[] args) {
            String sentence = "I know!";   // shadows static field
            System.out.println(sentence);  // prints local variable
        }
	}
```


``` java
    public class StrungOut {
       public static void main(String[] args) {
           String s = new String("Hello world");
           System.out.println(s);
       }
    }

    class String {
        private final java.lang.String s;
        public String(java.lang.String s) {
            this.s = s;
		}
        public java.lang.String toString() {
            return s;
		} 
    }
```

代码结果

```
Exception in thread "main" java.lang.NoSuchMethodError: main
```

产生 *Shadowing* 场景的 *Name Reuse* ，依最近的作用域来判定是哪个元素有效。


# Obscuring


*Obscuring* 是指在作用域中，一个变量的名字和一个类相同，直接看示例代码。

``` java
public class Obscure {
    static String System; // Obscures type java.lang.System
    public static void main(String[] args) {
        // Next line won’t compile:  System refers to static field 
        System.out.println("hello, obscure world!");
	}
}
```

{% blockquote %}
If a type or a package is obscured, you cannot refer to it by its simple name except in a context where the syntax allows only a name from its namespace. 
{% endblockquote %}

# 常见陷阱

# 总结

1.  除了 *Override* ，尽量避免 *Name Reuse* 。
2.  遵守 *Java* 命名规则，可以避免 *Obscuring* 。
3.  避免和 `java.lang` 类库中的命名冲突。

<!--链接-->