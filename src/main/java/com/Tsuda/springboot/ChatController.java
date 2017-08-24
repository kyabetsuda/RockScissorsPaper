

package com.Tsuda.springboot;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

	@Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


	@SubscribeMapping(value="/{roomname}")
	public void MessageProcess(
			@DestinationVariable String roomname,
			Principal principal,
			String message){
		String processed = principal.getName() + " : " + message;
		simpMessagingTemplate.convertAndSend("/topic/" + roomname, processed);
	}

}
