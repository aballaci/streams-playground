package com.ballaci.kstreams.config;

import com.ballaci.kstreams.model.Document;
import com.ballaci.kstreams.model.UserTags;
import com.ballaci.kstreams.serdes.CustomSerdes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsStateStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.SendTo;

@Slf4j
@Configuration
public class StreamsConfig {

    private static final String STATE_STORE_NAME = "tag-store";

    @StreamListener
    @SendTo({"documentApproval", "notRelevant"})
    public KStream<String, Document>[] route(@Input("documents") KStream<String, Document> documentKStream,
                                             @Input("userTags") GlobalKTable<String, UserTags> tagsGlobalKTable) {


        KStream<String, Document>[] stream = documentKStream
                .selectKey((key, document) -> document.getUserId())
                .join(tagsGlobalKTable,
                        (documentKey, userTagsKey) -> documentKey,
                        (document, userTags) -> {
                            document.setApprovalRelevant(isApprovalRelevant(document, userTags));
                            return document;
                        })
                .selectKey((documentKey, document) -> document.getId())
                .branch(
                        (key, document) -> document.isApprovalRelevant(),
                        (key, document) -> !document.isApprovalRelevant()
                );

        log.info("StoreName: {}",tagsGlobalKTable.queryableStoreName());
        return stream;

    }

    private boolean isApprovalRelevant(Document document, UserTags userTags) {
        return userTags.getTags().stream().anyMatch(t -> document.getTag().equals(t.getName()) && t.isRelevant());
    }

    @Bean
    public StoreBuilder myStore() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STATE_STORE_NAME), Serdes.String(),
                CustomSerdes.UserTagsSerde());
    }
}