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

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Allows interaction with a storage provider
 */
public class FileClient {

	private final AbstractFileProvider provider;

	public FileClient(AbstractFileProvider provider) {
		this.provider = provider;
	}

	/**
	 * Creates file at specified location
	 * Use this mechanism only when you want your own custom path.
	 * The file will go into BasePath/filePath
	 * @param filePath Custom path at which to create file
	 * @param is Input stream from which to read data
	 */
	public void create(String filePath, InputStream is) throws FileClientException {
			provider.create(filePath, is);
	}

	/**
	 * Creates file at specified location
	 * Use this mechanism only when you want your own custom path and the file is large.
	 * Only GCP File Provider has a separate mechanism which is fail safe for files > 2 GB.
	 * The file will go into BasePath/filePath
	 * @param filePath Custom path at which to create file
	 * @param is Input stream from which to read data
	 */
	public void createForLargeFiles(String filePath, InputStream is) throws FileClientException {
			provider.createForLargeFiles(filePath, is);
	}

	/**
	 * Delete file stored at specified file path
	 * Use this mechanism only when you want your own custom URI
	 * @param filePath Path to file to be deleted
	 */
	public void delete(String filePath) throws FileClientException {
		provider.delete(filePath);
	}

	/**
	 * Returns the complete Uniform resource identifier for a given filepath on the storage
	 * @param filePath Path of file whose URI is required
	 * @return URI String
	 */
	public String getReadUri(String filePath) throws FileClientException {
		try {
			return provider.getReadUri(filePath);
		} catch (Exception e) {
			throw new FileClientException(e);
		}
	}

	/**
	 * Get URI for writing/uploading data to a specified location
	 * @param filePath Path of file on which to write
	 * @return URI string for writing data
	 */
	public String getWriteUri(String filePath) throws FileClientException {
		try {
			return provider.getWriteUri(filePath);
		} catch (Exception e) {
			throw new FileClientException(e);
		}
	}

	/**
	 * Write data from InputStream to specified write URI
	 * @param uploadUri URI at which to write data
	 * @param is Input stream from which to read data
	 */
	public void writeToUri(String uploadUri, InputStream is) throws FileClientException {
		try {
			provider.writeToUri(uploadUri, is);
		} catch (Exception e) {
			throw new FileClientException(e);
		}
	}

	/**
	 * Returns a random file name prepended to specified extension
	 * @param ext Extension to be used
	 * @return Random file name with specified extension
	 */
	public static String getRandomFileName(String ext) {
		String normalizedExtension = ext.startsWith(".") ? ext.substring(1) : ext;
		String uuid = UUID.randomUUID().toString();
		String fileName = uuid + "." + normalizedExtension;
		return fileName.toLowerCase();
	}

	public List<ObjectSummary> getAllObjects(String directory) throws FileClientException {
		try {
			return provider.getAllObjects(directory);
		} catch (Exception e) {
			throw new FileClientException(e);
		}
	}

}
