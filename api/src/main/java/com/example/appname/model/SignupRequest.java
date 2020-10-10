package com.example.appname.model;

import com.example.appname.validation.ValidPassword;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Validated
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @Email
    private String email;

    @Size(min = 8, max = 50)
    @ValidPassword
    private String password;
}
