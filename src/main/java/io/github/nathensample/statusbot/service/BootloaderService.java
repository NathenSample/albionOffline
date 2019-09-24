package io.github.nathensample.statusbot.service;

import io.github.nathensample.statusbot.listener.AdminChannelListener;
import io.github.nathensample.statusbot.listener.GuildJoinListener;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/*
https://discordapp.com/oauth2/authorize?client_id=622384942755610624&scope=bot&permissions=3072 - Main application
https://discordapp.com/oauth2/authorize?client_id=623092400738533378&scope=bot&permissions=3072 - Developer
 */
@Service
public class BootloaderService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BootloaderService.class);


	private final AdminChannelListener adminChannelListener;
	private final DiscordService discordService;
	private final GuildJoinListener guildJoinListener;

	private BootloaderService(@Autowired  DiscordService discordService,
							  @Autowired AdminChannelListener adminChannelListener,
							  @Autowired GuildJoinListener guildJoinListener){
		this.discordService = discordService;
		this.adminChannelListener = adminChannelListener;
		this.guildJoinListener = guildJoinListener;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initializeBot(){
		List<ListenerAdapter> listeners = new ArrayList<>();
		listeners.add(guildJoinListener);
		listeners.add(adminChannelListener);
		discordService.initializeBot(listeners);
	}
}
