package com.ballaci.kstreams.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserTags implements Serializable {

    private String userId;
    private List<UserTag> tags;

}
