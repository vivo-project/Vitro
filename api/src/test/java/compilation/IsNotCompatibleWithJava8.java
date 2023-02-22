package compilation;

import static org.junit.Assert.*;

import org.junit.Test;

public class IsNotCompatibleWithJava8 {

    @Test
    public void test() {
        String value = "This code is compiled in Java 11";
        System.out.println(value.isBlank());
    }

}
