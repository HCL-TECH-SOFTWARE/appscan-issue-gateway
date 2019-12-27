/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.providers;

import common.IProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

// For now, when a new provider is added the service would need to be restarted. 
// Would want to avoid that with perhaps a directoryWatcher + reload function here
public class ProvidersRepository {

	private ProvidersRepository() {}

	private static Map<String, IProvider> providers = null;

	private static final Logger logger = LoggerFactory.getLogger(ProvidersRepository.class);

	public static synchronized Map<String, IProvider> getProviders() {
		if (providers == null) {
			providers = new HashMap<>();
//			File providersRoot = new File(System.getProperty("providers.path", "."));
//			for (File providerPath : getSubFolders(providersRoot)) {
//				File providerGroovy = getFirstProvider(providerPath);
//				if (providerGroovy == null) {
//					continue;
//				}
//				try (GroovyClassLoader classLoader = new GroovyClassLoader()) {
//					classLoader.addClasspath(providersRoot.getAbsolutePath());
//					IProvider provider = (IProvider) classLoader.parseClass(providerGroovy).newInstance();
//					providers.put(provider.getId(), provider);
//				} catch (Exception e) {
//					logger.error("Internal Server Error while loading providers", e);
//				}
//			}

			for (IProvider provider : ServiceLoader.load(IProvider.class)) {
				providers.put(provider.getId(), provider);
			}
		}
		return providers;
	}

	private static File[] getSubFolders(File parent) {
		return parent.listFiles((current, name) -> new File(current, name).isDirectory() && !name.equals("common"));
	}

	private static File getFirstProvider(File parent) {
		File[] children = parent.listFiles((current, name) ->
				name.endsWith("Provider.groovy") && new File(current, name).isFile());
		if (children.length > 0) {
			return children[0];
		}
		return null;
	}
}
