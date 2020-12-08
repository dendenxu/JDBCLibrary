// 要特别注意：包没有父子关系。java.util和java.util.zip是不同的包，两者没有任何继承关系。
// 即所有Java文件对应的目录层次要和包的层次一致。

/**
 * Java编译器最终编译出的.class文件只使用完整类名，因此，在代码中，当编译器遇到一个class名称时：
 * 
 * 如果是完整类名，就直接根据完整类名查找这个class；
 * 
 * 如果是简单类名，按下面的顺序依次查找：
 * 
 * 查找当前package是否存在这个class；
 * 
 * 查找import的包是否包含这个class；
 * 
 * 查找java.lang包是否包含这个class。
 * 
 * 因此，编写class的时候，编译器会自动帮我们做两个import动作：
 * 
 * 默认自动import当前package的其他class；
 * 
 * 默认自动import java.lang.*。
 */

// 编译后的.class文件也需要按照包结构存放。如果使用IDE，把编译后的.class文件放到bin目录下，那么，编译的文件结构就是：
// package_sample
// └─ bin
// ├─ hong
// │ └─ Person.class
// │ ming
// │ └─ Person.class
// └─ mr
// └─ jun
// └─ Arrays.class

class Person {
    // ABOUT PRIVATE PUBLIC PROTECTED
    // 实际上，确切地说，private访问权限被限定在class的内部，而且与方法声明顺序无关。推荐把private方法放到后面，因为public方法定义了类对外提供的功能，阅读代码的时候，应该先关注public方法：
    // 最后，包作用域是指一个类允许访问同一个package的没有public、private修饰的class，以及没有public、protected、private修饰的字段和方法。
    private String[] names = null;
    private int age = 101;
    // 在Java中，创建对象实例的时候，按照如下顺序进行初始化：
    // 先初始化字段，例如，int age = 10;表示字段初始化为10，double salary;表示字段默认初始化为0，String
    // name;表示引用类型字段默认初始化为null；
    // 执行构造方法的代码进行初始化。

    public void setAge(int age) {
        this.age = age;
    }

    public void printAge() {
        System.out.println(age);
    }

    public void setNames(String... names) {
        this.names = names;
    }
    // 完全可以把可变参数改写为String[]类型：
    // 但是，调用方需要自己先构造String[]，比较麻烦。例如：
    // 另一个问题是，调用方可以传入null：
    // public void setNames(String[] names) {
    // this.names = names;
    // }

    public void printNames() {
        if (names == null)
            return;
        for (String name : names) {
            System.out.println(name);
        }
    }

    public Person(int age, String... names) {
        this.names = names;
        this.age = age;
    }

    public Person() {
        this.names = new String[] { "default_name", };
        this.age = 10;
    }

    public Person(String... names) {
        // 一个构造方法可以调用其他构造方法，这样做的目的是便于代码复用。调用其他构造方法的语法是this(…)：
        this(1011, names);
    }

    public void run() {
        System.out.println("Person.run");
    }

    
}

public class LearnOOP {
    // 由于Java支持嵌套类，如果一个类内部还定义了嵌套类，那么，嵌套类拥有访问private的权限：
    // private方法:
    private static void hello() {
        System.out.println("private hello!");
    }

