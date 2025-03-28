package com.example.travelProj.domain.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String field;
    private String value;
}
