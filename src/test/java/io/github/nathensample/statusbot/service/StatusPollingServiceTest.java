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
	//TODO: Test names could likely be improved
	@Mock
	private ConfigLoader MOCK_CONFIG_LOADER;
	private StatusPollingService statusPollingService;

	private static final String BACKEND = "http://serverstatus.albiononline.com";
	private static final String JENKINS = "http://live.albiononline.com/status.txt";

	@Before
	public void init()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		statusPollingService = new StatusPollingService(MOCK_CONFIG_LOADER, objectMapper);
		when(MOCK_CONFIG_LOADER.getBackendStatusStr()).thenReturn(BACKEND);
		when(MOCK_CONFIG_LOADER.getJenkinsStatusStr()).thenReturn(JENKINS);
		statusPollingService.init();
	}

	@Test
	public void getValidStatusWithNoNeedToQueryJenkins() throws IOException, ResourceNotAvailableException
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
		Status myStatus = statusPollingService.getStatus(transport);
		assertEquals("online", myStatus.getStatus());
		assertEquals("All good", myStatus.getMessage());

	}

	@Test
	public void getValidStatusDuringDTSoWeFallBackToJenkins() throws IOException, ResourceNotAvailableException
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
						if (url.equals(BACKEND))
						{
							response.setStatusCode(500);
						}
						else
						{
							response.setStatusCode(200);
							response.setContent("{ \"status\": \"online\", \"message\": \"A Test Value\" }\n");
						}
						return response;
					}
				};
			}
		};
		Status myStatus = statusPollingService.getStatus(transport);
		assertEquals("online", myStatus.getStatus());
		assertEquals("A Test Value", myStatus.getMessage());
	}

	@Test(expected = ResourceNotAvailableException.class)
	public void whenBothResourcesUnavailableThrowException() throws IOException, ResourceNotAvailableException
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
		statusPollingService.getStatus(transport);
	}

	@Test
	public void whenPrimaryServiceOnlineShouldNotFallBack() throws IOException, ResourceNotAvailableException
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
						if (url.equals(JENKINS))
						{
							response.setStatusCode(500);
						}
						else
						{
							response.setStatusCode(200);
							response.setContent("{ \"status\": \"online\", \"message\": \"A Test Value\" }\n");
						}
						return response;
					}
				};
			}
		};
		Status myStatus = statusPollingService.getStatus(transport);
		assertEquals("online", myStatus.getStatus());
		assertEquals("A Test Value", myStatus.getMessage());
	}
}