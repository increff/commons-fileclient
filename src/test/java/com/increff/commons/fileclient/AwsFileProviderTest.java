package com.increff.commons.fileclient;

import com.amazonaws.HttpMethod;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AwsFileProviderTest {

	@Test
	public void testStaticValues() {
		assertEquals(30000, AwsFileProvider.duration);
		assertEquals(HttpMethod.PUT, AwsFileProvider.httpMethod);
	}

}
