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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class AbstractFileProvider {

	/* API to use when directly writing */
	public abstract void create(String filePath, InputStream is) throws FileClientException;

	public void create(String filePath, InputStream is, Map<String,String> metaData) throws FileClientException {
		throw new FileClientException("This method is not implemented");
	}

	/* API to use when directly writing large files */
	public void createForLargeFiles(String filePath, InputStream is) throws FileClientException {
		create(filePath, is);
	}

	/* API to use when giving someone else a read access (e.g. a browser) */
	public abstract String getReadUri(String filePath);

	/* API to use when directly deleting */
	public abstract void delete(String filePath) throws FileClientException;

	/* API to use when giving someone else a write access (e.g. a browser) */
	public abstract String getWriteUri(String filePath) throws FileClientException;

	public void writeToUri(String uploadUri, InputStream is) throws FileClientException {
		Utils.writeInputStreamToUri(uploadUri, is);
	}

	/* API to read all objects */
	public abstract List<ObjectSummary> getAllObjects(String directory) throws FileClientException;

	/* API to use when directly reading */
	public abstract InputStream get(String filePath) throws FileClientException;
}
