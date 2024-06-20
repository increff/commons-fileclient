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

import com.google.api.client.util.Base64;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GcpFileProvider extends AbstractFileProvider {

	static final int EXPIRATION = 30 * 1000;
	static final HttpMethod HTTP_METHOD = HttpMethod.PUT;

	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_EMAIL = "client_email";
	private static final String PRIVATE_KEY = "private_key";
	private static final String PRIVATE_KEY_ID = "private_key_id";
	// basically read URI should never expire
	private static final int READ_EXPIRY_DURATION_IN_DAYS = 1_00_000;

	// Size of the buffer used to write the file to remote bucket
	// https://stackoverflow.com/questions/8748960/how-do-you-decide-what-byte-size-to-use-for-inputstream-read
	private static final int WRITE_BUFFER_SIZE = 10_240;

	private Storage storage;
	private final String bucketName;
	private final String baseUrl;
	private Map<String, String> serviceCredentials;

	public GcpFileProvider(String baseUrl, String bucketName, String credentialFilePath) throws FileClientException {
		this.baseUrl = baseUrl;
		this.bucketName = bucketName;
		try {
			GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialFilePath));
			storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
			serviceCredentials = parseCredentialFile(credentialFilePath);
		}catch (IOException e){
			throw new FileClientException("Error while creating GCP File Provider Object:"+e.getMessage(), e);
		}
	}

	/*
	Do not use this constructor for creating signed URLS
	 */
	public GcpFileProvider(String baseUrl, String bucketName, InputStream inputStream) throws FileClientException {
		this.baseUrl = baseUrl;
		this.bucketName = bucketName;
		try {
			GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream);
			storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
		}catch (IOException e){
			throw new FileClientException("Error while creating GCP File Provider Object:"+e.getMessage(), e);
		}
	}

	/**
	 * This function will upload the file in the stream at the specified path.
	 * This will fail for files more than CAPACITY(INT) ~ 2GB = the maximum length of an array in JAVA.
	 */
	@Override
	public void create(String filePath, InputStream is) throws FileClientException {
		BlobId blobId = BlobId.of(bucketName, filePath);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		try {
			storage.create(blobInfo, IOUtils.toByteArray(is));
		}catch (IOException e){
			throw new FileClientException("Error while creating file: "+e.getMessage(), e);
		}
	}

	/**
	 * This function will upload the file in the stream at the specified path with metaData properties.
	 * This will fail for files more than CAPACITY(INT) ~ 2GB = the maximum length of an array in JAVA.
	 */
	@Override
	public void create(String filePath, InputStream is, Map<String,String> metadata) throws FileClientException {
		BlobId blobId = BlobId.of(bucketName, filePath);
		BlobInfo.Builder builder = BlobInfo.newBuilder(blobId);
		if (Objects.nonNull(metadata))
			builder.setMetadata(metadata);
		BlobInfo blobInfo = builder.build();

		try {
			storage.create(blobInfo, IOUtils.toByteArray(is));
		} catch (IOException e){
			throw new FileClientException("Error while creating file: "+e.getMessage(), e);
		}
	}

	/**
	 * This function uses a writer to upload a file to the remote bucket.
	 * Writes the file in batches of WRITE_BUFFER_SIZE.
	 */
	@Override
	public void createForLargeFiles(String filePath, InputStream is) throws FileClientException {
		BlobId blobId = BlobId.of(bucketName, filePath);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		try {
			WriteChannel writer = storage.writer(blobInfo);

			byte[] buffer = new byte[WRITE_BUFFER_SIZE];
			int limit;
			while ((limit = is.read(buffer)) >= 0) {
				writer.write(ByteBuffer.wrap(buffer, 0, limit));
			}
			writer.close();
		} catch (IOException e) {
			throw new FileClientException("Error while writing file: " + e.getMessage(), e);
		}
	}

	@Override
	public String getReadUri(String filePath) {
		BlobId blobId = BlobId.of(bucketName, filePath);
		Blob blob = storage.get(blobId);
		if(blob == null) {
			return null;
		}
		return blob.signUrl(READ_EXPIRY_DURATION_IN_DAYS, TimeUnit.DAYS).toString();
	}

	@Override
	public void delete(String filePath) {
		storage.delete(BlobId.of(bucketName, filePath));
	}

	/**
	 * No direct method is given for creating signedURL for objects which is
	 * non-existent in google storage currently. So, creating link manually by
	 * following below link:
	 *
	 * https://cloud.google.com/storage/docs/access-control/create-signed-urls-program
	 *
	 */
	@Override
	public String getWriteUri(String filePath) throws FileClientException {
		String canonicalUrl = "/" + bucketName + "/" + filePath;
		String blobUrl = baseUrl + canonicalUrl;
		Date expiration = new Date(System.currentTimeMillis() + EXPIRATION);
		String stringToSign = HTTP_METHOD + "\n\n\n" + expiration.getTime() + "\n" + canonicalUrl;
		String safeEncodedString;
		try {
			String signedUrl = Base64.encodeBase64String(ServiceAccountCredentials
					.fromPkcs8(serviceCredentials.get(CLIENT_ID), serviceCredentials.get(CLIENT_EMAIL),
							serviceCredentials.get(PRIVATE_KEY), serviceCredentials.get(PRIVATE_KEY_ID), null)
					.sign(stringToSign.getBytes()));
			safeEncodedString = URLEncoder.encode(signedUrl, "UTF-8");
		}catch (IOException e){
			throw  new FileClientException("Error while creating signedUrl:"+e.getMessage(), e);
		}
		return blobUrl + "?GoogleAccessId=" + serviceCredentials.get(CLIENT_EMAIL) + "&Expires=" + expiration.getTime()
				+ "&Signature=" + safeEncodedString;
	}

	@Override
	public InputStream get(String filePath) {
		BlobId blobId = BlobId.of(bucketName, filePath);
		Blob blob = storage.get(blobId);
		return new ByteArrayInputStream(blob.getContent());
	}

	@Override
	public URL getSignedUri(String filePath) {
		BlobId blobId = BlobId.of(bucketName, filePath);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		return storage.signUrl(blobInfo, 10, TimeUnit.SECONDS);
	}

	/* HELPER METHOD */
	private static Map<String, String> parseCredentialFile(String credentialFilePath) throws FileClientException {
		HashMap<String, String> map = new HashMap<>();
		JSONObject jObject;
		try {
			jObject = new JSONObject(new String(Files.readAllBytes(Paths.get(credentialFilePath))));
		}
		catch (IOException e){
			throw new FileClientException("Error at parsing Credential File:"+e.getMessage(), e);
		}
		Iterator<String> iter = jObject.keys();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = jObject.getString(key);
			map.put(key, value);
		}
		return map;
	}

	@Override
	public List<ObjectSummary> getAllObjects(String filePath){
		List<ObjectSummary> objectSummaryList = new ArrayList<>();
		Page<Blob> blobs =  storage.list(bucketName, Storage.BlobListOption.currentDirectory(),
				Storage.BlobListOption.prefix(filePath));
			for (Blob blob : blobs.iterateAll()) {
				ObjectSummary objectSummary = new ObjectSummary();
				objectSummary.setKey(blob.getName());
				objectSummary.setSize(blob.getSize());
				objectSummary.setBucketName(blob.getBucket());
				objectSummary.setLastModified(blob.getUpdateTime() != null ? new Date(blob.getUpdateTime()) : null);
				objectSummaryList.add(objectSummary);
		}
		return objectSummaryList;
    }
}