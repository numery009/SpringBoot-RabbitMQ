package com.rabbitmq.producer.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rabbitmq.producer.msg.Producer;

@RestController
@RequestMapping("/send")
public class WebController {

	@Autowired
	Producer producer;

	@GetMapping("/msg")
	public String sendMsg(@RequestParam("msg") String msg) {
		producer.sendMessage(msg);
		System.out.println( "Message -- " + msg + " --- sent");
		return "message Send";
	}

}
