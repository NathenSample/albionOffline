package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
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
	private static final GenericUrl BACKEND_URL = new GenericUrl("http://serverstatus.albiononline.com");
	private StatusPollingService statusPollingService;

	@Before
	public void init()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		statusPollingService = new StatusPollingService(objectMapper);
	}

	@Test
	public void givenValidResponseShouldParseStatus() throws IOException, ResourceNotAvailableException
	{
		HttpTransport transport = new MockHttpTransport()
		{
			@Override
			public LowLevelHttpRequest buildRequest(String method, String url)
			{
				return new MockLowLevelHttpRequest()
				{
					@Override
					public LowLevelHttpResponse execute()
					{
						MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
						response.setStatusCode(200);
						response.setContent("{ \"status\": \"online\", \"message\": \"All good.\" }\n");
						return response;
					}
				};
			}
		};
		Status myStatus = statusPollingService.getStatus(transport, BACKEND_URL);
		assertEquals("online", myStatus.getStatus());
		assertEquals("All good.", myStatus.getMessage());

	}

	@Test(expected = ResourceNotAvailableException.class)
	public void givenInvalidResponseShouldThrowResourceNotAvailable() throws IOException, ResourceNotAvailableException
	{
		HttpTransport transport = new MockHttpTransport()
		{
			@Override
			public LowLevelHttpRequest buildRequest(String method, String url)
			{
				return new MockLowLevelHttpRequest()
				{
					@Override
					public LowLevelHttpResponse execute()
					{
						MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
						response.setStatusCode(500);
						return response;
					}
				};
			}
		};
		statusPollingService.getStatus(transport, BACKEND_URL);
	}
}