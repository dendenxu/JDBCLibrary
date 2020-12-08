import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.*;

/**
 * You can create a multiline comment
 */
// Or single line comment
public class LearnBasic {
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            if ("-version".equals(arg)) {
                System.out.println("v 1.0");
                break;
            }
        }
        System.out.println("Hello Java");
        System.out.println("There's " + args.length + " command line argument(s)");
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
        /**
         * All kinds of datatype in Java (Well, not much)
         */

        // integers
        byte bt = 0;
        short st = 0;
        int it = 0;
        long lo = 0l;

        // Floats
        float fl = 0.0f;
        double db = 0.0;

        // Chars
        // 注意char类型使用单引号'，且仅有一个字符，要和双引号"的字符串类型区分开。
        char ch = 'A';

        // Boolean
        // Java语言对布尔类型的存储并没有做规定，因为理论上存储布尔类型只需要1 bit，但是通常JVM内部会把boolean表示为4字节整数。
        boolean bl = false;

        // Integers
        int i = 2147483647;
        int i2 = -2147483648;
        int i3 = 2_000_000_000; // 加下划线更容易识别
        int i4 = 0xff0000; // 十六进制表示的16711680
        int i5 = 0b1000000000; // 二进制表示的512
        long l = 9000000000000000000L; // long型的结尾需要加L

        // Final
        // 定义变量的时候，如果加上final修饰符，这个变量就变成了常量：
        final int CONSTANT = 100;
        final double PI = 3.14; // PI是一个常量
        double r = 5.0;
        double area = PI * r * r;
        // PI = 300; // compile error!

        // 有些时候，类型的名字太长，写起来比较麻烦。例如：
        StringBuilder sb = new StringBuilder();
        float af = 100.0f;
        // AND DO NOTICE TO CHANGE THE SETTINGS IN .SETTINGS FOLDER TO MAKE THE TARGET
        // PLATFORM COMPITABLE
        var al = new ArrayList<String>();

        // 需要特别注意，在一个复杂的四则运算中，两个整数的运算不会出现自动提升的情况。例如：
        // Just like when you try to do it in C or CPP
        // You'll get a small little bug
        double d = 1.2 + 24 / 5; // 5.2

        double d1 = 0.0 / 0; // NaN
        double d2 = 1.0 / 0; // Infinity
        double d3 = -1.0 / 0; // -Infinity
        System.out.println(d1);
        System.out.println(d2);
        System.out.println(d3);

        // 因为Java在内存中总是使用Unicode表示字符，所以，一个英文字符和一个中文字符都用一个char类型表示，它们都占用两个字节。要显示一个字符的Unicode编码，只需将char类型直接赋值给int类型即可：
        int n1 = 'A'; // 字母“A”的Unicodde编码是65
        int n2 = '中'; // 汉字“中”的Unicode编码是20013
        System.out.println(n1);
        System.out.println(n2);
        // 注意是十六进制:
        char c3 = '\u0041'; // 'A'，因为十六进制0041 = 十进制65
        char c4 = '\u4e2d'; // '中'，因为十六进制4e2d = 十进制20013
        System.out.println(c3);
        System.out.println(c4);
        // Multiline string is a preview feature currently disabled by default
        // String s = """
        // SELECT * FROM
        // users
        // WHERE id > 100
        // ORDER BY name DESC""";
        String s = "hello";
        String t = s;
        s = "world";
        System.out.println(t); // t是"hello"还是"world"?
        int a = 72;
        int b = 105;
        int c = 65281;
        // FIXME: You can let the compiler indicate the type
        var str = "" + (char) a + (char) b + (char) c;
        System.out.println(str);

        System.out.printf("%12.6f\n", 1.23f);
        System.out.printf("%12.6e\n", 1.23f);

        /**
         * NOTE: COMMENTED FOR A CLEAR RUN WITHOUT INPUT THESE LINE OF CODE PRODUCES NO
         * ERROR
         */
        // // We can use scanner to interpret the input
        // Scanner scanner = new Scanner(System.in); // 创建Scanner对象
        // System.out.print("Input your name: "); // 打印提示
        // String name = scanner.nextLine(); // 读取一行输入并获取字符串
        // System.out.print("Input your age: "); // 打印提示
        // int age;
        // while (true) {
        // try {
        // age = scanner.nextInt(); // 读取一行输入并获取整数
        // break;
        // } catch (InputMismatchException e) {
        // // e.printStackTrace();
        // str = scanner.nextLine();
        // System.out.printf("[ERROR] Input mismatch, \"%s\" is not an int.\n", str);
        // System.out.print("Input your age: "); // 打印提示
        // } catch (Exception e) {
        // e.printStackTrace();
        // return;
        // }
        // }
        // System.out.printf("Hi, %s, you are %d\n", name, age); // 格式化输出

        // 当判断浮点数是否相等时我们推荐判断两者的差值是否在一定的范围内
        double x = 1 - 9.0 / 10;
        if (Math.abs(x - 0.1) < 0.00001) {
            System.out.println("x is 0.1");
        } else {
            System.out.println("x is NOT 0.1");
        }
        // 当判断应用是否相等的时候我们推荐使用equals
        String s1 = "hello";
        String s2 = "HELLO".toLowerCase();
        System.out.println(s1);
        System.out.println(s2);
        if (s1.equals(s2)) {
            System.out.println("s1 equals s2");
        } else {
            System.out.println("s1 not equals s2");
        }
        // 要注意有一定可能性出现NullPointerException这样的错误
        s1 = null;
        if (s1 != null && s1.equals("hello")) { // 注意compiler会给出DeadCode的错误提示
            System.out.println("hello");
        } else {
            System.out.println("s1 is null or it doesn't not equal \"hello\".");
        }

        // Using switch
        // 注意穿透效应和default
        String fruit = "apple";
        switch (fruit) {
            case "apple":
                System.out.println("Selected apple");
                break;
            case "pear":
                System.out.println("Selected pear");
                break;
            case "mango":
                System.out.println("Selected mango");
                break;
            default:
                System.out.println("No fruit selected");
                break;
        }
        // Java 12 引入了更简洁的pattern matching，只有一条语句会被执行
        // 这种switch甚至可以直接返回值
        switch (fruit) {
            case "apple" -> System.out.println("Selected apple");
            case "pear" -> System.out.println("Selected pear");
            case "mango" -> {
                System.out.println("Selected mango");
                System.out.println("Good choice!");
            }
            default -> System.out.println("No fruit selected");
        }
        int opt = switch (fruit) {
            case "apple" -> 1;
            case "pear", "mango" -> 2;
            default -> 0;
        }; // 注意赋值语句要以;结束
        System.out.println("opt = " + opt);

        // 如果返回值比较复杂还可以用yield
        fruit = "orange";
        opt = switch (fruit) {
            case "apple" -> 1;
            case "pear", "mango" -> 2;
            default -> {
                int code = fruit.hashCode();
                // return;
                yield code; // switch语句返回值
            }
        };
        System.out.println("opt = " + opt);
        // Note:- In C++, it will run.But in java it is an error because in java,name of
        // variable of inner and outer loop must be different.
        // int a = 5;
        // for (int a = 0; a < 5; a++)
        // {
        // System.out.println(a);
        // }

        // THE SORTING LOOKS A LITTLE BIT F**KED
        Integer[] ns = { 28, 12, 89, 73, 65, 18, 96, 50, 8, 36 };
        Arrays.sort(ns, (Integer inta, Integer intb) -> {
            return inta % 10 - intb % 10;
        });
        System.out.println(ns.toString());
        System.out.println(Arrays.toString(ns));

        int[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        // To boxed array
        Integer[] what = Arrays.stream(data).boxed().toArray(Integer[]::new);
        Integer[] ever = IntStream.of(data).boxed().toArray(Integer[]::new);

        // To boxed list
        List<Integer> you = Arrays.stream(data).boxed().collect(Collectors.toList());
        List<Integer> like = IntStream.of(data).boxed().collect(Collectors.toList());

        int[] damn = you.stream().mapToInt(ii -> ii).toArray();

        // 操作二维数组
        // 类似于字符串，这里的各种东西还都是引用
        int[][] ns2 = { { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 10, 11, 12 } };
        System.out.println(ns2.length); // 3
        int[] arr0 = ns2[0];
        System.out.println(arr0.length); // 4

        // 要打印二维数组，可以直接写循环 或者用deepToString
        for (int[] arr : ns2) {
            for (int n : arr) {
                System.out.print(n);
                System.out.print(", ");
            }
            System.out.println();
        }

        System.out.println(ns2);
        System.out.println(ns2.toString());
        System.out.println(Arrays.toString(ns2));
        System.out.println(Arrays.deepToString(ns2));

        System.out.println(toGBK("中文"));
        try {
            process1();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            notNeg(100);
            // notNeg(-100);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The assertion error cannot be catched and will cause error");
        }
        System.out.println("End of the Main function.");
        // assert 100 < 10;
        // You need to run your program with the -ea switch (enable assertions),
        // otherwise no assert instructions will be run by the JVM at all. Depending on
        // asserts is a little dangerous. I suggest you do something like this:
        //
        //
        System.out.println("\"" + System.lineSeparator() + "\"");
    }

    static void notNeg(int x) {
        System.out.println("I'm in the assertion funtion.");
        System.out.printf("The argument passed is %d.\n", x);
        System.out.printf("Is x >= 0?: %s\n", Boolean.toString(x >= 0));
        assert x >= 0 : "x must >= 0";
    }

    static byte[] toGBK(String s) {
        try {
            // 用指定编码转换String为byte[]:
            return s.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            // 如果系统不支持GBK编码，会捕获到UnsupportedEncodingException:
            System.out.println(e); // 打印异常信息
            return s.getBytes(); // 尝试使用用默认编码
        }
        // return s.getBytes("GBK"); // IS GOING TO COMPLAIN THE ERROR MIGHT NOT BE
        // HANDLED
    }

    static void process1() {
        try {
            process2();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static void process2() {
        throw new NullPointerException();
    }
}
/**
 * 注意到Caused by:
 * Xxx，说明捕获的IllegalArgumentException并不是造成问题的根源，根源在于NullPointerException，是在Main.process2()方法抛出的。
 * 
 * 在代码中获取原始异常可以使用Throwable.getCause()方法。如果返回null，说明已经是“根异常”了。
 * 
 * 有了完整的异常栈的信息，我们才能快速定位并修复代码的问题。
 * 
 * 
 * 这说明finally抛出异常后，原来在catch中准备抛出的异常就“消失”了，因为只能抛出一个异常。没有被抛出的异常称为“被屏蔽”的异常（Suppressed
 * Exception）。
 * 
 * 在极少数的情况下，我们需要获知所有的异常。如何保存所有的异常信息？方法是先用origin变量保存原始异常，然后调用Throwable.addSuppressed()，把原始异常添加进来，最后在finally抛出：
 */