    // 静态内部类:
    static class Inner {
        public void hi() {
            LearnOOP.hello();
        }
    }

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
            if ("-version".equals(arg)) {
                System.out.println("v 1.0.0");
                break;
            }
        }
        Person p = new Person();
        // 注意到setName()的参数现在是一个数组。一开始，把fullname数组传进去，然后，修改fullname数组的内容，结果发现，实例p的字段p.name也被修改了！
        // 结论：引用类型参数的传递，调用方的变量，和接收方的参数变量，指向的是同一个对象。双方任意一方对这个对象的修改，都会影响对方（因为指向同一个对象嘛）。
        // 有了上面的结论，我们再看一个例子：
        String[] names = new String[] { "names1", "names2", "names3", "names4" };
        p.setNames(names);
        p.printNames();
        names[0] = "default_name";
        System.out.println("\nName changed since here\n");
        p.printNames();

        Person p2 = new Person();
        p2.printAge();
        p2.printNames();

        Person p3 = new Person(100, null);
        p3.printNames();
        //
        // Person类型p1实际指向Student实例，Person类型变量p2实际指向Person实例。在向下转型的时候，把p1转型为Student会成功，因为p1确实指向Student实例，把p2转型为Student会失败，因为p2的实际类型是Person，不能把父类变为子类，因为子类功能比父类多，多的功能无法凭空变出来。
        // 因此，向下转型很可能会失败。失败的时候，Java虚拟机会报ClassCastException。
        // 为了避免向下转型出错，Java提供了instanceof操作符，可以先判断一个实例究竟是不是某种类型：
        // 从Java 14开始，判断instanceof后，可以直接转型为指定变量，避免再次强制转型。例如，对于以下代码：

        Object obj = "hello";
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println(s.toUpperCase());
        }
        // 但是这是一个preview feature
        // if (obj instanceof String s) {
        // // 可以直接使用变量s:
        // System.out.println(s.toUpperCase());
        // }

        Person ps = new Student();
        ps.run();
        System.out.println("");
        run_twice(ps);
        AbsPerson absp = new AbsStudent();
        run_twice(absp);

    }
    // 它传入的参数类型是Person，我们是无法知道传入的参数实际类型究竟是Person，还是Student，还是Person的其他子类，因此，也无法确定调用的是不是Person类定义的run()方法。

    // 所以，多态的特性就是，运行期才能动态决定调用的子类方法。对某个类型调用某个方法，执行的实际方法可能是某个子类的覆写方法。这种不确定性的方法调用，究竟有什么作用？
    // 可见，多态具有一个非常强大的功能，就是允许添加更多类型的子类实现功能扩展，却不需要修改基于父类的代码。

    // STATIC FIELD IS SOMETHING THAT BELONGS TO THE CLASS WE'RE DISCUSSING
    // 调用实例方法必须通过一个实例变量，而调用静态方法则不需要实例变量，通过类名就可以调用。静态方法类似其它编程语言的函数。
    // 调用静态方法不需要实例，无法访问this，但可以访问静态字段和其他静态方法；
    // 推荐用类名来访问静态字段。可以把静态字段理解为描述class本身的字段（非实例字段）。对于上面的代码，更好的写法是：
    private static void run_twice(Person p) {
        p.run();
        p.run();
    }

    private static void run_twice(AbsPerson p) {
        p.run();
        p.run();
    }

    // Kind of like the constant, you cannot inherit from a final class
    // And you cannot override a final function
    // And you cannot change the value of a final variable after initialization
    // (like a normal constant)
    // 如果一个类不希望任何其他类继承自它，那么可以把这个类本身标记为final。用final修饰的类不能被继承：

    // 继承可以允许子类覆写父类的方法。如果一个父类不允许子类对它的某个方法进行覆写，可以把该方法标记为final。用final修饰的方法不能被Override：

}

// 如果父类的方法本身不需要实现任何功能，仅仅是为了定义方法签名，目的是让子类去覆写它，那么，可以把父类的方法声明为抽象方法：
/**
 * 通过abstract定义的方法是抽象方法，它只有定义，没有实现。抽象方法定义了子类必须实现的接口规范；
 * 定义了抽象方法的class必须被定义为抽象类，从抽象类继承的子类必须实现抽象方法； 如果不实现抽象方法，则该子类仍是一个抽象类；
 * 面向抽象编程使得调用者只关心抽象方法的定义，不关心子类的具体实现。
 */
abstract class AbsPerson {
    protected abstract void run();
}

class AbsStudent extends AbsPerson {
    @Override
    public void run() {
        System.out.println("AbsStudent is running, inheriting the abstract method from AbsPerson.");
    }
}

class Student extends Person {
    @Override
    public void run() {
        System.out.println("Student.run");
    }
}

/**
 * 如果一个抽象类没有字段，所有方法全部都是抽象方法： 就可以把该抽象类改写为接口：interface。
 * 在Java中，使用interface可以声明一个接口：
 */
// 因为interface是一个纯抽象类，所以它不能定义实例字段。但是，interface是可以有静态字段的，并且静态字段必须为final类型：
interface Hello {
    void hello();
}

interface IntfPerson extends Hello {
    int MALE = 1; // 会被自动变成：public static final
    int FEMALE = 2;

    String getName();

    default void run() {
        System.out.println(getName() + " run");
    }
}

class IntfStudent implements IntfPerson {
    private String name;

    public IntfStudent(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void hello() {
        System.out.println("Hello!");
    }
}