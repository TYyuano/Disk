package com.YuAN.errors;

import com.YuAN.interfaces.IResponse;
import lombok.Data;

import java.text.MessageFormat;

@Data
public class BusinessException extends RuntimeException implements IResponse {
    private final int code;
    private final String message;
    private final IResponse response;
    public BusinessException(String message, IResponse response, Object[] args){
        super(message == null ? response.getMessage() : message);
        this.code = response.getCode();
        this.response = response;
        this.message = MessageFormat.format(response.getMessage(), args);
    }

    public BusinessException(IResponse response){
        this("",response, null);
    }

    public BusinessException(String message, IResponse response){
        this(message, response,null);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
