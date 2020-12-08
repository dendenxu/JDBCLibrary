import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnLogging {
    public static void main(String[] args) {
        foo();
        PersonLog pl = new PersonLog();
        pl.foo();
        pl.fooslf4j();
        StudentLog sl = new StudentLog();
        sl.foo();
    }

    // 注意到实例变量log的获取方式是LogFactory.getLog(getClass())，虽然也可以用LogFactory.getLog(Person.class)，但是前一种方式有个非常大的好处，就是子类可以直接使用该log实例。例如：
    static final Log log = LogFactory.getLog(LearnLogging.class);

    static void foo() {
        log.info("foo");
    }
}

class PersonLog {
    protected final Log log = LogFactory.getLog(getClass());
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    void foo() {
        log.info("foo");
    }
    void fooslf4j() {
        logger.info("Hello, world.");
    }
}

class StudentLog extends PersonLog {
    void bar() {
        log.info("bar");
    }
}