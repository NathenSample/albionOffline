package io.github.nathensample.statusbot.service;

import java.util.List;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DiscordService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DiscordService.class);

	@Value("${discordToken}")
	private String discordToken;

	private JDA jda;

	public void initializeBot(List<ListenerAdapter> listeners){
		try
		{
			JDABuilder builder = new JDABuilder(AccountType.BOT);
			builder.setToken(discordToken);
			jda = builder.build();
			jda.awaitReady();
			listeners.forEach(jda::addEventListener);
		} catch (LoginException | InterruptedException e){
			LOGGER.error("exception: ", e);
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
