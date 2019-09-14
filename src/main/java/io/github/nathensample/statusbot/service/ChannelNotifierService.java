package io.github.nathensample.statusbot.service;

import io.github.nathensample.statusbot.model.Status;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelNotifierService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelNotifierService.class);

	private ChannelSubscriptionService channelSubscriptionService;
	private DiscordService discordService;

	public ChannelNotifierService(@Autowired DiscordService discordService,
								  @Autowired ChannelSubscriptionService channelSubscriptionService) {
		this.discordService = discordService;
		this.channelSubscriptionService = channelSubscriptionService;
	}

	public void notifyChannels(Status newStatus, Status oldStatus) {
		List<String> channelsToNotify = channelSubscriptionService.getChannelsToNotify();
		channelsToNotify.forEach(channelId -> {
			discordService.getJda().getTextChannelById(channelId).sendMessage(newStatus.prettyPrint(oldStatus)).queue();
		});
		LOGGER.info("Notified {} channels of the update to {}", channelsToNotify.size(), newStatus);
	}

	public void sendMessageToChannels(String message)
	{
		channelSubscriptionService.getChannelsToNotify().forEach(c -> discordService.getJda().getTextChannelById(c).sendMessage(message).queue());
	}
}
