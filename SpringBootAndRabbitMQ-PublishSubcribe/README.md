
RabbitMq – How to create Spring RabbitMq Publish/Subcribe pattern with SpringBoot

In the tutorial, JavaSampleApproach will guide how to create Spring RabbitMq Publish/Subcribe pattern by using fanout exchanges of RabbitMq and SpringBoot.

I. Spring RabbitMq Publish/Subcribe pattern
We create a Publisher, and 3 Subcribers and using fanout exchanges of RabbitMQ for create a Publish/Subcribe Pattern System:

Spring RabbitMq Publish–Subscribe pattern - architechture


 
Flow messages:
– Publisher will send messages to the fanout exchange.
– The fanout exchange routes messages to all of the queues that are bound to it and the routing key is ignored.
– Subcribers instances recieves messages from the queues.

II. Practice
RabbitMQ – How to create Spring RabbitMQ Producer/Consumer applications with SpringBoot
Technologies
– Java 8
– Maven 3.6.1
– Spring Tool Suite: Version 3.8.4.RELEASE
– Spring Boot: 1.5.4.RELEASE
– RabbitMQ

We create 2 SpringBoot projects {Publisher, Subcriber}:

Spring RabbitMq Publish–Subscribe pattern - project structures

Step to do:
– Create SpringBoot projects
– Create Publiser/Subcriber
– Setup RabbitMQ exchange, queues
– Run and check results

1. Create SpringBoot projects
Using Spring Tool Suite, create 2 Spring Starter Projects then add amqp dependency for both of them:


<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

– With SpringRabbitMqPubliser we add more web dependency for creating RestController:

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

2. Create Publiser/Subcriber

2.1 Create Publisher
– Add RabbitMq configuration:


spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
jsa.rabbitmq.exchange=jsa.fanout

– Implement RabbitMq Publisher:


package com.javasampleapproach.rabbitmq.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Publisher {
	
	@Autowired
	private AmqpTemplate amqpTemplate;
	
	@Value("${jsa.rabbitmq.exchange}")
	private String exchange;
	
	public void produceMsg(String msg){
		amqpTemplate.convertAndSend(exchange, "",msg);
		System.out.println("Send msg = " + msg);
	}
}

 
– Implement a sending restApi:


package com.javasampleapproach.rabbitmq.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.rabbitmq.publisher.Publisher;

@RestController
public class WebController {
	
	@Autowired
	Publisher publisher;
	
	@RequestMapping("/send")
	public String sendMsg(@RequestParam("msg")String msg){
		publisher.produceMsg(msg);
		return "Done";
	}
}

2.2 Create Subcriber
– Add RabbitMq configuration:


spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
jsa.rabbitmq.queue=jsa.queue.1

– Create RabbitMq Subcriber:


package com.javasampleapproach.rabbitmq.subcriber;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Subcriber {

	@RabbitListener(queues="${jsa.rabbitmq.queue}")
    public void recievedMessage(String msg) {
        System.out.println("Recieved Message: " + msg);
    }
}

3. Setup RabbitMQ exchange, queues
Enable rabbitmq_management by cmd: rabbitmq-plugins enable rabbitmq_management --online
Go to: http://localhost:15672. Then login with user/password: guest/guest.

3.1 Add an exchange
Create a fanout exchange: jsa.fanout

Spring RabbitMq Publish–Subscribe pattern - create exchanges

3.2 Add queues
Create 3 RabbitMq queues {jsa.queue.1, jsa.queue.2, jsa.queue.3}:

Spring RabbitMq Publish–Subscribe pattern - create 3 queues

Binding all above queues with the fanout exchange jsa.fanout:

Spring RabbitMq Publish–Subscribe pattern - binding queue with exchanges - exchange sige

RabbitMQ – How to send/receive Java object messages with Spring RabbitMq | SpringBoot
4. Run and check results
Build 2 SpringBoot projects {Publisher, Subcriber} with the commandline: mvn clean install.
Run Publisher with commandline: mvn spring-boot:run, then send a message by a request: localhost:8080/send?msg=Hello World!

-> See status of the Queues:

Spring RabbitMq Publish–Subscribe pattern - queue status after send first message

Run 3 Subcriber instances which different configured values of jsa.rabbitmq.queue: {jsa.queue.1, jsa.queue.2, jsa.queue.3}.
See console logs of each Subcriber instances, we got the same message: ‘Recieved Message: Hello World!’.

-> Again, check status of the queues:

Spring RabbitMq Publish–Subscribe pattern - consumer messages

-> All messages has been consumed by Subcribers.

