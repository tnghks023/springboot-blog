package me.kimsuhwan.springbootdeveloper.config.oauth;

import lombok.RequiredArgsConstructor;
import me.kimsuhwan.springbootdeveloper.domain.User;
import me.kimsuhwan.springbootdeveloper.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();

        String userNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        UserProfile userProfile = OAuthAttributes.extract(registrationId, user.getAttributes());
        userProfile.setProvider(registrationId);
        saveOrUpdate(userProfile);
        return user;
    }

    private User saveOrUpdate(UserProfile userProfile) {
        String email = userProfile.getEmail();
        String name = userProfile.getNickname();
        String provider = userProfile.getProvider();

        User user = userRepository.findByEmailAndProvider(email, provider)
                .map(entity -> entity.update(name))
                .orElse(User.builder()
                        .email(email)
                        .nickname(name)
                        .provider(provider)
                        .build());

        return userRepository.save(user);
    }

}
