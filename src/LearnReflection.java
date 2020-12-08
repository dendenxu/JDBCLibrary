import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnReflection {
    final static Logger logger = LoggerFactory.getLogger(LearnReflection.class);

    public static void main(String[] args) {
        Class cls1 = String.class;

        String s = "Hello";
        Class cls2 = s.getClass();

        boolean sameClass = cls1 == cls2; // true
        logger.info("Are they the same class? The answer is \"{}\"", sameClass);
        Integer n = 123;

        boolean b1 = n instanceof Integer; // true，因为n是Integer类型
        boolean b2 = n instanceof Number; // true，因为n是Number类型的子类

        boolean b3 = n.getClass() == Integer.class; // true，因为n.getClass()返回Integer.class
        // It's stopping you from thinking you've got a meaningful test when actually it
        // will always print "false".
        // boolean b4 = n.getClass() == Number.class; //
        // false，因为Integer.class!=Number.class
        logger.info("{}", b1);
        logger.info("{}", b2);
        logger.info("{}", b3);
        // logger.info("{}", b4);
        printClassInfo("".getClass());
        printClassInfo(Runnable.class);
        printClassInfo(java.time.Month.class);
        printClassInfo(String[].class);
        printClassInfo(int.class);

        // 获取String的Class实例:
        Class cls = String.class;
        // 创建一个String实例:
        try {
            String str = (String) cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("We've encounterd error, damn", e);
        }
        // 上述代码相当于new
        // String()。通过Class.newInstance()可以创建类实例，它的局限是：只能调用public的无参数构造方法。带参数的构造方法，或者非public的构造方法都无法通过Class.newInstance()被调用。
        //
        try {
            Class stdClass = Student.class;
            // 获取public字段"score":
            System.out.println(stdClass.getField("score"));
            // 获取继承的public字段"name":
            System.out.println(stdClass.getField("name"));
            // 获取private字段"grade":
            System.out.println(stdClass.getDeclaredField("grade"));
        } catch (Exception e) {
            logger.error("We've encounterd error, damn", e);
        }

        Field f;
        try {
            f = String.class.getDeclaredField("value");
            f.getName(); // "value"
            f.getType(); // class [B 表示byte[]类型
            int m = f.getModifiers();
            System.out.println(Modifier.isFinal(m)); // true
            System.out.println(Modifier.isPublic(m)); // false
            System.out.println(Modifier.isProtected(m)); // false
            System.out.println(Modifier.isPrivate(m)); // true
            System.out.println(Modifier.isStatic(m)); // false
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static void printClassInfo(Class cls) {
        System.out.println("Class name: " + cls.getName());
        System.out.println("Simple name: " + cls.getSimpleName());
        if (cls.getPackage() != null) {
            System.out.println("Package name: " + cls.getPackage().getName());
        }
        System.out.println("is interface: " + cls.isInterface());
        System.out.println("is enum: " + cls.isEnum());
        System.out.println("is array: " + cls.isArray());
        System.out.println("is primitive: " + cls.isPrimitive());
    }

    class Student extends Person {
        public int score;
        private int grade;
    }

    class Person {
        public String name;
    }

}
// Using annotation
// class Hello {
//     @Check(min = 0, max = 100, value = 55)
//     public int n;

//     @Check(value = 99)
//     public int p;

//     @Check(99) // @Check(value=99)
//     public int x;

//     @Check
//     public int y;
// }