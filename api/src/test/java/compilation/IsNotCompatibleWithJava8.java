package compilation;

import static org.junit.Assert.*;

import org.junit.Test;

public class IsNotCompatibleWithJava8 {
/*
 * This code segment is compatible with Java 11 and not compatible with java 8. It will abort the compilation if the Java context is version 8
 */
    @Test
    public void test() {
        String value = "This code is compiled in Java 11";
        System.out.println(value.isBlank());
    }

}
