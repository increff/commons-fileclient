package com.increff.commons.fileclient;

import java.io.IOException;

public class FileClientException extends IOException {

	private static final long serialVersionUID = 1L;

	public FileClientException(Throwable t) {
		super(t);
	}

	public FileClientException(String message, Throwable t){
		super(message, t);
	}

}
