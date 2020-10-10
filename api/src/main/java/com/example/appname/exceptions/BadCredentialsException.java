package com.example.appname.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN, reason="Bad Credentials")
public class BadCredentialsException extends RuntimeException {
}
