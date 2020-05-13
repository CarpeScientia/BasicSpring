package org.cs.springbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cs.springbase.services.RestTemplateService;
import org.cs.springbase.services.interfaces.PowerOfAttorneyServiceInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockserver.cli.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {MainApplicationTest.class, MockServiceTest.TestConfig.class})
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@MockBeans({@MockBean(PowerOfAttorneyServiceInterface.class)
		//,@MockBean(SomeotherService.class)
})
public class MockServiceTest {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private RestTemplateService restTemplate;

	private static final Logger LOG = LogManager.getLogger(MockServiceTest.class);
	private static final String someParam = UUID.randomUUID().toString();
	private static final String dateStr = "2020-01-01";//should match date
	private final ObjectMapper mapper = new ObjectMapper();
	private static final Object OBJECT = new Object();

	@TestConfiguration
	public static class TestConfig {


		@Bean
		@Primary
		@Qualifier("httpRestTemplate")
		public RestTemplateService restTemplate2() {
			RestTemplateService restTemplateService = Mockito.mock(RestTemplateService.class);
			Mockito.when(restTemplateService
					.getMethod(eq(someParam)))
					.thenReturn(OBJECT);
			Mockito.when(restTemplateService
					.postMethod(any())).thenReturn(OBJECT);
			return restTemplateService;
		}
	}


	@Test
	public void test(){
		assertEquals(OBJECT, restTemplate.getMethod(someParam));
		assertEquals(OBJECT, restTemplate.postMethod(""));
	}
}
