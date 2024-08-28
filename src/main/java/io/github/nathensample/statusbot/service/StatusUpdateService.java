package io.github.nathensample.statusbot.service;

import com.google.api.client.http.GenericUrl;
import io.github.nathensample.statusbot.config.ConfigLoader;
import io.github.nathensample.statusbot.exception.ResourceNotAvailableException;
import io.github.nathensample.statusbot.model.Status;
import java.io.IOException;
import java.time.Instant;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusUpdateService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdateService.class);
	private static final String DT_MESSAGE =
		"Server is currently unavailable due to daily maintenance. " +
		"Estimated downtime is: 10:00 to 11:00 (UTC) / 3AM to 4AM (PDT)";

	private final ChannelNotifierService channelNotifierService;
	private final DowntimeService downtimeService;
	private final StatusPollingService statusPollingService;
	private final ConfigLoader configLoader;

	private GenericUrl backendStatusUrl;

	private Status previousStatus = new Status("online", "Server is online.");
	private Status currentStatus = new Status("online", "Server is online.");

	public StatusUpdateService(@Autowired ChannelNotifierService channelNotifierService,
							   @Autowired DowntimeService downtimeService,
							   @Autowired StatusPollingService statusPollingService,
							   @Autowired ConfigLoader configLoader){
		this.channelNotifierService = channelNotifierService;
		this.downtimeService = downtimeService;
		this.statusPollingService = statusPollingService;
		this.configLoader = configLoader;

	}

	@PostConstruct
	public void init()
	{
		backendStatusUrl = new GenericUrl(configLoader.getBackendStatusStr());
	}

	@EventListener(ApplicationReadyEvent.class)
	private void forcePoll() throws IOException, ResourceNotAvailableException
	{
		updateStatus();
	}

	/**
	 * Poll the API and update status as required
	 * @return false if nothing changed, true if an update occurred
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when the Response returns an error code as defined by HttpResponseException
	 */
	private boolean updateStatus() throws IOException, ResourceNotAvailableException
	{
		Status newStatus;
		try
		{
			//Try backend first, it gives more up to date information but less pretty messages.
			newStatus = statusPollingService.getStatus(backendStatusUrl);
			if (newStatus.getStatus().equals("offline") && downtimeService.isDowntime(Instant.now()))
			{
				newStatus.setMessage(DT_MESSAGE);
			}
		}
		catch (ResourceNotAvailableException e)
		{
			if (downtimeService.isDowntime(Instant.now()))
			{
				newStatus = currentStatus.clone();
				newStatus.setMessage(DT_MESSAGE);
			}
			else
			{
				throw e;
			}
		}
		LOGGER.info("Polled albion for status: {}", newStatus);
		if (currentStatus.equals(newStatus))
		{
			return false;
		}
		previousStatus = currentStatus;
		currentStatus = newStatus;
		return true;
	}

	/**
	 * Every 5 seconds poll the available api's to check for status changes
	 * notify all subscribers if a status change is detected
	 * @throws IOException when an IO error around the creation/execution of HTTP resources occurs
	 * @throws ResourceNotAvailableException when the Response returns an error code as defined by HttpResponseException
	 */
	@Scheduled(cron = "0/5 * * * * ?")
	private void pollStatus() throws IOException, ResourceNotAvailableException
	{
		boolean updated = updateStatus();
		if (updated) {
			channelNotifierService.notifyChannels(currentStatus, previousStatus);
		}
	}
}
