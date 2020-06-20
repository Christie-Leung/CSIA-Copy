package com.example.CSIA.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RoleException extends RuntimeException {

    /**
     * This method formats an error message.
     * @param userRole User's current role
     * @param reqRole Required role
     */
    public RoleException(String userRole, String reqRole) {
        super(String.format("User is a %s! Required %s" , userRole, reqRole));
    }
}
