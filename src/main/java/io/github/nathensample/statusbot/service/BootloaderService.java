package io.github.nathensample.statusbot.service;

import io.github.nathensample.statusbot.listener.AdminChannelListener;
import java.util.Arrays;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/*
https://discordapp.com/oauth2/authorize?client_id=622384942755610624&scope=bot&permissions=3072
 */
@Service
public class BootloaderService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BootloaderService.class);


	private final AdminChannelListener adminChannelListener;
	private final DiscordService discordService;
	private final ChannelNotifierService channelNotifierService;

	private BootloaderService(@Autowired  DiscordService discordService,
							  @Autowired AdminChannelListener adminChannelListener,
							  @Autowired ChannelNotifierService channelNotifierService){
		this.discordService = discordService;
		this.adminChannelListener = adminChannelListener;
		this.channelNotifierService = channelNotifierService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initializeBot(){
		discordService.initializeBot(Collections.singletonList(adminChannelListener));
	}
}
