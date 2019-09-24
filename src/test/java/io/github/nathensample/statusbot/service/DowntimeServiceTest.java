package io.github.nathensample.statusbot.service;

import java.text.ParseException;
import java.time.Instant;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class DowntimeServiceTest
{

	@InjectMocks
	private DowntimeService downtimeService;

	@Before
	public void setup() throws ParseException
	{
		downtimeService.init();
	}

	@Test
	public void givenValidDTShouldReturnTrue()
	{
		//Sun Sep 29 2019 10:45:01
		assertTrue(downtimeService.isDowntime(Instant.ofEpochMilli(1569753901893L)));
	}

	@Test
	public void givenInvalidDtShouldReturnFalse()
	{
		//Tue Sep 24 2019 22:38:11
		assertFalse(downtimeService.isDowntime(Instant.ofEpochMilli(1569364688954L)));
	}
}