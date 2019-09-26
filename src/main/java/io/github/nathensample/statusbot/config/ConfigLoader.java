package io.github.nathensample.statusbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigLoader
{
	@Value("${BACKEND_STATUS_URL_STR:http://serverstatus.albiononline.com}")
	private String BACKEND_STATUS_URL_STR;

	@Value("${discordToken}")
	private String DISCORD_TOKEN;

	public String getBackendStatusStr()
	{
		return BACKEND_STATUS_URL_STR;
	}

	public String getDiscordToken()
	{
		return DISCORD_TOKEN;
	}
}
