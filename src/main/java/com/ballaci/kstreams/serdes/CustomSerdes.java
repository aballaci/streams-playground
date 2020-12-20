package com.ballaci.kstreams.serdes;


import com.ballaci.kstreams.model.Document;
import com.ballaci.kstreams.model.UserTags;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public final class CustomSerdes {

    public static Serde<Document> DocumentSerde() {
        return Serdes.serdeFrom(new JsonSerializer<Document>(),
                new JsonDeserializer<Document>(Document.class));
    }

    public static Serde<UserTags> UserTagsSerde() {
        return Serdes.serdeFrom(new JsonSerializer<UserTags>(),
                new JsonDeserializer<UserTags>(UserTags.class));
    }


}