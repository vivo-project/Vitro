package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public abstract class ServletContextIT extends ServletContextTest {

    protected Map<String, String[]> parameterMap;

    protected int status;

    public void setup() {
        super.setup();

        parameterMap = new HashMap<>();

        status = 0;
    }

    protected String readMockFile(String path) throws IOException {
        return super.readFile("src/test/resources/dynapi/mock/" + path);
    }

    protected void mockParameterIntoMap(String name, String value) {
        if (value == null) {
            parameterMap.put(name, new String[] { });
        } else {
            parameterMap.put(name, new String[] { value });
        }
    }

    protected void mockStatus(HttpServletResponse response) {
        doAnswer(invocation -> {
            if (invocation.getArguments().length == 1) {
                status = invocation.getArgument(0);
            }

            return status;
        }).when(response).setStatus(any(Integer.class));

        when(response.getStatus()).thenAnswer(invocation -> {
            return status;
        });
    }

    protected void runCallback(Method callback) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (callback != null) {
            callback.invoke(this);
        }
    }

}
