/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.errors;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class ResponseErrorHandler extends DefaultResponseErrorHandler {
	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		System.out.println("REST ERROR: Connecting to: " + response.toString());
		System.out.println("REST ERROR: Status Text = " + response.getStatusText());
	    System.out.println("REST ERROR: Status Code = " + response.getStatusCode());
	    System.out.println("REST ERROR: Response Body = " + response.getBody());
	}
}
