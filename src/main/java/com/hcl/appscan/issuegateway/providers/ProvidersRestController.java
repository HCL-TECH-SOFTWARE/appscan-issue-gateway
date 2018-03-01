/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.providers;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import common.IProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "providers")
@RequestMapping("/providers")
public class ProvidersRestController {

	@Autowired
    public ProvidersRestController() {
    }
        
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value="Get configured issue service providers",notes="Returns all configured issue service providers. " +
    		"issue service providers are encouraged to also include documentation about their specific configuration requirements " +
    		"in the response to this API. Note:Due to some swagger limitations, such as the inability to handle formatting characters, you'll find that the response " + 
    		"is somewhat oddly formattted, but it should still be helpful")
	Collection<IProvider> readProviders() {
		return ProvidersRepository.getProviders(new ArrayList<String>()).values();
	}
     
}