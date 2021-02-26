package com.javatest.flowable.common.exception;

import com.javatest.flowable.common.enums.ReturnCode;

public class MyException extends BaseException {

    public MyException(String msg) {
        super(ReturnCode.MY_EXCEPTION);
    }
}
