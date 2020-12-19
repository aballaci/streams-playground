package com.ballaci.kstreams.config;

import com.ballaci.kstreams.model.OrderFull;
import com.ballaci.kstreams.model.OrderThin;
import com.ballaci.kstreams.model.UserData;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.SendTo;

@Configuration
public class StreamsConfig {

    @StreamListener
    @SendTo("outputStream")
    public KStream<String, OrderFull> process(@Input("inputStream") KStream<String, OrderThin> orders,
                                              @Input("inputTable") GlobalKTable<String, UserData> users) {

        return orders
                .selectKey((key, value) -> value.getUserId())
                .join(users,
                        (orderKey, userDataKey) -> orderKey,
                        (order, userData) -> new OrderFull(order.getId(), order.getProductId(), userData, order.getAmount()))
                .selectKey((key, value) -> value.getId());

    }
}