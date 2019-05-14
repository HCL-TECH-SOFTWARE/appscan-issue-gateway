package com.hcl.appscan.issuegateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.hcl.appscan.issuegateway.controller.ProvidersController;
import com.hcl.appscan.issuegateway.controller.PushJobController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private ProvidersController providersController;

	@Autowired
	private PushJobController pushJobController;

	@Test
	public void contextLoads() {
		assertThat(providersController).isNotNull();
		assertThat(pushJobController).isNotNull();
	}

}
