/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.controller;

import java.util.Collection;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.appscan.issuegateway.providers.ProvidersRepository;

import common.IProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "providers")
public class ProvidersController {

	@RequestMapping(value = "/providers", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get configured issue service providers", notes = "This API returns all configured issue service providers. "
			+ "Issue service providers are encouraged to include documentation about their specific configuration requirements "
			+ "in the response to this API. Note:Due to some swagger limitations, such as the inability to handle formatting characters, it is observed that the response "
			+ "is oddly formattted to some extent,it may be still helpful")
	Collection<IProvider> readProviders() {
		return ProvidersRepository.getProviders().values();
	}

	// this is to create a place holder for v2 version.
	@RequestMapping(value = "/v2/providers", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get configured issue service providers", notes = "This API returns all configured issue service providers. "
			+ "Issue service providers are encouraged to include documentation about their specific configuration requirements "
			+ "in the response to this API. Note:Due to some swagger limitations, such as the inability to handle formatting characters, it is observed that the response "
			+ "is oddly formatted to some extent,it may be still helpful")
	Collection<IProvider> readProvidersV2() {
		return ProvidersRepository.getProviders().values();
	}
}
