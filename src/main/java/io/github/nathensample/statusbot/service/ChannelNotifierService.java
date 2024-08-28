package io.github.nathensample.statusbot.service;

import io.github.nathensample.statusbot.model.Status;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelNotifierService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelNotifierService.class);

	private final ChannelSubscriptionService channelSubscriptionService;
	private final DiscordService discordService;

	public ChannelNotifierService(@Autowired DiscordService discordService,
								  @Autowired ChannelSubscriptionService channelSubscriptionService) {
		this.discordService = discordService;
		this.channelSubscriptionService = channelSubscriptionService;
	}

	public void notifyChannels(@NotNull Status newStatus, @NotNull Status oldStatus) {
		List<String> toBeRemoved = new ArrayList<>();
		List<String> channelsToNotify = channelSubscriptionService.getChannelsToNotify();
		channelsToNotify.forEach(channelId -> {
			TextChannel channel = discordService.getJda().getTextChannelById(channelId);
			if (channel != null) {
				channel.sendMessageEmbeds(newStatus.prettyPrint(oldStatus)).queue();
			} else {
				LOGGER.error("Removing ID {} was unable to retrieve it.", channelId);
				toBeRemoved.add(channelId);
			}
		});
		toBeRemoved.forEach(r-> channelSubscriptionService.getChannelsToNotify().remove(r));
		LOGGER.info("Notified {} channels of the update to {}", channelsToNotify.size(), newStatus);
	}

	public void sendMessageToChannels(@NotNull String message)
	{
		List<String> toBeRemoved = new ArrayList<>();
		channelSubscriptionService.getChannelsToNotify().forEach(c -> {
			TextChannel channelToNotify = discordService.getJda().getTextChannelById(c);
			if (channelToNotify != null) {
				channelToNotify.sendMessage(message).queue();
			}else {
				LOGGER.error("Removing ID {} was unable to retrieve it.", c);
				toBeRemoved.add(c);
			}
		});
		toBeRemoved.forEach(r-> channelSubscriptionService.getChannelsToNotify().remove(r));
	}
}
