package com.sheemab.linkedin.post_service.Configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic createPost(){
        return new NewTopic("post-created-topic",3 , (short) 1);
    }

    @Bean
    public NewTopic postLike(){
        return new NewTopic("post-liked-topic",3 , (short) 1);
    }

    @Bean
    public NewTopic comment(){
        return new NewTopic("post-comment-topic",3 , (short) 1);
    }
}
