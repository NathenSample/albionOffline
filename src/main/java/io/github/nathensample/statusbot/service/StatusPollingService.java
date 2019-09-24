package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
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

		return queryGenericUrlForStatus(transport, JENKINS_STATUS_URL);
	}

	private Status queryGenericUrlForStatus(HttpTransport httpTransport, GenericUrl queryUrl) throws IOException, ResourceNotAvailableException
	{
		HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
		HttpRequest request = requestFactory.buildGetRequest(queryUrl);
		HttpResponse response = request.execute();
		if (response.getStatusCode() != 200)
		{
			throw new ResourceNotAvailableException(response.getStatusMessage(), response.parseAsString());
		}
		BufferedReader myReader = new BufferedReader(new InputStreamReader(response.getContent(), "UTF-8"));
		String body = myReader.lines().collect(joining(lineSeparator()));
		body = body.replaceAll("[^ a-zA-Z0-9{}:\",]", "");
		return objectMapper.readValue(body, Status.class);
	}
}
