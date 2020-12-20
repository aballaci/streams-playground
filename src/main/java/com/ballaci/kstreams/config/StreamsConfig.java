package com.ballaci.kstreams.config;

import com.ballaci.kstreams.model.Document;
import com.ballaci.kstreams.model.UserTags;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.SendTo;

@Configuration
public class StreamsConfig {

    @StreamListener
    @SendTo({"documentApproval", "notRelevant"})
    public KStream<String, Document>[] route(@Input("documents") KStream<String, Document> documentKStream,
                                             @Input("userTags") GlobalKTable<String, UserTags> tagsGlobalKTable) {

        return documentKStream
                .selectKey((key, value) -> value.getUserId())
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

    }

    private boolean isApprovalRelevant(Document document, UserTags userTags) {
        return userTags.getTags().stream().anyMatch(t-> document.getTag().equals(t.getName()) && t.isRelevant());
    }
}