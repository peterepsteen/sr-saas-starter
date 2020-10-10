package com.example.appname.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN, reason="No token supplied")  // 403
public class NoTokenSuppliedException extends RuntimeException {
}
