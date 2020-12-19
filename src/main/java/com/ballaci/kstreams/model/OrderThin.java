package com.ballaci.kstreams.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderThin {

    private String id;
    private String productId;
    private String userId;
    private int amount;

}
