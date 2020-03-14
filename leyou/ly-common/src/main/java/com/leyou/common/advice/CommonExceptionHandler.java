package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionRusult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ResourceBundle;

/**
 * author:lu
 * create time: 2019/11/24.
 */
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionRusult> handlerException(LyException le){
        return  ResponseEntity.status(le.getExceptionEnum().getCode()).body(new ExceptionRusult(le.getExceptionEnum()));

    }
}
