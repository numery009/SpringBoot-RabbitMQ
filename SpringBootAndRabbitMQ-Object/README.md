RabbitMQ – How to send/receive Java object messages with Spring RabbitMq | SpringBoot


In the previous posts, Spring RabbitMQ applications had produced/consumed String messages. Today, JavaSampleApproach will guide how to send/receive Java object messages with RabbitMQ server.


I. Technologies
– Java 8
– Maven 3.6.1
– Spring Tool Suite: Version 3.8.4.RELEASE
– Spring Boot: 1.5.4.RELEASE
– RabbitMQ

II. RabbitMq – Produce/consume Java object messages
In the tutorial, we create 2 SpringBoot applications {Producer, Consumer} for sending/receiving Java object messages to/from RabbitMQ:


1. Message Converter
We create a simple Java model:


public class Company {
    private String name;
    
    public Company(){}
    
    public Company(String name){
    	this.name = name;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public String getName(){
    	return this.name;
    }
}
Then send a Company object to RabbitMQ by segment code:


Company company = new Company("Apple");
amqpTemplate.convertAndSend(exchange, "", company);
And receiving it from Consumer by segment code:


@RabbitListener(queues="${jsa.rabbitmq.queue}")
public void recievedMessage(Company company) {
	System.out.println("Recieved Message: " + company);
}
-> Got Exception:


Bean [com.javasampleapproach.rabbitmq.consumer.Consumer@704f1591]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:135) ~[spring-rabbit-1.7.3.RELEASE.jar:na]
...

Caused by: org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException: Could not resolve method parameter at index 0 in public void com.javasampleapproach.rabbitmq.consumer.Consumer.recievedMessage(com.javasampleapproach.rabbitmq.model.Company): 1 error(s): [Error in object 'company': codes []; arguments []; default message [Payload value must not be empty]] 
	at org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver.resolveArgument(PayloadArgumentResolver.java:119) ~[spring-messaging-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    
How to resolve it? -> We need a MessageConverter for Producer and Consumer:


@Bean
public MessageConverter jsonMessageConverter(){
    return new Jackson2JsonMessageConverter();
}

– Producer: set MessageConverter for rabbitTemplate


public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
}

 
– Consumer: set MessageConverter for listenerContainerFactory


SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
factory.setMessageConverter(jsonMessageConverter());

2. Bi-Directional Java object messages
Now we change models with more complex structure – Bi-Directional relationship:

– Company:


public class Company {
    private String name;
 
    private List<Product> products;
	
    public Company(){
    }
	

	
	...
– Product


public class Product {
    private String name;
    
    private Company company;
	
    public Product(){
    }
    

Init a Company object as below:


Product iphone7 = new Product("Iphone 7");
Product iPadPro = new Product("IPadPro");
 
List<Product> appleProducts = new ArrayList<Product>(Arrays.asList(iphone7, iPadPro));
 
Company apple = new Company("Apple", appleProducts);
 
iphone7.setCompany(apple);
iPadPro.setCompany(apple);
 
List<Product> appleProducts = new ArrayList<Product>(Arrays.asList(iphone7, iPadPro));
 
Company apple = new Company("Apple", appleProducts);
 
iphone7.setCompany(apple);
iPadPro.setCompany(apple);
Then send a message to RabbitMQ again:


producer.produce(apple);

-> We got an Infinite recursion (StackOverflowError) exception:


Caused by: org.springframework.amqp.support.converter.MessageConversionException: Failed to convert Message content
	at org.springframework.amqp.support.converter.Jackson2JsonMessageConverter.createMessage(Jackson2JsonMessageConverter.java:212)
...
Caused by: com.fasterxml.jackson.databind.JsonMappingException: Infinite recursion (StackOverflowError) (through reference chain: com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"]->com.javasampleapproach.rabbitmq.model.Company["products"]->java.util.ArrayList[0]->com.javasampleapproach.rabbitmq.model.Product["company"])
	at com.fasterxml.jackson.databind.ser.std.BeanSerializerBase.serializeFields(BeanSerializerBase.java:706)
	at com.fasterxml.jackson.databind.ser.BeanSerializer.serialize(BeanSerializer.java:155)


Why? -> Beacause of the Bidirectional-Relationships between Company object and Product objects.

>>> See more at: Json Infinite Recursion problem

-> Solution:
We have can refer solutions at How to resolve Json Infinite Recursion problem when working with Jackson.

For preserving Bidirectional-Relationships when deserialize Json strings to Java objects, we can choose a solution with 

@JsonIdentityInfo, details as below code:

– Company

@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Company.class)
public class Company {
    private String name;
 
    private List<Product> products;
	
    public Company(){
    }
    

– Product


@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Product.class)
public class Product {
    private String name;
    
    private Company company;
	
    public Product(){
    }
    
III. Practice
We create 2 SpringBoot projects {Producer, Consumer}:

Spring RabbitMq - Send Java Objects - project structures


 
Step to do:
– Create SpringBoot projects
– Create Java models
– Create RabbitMq configuration
– Implement RabbitMq producer/consumer
– Implement sending client
– Run and check results

1. Create SpringBoot projects
Create 2 SpringBoot projects {Producer, Consumer}, then add dependency:


<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
</dependency>

2. Create Java models


– Company:

package com.javasampleapproach.rabbitmq.model;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
 
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Company.class)
public class Company {
    private String name;
 
    private List<Product> products;
	
    public Company(){
    }
    
