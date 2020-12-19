package com.ballaci.kstreams.serdes;


import com.ballaci.kstreams.model.OrderFull;
import com.ballaci.kstreams.model.OrderThin;
import com.ballaci.kstreams.model.UserData;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public final class CustomSerdes {

    static public final class OrderThinSerde extends Serdes.WrapperSerde<OrderThin> {
        public OrderThinSerde() {
            super(new JsonSerializer<OrderThin>(),
                    new JsonDeserializer<OrderThin>(OrderThin.class));
        }
    }

    static public final class OrderFullSerde extends Serdes.WrapperSerde<OrderFull> {
        public OrderFullSerde() {
            super(new JsonSerializer<OrderFull>(),
                    new JsonDeserializer<OrderFull>(OrderFull.class));
        }
    }

    static public final class UserDataSerde extends Serdes.WrapperSerde<UserData> {
        public UserDataSerde() {
            super(new JsonSerializer<UserData>(),
                    new JsonDeserializer<UserData>(UserData.class));
        }
    }


    public static Serde<OrderThin> OrderThin() {
        return new OrderThinSerde();
    }

    public static Serde<OrderFull> OrderFullSerde() {
        return new CustomSerdes.OrderFullSerde();
    }

    public static Serde<UserData> UserDataSerde() {
        return new CustomSerdes.UserDataSerde();
    }


}