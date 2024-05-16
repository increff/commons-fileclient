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
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileClientWithAwsIT {

	private static Properties props;
	private static FileClient fileClient;
	private static final String tempBasePath = "test";

	@BeforeAll
	public static void beforeClass() throws IOException {
		props = new Properties();
		props.load(new FileInputStream("test.properties"));

		String awsRegion = props.getProperty("aws.region");
		String awsAccessKey = props.getProperty("aws.accessKey");
		String awsSecretKey = props.getProperty("aws.secretKey");
		String awsBucketName = props.getProperty("aws.bucketName");
		String awsBucketUrl = props.getProperty("aws.bucketUrl");

		AbstractFileProvider provider = new AwsFileProvider(awsRegion, awsAccessKey, awsSecretKey, awsBucketName,
				awsBucketUrl);
		fileClient = new FileClient(provider);
	}

	@Test
	public void create() throws IOException {
		InputStream is = FileData.getResource(FileData.testText);
		assertNotNull(is);
		String fileName = FileClient.getRandomFileName(".txt");
		String filePath = getAwsFilePath(fileName);
		fileClient.create(filePath, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
		fileClient.delete(filePath);
	}

	@Test
	public void delete() throws IOException {
		InputStream is = FileData.getResource(FileData.testText);
		String filePath = getAwsFilePath("test.txt");
		fileClient.create(filePath, is);
		is.close();
		fileClient.delete(filePath);
	}

	@Test
	public void getUploadUriAndUpload() throws IOException {
		String fileName = FileClient.getRandomFileName(".txt");
		String filePath = getAwsFilePath(fileName);
		InputStream is = FileData.getResource(FileData.testText);
		String uploadUri = fileClient.getWriteUri(filePath);
		fileClient.writeToUri(uploadUri, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
	}

	@Test
	public void getFilePath() throws FileClientException {
		String filePath = getAwsFilePath("test.txt");
		String fileUri = fileClient.getReadUri(filePath);
		assertEquals(props.getProperty("aws.bucketUrl") + "/" + filePath, fileUri);
	}

	private static void validateFile(String urlStr, int fileSize) throws IOException {
		URL url = new URL(urlStr);
		InputStream is = url.openStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Utils.copy(is, baos);
		byte[] bytes = baos.toByteArray();
		assertEquals(fileSize, bytes.length);
		is.close();
	}

	private static String getAwsFilePath(String fileName) {
		return tempBasePath + "/" + fileName;
	}
}
