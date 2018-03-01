/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway;
import java.util.Locale;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class Messages {
	
    private ReloadableResourceBundleMessageSource messageSource;
    
    public String getMessage(String id) {
    	String message = null;
    	try {
    		message = getMessageSource().getMessage(id,null,LocaleContextHolder.getLocale());
    	} catch (NoSuchMessageException e) {
    		message = getMessageSource().getMessage(id,null,Locale.ROOT);
    	}
        return message;
    }
    
	public ReloadableResourceBundleMessageSource getMessageSource() {
		if (messageSource == null) {
			messageSource = new ReloadableResourceBundleMessageSource();
		    messageSource.setBasename("classpath:/i18n/messages");
		    messageSource.setCacheMillis(5000);
		}
	    return messageSource;
	}
}