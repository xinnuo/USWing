/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzg.extend.jackson;

import android.app.Activity;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.lzy.ProgressDialogManager;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.exception.StorageException;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 网络数据请求时弹出进度对话框
 */
public abstract class JacksonDialogCallback<T> extends JacksonCallback<T> {

    private ProgressDialogManager mProgressManager;
    private Activity activity;
    private boolean isVisible;

    private void initDialog(Activity activity) {
        this.activity = activity;
        if (isVisible) mProgressManager = new ProgressDialogManager(activity);
    }

    public JacksonDialogCallback(Activity activity) {
        this(activity, null, false);
    }

    public JacksonDialogCallback(Activity activity, Class<T> clazz) {
        this(activity, clazz, false);
    }

    public JacksonDialogCallback(Activity activity, boolean isVisible) {
        this(activity, null, isVisible);
    }

    public JacksonDialogCallback(Activity activity, Class<T> clazz, boolean isVisible) {
        super(clazz);
        this.isVisible = isVisible;
        initDialog(activity);
    }

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        if (isVisible) mProgressManager.show();
    }

    /**
     * 当缓存读取成功后，回调该方法
     */
    @Override
    public void onCacheSuccess(Response<T> response) {
        onSuccess(response);
    }

    @Override
    public void onFinish() {
        //网络请求结束后关闭对话框
        if (isVisible) mProgressManager.dismiss();
    }

    @Override
    public void onError(Response<T> response) {
        super.onError(response);
        Throwable exception = response.getException();
        if (exception instanceof UnknownHostException
                || exception instanceof ConnectException) {
            showToast("网络连接失败，请连接网络！");
        } else if (exception instanceof SocketTimeoutException) {
            showToast("网络请求超时！");
        } else if (exception instanceof HttpException) {
            showToast("服务器发生未知错误！");
        } else if (exception instanceof StorageException) {
            showToast("SD卡不存在或没有权限！");
        } else if (exception instanceof JsonSyntaxException
                || exception instanceof JSONException) {
            showToast("数据格式错误或解析失败！");
        } else {
            showToast("网络数据请求失败！");
        }
    }

    private void showToast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
