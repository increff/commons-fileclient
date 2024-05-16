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
import com.jcraft.jsch.*;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Log4j2
public class SftpFileProvider extends AbstractFileProvider {

	private final String remoteHost;
	private final String username;
	private final String password;

	public SftpFileProvider(String remoteHost, String username, String password) throws FileClientException {
		try {
			this.remoteHost = remoteHost;
			this.username = username;
			this.password = password;

			testConnection();
		} catch (Exception e) {
			log.error("SFTP Connection Error. " + e.getMessage() + " " + Arrays.asList(e.getStackTrace()));
			throw new FileClientException(e.getMessage(), e);
		}
	}

	@Override
	public void create(String filePath, InputStream is) throws FileClientException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void create(String localFilepath, String remoteFilepath) throws FileClientException {
		Session session = null;
		ChannelSftp channel = null;
		try {
			session = connectSession();
			channel = connectChannel(session);

			channel.put(localFilepath, remoteFilepath);

		} catch (Exception e) {
			log.error("Failed to create local file: " + localFilepath + ", remote file: " + remoteFilepath + " on SFTP. " + e.getMessage() + " " + Arrays.asList(e.getStackTrace()));
			throw new FileClientException("Failed to create local file: " + localFilepath + ", remote file: " + remoteFilepath + " on SFTP. " + e.getMessage(), e);
		} finally {
			closeQuietly(channel, session);
		}
	}

	@Override
	public String getReadUri(String filePath) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void delete(String filePath) throws FileClientException {
		Session session = null;
		ChannelSftp channel = null;
		try {
			session = connectSession();
			channel = connectChannel(session);

			channel.rm(filePath);
		} catch (Exception e) {
			throw new FileClientException("Failed to delete file: " + filePath + " on SFTP. " + e.getMessage(), e);
		} finally {
			closeQuietly(channel, session);
		}
	}

	@Override
	public String getWriteUri(String filePath) throws FileClientException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public List<ObjectSummary> getAllObjects(String directory) throws FileClientException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public InputStream get(String filePath) throws FileClientException {
		throw new UnsupportedOperationException("Not implemented");
	}

	private Session connectSession() throws JSchException {
		JSch jsch = getjSch();
		Session session = jsch.getSession(username, remoteHost);
		session.setPassword(password);
		session.connect();
		return session;
	}

	private ChannelSftp connectChannel(Session session) throws JSchException {
		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		return channel;
	}

	private void closeQuietly(ChannelSftp channel, Session session) {
		if(Objects.nonNull(channel))
			channel.exit();
		if(Objects.nonNull(session))
			session.disconnect();
	}

	private JSch getjSch() {
		JSch jsch = new JSch();
		JSch.setConfig("StrictHostKeyChecking", "no");
		return jsch;
	}

	private void testConnection() throws FileClientException {
		Session session = null;
		ChannelSftp channel = null;
		try {
			session = connectSession();
			channel = connectChannel(session);
		} catch (Exception e) {
			throw new FileClientException("Failed to test connection on SFTP. " + e.getMessage(), e);
		} finally {
			closeQuietly(channel, session);
		}
	}


}
