package com.meida.receiver;

import android.content.Context;

import com.lzy.okgo.utils.OkLogger;

import cn.jpush.android.api.JPushMessage;

/**
 * 自定义JPush message 接收器,包括操作tag/alias的结果返回(仅仅包含tag/alias新接口部分)
 */
public class JPushMessageReceiver extends cn.jpush.android.service.JPushMessageReceiver {

    /**
     * alias相关的操作会在此方法中回调结果
     *
     * @param context      上下文
     * @param jPushMessage alias相关操作返回的消息结果体
     */
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        OkLogger.i("JPush_Message", jPushMessage.toString());
        super.onAliasOperatorResult(context, jPushMessage);
    }

    /**
     * tag增删查改的操作会在此方法中回调结果
     *
     * @param context      上下文
     * @param jPushMessage tag相关操作返回的消息结果体
     */
    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        OkLogger.i("JPush_Message", jPushMessage.toString());
        super.onTagOperatorResult(context, jPushMessage);
    }

    /**
     * 查询某个tag与当前用户的绑定状态的操作会在此方法中回调结果
     *
     * @param context      上下文
     * @param jPushMessage check tag与当前用户绑定状态的操作返回的消息结果体
     */
    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        OkLogger.i("JPush_Message", jPushMessage.toString());
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    /**
     * 设置手机号码会在此方法中回调结果
     *
     * @param context      上下文
     * @param jPushMessage 设置手机号码返回的消息结果体
     */
    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        OkLogger.i("JPush_Message", jPushMessage.toString());
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }
}
