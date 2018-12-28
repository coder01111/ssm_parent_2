package com.pinyougou.entity;

import java.io.Serializable;

public class Result implements Serializable {
    /**
     * 返回结果封装
     * @author Administrator
     * 必须实现序列化因为要在服务器上进行流的传输
     *
     */
        private boolean success;
        private String message;

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
