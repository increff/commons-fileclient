/*
 * Copyright (c) 2021. Increff
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.increff.commons.fileclient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FileClientWithNativeTest {

    private static FileClient fileClient;

	private static final String tempBasePath = "test";

	@BeforeAll
	public static void beforeClass() throws IOException {
        Properties props = new Properties();
		props.load(new FileInputStream("test.properties"));
		String rootFolder = props.getProperty("native.rootFolder");
		AbstractFileProvider provider = new NativeFileProvider(rootFolder);
		fileClient = new FileClient(provider);
	}

	@Test
	public void create() throws IOException {
		String fileName = FileClient.getRandomFileName(".txt");
		String filePath = getNativeFilePath(fileName);
		InputStream is = FileData.getResource(FileData.testText);
		fileClient.create(filePath, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
		fileClient.delete(filePath);
		invalidateFile(url);
	}

	@Test
	public void delete() throws IOException {
		String filePath = getNativeFilePath("test.txt");
		InputStream is = FileData.getResource(FileData.testText);
		fileClient.create(filePath, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
		fileClient.delete(filePath);
		invalidateFile(url);
	}

	private static void validateFile(String fileUrl, int fileSize) throws IOException {
		System.out.println(fileUrl);
		URL url = new URL(fileUrl);
		InputStream is = url.openStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Utils.copy(is, baos);
		is.close();
		byte[] bytes = baos.toByteArray();
		assertEquals(fileSize, bytes.length);
	}

	private static void invalidateFile(String fileUrl) {
		System.out.println(fileUrl);
		try {
			URL url = new URL(fileUrl);
			url.openStream();
			fail("File should be absent: " + fileUrl);
		} catch (IOException e) {
			// do nothing
		}
	}

	private static String getNativeFilePath(String fileName) {
		return tempBasePath + "/" + fileName;
	}

}
