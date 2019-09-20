package io.github.nathensample.statusbot.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nathensample.statusbot.model.Status;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatusPollingService
{
	//TODO: Implement a HTTP service to unshit the api call logic
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
		Status newStatus = objectMapper.readValue(new URL("http://serverstatus.albiononline.com"), Status.class);
		LOGGER.info("Polled albion for status: {}", newStatus);
		if (newStatus.equals(currentStatus)) {
			return false;
		}
		previousStatus = currentStatus;
		currentStatus = newStatus;
		if (currentStatus.getStatus().equalsIgnoreCase("offline") && isItDTTime(new Date().getTime())) {
			Status getMessage = objectMapper.readValue(new URL("https://live.albiononline.com/status.txt"), Status.class);
			currentStatus.setMessage(getMessage.getMessage());
		}
		return true;
	}

	//TODO: Fix this method, add test
	private boolean isItDTTime(long currentTimeInMillis)
	{
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss");
			Date dtStart = formatter.parse("10:00:00");
			Date dtEnd = formatter.parse("11:00:00");
			Time now = new Time(currentTimeInMillis);

			LOGGER.info("Is it currently valid DT period? {}", now.after(dtStart) && now.before(dtEnd));
			return now.after(dtStart) && now.before(dtEnd);
		} catch (ParseException e)
		{
			LOGGER.error("Parse exception e: {}", e.getMessage(), e);
		}
		return false;
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
