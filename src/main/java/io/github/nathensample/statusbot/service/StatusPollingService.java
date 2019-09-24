package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nathensample.statusbot.model.Status;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusPollingService
{
	//TODO: Implement a HTTP service to unshit the api call logic
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusPollingService.class);

	private final ChannelNotifierService channelNotifierService;
	private final DowntimeService downtimeService;
	private Status previousStatus = new Status("First Run", "First Run");
	private Status currentStatus = new Status("First Run", "First Run");

	public StatusPollingService(@Autowired ChannelNotifierService channelNotifierService,
								@Autowired DowntimeService downtimeService){
		this.channelNotifierService = channelNotifierService;
		this.downtimeService = downtimeService;
	}

	@EventListener(ApplicationReadyEvent.class)
	private void forcePoll() throws IOException
	{
		updateStatus();
	}

	/**
	 *
	 * @return false if nothing changed, true if an update occurred
	 * @throws IOException
	 */
	private boolean updateStatus() throws IOException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		Status newStatus = objectMapper.readValue(new URL("http://serverstatus.albiononline.com"), Status.class);
		LOGGER.info("Polled albion for status: {}", newStatus);
		if (newStatus.equals(currentStatus)) {
			return false;
		}
		previousStatus = currentStatus;
		currentStatus = newStatus;
		if (currentStatus.getStatus().equalsIgnoreCase("offline") && downtimeService.isDowntime(Instant.now())) {
			Status getMessage = objectMapper.readValue(new URL("https://live.albiononline.com/status.txt"), Status.class);
			currentStatus.setMessage(getMessage.getMessage());
		}
		return true;
	}

	@Scheduled(cron = "0/5 * * * * ?")
	private void pollStatus() throws IOException
	{
		boolean updated = updateStatus();
		if (updated) {
			channelNotifierService.notifyChannels(currentStatus, previousStatus);
		}
	}
}
