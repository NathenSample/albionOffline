package io.github.nathensample.statusbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigLoader
{
	@Value("${BACKEND_STATUS_URL_STR:http://serverstatus.albiononline.com}")
	private String BACKEND_STATUS_URL_STR;

	@Value("${JENKINS_STATUS_URL_STR:http://live.albiononline.com/status.txt}")
	private String JENKINS_STATUS_URL_STR;

	public String getJenkinsStatusStr()
	{
		return JENKINS_STATUS_URL_STR;
	}

	public String getBackendStatusStr()
	{
		return BACKEND_STATUS_URL_STR;
	}
}
