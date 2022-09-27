package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;

@RunWith(Parameterized.class)
public class JsonContainerTest {

	@org.junit.runners.Parameterized.Parameter(0)
	public String jsonString;

	public JsonContainer createJsonContainder()  {
		JsonContainer container = null;
		try {
			container = JsonContainer.deserialize(jsonString);
			 assertTrue(container != null);
		} catch (ConversionException e) {
			e.printStackTrace();
		}
		 return container;
	}

	@Test
	public void testPutData() {
		JsonContainer object = createJsonContainder();
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
