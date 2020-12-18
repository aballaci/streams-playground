package com.ballaci.kstreams.config;

import com.ballaci.kstreams.model.OrderFull;
import com.ballaci.kstreams.model.OrderThin;
import com.ballaci.kstreams.model.UserData;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.streams.RecoveringDeserializationExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class MyStreamsConfig {

    private static final String ORDERS_TOPIC = "orders";

    @Bean
    public KStream<String, OrderThin> ordersStream(@Qualifier("myKStreamBuilder") StreamsBuilder kStreamBuilder) {
        return kStreamBuilder.stream(ORDERS_TOPIC);
    }

    @Bean
    public FactoryBean<StreamsBuilder> myKStreamBuilder(KafkaStreamsConfiguration streamsConfig) {
        return new StreamsBuilderFactoryBean(streamsConfig);
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-app");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, RecoveringDeserializationExceptionHandler.class);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String, OrderThin> kStream(@Qualifier("myKStreamBuilder") StreamsBuilder kStreamBuilder) {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, OrderThin> stream = builder.stream("orders");

//        KStream<String, String> joined = left.join(right,
//                (leftKey, leftValue) -> leftKey.length(), /* derive a (potentially) new key by which to lookup against the table */
//                (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue /* ValueJoiner */
//        );

        GlobalKTable<String, UserData> userData = builder.globalTable("user_data");

        stream
                .selectKey((key, value) -> value.getUserId())
                .join(userData,
                        (orderKey, userKey) -> orderKey,
                        (order, user) -> new OrderFull(order.getId(), order.getProductId(), user, order.getAmount()))
                .selectKey((key, value) -> value.getId())
                .to("enriched_orders");

        return stream;
    }
}