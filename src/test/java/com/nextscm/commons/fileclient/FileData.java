package com.nextscm.commons.fileclient;

import java.io.InputStream;

class FileData {

	private static final String basePackage = "/com/nextscm/commons/fileclient";
	public static final String testText = basePackage + "/test.txt";

	public static InputStream getResource(String resource) {
		return FileData.class.getResourceAsStream(resource);
	}

}
