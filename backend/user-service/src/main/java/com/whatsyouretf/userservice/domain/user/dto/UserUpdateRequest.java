package com.whatsyouretf.userservice.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(min = 2, max = 50, message = "닉네임은 2~50자 사이여야 합니다.")
    private String nickname;
}
