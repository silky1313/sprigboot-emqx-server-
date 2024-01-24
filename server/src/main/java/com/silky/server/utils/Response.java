package com.silky.server.utils;

import lombok.Data;
import java.io.Serializable;

@Data
public class Response<T> implements Serializable {
    private String code;
    private String msg;
    private T data;

    public Response() {
        this.code = "200";
        this.msg = "查询成功";
    }

    // =================================== 成功响应 ===================================
    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        return response;
    }
    public static <T> Response<T> success(String msg) {
        Response<T> response = new Response<>();
        response.setMsg(msg);
        return response;
    }

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <T> Response<T> success(T data, String msg) {
        Response<T> response = Response.success(data);
        response.setMsg(msg);
        return response;
    }

    // =================================== 失败响应 ===================================
    public static <T> Response<T> fail(String code, String msg) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
}

