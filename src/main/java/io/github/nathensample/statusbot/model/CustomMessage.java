package io.github.nathensample.statusbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomMessage {
	@JsonProperty("message")
	private String customMessage;

	public CustomMessage(){

	}

	public String getCustomMessage()
	{
		return customMessage;
	}

	public void setCustomMessage(String customMessage)
	{
		this.customMessage = customMessage;
	}
}
