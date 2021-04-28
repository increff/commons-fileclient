package com.nextscm.commons.fileclient;

import java.io.InputStream;
import java.util.List;

public abstract class AbstractFileProvider {

	/* API to use when directly writing */
	public abstract void create(String filePath, InputStream is) throws FileClientException;

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
