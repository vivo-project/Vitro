package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

@RunWith(Parameterized.class)
public class DynapiJsonObjectTest {

	@org.junit.runners.Parameterized.Parameter(0)
	public String jsonString;

	public DynapiJsonObject createJsonObject()  {
		DynapiJsonObject object = null;
		try {
			object = DynapiJsonObject.deserialize(jsonString);
			 assertTrue(object != null);
		} catch (ConversionException e) {
			e.printStackTrace();
		}
		 return object;
	}

	@Test
	public void testPutData() {
		DynapiJsonObject object = createJsonObject();
	}
	

	@Parameterized.Parameters
	public static Collection<Object[]> requests() {
		return Arrays.asList(
				new Object[][] { 
					{ "{}" }, 
					{ "{\"a\":\"b\"}", },
					{ "[]", }, 
					{ "[ 1, 2, 3]", }, 
				});
	}
}
