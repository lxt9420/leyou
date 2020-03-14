package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

/**
 * author:lu
 * create time: 2019/11/24.
 */
@Data
public class ExceptionRusult {
    private int status;
    private String message;
    private Long  timestamp;
    public ExceptionRusult(ExceptionEnum em){
        this.status=em.getCode();
        this.message=em.getMsg();
        this.timestamp=System.currentTimeMillis();
    }
}
