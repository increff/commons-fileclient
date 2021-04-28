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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class NativeFileProvider extends AbstractFileProvider {

	private final String rootFolder;

	public NativeFileProvider(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	@Override
	public void create(String filePath, InputStream is) throws FileClientException {
		Path path = normalizePath(filePath);
		try {
			Files.createDirectories(path.getParent());
			Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
		}catch (IOException e){
			throw new FileClientException("Error while creating file:"+e.getMessage(), e);
		}
	}

	@Override
	public String getReadUri(String filePath) {
		return normalizePath(filePath).toAbsolutePath().toUri().toString();
	}

	@Override
	public void delete(String filePath) throws FileClientException {
		try {
			Files.deleteIfExists(normalizePath(filePath));
		}catch (IOException e){
			throw new FileClientException("Error while deleting file:"+e.getMessage(), e);
		}
	}

	@Override
	public String getWriteUri(String filePath) {
		return getReadUri(filePath);
	}

	@Override
	public void writeToUri(String uploadUri, InputStream is) throws FileClientException {
		throw new FileClientException("This method is not implemented", new Exception());
	}

	@Override
	public List<ObjectSummary> getAllObjects(String directory) throws FileClientException {
		throw new FileClientException("This method is not implemented", new Exception());
	}

	@Override
	public InputStream get(String filePath) throws FileClientException {
		try {
			return new FileInputStream(normalizePath(filePath).toFile());
		}catch (IOException e){
			throw  new FileClientException("Error while getting file:"+e.getMessage(), e);
		}
	}

	protected Path normalizePath(String filePath) {
		return Paths.get(rootFolder + "/" + filePath);
	}
}
