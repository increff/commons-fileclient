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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import lombok.extern.log4j.Log4j;
import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@Log4j
public class SftpFileProvider extends AbstractFileProvider {

	private final ChannelSftp channelSftp;
	private final String filePath; // can be local or remote path based on get/put

	public SftpFileProvider(String remoteHost, String username, String password,
							String filePath) throws IOException {
		try {
			JSch jsch = new JSch();
			JSch.setConfig("StrictHostKeyChecking", "no");
			Session jschSession = jsch.getSession(username, remoteHost);
			jschSession.setPassword(password);
			jschSession.connect();
			this.channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
			this.filePath = filePath;
		} catch (Exception e) {
			log.error("SFTP Error for file " + filePath + ". " + e.getMessage() + " " + Arrays.asList(e.getStackTrace()));
			throw new IOException("SFTP Error for file " + filePath + ". " + e.getMessage());
		}
	}

	@Override
	public void create(String remoteFilePath, InputStream is) throws FileClientException {
		try {
			channelSftp.connect();
			channelSftp.put(this.filePath, remoteFilePath);
			channelSftp.exit();
		} catch (Exception e) {
			log.error("SFTP Create Error for file " + filePath + ". " + e.getMessage() + " " + Arrays.asList(e.getStackTrace()));
			throw new FileClientException("SFTP Create Error for file " + filePath + ". " + e.getMessage());
		}
	}

	@Override
	public String getReadUri(String filePath) {
		return null;
	}

	@Override
	public void delete(String filePath) throws FileClientException {

	}

	@Override
	public String getWriteUri(String filePath) throws FileClientException {
		return null;
	}

	@Override
	public List<ObjectSummary> getAllObjects(String directory) throws FileClientException {
		return null;
	}

	@Override
	public InputStream get(String filePath) throws FileClientException {
		return null;
	}

}
