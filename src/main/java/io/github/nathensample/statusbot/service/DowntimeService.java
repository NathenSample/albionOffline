package io.github.nathensample.statusbot.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class DowntimeService
{
	private static final DateFormat DATE_FORMAT =  new SimpleDateFormat("HH:mm:ss");
	private static final String DT_START = "10:00:00";
	private static final String DT_END = "11:00:00";
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	private Calendar BEFORE = Calendar.getInstance();
	private Calendar AFTER = Calendar.getInstance();

	@PostConstruct
	void init() throws ParseException
	{
		Date beforeDate = DATE_FORMAT.parse(DT_START);
		Date afterDate = DATE_FORMAT.parse(DT_END);
		BEFORE.setTimeZone(UTC);
		AFTER.setTimeZone(UTC);
		BEFORE.setTime(beforeDate);
		AFTER.setTime(afterDate);
	}

	public boolean isDowntime(Instant instant)
	{
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset. UTC);
		int hour = ldt.getHour();
		return hour == 10;
	}
}
