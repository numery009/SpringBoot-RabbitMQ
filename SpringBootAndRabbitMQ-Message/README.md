
I. SpringBoot RabbitMQ
In the tutorial, we create 2 SpringBoot applications {Producer, Consumer} for working with RabbitMQ:
– Producer will send messages to RabbitMQ Exchanges with a routingKey. RabbitMQ uses routingKey to determine which queues for routing messages.
– Consumer listens on a RabbitMQ Queue to receive messages.

With SpringBoot, we use spring.rabbitmq.* for controlling RabbitMQ configuration:


spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

Producer uses AmqpTemplate to send messages:

@Component
public class Producer {
	
	@Autowired
	private AmqpTemplate amqpTemplate;
	
	public void produceMsg(String msg){
		amqpTemplate.convertAndSend(exchange, routingKey, msg);
	}
}

Consumer uses @RabbitListener to recieve messages:

@Component
public class Consumer {
 
	@RabbitListener(queues="${jsa.rabbitmq.queue}")
    public void recievedMessage(String msg) {
        // to do
    }
}

II. Practice
RabbitMq – How to create Spring RabbitMq Publish/Subcribe pattern with SpringBoot
We create 2 SpringBoot projects {Producer, Consumer}:

Spring rabbitmq producer/consumer application - structure of projects

Step to do:
– Create SpringBoot projects
– Create Producer/Consumer
– Setup RabbitMQ exchange, queue
– Run and check results

1. Create SpringBoot projects
Create 2 SpringBoot projects {Producer, Consumer}, then add dependency:


<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

With SpringRabbitMqProducer we add more web for creating RestController:


 

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

2. Create Producer/Consumer

2.1 Create Producer
– Add RabbitMq configuration:


spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
jsa.rabbitmq.exchange=jsa.direct
jsa.rabbitmq.routingkey=jsa.routingkey

– Create RabbitMq Producer:


package com.javasampleapproach.rabbitmq.producer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Producer {
	
	@Autowired
	private AmqpTemplate amqpTemplate;
	
	@Value("${jsa.rabbitmq.exchange}")
	private String exchange;
	
	@Value("${jsa.rabbitmq.routingkey}")
	private String routingKey;
	
	public void produceMsg(String msg){
		amqpTemplate.convertAndSend(exchange, routingKey, msg);
		System.out.println("Send msg = " + msg);
	}
}

– Implement a sending restApi:


package com.javasampleapproach.rabbitmq.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javasampleapproach.rabbitmq.producer.Producer;

@RestController
public class WebController {
	
	@Autowired
	Producer producer;
	
	@RequestMapping("/send")
	public String sendMsg(@RequestParam("msg")String msg){
		producer.produceMsg(msg);
		return "Done";
	}
}

2.2 Create Consumer
– Add RabbitMq configuration


spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
jsa.rabbitmq.queue=jsa.queue

– Create RabbitMq Consumer:


package com.javasampleapproach.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

	@RabbitListener(queues="${jsa.rabbitmq.queue}")
    public void recievedMessage(String msg) {
        System.out.println("Recieved Message: " + msg);
    }
}

3. Setup RabbitMQ Exchange, Queue
Enable rabbitmq_management by cmd: rabbitmq-plugins enable rabbitmq_management --online
Go to: http://localhost:15672. Then login with user/password: guest/guest

rabbit monitoring overview

3.1 Add exchange
Go to http://localhost:15672/#/exchanges.
– Add exchange: jsa.direct

rabbitmq - add exchange

3.2 Add queue
Go to http://localhost:15672/#/queues.
– Add a queue: jsa.queue

rabbitmq - create a queue

Binding the queue with above exchange:

binding queue with exchange

4. Run and check results
Build and run 2 SpringBoot projects with commandlines {mvn clean install, mvn spring-boot:run}.
With the SpringBoot Producer, send a message: http://localhost:8080/send?msg=Hello World!

At the SpringBoot Subcriber, we will get a console message: Recieved Message: Hello World!

Check connections -> go to http://localhost:15672/#/connections:


 
rabbit connections

RabbitMQ – How to send/receive Java object messages with Spring RabbitMq | SpringBoot
