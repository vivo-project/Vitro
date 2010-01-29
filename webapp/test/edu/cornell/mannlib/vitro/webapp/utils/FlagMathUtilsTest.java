package edu.cornell.mannlib.vitro.webapp.utils;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import static edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils.bits2Numeric;
import static edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils.numeric2Portalid;
import static edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils.numeric2numerics;
import static edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils.portalId2Numeric;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * @author jeb228
 */
public class FlagMathUtilsTest extends AbstractTestClass {

	@Test
	public void testBits2Num() {
		boolean[] bits = { false, false, false, false };
		assertEquals(0, bits2Numeric(bits));

		boolean[] bits2 = { true, false, false, false };
		assertEquals(1, bits2Numeric(bits2));

		boolean[] bits3 = { true, true, false, false };
		assertEquals(3, bits2Numeric(bits3));

		boolean[] bits4 = { true, false, false, true };
		assertEquals(1 + 8, bits2Numeric(bits4));

		boolean[] bits5 = { false, false, false, true };
		assertEquals(8, bits2Numeric(bits5));

		boolean[] bits6 = { true, true, true, true };
		assertEquals(1 + 2 + 4 + 8, bits2Numeric(bits6));
	}

	@Test
	public void testNumeric2numerics() {
		Long[] num0 = { new Long(1), new Long(2), new Long(4), new Long(8) };
		assertTrue(Arrays.equals(num0, numeric2numerics(1 + 2 + 4 + 8)));

		Long[] num1 = { new Long(1) };
		assertTrue(Arrays.equals(num1, numeric2numerics(1)));

		Long[] num2 = {};
		assertTrue(Arrays.equals(num2, numeric2numerics(0)));

		Long[] num3 = { new Long(1), new Long(8) };
		assertTrue(Arrays.equals(num3, numeric2numerics(1 + 8)));

		Long[] num4 = { new Long(4), new Long(8) };
		assertTrue(Arrays.equals(num4, numeric2numerics(4 + 8)));

		Long[] num5 = { new Long(8) };
		assertTrue(Arrays.equals(num5, numeric2numerics(8)));

		Long[] num6 = { new Long(2), new Long(4) };
		assertTrue(Arrays.equals(num6, numeric2numerics(2 + 4)));
	}

	@Test
	public void testNumeric2Portalid() {
		assertEquals(0, numeric2Portalid(1));
		assertEquals(1, numeric2Portalid(2));
		assertEquals(2, numeric2Portalid(4));
		assertEquals(3, numeric2Portalid(8));
		assertEquals(4, numeric2Portalid(16));
		assertEquals(5, numeric2Portalid(32));
		assertEquals(6, numeric2Portalid(64));
		assertEquals(7, numeric2Portalid(128));
		assertEquals(8, numeric2Portalid(256));

		// make sure we throw errors on bad inputs
		try {
			numeric2Portalid(0);
			fail("should have thrown Error");
		} catch (Throwable e) {
		}
		try {
			numeric2Portalid(3);
			fail("should have thrown Error");
		} catch (Throwable e) {
		}
		try {
			numeric2Portalid(15);
			fail("should have thrown Error");
		} catch (Throwable e) {
		}
		try {
			numeric2Portalid(21);
			fail("should have thrown Error");
		} catch (Throwable e) {
		}
	}

	@Test
	public void testPortalId2Num() {
		assertEquals(2, portalId2Numeric(1));
		assertEquals(4, portalId2Numeric(2));
		assertEquals(8, portalId2Numeric(3));
		assertEquals(16, portalId2Numeric(4));
		assertEquals(32, portalId2Numeric(5));
		assertEquals(64, portalId2Numeric(6));
		assertEquals(128, portalId2Numeric(7));
	}

	@Test
	public void testBackAndForth() {
		for (long i = 1; i < Long.SIZE - 1; i++) {
			long num = portalId2Numeric(i);
			int portal = numeric2Portalid(num);
			assertEquals(i, portal);
		}
	}

}
