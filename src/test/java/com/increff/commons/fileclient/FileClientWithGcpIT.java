package com.increff.commons.fileclient;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileClientWithGcpIT {

	private static FileClient fileClient;
	private static final String tempBasePath = "test";

	@BeforeClass
	public static void beforeClass() throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("test.properties"));

		String baseUrl = props.getProperty("gcp.baseUrl");
		String credentialsFilePath = props.getProperty("gcp.credentialsFilePath");
		String bucketName = props.getProperty("gcp.bucketName");

		AbstractFileProvider provider = new GcpFileProvider(baseUrl, bucketName, credentialsFilePath);
		fileClient = new FileClient(provider);
	}

	@Test
	public void create() throws IOException {
		InputStream is = FileData.getResource(FileData.testText);
		assertNotNull(is);
		String fileName = FileClient.getRandomFileName(".txt");
		String filePath = getGcpFilePath(fileName);
		fileClient.create(filePath, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
		fileClient.delete(filePath);
	}

	@Test
	public void createForLargeFiles() throws IOException {
		InputStream is = FileData.getResource(FileData.testText);
		assertNotNull(is);
		String fileName = FileClient.getRandomFileName(".txt");
		String filePath = getGcpFilePath(fileName);
		fileClient.createForLargeFiles(filePath, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
		fileClient.delete(filePath);
	}

	@Test
	public void delete() throws IOException {
		InputStream is = FileData.getResource(FileData.testText);
		String filePath = getGcpFilePath("test.txt");
		fileClient.create(filePath, is);
		is.close();
		fileClient.delete(filePath);
	}

	@Test
	public void getUploadUriAndUpload() throws IOException {
		String fileName = FileClient.getRandomFileName(".txt");
		String filePath = getGcpFilePath(fileName);
		InputStream is = FileData.getResource(FileData.testText);
		String uploadUri = fileClient.getWriteUri(filePath);
		fileClient.writeToUri(uploadUri, is);
		is.close();
		String url = fileClient.getReadUri(filePath);
		validateFile(url, 11);
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

	private static String getGcpFilePath(String fileName) {
		return tempBasePath + "/" + fileName;
	}
}
