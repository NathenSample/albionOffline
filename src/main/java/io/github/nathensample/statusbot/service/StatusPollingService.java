package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nathensample.statusbot.model.Status;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusPollingService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusPollingService.class);

	private ChannelNotifierService channelNotifierService;
	private Status previousStatus = new Status("First Run", "First Run");
	private Status currentStatus = new Status("First Run", "First Run");

	public StatusPollingService(ChannelNotifierService channelNotifierService){
		this.channelNotifierService = channelNotifierService;
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
		Status newStatus = objectMapper.readValue(new URL("https://live.albiononline.com/status.txt"), Status.class);
		LOGGER.info("Polled albion for status: {}", newStatus);
		if (newStatus.equals(currentStatus)) {
			return false;
		}
		previousStatus = currentStatus;
		currentStatus = newStatus;
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
