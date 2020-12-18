package com.ballaci.kstreams.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
@ToString
@AllArgsConstructor
public class UserData implements Serializable {

    private int id;
    private String userName;
}
