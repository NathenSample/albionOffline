package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import io.github.nathensample.statusbot.config.ConfigLoader;
import io.github.nathensample.statusbot.exception.ResourceNotAvailableException;
import io.github.nathensample.statusbot.model.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusPollingService
{
	public static final String UTF8_BOM = "\uFEFF";
	private ConfigLoader CONFIG_LOADER;
	private ObjectMapper objectMapper;

	private GenericUrl BACKEND_STATUS_URL;
	private GenericUrl JENKINS_STATUS_URL;

	public StatusPollingService(@Autowired ConfigLoader configLoader,
								@Autowired ObjectMapper objectMapper){
		this.CONFIG_LOADER = configLoader;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void init()
	{
		BACKEND_STATUS_URL = new GenericUrl(CONFIG_LOADER.getBackendStatusStr());
		JENKINS_STATUS_URL = new GenericUrl(CONFIG_LOADER.getJenkinsStatusStr());
	}

	public Status getStatus() throws IOException, ResourceNotAvailableException
	{
		return getStatus(new NetHttpTransport());
	}

	public Status getStatus(HttpTransport transport) throws IOException, ResourceNotAvailableException
	{
		//Query Backend for most up to date serverstatus.albionline.com
		//If it returns a 500, or offline, check jenkins .txt
		Status newStatus;
		try
		{
			 newStatus = queryGenericUrlForStatus(transport, BACKEND_STATUS_URL);
		} catch (ResourceNotAvailableException e) {
			if (e.getStatusCode() != 500)
			{
				//If it's not a 500 then it's unexpected so we rethrow.
				throw e;
			}
			//Backend URL returns 500 during DT
			//If this throws ResourceNotAvailable then we've got no way of getting the status
			newStatus = queryGenericUrlForStatus(transport, JENKINS_STATUS_URL);

		}
		return newStatus;
	}

	private Status queryGenericUrlForStatus(HttpTransport httpTransport, GenericUrl queryUrl) throws IOException, ResourceNotAvailableException
	{
		try
		{
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(queryUrl);
			HttpResponse response = request.execute();
			if (response.getStatusCode() != 200)
			{
				throw new ResourceNotAvailableException(response.getStatusCode(), response.parseAsString());
			}
			BufferedReader myReader = new BufferedReader(new InputStreamReader(response.getContent(), "UTF-8"));
			String body = myReader.lines().collect(joining(lineSeparator()));
			body = body.replaceAll("[^ a-zA-Z0-9{}:\",]", "");
			return objectMapper.readValue(body, Status.class);
		}
		catch (HttpResponseException e)
		{
			throw new ResourceNotAvailableException(e.getStatusCode(), e.getContent());
		}
	}
}
