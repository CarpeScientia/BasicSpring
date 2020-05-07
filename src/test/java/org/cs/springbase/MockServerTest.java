package org.cs.springbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import MainApplicationTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.util.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Keep an eye on the org.junit and the org.junit.jupiter differences
 */
@SpringBootTest(classes = {MainApplicationTest.class})
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class MockServerTest {
	public static final int PORT = 2080;

	@Autowired
	private MockMvc mvc;

	private ClientAndServer mockServer;


	private final ObjectMapper mapper = new ObjectMapper();
	private final ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();


	@Before
	public void setup() throws IOException {
		mockServer = startClientAndServer(PORT);

		mockServercreateExpectationCsvFile();
	}
	@After
	public void stopMockServer() {
		mockServer.stop();
	}

	private void mockServercreateExpectationCsvFile() throws IOException {
		new MockServerClient("127.0.0.1", PORT)
				.when(
						request()
								.withMethod("GET")
								.withPath("/download")
						//.withHeader("\"Content-type\", \"application/json\"")
						//.withBody(exact("{username: 'foo', password: 'bar'}")),
						,exactly(1))
				.respond(
						response()
								.withStatusCode(200)
								.withHeaders(
										new Header("Content-Type", "text/csv; charset=utf-8")
										//,Header("Cache-Control", "public, max-age=86400")
								)
								.withBody("TestBytes".getBytes(StandardCharsets.UTF_8))
								.withDelay(TimeUnit.SECONDS, 1)
				);
	}

	private <T> T doGet( String path, Class<T> clazz ) throws Exception {
		String respString = mvc.perform(get(path))
				.andExpect(status().isOk() )
				.andReturn()
				.getResponse()
				.getContentAsString();
		return mapper.readValue(respString, clazz);
	}

	private String postTo(String requestJson, String path) throws Exception {
		return mvc.perform(
				post(path)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson)
		).andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse()
				.getContentAsString();
	}

	public String sendJsonObject(Object request, String path, Object header1Value, Object header2Value) throws Exception {
		String responseString = mvc.perform(
				post(path)
						.contentType(MediaType.APPLICATION_JSON)
						.content(ow.writeValueAsString(request))
						.header("HEADER1" , header1Value)
						.header("HEADER2", header2Value )
		)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		//System.out.println(responseString);
		return responseString;
	}

	@Disabled
	@Test
	public void testNothing() {

	}
	@Test
	@Ignore("nothing here yet")
	public void testSomething() throws Exception {
		//TODO
	}

	public String throwIfNotNullOrTheSame(String newValue, String oldValue){
		if (newValue != null && !newValue.equals(oldValue)) {
			fail("value is not constant");
		}
		return newValue;
	}

}
