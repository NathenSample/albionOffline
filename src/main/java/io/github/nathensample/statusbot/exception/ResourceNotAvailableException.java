package io.github.nathensample.statusbot.exception;

public class ResourceNotAvailableException extends Exception
{
	private final String statusCode;
	private final String responseBody;

	public ResourceNotAvailableException(String statusCode, String responseBody)
	{
		this.statusCode = statusCode;
		this.responseBody = responseBody;
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
