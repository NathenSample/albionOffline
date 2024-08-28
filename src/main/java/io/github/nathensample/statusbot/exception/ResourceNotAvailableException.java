package io.github.nathensample.statusbot.exception;

import org.jetbrains.annotations.NotNull;

public class ResourceNotAvailableException extends Exception
{
	private final int statusCode;
	private final String responseBody;

	public ResourceNotAvailableException(int statusCode, String responseBody)
	{
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public String getResponseBody()
	{
		return responseBody;
	}

	@NotNull
	@Override
	public String toString()
	{
		return "ResourceNotAvailableException{" +
			"statusCode='" + statusCode + '\'' +
			", responseBody='" + responseBody + '\'' +
			'}';
	}

}
