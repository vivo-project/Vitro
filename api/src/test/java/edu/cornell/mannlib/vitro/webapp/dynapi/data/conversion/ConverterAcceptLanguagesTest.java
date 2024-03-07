package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ConverterAcceptLanguagesTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String header;

    @org.junit.runners.Parameterized.Parameter(1)
    public List<String> expectedList;

    public List<String> actualList;

    @Test
    public void getAcceptLanguagesTest() {
        actualList = Converter.getAcceptLanguages(header);
        assertEquals(expectedList, actualList);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { "", Collections.emptyList() },
                { "en-US", Arrays.asList("en-US") },
                { "da, en-gb;q=0.8, en;q=0.7", Arrays.asList("da", "en-gb", "en") },
                { "da;q=0.2, en-gb;q=0.8, en;q=0.7", Arrays.asList("en-gb", "en", "da") },
                { "da;q=0.2, en-gb;q=0.8, en", Arrays.asList("en", "en-gb", "da") }, });
    }
}
