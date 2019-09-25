package io.github.nathensample.statusbot.listener;

import io.github.nathensample.statusbot.service.ChannelSubscriptionService;
import java.util.List;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminChannelListener extends ListenerAdapter
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminChannelListener.class);

	private final ChannelSubscriptionService channelSubscriptionService;

	private AdminChannelListener(@Autowired  ChannelSubscriptionService channelSubscriptionService){
		this.channelSubscriptionService = channelSubscriptionService;
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getMessage().getContentStripped().equalsIgnoreCase("@status toggle") && doesUserHaveRoles(event)) {
			boolean enabled = channelSubscriptionService.toggleChannel(event);
			String toggledTo = enabled ? "Service is now enabled." : "Service is now disabled.";
			event.getChannel().sendMessage(String.format("Toggling channel status subscription. %s", toggledTo)).queue();
		}
	}

	private boolean doesUserHaveRoles(GuildMessageReceivedEvent event)
	{
		List<Role> roles = event.getGuild().getRolesByName("channel admin", true);

		if (roles.size() != 0 )
		{
			Role channelAdmin = roles.get(0);
			if (event.getGuild().getMembersWithRoles(channelAdmin).contains(event.getMember())) {
				return true;
			}
		}

		if (event.getGuild().getOwner().equals(event.getMember())) {
			return true;
		}

		return false;
	}
}
