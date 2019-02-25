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
package com.lzg.extend;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.lzy.ProgressDialogManager;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.exception.StorageException;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.lzy.okgo.utils.OkLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public abstract class StringDialogCallback extends StringCallback {

    private ProgressDialogManager mProgressManager;
    private Activity activity;
    private boolean isVisible;

    public StringDialogCallback(Activity activity) {
        this(activity, true);
    }

    public StringDialogCallback(Activity activity, boolean isVisible) {
        this.activity = activity;
        this.isVisible = isVisible;

        if (isVisible) mProgressManager = new ProgressDialogManager(activity);
    }

    @Override
    public void onStart(Request<String, ? extends Request> request) {
        if (isVisible) mProgressManager.show();
    }

    @Override
    public void onSuccess(Response<String> response) {
        OkLogger.i(response.body());

        try {
            JSONObject obj = new JSONObject(response.body());

            String msgCode = obj.optString("msgcode", obj.optString("code"));
            String msg = obj.optString("msg", obj.optString("info", "请求成功！"));

            if (!TextUtils.equals("100", msgCode)) {
                onSuccessResponseErrorCode(response, msg, msgCode);
            } else {
                onSuccessResponse(response, msg, msgCode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public abstract void onSuccessResponse(Response<String> response, String msg, String msgCode);

    public void onSuccessResponseErrorCode(Response<String> response, String msg, String msgCode) {
        showToast(msg);
    }

    /**
     * 当缓存读取成功后，回调该方法
     */
    @Override
    public void onCacheSuccess(Response<String> response) {
        try {
            JSONObject obj = new JSONObject(response.body());

            String msgCode = obj.optString("msgcode", obj.optString("code"));
            String msg = obj.optString("msg", obj.optString("info", "请求成功！"));

            if (TextUtils.equals("100", msgCode)) onSuccessResponse(response, msg, msgCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinish() {
        if (isVisible) mProgressManager.dismiss();
    }

    @Override
    public void onError(Response<String> response) {
        super.onError(response);
        Throwable exception = response.getException();
        if (exception instanceof UnknownHostException || exception instanceof ConnectException) {
            showToast("网络连接失败，请连接网络！");
        } else if (exception instanceof SocketTimeoutException) {
            showToast("网络请求超时！");
        } else if (exception instanceof HttpException) {
            showToast("服务器发生未知错误！");
        } else if (exception instanceof StorageException) {
            showToast("SD卡不存在或没有权限！");
        } else {
            showToast("网络数据请求失败！");
        }
    }

    private void showToast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
