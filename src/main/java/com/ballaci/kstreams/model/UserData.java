package com.ballaci.kstreams.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserData implements Serializable {

    private int id;
    private String userName;
}
