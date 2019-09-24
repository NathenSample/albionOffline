package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import io.github.nathensample.statusbot.config.ConfigLoader;
import io.github.nathensample.statusbot.exception.ResourceNotAvailableException;
import io.github.nathensample.statusbot.model.Status;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StatusPollingServiceTest
{
	@Mock
	private ConfigLoader MOCK_CONFIG_LOADER;
	private StatusPollingService statusPollingService;

	@Before
	public void init()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		statusPollingService = new StatusPollingService(MOCK_CONFIG_LOADER, objectMapper);
		when(MOCK_CONFIG_LOADER.getBackendStatusStr()).thenReturn("http://serverstatus.albiononline.com");
		when(MOCK_CONFIG_LOADER.getJenkinsStatusStr()).thenReturn("http://live.albiononline.com/status.txt");
		statusPollingService.init();
	}

	@Test
	public void getValidStatusWithNoNeedToQueryJenkins() throws IOException, ResourceNotAvailableException
	{
		HttpTransport transport = new MockHttpTransport() {
			@Override
			public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
				return new MockLowLevelHttpRequest() {
					@Override
					public LowLevelHttpResponse execute() throws IOException {
						MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
						response.setStatusCode(200);
						response.setContent("{ \"status\": \"online\", \"message\": \"All good.\" }\n");
						return response;
					}
				};
			}
		};
		Status myStatus = statusPollingService.getStatus(transport);
		assertEquals("online", myStatus.getStatus());
		assertEquals("All good", myStatus.getMessage());

	}
}