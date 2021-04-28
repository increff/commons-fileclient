package com.nextscm.commons.fileclient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileClientTest {

	@Test
	public void getRandomFileName() {
		String fileName = FileClient.getRandomFileName(".txt");
		assertEquals(40, fileName.length());
		assertTrue(fileName.endsWith(".txt"));
	}

}
