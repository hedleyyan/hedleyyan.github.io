---
layout: post
title: "危机四伏整型运算"
date: 2015-06-24 09:10:09 +0800
comments: true
categories: 技术
tags: Java
toc: true
---

哈？整型运算？听起来好 *EASY* 啊喂！直奔主题咯。

# 边界陷阱

无论何时，请注意整型运算的边界问题，考虑最大值，最小值，越界的可能性。

## *Puzzle 3: Long Division*

```java
	public class LongDivision {
       	public static void main(String[] args) {
			final long MICROS_PER_DAY = 24 * 60 * 60 * 1000 * 1000;
			final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
			System.out.println(MICROS_PER_DAY / MILLIS_PER_DAY);
		} 
	}
```

<!--more-->

*EASY* 无压力，一目了然整型越界。 `MICROS_PER_DAY` 虽然声明为 `long`，可以装下计算结果，但是计算结果本身先是以 `int` 类型进行计算，计算完成后再赋值给 `MICROS_PER_DAY` 的。很不幸，在计算的时候就溢出了。

修改太简单，加一个类型声明后缀，大写的 `L`。

``` java
	public class LongDivision {
       	public static void main(String[] args) {
			final long MICROS_PER_DAY = 24 * 60 * 60 * 1000 * 1000L;
			final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
			System.out.println(MICROS_PER_DAY / MILLIS_PER_DAY);
		} 
	}
```

## *Puzzle 26: In the Loop*

```java
	public class InTheLoop {
		public static final int END = Integer.MAX_VALUE;
		public static final int START = END - 100;

		public static void main(String[] args) {
			int count = 0;
			for (int i = START; i <= END; i++)
				count++;
				System.out.println(count);
			}
	}
```

如果你没有仔细的看代码，可能会认为输出 `100`；如果你仔细一点，会发现这不是 `for` 循环的惯用语法 *(idiom)*，可能会认为输出 `101`。*Well* 都不是，你会发现没有输出，代码陷入了无限循环 *(Infinite loop)*。因为所有的 `int` 值都 `<= Integer.MAX_VALUE`。

此外，将函数 `f(int)` 应用到所有的 40 亿整数的 *idiom* 是这个样子滴

```java
// Apply the function f to all four billion int values
int i = Integer.MIN_VALUE;
do {
    f(i);
} while (i++ != Integer.MAX_VALUE);
```

## *Puzzle 33: Infinite loop*

```java
	while (i == -i && i != 0) {

	}
```

给 `i` 一个声明，使上面的语句陷入 *Infinite loop*。

答案是 `int i = Integer.Integer.MIN_VALUE`。所以注意，相应的还有一个小陷阱， `Math.abs()` 可以 `< 0`。

## *Puzzle 65: A Strange Saga of a Suspicious Sort*

以下是一个将 100 个随机整数排序的代码段，短小精悍帅的很呢。但是很遗憾，输出基本都不会是众望所归的 `Order.DESCENDING`，问题出在哪呢？

```java
import java.util.*;

public class SuspiciousSort {
    public static void main(String[] args) {
        Random rnd = new Random();
        Integer[] arr = new Integer[100];
        for (int i = 0; i < arr.length; i++)
            arr[i] = rnd.nextInt();
        Comparator<Integer> cmp = new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                return i2 - i1;
            }
        };
        Arrays.sort(arr, cmp);
        System.out.println(order(arr));
    }

    enum Order {
        ASCENDING, DESCENDING, CONSTANT, UNORDERED
    };

    static Order order(Integer[] a) {
        boolean ascending = false;
        boolean descending = false;
        for (int i = 1; i < a.length; i++) {
            ascending |= (a[i] > a[i - 1]);
            descending |= (a[i] < a[i - 1]);
        }
        if (ascending && !descending)
            return Order.ASCENDING;
        if (descending && !ascending)
            return Order.DESCENDING;
        if (!ascending)
            return Order.CONSTANT;  // All elements equal
        return Order.UNORDERED;     // Array is not sorted
    }
}
```
 
问题出在 `Comparator<Integer> cmp` 上，虽然它的实现是喜闻乐见的 *idiom*。考虑以下的代码段

