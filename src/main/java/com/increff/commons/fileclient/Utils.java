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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.http.client.methods.HttpPut;

class Utils {

	private static final int bufferSize = 1024;

	public static void copy(InputStream in, OutputStream out) throws FileClientException {
		// Read bytes and write to destination until eof
		byte[] buf = new byte[bufferSize];
		int len;
		try {
			while ((len = in.read(buf)) >= 0) {
				out.write(buf, 0, len);
			}
		}catch (IOException e){
			throw new FileClientException("Error while copying the data:"+e.getMessage(), e);
		}
	}

	public static Date getExpirationDate(int duration) {
		Date date = new Date();
		date.setTime(date.getTime() + duration);
		return date;
	}

	public static void writeInputStreamToUri(String uploadUri, InputStream is) throws FileClientException {
		OutputStream out = null;
		try {
			URL url = new URL(uploadUri);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(HttpPut.METHOD_NAME);
			out = connection.getOutputStream();
			Utils.copy(is, out);
			out.flush();
			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				throw new IOException("Error uploading object to cloud storage, FilePath: " + uploadUri + ", ErrorCode:"
						+ responseCode);
			}
		}catch (IOException e){
			throw new FileClientException("Error while writing Input Stream to Uri:"+e.getMessage(), e);
		}
		finally {
			closeQuitely(out);
		}
	}

	public static void closeQuitely(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			// DO NOTHING
		}
	}
}
