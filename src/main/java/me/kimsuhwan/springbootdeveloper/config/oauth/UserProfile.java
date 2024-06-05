package me.kimsuhwan.springbootdeveloper.config.oauth;


import lombok.Getter;
import lombok.Setter;
import me.kimsuhwan.springbootdeveloper.domain.User;

@Getter
@Setter
public class UserProfile {

    private String email;
    private String provider;
    private String nickname;

    public User toUser() {
        return User.builder()
                .email(email)
                .provider(provider)
                .nickname(nickname)
                .build();
    }

}
