package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import io.github.nathensample.statusbot.exception.ResourceNotAvailableException;
import io.github.nathensample.statusbot.model.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.lineSeparator;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.joining;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusPollingService
{
	private static final String UA = "Christy Cloud | Server Status Discord Bot";
	private static final Pattern SANITY_FILTER = Pattern.compile("[^ a-zA-Z0-9{}:\",.]");
	private final ObjectMapper objectMapper;



	public StatusPollingService(@Autowired ObjectMapper objectMapper){
		this.objectMapper = objectMapper;
	}
	/**
	 * Uses a NetHttpTransport to generate a HttpRequest and perform a GET request upon the provided GenericUrl
	 * @param genericUrl Google API representation of the URL to perform the GET operation upon
	 * @return
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when the Response returns an error code as defined by HttpResponseException
	 */
	public Status getStatus(GenericUrl genericUrl) throws IOException, ResourceNotAvailableException
	{
		return getStatus(new NetHttpTransport(), genericUrl);
	}

	/**
	 * Uses the provided HttpTransport to generate a HttpRequest and perform a GET request upon the provided GenericUrl
	 * @param transport HttpTransport to be used when generating a HttpRequest to perform a GET operation
	 * @param genericUrl Google API representation of the URL to perform the GET operation upon
	 * @return
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when the Response returns an error code as defined by HttpResponseException
	 */
	Status getStatus(HttpTransport transport, GenericUrl genericUrl) throws IOException, ResourceNotAvailableException
	{
		{
			try
			{
				HttpRequestFactory requestFactory = transport.createRequestFactory();
				HttpRequest request = requestFactory.buildGetRequest(genericUrl);
				request.getHeaders().setUserAgent(UA);
				HttpResponse response = request.execute();
				if (response.getStatusCode() != 200)
				{
					throw new ResourceNotAvailableException(response.getStatusCode(), response.parseAsString());
				}
				BufferedReader myReader = new BufferedReader(new InputStreamReader(response.getContent(), StandardCharsets.UTF_8));
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
}
