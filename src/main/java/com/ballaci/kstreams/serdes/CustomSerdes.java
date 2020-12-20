package com.ballaci.kstreams.serdes;


import com.ballaci.kstreams.model.OrderFull;
import com.ballaci.kstreams.model.OrderThin;
import com.ballaci.kstreams.model.UserData;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public final class CustomSerdes {

    public static Serde<OrderThin> OrderThinSerde() {
        return Serdes.serdeFrom(new JsonSerializer<OrderThin>(),
                new JsonDeserializer<OrderThin>(OrderThin.class));
    }

    public static Serde<OrderFull> OrderFullSerde() {
        return Serdes.serdeFrom(new JsonSerializer<OrderFull>(),
                new JsonDeserializer<OrderFull>(OrderFull.class));
    }

    public static Serde<UserData> UserDataSerde() {
        return Serdes.serdeFrom(new JsonSerializer<UserData>(),
                new JsonDeserializer<UserData>(UserData.class));
    }


}