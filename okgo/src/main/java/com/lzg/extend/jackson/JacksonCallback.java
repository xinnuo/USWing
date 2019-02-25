package com.lzg.extend.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lzg.extend.BaseResponse;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.base.Request;
import com.lzy.okgo.utils.OkLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class JacksonCallback<T> extends AbsCallback<T> {

    private Class<T> clazz;

    public JacksonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        super.onStart(request);
        // 主要用于在所有请求之前添加公共的请求头或请求参数
        // 例如登录授权的 token
        // 使用的设备信息
        // 可以随意添加,也可以什么都不传
        // 还可以在这里对所有的参数进行加密，均在这里实现
        // request.headers("header1", "").params("params1", "").params("token", "");
    }

    /**
     * 该方法是子线程处理，不能做ui相关的工作
     * 主要作用是解析网络返回的 response 对象,生产onSuccess回调中需要的数据对象
     * 这里的解析工作不同的业务逻辑基本都不一样,所以需要自己实现,以下给出的时模板代码,实际使用根据需要修改
     */
    @Override
    public T convertResponse(Response response) throws Throwable {

        // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用
        // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用
        // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用

        //详细自定义的原理和文档，看这里： https://github.com/jeasonlzy/okhttp-OkGo/wiki/JsonCallback

        ResponseBody body = response.body();
        if (body == null) return null;

        String json = body.string();
        OkLogger.i(json);

        if (clazz == null) {
            Type genType = getClass().getGenericSuperclass();
            final Type type = ((ParameterizedType) genType).getActualTypeArguments()[0];

            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType(); //泛型的实际类型
                Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0]; //泛型的参数

                if (rawType == BaseResponse.class) {

                    if (typeArgument instanceof ParameterizedType) {
                        Type rawTypeInner = ((ParameterizedType) typeArgument).getRawType(); //泛型的实际类型
                        Type typeArgumentInner = ((ParameterizedType) typeArgument).getActualTypeArguments()[0]; //泛型的参数

                        if (rawTypeInner == ArrayList.class) {
                            Class<?> clazzArgument = getClass(typeArgumentInner);

                            BaseResponse baseResponse = new BaseResponse();
                            JSONArray jsonArray = new JSONObject(json).optJSONArray("object");
                            baseResponse.object = JsonUtil.toList(jsonArray == null ? "[]" : jsonArray.toString(), clazzArgument);

                            response.close();
                            return (T) baseResponse;
                        } else {

                            T t = JsonUtil.jsonToBean(json, new TypeReference<T>() { }); //解析成为LinkedHashMap
                            response.close();
                            return t;
                        }
                    } else {
                        Class<?> clazzArgument = getClass(typeArgument);

                        BaseResponse baseResponse = new BaseResponse();
                        JSONObject jsonObject = new JSONObject(json).optJSONObject("object");
                        baseResponse.object = JsonUtil.jsonToBean(jsonObject == null ? "{}" : jsonObject.toString(), clazzArgument);

                        response.close();
                        return (T) baseResponse;
                    }
                } else {

                    T t = JsonUtil.jsonToBean(json, new TypeReference<T>() { }); //解析成为LinkedHashMap
                    response.close();
                    return t;
                }
            }
        } else {
            T t = JsonUtil.jsonToBean(json, clazz);
            response.close();
            return t;
        }
        return null;
    }

    private String getClassName(Type type) {
        if (type == null) return "";
        String className = type.toString();
        if (className.startsWith("class ")) {
            className = className.substring("class ".length());
        } else if (className.startsWith("interface ")) {
            className = className.substring("interface ".length());
        }
        return className;
    }

    private Class<?> getClass(Type type) throws ClassNotFoundException {
        String className = getClassName(type);
        if (className == null || className.isEmpty()) return null;
        return Class.forName(className);
    }

    private Object newInstance(Type type) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> clazz = getClass(type);
        if (clazz == null) return null;
        return clazz.newInstance();
    }
}
