package com.example.appname.controller;


import com.example.appname.exceptions.UserAlreadyExistsException;
import com.example.appname.model.AppUser;
import com.example.appname.model.SignupRequest;
import com.example.appname.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/v1")
public class AppUserController {
    @Autowired
    private AppUserService userService;

    @PostMapping("/signup")
    public AppUser signup(@Valid @RequestBody SignupRequest request, BindingResult result) {
        if (result.hasErrors()) {
            String errorMsg = result.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(" "));
            log.warn(errorMsg);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    errorMsg
            );
        }

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        try {
            return userService.addUser(appUser);
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException();
        }

    }
}
