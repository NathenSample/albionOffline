package io.github.nathensample.statusbot.listener;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GuildJoinListener extends ListenerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(GuildJoinListener.class);

	@Override
	public void onGuildJoin(GuildJoinEvent event)
	{
		logger.info("Joined new guild named {} with {} members visible", event.getGuild().getName(), event.getGuild().getMembers().size());
	}
}
