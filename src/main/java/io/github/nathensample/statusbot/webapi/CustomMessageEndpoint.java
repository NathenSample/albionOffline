package io.github.nathensample.statusbot.webapi;

import io.github.nathensample.statusbot.model.CustomMessage;
import io.github.nathensample.statusbot.service.ChannelNotifierService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomMessageEndpoint
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomMessageEndpoint.class);

	private final ChannelNotifierService channelNotifierService;

	public CustomMessageEndpoint(@Autowired ChannelNotifierService channelNotifierService) {
		this.channelNotifierService = channelNotifierService;
	}

	@RequestMapping(value = "/api/internal/discord/postmessage", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> customMessage(HttpServletRequest request,
												@RequestBody CustomMessage message) {
		LOGGER.info("Received custom message {}", message.getCustomMessage());

		channelNotifierService.sendMessageToChannels(message.getCustomMessage());
		return new ResponseEntity<>(HttpStatus.OK);
	}


}
