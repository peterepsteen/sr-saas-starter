package com.example.appname.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="User with that email already exists")
public class UserAlreadyExistsException extends RuntimeException {
}
