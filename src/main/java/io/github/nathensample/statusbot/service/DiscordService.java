package io.github.nathensample.statusbot.service;

import io.github.nathensample.statusbot.config.ConfigLoader;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DiscordService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordService.class);

	private String discordToken;
	private ConfigLoader configLoader;
	private JDA jda;

	public DiscordService(@Autowired ConfigLoader configLoader)
	{
		this.configLoader = configLoader;
	}

	@PostConstruct
	public void init()
	{
		discordToken = configLoader.getDiscordToken();
	}

	public void initializeBot(List<ListenerAdapter> listeners) throws LoginException, InterruptedException
	{
		try
		{
			JDABuilder builder = new JDABuilder(AccountType.BOT);
			builder.setToken(discordToken);
			jda = builder.build();
			jda.awaitReady();
			listeners.forEach(jda::addEventListener);
			LOGGER.info("Finished registering listeners");
		} catch (LoginException | InterruptedException e){
			LOGGER.error("==EXCEPTION DURING STARTUP==", e);
			throw e;
		}
	}

	public JDA getJda() {
		return jda;
	}

	public void addListener(ListenerAdapter listener) {
		jda.addEventListener(listener);
	}

	@Scheduled(cron = "0 0/5 * * * ?")
	private void logInfo()
	{
		LOGGER.info("Currently broadcasting to {} guilds.", jda.getGuilds().size());
	}
}
