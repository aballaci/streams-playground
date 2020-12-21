package com.ballaci.kstreams.rest;

import com.ballaci.kstreams.model.UserTags;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.binder.kafka.streams.KafkaStreamsRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class TagsController {



//    private static final String STATE_STORE_NAME = "user_tags-STATE-STORE-0000000001";
    private static final String STATE_STORE_NAME = "mystore";

    private final InteractiveQueryService interactiveQueryService;

    public TagsController(InteractiveQueryService interactiveQueryService) {
        this.interactiveQueryService = interactiveQueryService;
    }

    @GetMapping("/tags/{id}")
    UserTags getUserTags(@PathVariable("id") String id) {
        ReadOnlyKeyValueStore<String, UserTags> keyValueStore = interactiveQueryService.getQueryableStore(STATE_STORE_NAME, QueryableStoreTypes.keyValueStore());
//        StreamsBuilderFactoryBean streamsBuilderFactoryBean = context.getBean("&stream-builder-route", StreamsBuilderFactoryBean.class);
//        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
//        ReadOnlyKeyValueStore<String, UserTags> keyValueStore =
//                kafkaStreams.store(StoreQueryParameters.fromNameAndType("STATE_STORE_NAME", QueryableStoreTypes.keyValueStore()));

        UserTags userTags = keyValueStore.get(id);

        return userTags;
    }
}