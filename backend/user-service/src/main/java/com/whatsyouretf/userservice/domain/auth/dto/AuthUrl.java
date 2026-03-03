package com.whatsyouretf.userservice.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthUrl {
    String authUrl;

    public static AuthUrl of(String authUrl) {
        AuthUrl url = new AuthUrl();
        url.authUrl = authUrl;
        return url;
    }
}
