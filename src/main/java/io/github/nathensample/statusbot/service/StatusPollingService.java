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
import java.util.regex.Pattern;
import static java.util.stream.Collectors.joining;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusPollingService
{
	private static final Pattern SANITY_FILTER = Pattern.compile("[^ a-zA-Z0-9{}:\",.]");
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

	/**
	 * Query the two configured resources
	 * @return latest status from the configured status urls ${JENKINS_STATUS_URL_STR} and ${BACKEND_STATUS_URL_STR}
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when we receive an unexpected status code (not 200 or 500)
	 * from the primary resource as well as from the fallback resource.
	 */
	public Status getStatus() throws IOException, ResourceNotAvailableException
	{
		return getStatus(new NetHttpTransport());
	}

	/**
	 * Invocation of getStatus() allowing for dependency injection
	 * @param transport HttpTransport to be used for the creation of HTTP requests via the Google HTTP lib.
	 * @return latest status from the configured status urls
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when we receive an unexpected status code (not 200 or 500)
	 * from the primary resource as well as from the fallback resource.
	 */
	Status getStatus(HttpTransport transport) throws IOException, ResourceNotAvailableException
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

	/**
	 * Uses the provided HttpTransport to generate a HttpRequest and perform a GET request upon the provided GenericUrl
	 * @param httpTransport HttpTransport to be used when generating a HttpRequest to perform a GET operation
	 * @param queryUrl Google API representation of the URL to perform the GET operation upon
	 * @return
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when the Response returns an error code as defined by HttpResponseException
	 */
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
			/*
				SBI serve data in weird ways (Including JSON through a .txt file) this tries to limit any insanity,
				as well as being a lazy way to remove the BOM from the .txt resource we expect.
				It could remove data from erroneous output from the API's but handles expected values fine.
			 */
			body = SANITY_FILTER.matcher(body).replaceAll("");
			return objectMapper.readValue(body, Status.class);
		}
		catch (HttpResponseException e)
		{
			throw new ResourceNotAvailableException(e.getStatusCode(), e.getContent());
		}
	}
}
