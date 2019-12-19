/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.errors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class ResponseErrorHandler extends DefaultResponseErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(ResponseErrorHandler.class);

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		logger.error("REST ERROR: Connecting to: " + response.toString());
		logger.error("REST ERROR: Status Text = " + response.getStatusText());
		logger.error("REST ERROR: Status Code = " + response.getStatusCode());
		logger.error("REST ERROR: Response Body = " + response.getBody());
	}
}
