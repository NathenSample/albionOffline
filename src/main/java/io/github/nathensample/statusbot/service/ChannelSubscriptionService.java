package io.github.nathensample.statusbot.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ChannelSubscriptionService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSubscriptionService.class);
	private final SqlLiteService sqlLiteService;

	private List<String> channelsToNotify = new ArrayList<>();

	public ChannelSubscriptionService(@Autowired SqlLiteService sqlLiteService) {
		this.sqlLiteService = sqlLiteService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void populateChannelsFromDb() {
		LOGGER.info("Attempting to load channels from DB");
		channelsToNotify = sqlLiteService.loadChannelIdsFromDatabase();
		LOGGER.info("Loaded {}", channelsToNotify);
	}

	/**
	 *
	 * @param event event requesting the toggle
	 * @return false if disabled, true if enabled
	 * @throws IOException when it cant persist the status
	 */
	public boolean toggleChannel(GuildMessageReceivedEvent event)
	{
		boolean enabled = false;
		String channelId = event.getChannel().getId();
		if (channelsToNotify.contains(channelId)) {
			channelsToNotify.remove(channelId);
			sqlLiteService.removeChannelFromDatabase(channelId);
			LOGGER.info("Removed channel {}", channelId);
		} else {
			channelsToNotify.add(channelId);
			sqlLiteService.persistChannelToDatabase(channelId);
			LOGGER.info("added channel {}", channelId);
			enabled = true;
		}
		return enabled;
	}

	public List<String> getChannelsToNotify() {
		return channelsToNotify;
	}

	@Scheduled(cron = "0 0/5 * * * ?")
	private void logInfo()
	{
		LOGGER.info("Currently broadcasting to {} channels.", channelsToNotify.size());
	}
}
