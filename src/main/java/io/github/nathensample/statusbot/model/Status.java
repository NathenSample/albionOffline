package io.github.nathensample.statusbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

//TODO: Fix tidyUpIrritatingNewLines(); laziness
public class Status
{
	private String status;
	private String message;

	public Status(@JsonProperty("status") String status, @JsonProperty("message") String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Status status1 = (Status) o;
		tidyUpIrritatingNewLines();
		status1.tidyUpIrritatingNewLines();
		return Objects.equals(status, status1.status) &&
			Objects.equals(message, status1.message);
	}

	@Override
	public int hashCode()
	{
		tidyUpIrritatingNewLines();
		return Objects.hash(status, message);
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private void tidyUpIrritatingNewLines() {
		this.status = this.status.replaceAll("\\n", " ");
		this.message = this.message.replaceAll("\\n", " ");
	}

	public MessageEmbed prettyPrint(Status oldStatus)
	{
		tidyUpIrritatingNewLines();

		SimpleDateFormat isoFormat = new SimpleDateFormat("hh:mm:ss dd MMMM yyyy zzzz");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String strDate = isoFormat.format(new Date());
		String cp = " | © Christy Cloud";

		EmbedBuilder eb = new EmbedBuilder();
		eb.setDescription("A change in server status has been detected!");
		eb.addField("Status", oldStatus.getStatus() + " -> " + this.status, false);
		eb.addField("Message", oldStatus.getMessage() + " -> " + this.message, false);
		eb.setFooter(String.format("%s %s", strDate, cp),  "https://avatars3.githubusercontent.com/u/9803552?s=460&v=4");
		eb.setAuthor("Status Monitor.", "https://github.com/NathenSample", "https://avatars3.githubusercontent.com/u/9803552?s=460&v=4");

		if (this.status.equalsIgnoreCase("online"))
		{
			eb.setImage("https://cdn.discordapp.com/attachments/493642125561430026/621292905281224724/Serverup10.gif");
		}
		if (this.status.equalsIgnoreCase("offline"))
		{
			eb.setImage("https://cdn.discordapp.com/attachments/493642125561430026/618036023934451712/BSGoffline.gif");
		}
		return eb.build();
	}

	@Override
	public String toString()
	{
		tidyUpIrritatingNewLines();
		return "Status{" +
			"status='" + status + '\'' +
			", message='" + message + '\'' +
			'}';
	}
}
