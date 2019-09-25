package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nathensample.statusbot.exception.ResourceNotAvailableException;
import io.github.nathensample.statusbot.model.Status;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;

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
	//TODO: Implement a HTTP service to unshit the api call logic
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdateService.class);
	private static final String DT_MESSAGE =
		"Server is currently unavailable due to daily maintenance." +
		"Estimated downtime is: 10:00 to 11:00 (UTC) / 3AM to 4AM (PDT)";

	private final ChannelNotifierService channelNotifierService;
	private final DowntimeService downtimeService;
	private StatusPollingService statusPollingService;
	private Status previousStatus = new Status("online", "All good.");
	private Status currentStatus = new Status("online", "All good.");

	public StatusUpdateService(@Autowired ChannelNotifierService channelNotifierService,
							   @Autowired DowntimeService downtimeService,
							   @Autowired StatusPollingService statusPollingService){
		this.channelNotifierService = channelNotifierService;
		this.downtimeService = downtimeService;
		this.statusPollingService = statusPollingService;
	}

	@EventListener(ApplicationReadyEvent.class)
	private void forcePoll() throws IOException, ResourceNotAvailableException
	{
		updateStatus();
	}

	/**
	 *
	 * @return false if nothing changed, true if an update occurred
	 * @throws IOException
	 */
	private boolean updateStatus() throws IOException, ResourceNotAvailableException
	{
		Status newStatus = statusPollingService.getStatus();
		LOGGER.info("Polled albion for status: {}", newStatus);
		// If the server is reporting as offline, and it's the time period in which we expect DT, add the rich message
		if (newStatus.getStatus().equalsIgnoreCase("offline") && downtimeService.isDowntime(Instant.now())) {
			newStatus.setMessage(DT_MESSAGE);
		}
		if (currentStatus.equals(newStatus))
		{
			return false;
		}
		previousStatus = currentStatus;
		currentStatus = newStatus;
		return true;
	}

	@Scheduled(cron = "0/5 * * * * ?")
	private void pollStatus() throws IOException, ResourceNotAvailableException
	{
		boolean updated = updateStatus();
		if (updated) {
			channelNotifierService.notifyChannels(currentStatus, previousStatus);
		}
	}
}