```java
    public class Overflow {
        public static void main(String[] args) {
            int x = -2000000000;
            int z = 2000000000;
            System.out.println(x - z);
        } 
    }
```

`x < y`，并不一定代表 `x - y < 0`。在用 `Comparator.compare()` 的 *idiom* 时候，请确认边界问题。

修改代码很简单，用现成的类库方法 `Collections.reverseOrder()` 提供逆序排列子。

{% blockquote %}
Do not use a subtraction-based comparator unless you are sure that the difference between values will never be greater than Integer.MAX_VALUE.
{% endblockquote %}

# 类型转换

*[JSL][]* 定义了若干类型转换，关于整型的有三种

*   Widening Primitive Conversion   
    *   byte to short, int, long, float, or double
    *   short to int, long, float, or double
    *   char to int, long, float, or double
    *   int to long, float, or double
    *   long to float or double
    *   float to double
*   Narrowing Primitive Conversion
    *   short to byte or char
    *   char to byte or short
    *   int to byte, short, or char
    *   long to byte, short, char, or int
    *   float to byte, short, char, int, or long
    *   double to byte, short, char, int, long, or float
*   Widening and Narrowing Primitive Conversion
    *   byte to char

整型的扩宽类型转换不会丢失值信息，声明中可直接转换。所有的缩窄类型转换都有可能丢失值信息，所以必须做显示的类型转换。

最奇怪的是第三种，一次类型转换既有扩宽又有缩窄。因为 `char` 无符号整型特殊一点，其实 `byte` 是先转为 `int`，然后再转为了 `char`。

## *Puzzle 6: Multicast*

```java
    public class Multicast {
        public static void main(String[] args) {
            System.out.println((int) (char) (byte) -1);     // 65535
        }
    }
```

转换过程

*   (int) -1 ➡️ (byte) -1 : 0xffff ➡️ 0xf   *(narrowing)*
*   (byte) -1 ➡️ (char) -1      *(widening and narrowing)*
    *   (byte) -1 ➡️ (int) - 1 : 0xf ➡️ 0xffff
    *   (int) -1 ➡️ (char) 65535 : 0xffff ➡️ 0xff
*   (char) 65535 ➡️ (int) 65535 : 0xff ➡️ 0xffff    *(widening)*

**秘籍：当心 *Narrowing Primitive Conversion* 的值变化。**

# *Other Tips*

## *Puzzle 1: 求余*

以下是判断一个整数是否为奇数的方法，可行吗？

```java
	public static boolean isOdd(int i) {
       return i % 2 == 1;
	}
```

*One Quarter* 的情况下不可行：当 `i` 为负奇数时， `i % 2 == -1`。 

```java
	public static boolean isOdd(int i) {
	   // esay fix
       return i % 2 != 0;
       // for performance-critical
       return i & 1 == 0;
	}
```

## *Puzzle 27: 移位* 

```java
    public class Shifty {
        public static void main(String[] args) {
            int i = 0;
            while (-1 << i != 0)
                i++;
            System.out.println(i);
        } 
    }
```

因为 -1 的二进制表示 0xffff 有 32 位 1，所以输出应该是 32 吧？但是这是一个 *Infinite loop*。因为 -1 左移 32 位还是 -1。

{% blockquote %}
Shift operators use only the five low-order bits of their right operand as the shift distance, or six bits if the left operand is a long.
{% endblockquote %}

## *Puzzle 31: 组合运算符*

```java
    while (i != 0) {
        i >>>= 1;
    }
```

给 `i` 一个声明，使上面的语句陷入 *Infinite loop*。

{% blockquote %}
Compound assignment operators is that they can silently perform narrowing primitive conversions.
{% endblockquote %}

答案可以是 `short i = -1`。所有非 `long` 的整型在运算时都需要转换为 `int`，该运算的步骤拆分为

*   (short) -1 ➡️ (int) -1 : 0xff ➡️ 0xffff
*   -1 >>> 1 ➡️ 0x7fff
*   (int) 2^31 - 1 ➡️ (short) -1 : 0x7fff ➡️ 0xff
*   *Infinite loop*

秘籍：不要对 `byte`, `short`, `char` 使用组合运算符。

# 总结

注意边界，注意转型，注意 `byte` `char`，注意特殊运算。

[JSL]:  http://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html

