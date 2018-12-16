package com.rabbitmq.consumer.msg;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
	@RabbitListener(queues="${jsa.rabbitmq.queue}")
	public void receiveMessage(String msg){
		System.out.println("Received Message " + msg);
	}
}
