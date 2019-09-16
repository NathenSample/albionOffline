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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ChannelSubscriptionService
{
	//TODO: Unshitfuck the path logic

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSubscriptionService.class);

	private List<String> channelsToNotify = new ArrayList<>();

	public ChannelSubscriptionService() {
		try {
			File out = new File("./persistedChannels.properties");
			out.createNewFile();
			Stream<String> lines = Files.lines(Paths.get("./persistedChannels.properties"));
			channelsToNotify = lines.collect(Collectors.toList());
			LOGGER.info("Loaded {} channels", channelsToNotify.size());
		}
		catch (IOException e)
		{
			LOGGER.error("Failed to initialise server properly, no persisted channels", e);
		}
	}

	/**
	 *
	 * @param event event requesting the toggle
	 * @return false if disabled, true if enabled
	 * @throws IOException when it cant persist the status
	 */
	public boolean toggleChannel(GuildMessageReceivedEvent event) throws IOException
	{
		boolean enabled = false;
		String channelId = event.getChannel().getId();
		if (channelsToNotify.contains(channelId)) {
			channelsToNotify.remove(channelId);
		} else {
			channelsToNotify.add(channelId);
			enabled = true;
		}

		File out = new File("./persistedChannels.properties");
		Files.write(Paths.get(out.getPath()), channelsToNotify, Charset.defaultCharset());
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
