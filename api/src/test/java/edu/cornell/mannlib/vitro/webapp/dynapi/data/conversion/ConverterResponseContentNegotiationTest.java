package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.apache.http.entity.ContentType.APPLICATION_ATOM_XML;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;
import static org.apache.http.entity.ContentType.TEXT_HTML;
import static org.apache.http.entity.ContentType.WILDCARD;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ConverterResponseContentNegotiationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String header;

    @org.junit.runners.Parameterized.Parameter(1)
    public ContentType requestType;

    @org.junit.runners.Parameterized.Parameter(2)
    public boolean exceptionExpected;

    @org.junit.runners.Parameterized.Parameter(3)
    public String expectedType;

    private ContentType actualType;
    private boolean exception;

    @Test
    public void getResponseTypeTest() {
        try {
            actualType = Converter.getResponseType(header, requestType);
        } catch (ConversionException e) {
            exception = true;
        }
        assertEquals(exceptionExpected, exception);
        if (!exceptionExpected) {
            assertEquals(expectedType, actualType.toString());
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { "application/json", APPLICATION_JSON, false, APPLICATION_JSON.toString() },
                { "Application/json", APPLICATION_JSON, false, APPLICATION_JSON.toString() },
                { "", APPLICATION_JSON, false, APPLICATION_JSON.toString() },
                { "", MULTIPART_FORM_DATA, false, MULTIPART_FORM_DATA.toString() },
                { "", APPLICATION_ATOM_XML, true, null },
                {
                APPLICATION_JSON.toString() + "bad, " + APPLICATION_JSON.toString(),
                APPLICATION_JSON,
                false,
                APPLICATION_JSON.toString() },
                { MULTIPART_FORM_DATA.toString(), APPLICATION_JSON, false, MULTIPART_FORM_DATA.toString() },
                { APPLICATION_JSON.toString(), MULTIPART_FORM_DATA, false, APPLICATION_JSON.toString() },
                { WILDCARD.toString(), MULTIPART_FORM_DATA, false, APPLICATION_JSON.toString() },
                { WILDCARD.toString(), MULTIPART_FORM_DATA, false, APPLICATION_JSON.toString() },
                {
                TEXT_HTML.toString() + ", " + MULTIPART_FORM_DATA + ";q=0.9",
                MULTIPART_FORM_DATA,
                false,
                MULTIPART_FORM_DATA.toString()
                },
                {
                TEXT_HTML.toString() + ", " + APPLICATION_JSON + ";q=0.9",
                MULTIPART_FORM_DATA,
                false,
                APPLICATION_JSON.toString()
                },
                {
                APPLICATION_JSON.toString() + ";q=0.8, " + MULTIPART_FORM_DATA + ";q=0.9",
                MULTIPART_FORM_DATA,
                false,
                MULTIPART_FORM_DATA.toString()
                },
                {
                APPLICATION_JSON.toString() + ";q=0.001, " + MULTIPART_FORM_DATA + ";q=0.9",
                MULTIPART_FORM_DATA,
                false,
                MULTIPART_FORM_DATA.toString()
                },
                // 0.00001 is too long so it is skipped and default 1 used.
                {
                APPLICATION_JSON.toString() + ";q=0.0001, " + MULTIPART_FORM_DATA + ";q=0.9",
                MULTIPART_FORM_DATA,
                false,
                APPLICATION_JSON.toString()
                }, });
    }
}
