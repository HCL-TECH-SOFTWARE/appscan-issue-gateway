/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

//	private static final String PROVIDERS_PATH = "providers.path";
	
//    private static final Logger logger = LoggerFactory.getLogger(Application.class);
		
	public static void main(String[] args) {
//		validateProviders();
		SpringApplication.run(Application.class, args);
//		logger.debug("--Application Started--");
	}

//	private static void validateProviders() {
//
//		//check that the folder exists
//		if (System.getProperty(PROVIDERS_PATH) == null) {
//			System.setProperty(PROVIDERS_PATH, "./providers");
//		}
//		File providers = new File(System.getProperty(PROVIDERS_PATH));
//		if (!providers.exists()) {
//			logger.error("Unable to find the providers path: " + providers.getAbsolutePath());
//			System.exit(1);
//		}
//
//		File common = new File(providers, "common");
//		if (!common.exists()) {
//			logger.error("There seems to be a problem with the providers path: " + providers.getAbsolutePath());
//			logger.error("Unable to find the 'common' folder");
//			System.exit(1);
//		}
//	}
}
