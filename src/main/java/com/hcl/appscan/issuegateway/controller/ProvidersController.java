/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019, 2023. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.controller;

import com.hcl.appscan.issuegateway.providers.ProvidersRepository;
import com.hcl.appscan.issuegateway.providers.ProvidersRepositoryV2;
import common.IProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@Tag(name = "providers")
public class ProvidersController {

	@Deprecated
	@GetMapping(value = "/providers", produces = "application/json")
	@Operation(summary = "Get configured issue service providers", description = "This API returns all configured issue service providers. "
			+ "Issue service providers are encouraged to include documentation about their specific configuration requirements "
			+ "in the response to this API. Note:Due to some swagger limitations, such as the inability to handle formatting characters, it is observed that the response "
			+ "is oddly formattted to some extent,it may be still helpful")
	public Collection<IProvider> readProviders() {
		return ProvidersRepository.getProviders().values();
	}

	@GetMapping(value = "/v2/providers", produces = "application/json")
	@Operation(summary = "Get configured issue service providers", description = "This API returns all configured issue service providers. "
			+ "Issue service providers are encouraged to include documentation about their specific configuration requirements "
			+ "in the response to this API. Note:Due to some swagger limitations, such as the inability to handle formatting characters, it is observed that the response "
			+ "is oddly formatted to some extent,it may be still helpful")
	public Collection<IProvider> readProvidersV2() {
		return ProvidersRepositoryV2.getProviders().values();
	}
}
