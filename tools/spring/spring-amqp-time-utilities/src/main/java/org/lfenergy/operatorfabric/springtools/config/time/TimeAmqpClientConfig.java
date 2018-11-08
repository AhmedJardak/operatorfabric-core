package org.lfenergy.operatorfabric.springtools.config.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 * Created on 29/06/18
 *
 * @author davibind
 */
@Configuration
@Slf4j
public class TimeAmqpClientConfig {

    final static String exchangeName = "timeExchange";

    @Bean
    Queue timeQueue(){
        return QueueBuilder.durable().autoDelete().build();
    }

    @Bean
    FanoutExchange timeExchange(){
        return (FanoutExchange) ExchangeBuilder.fanoutExchange(exchangeName).build();
    }

    @Bean
    Binding timeDataBinding(Queue timeQueue, FanoutExchange timeExchange){
        return BindingBuilder.bind(timeQueue).to(timeExchange);
    }

    @Bean
    TimeReceiver timeReceiver(ObjectMapper objectMapper){
        log.info("Now listening for time data");
        return new TimeReceiver(objectMapper);
    }

}