    public Company(String name, List<Product> products){
    	this.name = name;
    	this.products = products;
    }
    
    // name
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // products
    public void setProducts(List<Product> products){
    	this.products = products;
    }
    
    public List<Product> getProducts(){
    	return this.products;
    }
 
	/**
	 * 
	 * Show Detail View
	 */
	public String toString(){
		JSONObject jsonInfo = new JSONObject();
		
		try {
			jsonInfo.put("name", this.name);
 
			JSONArray productArray = new JSONArray();
			if (this.products != null) {
				this.products.forEach(product -> {
					JSONObject subJson = new JSONObject();
					try {
						subJson.put("name", product.getName());
					} catch (JSONException e) {}
					
					productArray.put(subJson);
				});
			}
			jsonInfo.put("products", productArray);
		} catch (JSONException e1) {}
		return jsonInfo.toString();
	}
 
}

– Product


package com.javasampleapproach.rabbitmq.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
 
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = Product.class)
public class Product {
    private String name;
    
    private Company company;
	
    public Product(){
    }
    
    public Product(String name){
    	this.name = name;
    }
    
    public Product(String name, Company company){
    	this.name = name;
    	this.company = company;
    }
    
    // name
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // products
    public void setCompany(Company company){
    	this.company = company;
    }
    
    public Company getCompany(){
    	return this.company;
    }
}

3. Create RabbitMq Configuration
For each projects, open application.properties file, configure spring.rabbitmq.*:


spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
jsa.rabbitmq.queue=jsa.queue
logging.file=jsa-app.log

For logging in file, under /src/main/resources folder, create a logback-spring.xml file:


<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml" />
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}/}spring.log}"/>
    <include resource="org/springframework/boot/logging/logback/file-appender.xml" />
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>

3.1 Producer Config
– Create a RabbitMqConfig:


package com.javasampleapproach.rabbitmq.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
	
	@Value("${jsa.rabbitmq.queue}")
	String queueName;
	
	@Value("${jsa.rabbitmq.exchange}")
	String exchange;
	
	@Value("${jsa.rabbitmq.routingkey}")
	private String routingkey;
	
    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    DirectExchange exchange() {
    	return new DirectExchange(exchange);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingkey);
    }
	
	@Bean
	public MessageConverter jsonMessageConverter(){
	    return new Jackson2JsonMessageConverter();
	}
    
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
	    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
	    rabbitTemplate.setMessageConverter(jsonMessageConverter());
	    return rabbitTemplate;
	}
}

Note: we create 3 bean {Queue, DirectExchange, Binding} for automatically creating a direct RabbitMq exchange, a queue and binding them together.

3.2 Consumer Config
– Create a RabbitMqConfig:


package com.javasampleapproach.rabbitmq.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
	
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory jsaFactory(ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}

4. Implement RabbitMq Producer/Consumer
4.1 Implement Producer

package com.javasampleapproach.rabbitmq.producer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.javasampleapproach.rabbitmq.model.Company;

@Component
public class Producer {
	
	@Autowired
	private AmqpTemplate amqpTemplate;
	
	@Value("${jsa.rabbitmq.exchange}")
	private String exchange;
	
	@Value("${jsa.rabbitmq.routingkey}")
	private String routingkey;

	public void produce(Company company){
		amqpTemplate.convertAndSend(exchange, routingkey, company);
		System.out.println("Send msg = " + company);
	}
}

4.2 Consumer

package com.javasampleapproach.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.javasampleapproach.rabbitmq.model.Company;

@Component
public class Consumer {
	
	@RabbitListener(queues="${jsa.rabbitmq.queue}", containerFactory="jsaFactory")
    public void recievedMessage(Company company) {
        System.out.println("Recieved Message: " + company);
    }
}

5. Implement Sending Client
In SpringBoot main class, use CommandLineRunner to implement a client for producer:


package com.javasampleapproach.rabbitmq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.javasampleapproach.rabbitmq.model.Company;
import com.javasampleapproach.rabbitmq.model.Product;
import com.javasampleapproach.rabbitmq.producer.Producer;

@SpringBootApplication
public class SpringRabbitMqProducerApplication  implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(SpringRabbitMqProducerApplication.class, args);
	}
	
	@Autowired
	Producer producer;

	@Override
	public void run(String... args) throws Exception {
		/*
		 * Init Java objects
		 */
		Product iphone7 = new Product("Iphone 7");
		Product iPadPro = new Product("IPadPro");
		
		List<Product> appleProducts = new ArrayList<Product>(Arrays.asList(iphone7, iPadPro));
		
		Company apple = new Company("Apple", appleProducts);
		
		iphone7.setCompany(apple);
		iPadPro.setCompany(apple);
		
        /*
         * send message to RabbitMQ
         */
		producer.produce(apple);
	}
}

6. Run and check results
Build and run the SpringBoot projects {Producer, Consumer} with commandlines: mvn clean install, mvn spring-boot:run.

– Go to http://localhost:15672/#/exchanges/%2F/jsa.exchange
-> Checking the automatically creating and binding of RabbitMq exchange jsa.exchange and queue jsa.queue with routingKey jsa.routingkey:

Spring RabbitMq - Send Java Objects - exchange binding with queue

– Checking console logs:
-> From Producer: ‘Send msg = {"name":"Apple","products":[{"name":"Iphone 7"},{"name":"IPadPro"}]}‘
-> From Consumer: ‘Recieved Message: {"name":"Apple","products":[{"name":"Iphone 7"},{"name":"IPadPro"}]}‘
