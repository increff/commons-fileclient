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

import com.amazonaws.services.s3.model.*;
import lombok.extern.log4j.Log4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Log4j
public class SftpFileProvider extends AbstractFileProvider {

	private final SSHClient client;
	private final String filePath; // can be local or remote based on get/put

	public SftpFileProvider(String remoteHost, String username, String password,
							String filePath) throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(remoteHost);
		client.useCompression();
		client.authPassword(username, password);
		this.client = client;
		this.filePath = filePath;
	}

	@Override
	public void create(String remoteFilePath, InputStream is) throws FileClientException {
		try {
			SFTPClient sftpClient = client.newSFTPClient();
			sftpClient.put(filePath, remoteFilePath);

			sftpClient.close();
			client.disconnect();
		} catch (IOException e) {
			log.error("SFTP Create Error for file " + filePath + ". " + e.getMessage());
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
