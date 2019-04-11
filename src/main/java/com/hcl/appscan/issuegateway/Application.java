/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
		
	public static void main(String[] args) {
		validateProviders();
		SpringApplication.run(Application.class, args);
		logger.debug("--Application Started--");
	}

	private static void validateProviders() {
		
		//check that the folder exists
		if (System.getProperty("providers.path") == null) {
			System.setProperty("providers.path", "./providers");
		}
		File providers = new File(System.getProperty("providers.path"));
		if (!providers.exists()) {
			System.out.println("Unable to find the providers path: " + providers.getAbsolutePath());
			System.exit(1);
		}
		
		File common = new File(providers, "common");
		if (!common.exists()) {
			System.out.println("There seems to be a problem with the providers path: " + providers.getAbsolutePath());
			System.out.println("Unable to find the 'common' folder");
			System.exit(1);
		}
	}
}
