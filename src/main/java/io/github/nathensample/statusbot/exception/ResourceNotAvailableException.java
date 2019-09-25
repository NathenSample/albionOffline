package io.github.nathensample.statusbot.exception;

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

	@Override
	public String toString()
	{
		return "ResourceNotAvailableException{" +
			"statusCode='" + statusCode + '\'' +
			", responseBody='" + responseBody + '\'' +
			'}';
	}

}
