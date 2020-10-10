package com.example.appname.integ;

import com.example.appname.model.AppUser;
import com.example.appname.model.SignupRequest;
import com.example.appname.model.TokenRequest;
import com.example.appname.model.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class SecurityIntegTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    public void shouldRestrictAccess() throws Exception {
        this.mockMvc.perform(get("/hello"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldRegisterEndToEnd() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        val user = SignupRequest.builder()
                .email("testemail@email.com")
                .password("Testpassword1")
                .build();

        String json = mapper.writeValueAsString(user);

        TokenRequest wrongCreds = TokenRequest.builder()
                .email(user.getEmail())
                .password(user.getPassword()+"1")
                .build();

        String wrongCredsJson = mapper.writeValueAsString(wrongCreds);
        wrongCreds.setPassword(user.getPassword());
        String rightCredsJson = mapper.writeValueAsString(wrongCreds);

        MvcResult signupResult = this.mockMvc.perform(
                post("/api/v1/signup")
                        .content(json)
                        .contentType("application/json")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        AppUser signupUser = mapper.readValue(
                signupResult.getResponse().getContentAsString(),
                AppUser.class
        );

        mockMvc.perform(post("/api/v1/authenticate").content(wrongCredsJson).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        MvcResult result = mockMvc.perform(post("/api/v1/authenticate").content(rightCredsJson).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(signupUser.getId()))
                .andReturn();

        TokenResponse tokenResponse = mapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);

        MockHttpServletRequestBuilder req = get("/hello")
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken());

        mockMvc.perform(req).andDo(print()).andExpect(status().isOk());

        this.mockMvc.perform(get("/hello"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldRejectBasicPassword() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        val user = SignupRequest.builder()
                .email("testemail@email.com")
                .password("testpassword1") // no caps
                .build();

        String json = mapper.writeValueAsString(user);

        this.mockMvc.perform(
                post("/api/v1/signup")
                        .content(json)
                        .contentType("application/json")
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void shouldRefreshToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        val user = SignupRequest.builder()
                .email("testemail@email.com")
                .password("Testpassword1")
                .build();

        String json = mapper.writeValueAsString(user);

        val signupResult = this.mockMvc.perform(
                post("/api/v1/signup")
                        .content(json)
                        .contentType("application/json")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        AppUser signupUser = mapper.readValue(
                signupResult.getResponse().getContentAsString(),
                AppUser.class
        );

        TokenRequest rightCreds = TokenRequest.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        String rightCredsJson = mapper.writeValueAsString(rightCreds);

        MvcResult result = mockMvc.perform(post("/api/v1/authenticate").content(rightCredsJson).contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(signupUser.getId()))
                .andReturn();

        TokenResponse tokenResponse = mapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);

        MockHttpServletRequestBuilder req = post("/api/v1/refresh")
                .param("refresh_token", tokenResponse.getRefreshToken())
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken());

        mockMvc.perform(req).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(is(not(tokenResponse.getAccessToken()))))
                .andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken()))
                .andExpect(jsonPath("$.userId").value(signupUser.getId()))
                .andReturn();

        req = post("/api/v1/refresh")
                .param("refresh_token", tokenResponse.getRefreshToken()+"1")
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken());

        mockMvc.perform(req).andDo(print()).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andReturn();
    }

}

