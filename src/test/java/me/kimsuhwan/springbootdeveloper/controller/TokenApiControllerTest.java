package me.kimsuhwan.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimsuhwan.springbootdeveloper.config.jwt.JwtFactory;
import me.kimsuhwan.springbootdeveloper.config.jwt.JwtProperties;
import me.kimsuhwan.springbootdeveloper.domain.RefreshToken;
import me.kimsuhwan.springbootdeveloper.domain.User;
import me.kimsuhwan.springbootdeveloper.dto.CreateAccessTokenRequest;
import me.kimsuhwan.springbootdeveloper.repository.RefreshTokenRepository;
import me.kimsuhwan.springbootdeveloper.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
 class TokenApiControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    User user;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setSecurityContest() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                        .provider("google")
                .build());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @DisplayName("createNewAccessToken: 새로운 액세스 토큰을 발급한다.")
    @Test
    public void createNewAccessToken() throws Exception {
        //given
        final String url = "/api/token";

        String refreshToken = createRefreshToken();

        refreshTokenRepository.save(new RefreshToken(user.getId(),refreshToken));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(request);

        //when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        //then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

    }

    @DisplayName("deleteRefreshToken : 리프레시 토큰을 삭제한다")
    @Test
    public void deleteRefreshToken() throws Exception{
        //given
        String url = "/api/refresh-token";

        String refreshToken = createRefreshToken();

        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, refreshToken, user.getAuthorities()));

        //when
        ResultActions resultActions = mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        //then
        resultActions
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.findByRefreshToken(refreshToken).isEmpty());

    }

    private String createRefreshToken() {
        return JwtFactory.builder()
                .claims(Map.of("id", user.getId()))
                .build()
                .createToken(jwtProperties);
    }

}
