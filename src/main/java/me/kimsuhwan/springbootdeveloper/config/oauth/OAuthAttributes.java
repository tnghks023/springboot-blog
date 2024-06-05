package me.kimsuhwan.springbootdeveloper.config.oauth;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public enum OAuthAttributes {

    GOOGLE("google", (attributes) -> {
        UserProfile userProfile = new UserProfile();
        userProfile.setNickname((String) attributes.get("name"));
        userProfile.setEmail((String) attributes.get("email"));
        return userProfile;
    }),
    GITHUB("github", (attributes) -> {
        UserProfile userProfile = new UserProfile();
        String name = (String)attributes.get("login");
        if((String) attributes.get("email") == null) {
            userProfile.setEmail(name);
        } else {
            userProfile.setEmail((String) attributes.get("email"));
        }
        userProfile.setNickname(name);

        return userProfile;
    }),;

    private final String registrationId;
    private final Function<Map<String, Object>, UserProfile> of;

    OAuthAttributes(String registrationId, Function<Map<String, Object>, UserProfile> of) {
        this.registrationId = registrationId;
        this.of = of;
    }

    public static UserProfile extract(String registrationId, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(provider -> registrationId.equals(provider.registrationId))
                .findFirst()
                .orElseThrow(IllegalAccessError::new)
                .of.apply(attributes);
    }
}
