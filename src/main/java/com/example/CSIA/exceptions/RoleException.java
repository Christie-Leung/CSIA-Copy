package com.example.CSIA.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class RoleException extends RuntimeException {

    private String userRole;
    private String reqRole;

    public RoleException(String userRole, String reqRole) {
        super(String.format("User is a %s! Required %s" , userRole, reqRole));
        this.reqRole = reqRole;
        this.userRole = userRole;
    }

}
