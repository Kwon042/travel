package com.example.travelProj.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
// 좋아요에 대한 사용자의 정보를 반환해주는
public class UserResponseDTO {

    private String nickname;
    private String profileImageUrl;
    private String email;

    public UserResponseDTO(String nickname, String profileImageUrl, String email) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
    }

}
