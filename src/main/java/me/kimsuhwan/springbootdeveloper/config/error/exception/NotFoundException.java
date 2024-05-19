package me.kimsuhwan.springbootdeveloper.config.error.exception;

import me.kimsuhwan.springbootdeveloper.config.error.ErrorCode;

public class NotFoundException extends BusinessBaseException{

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }
}
