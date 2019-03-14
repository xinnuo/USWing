/**
 * created by 小卷毛, 2019/02/26
 * Copyright (c) 2019, 416143467@qq.com All Rights Reserved.
 * #                   *********                            #
 * #                  ************                          #
 * #                  *************                         #
 * #                 **  ***********                        #
 * #                ***  ****** *****                       #
 * #                *** *******   ****                      #
 * #               ***  ********** ****                     #
 * #              ****  *********** ****                    #
 * #            *****   ***********  *****                  #
 * #           ******   *** ********   *****                #
 * #           *****   ***   ********   ******              #
 * #          ******   ***  ***********   ******            #
 * #         ******   **** **************  ******           #
 * #        *******  ********************* *******          #
 * #        *******  ******************************         #
 * #       *******  ****** ***************** *******        #
 * #       *******  ****** ****** *********   ******        #
 * #       *******    **  ******   ******     ******        #
 * #       *******        ******    *****     *****         #
 * #        ******        *****     *****     ****          #
 * #         *****        ****      *****     ***           #
 * #          *****       ***        ***      *             #
 * #            **       ****        ****                   #
 */
package com.meida

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.rong.imkit.RongContext
import io.rong.imkit.RongIM
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message

fun RongIM.startChatRoomChat(
    context: Context,
    chatRoomId: String,
    chatRoomName: String,
    createIfNotExist: Boolean
) =
    if (chatRoomId.isNotEmpty()) {
        if (RongContext.getInstance() == null) {
            throw ExceptionInInitializerError("RongCloud SDK not init")
        } else {
            val uri = Uri.parse("rong://" + context.applicationInfo.packageName)
                .buildUpon().appendPath("conversation")
                .appendPath(Conversation.ConversationType.CHATROOM.getName().toLowerCase())
                .appendQueryParameter("targetId", chatRoomId)
                .appendQueryParameter("title", chatRoomName)
                .build()

            val intent = Intent("android.intent.action.VIEW", uri)
            intent.putExtra("createIfNotExist", createIfNotExist)
            context.startActivity(intent)
        }
    } else {
        throw IllegalArgumentException("chatRoomId can not be empty!")
    }

fun Context.getUnreadCount(targetId: String, init: _ResultCallback.() -> Unit) {
    val listener = _ResultCallback()
    listener.init()
    RongIM.getInstance().getUnreadCount(Conversation.ConversationType.GROUP, targetId, listener)
}

open class _OnSendMessageListener : RongIM.OnSendMessageListener {

    private var _onSend: ((Message) -> Message)? = null

    override fun onSend(message: Message): Message? {
        return _onSend?.invoke(message)
    }

    fun onSend(listener: (Message) -> Message) {
        _onSend = listener
    }

    private var _onSent: ((Message, RongIM.SentMessageErrorCode?) -> Boolean)? = null

    override fun onSent(
        message: Message,
        sentMessageErrorCode: RongIM.SentMessageErrorCode?
    ): Boolean {
        return _onSent?.invoke(message, sentMessageErrorCode) ?: false
    }

    fun onSent(listener: (Message, sentMessageErrorCode: RongIM.SentMessageErrorCode?) -> Boolean) {
        _onSent = listener
    }

}

class _ResultCallback : RongIMClient.ResultCallback<Int>() {

    private var _onSuccess: ((Int) -> Unit)? = null

    override fun onSuccess(count: Int) {
        _onSuccess?.invoke(count)
    }

    fun onSuccess(listener: (Int) -> Unit) {
        _onSuccess = listener
    }

    private var _onError: ((RongIMClient.ErrorCode) -> Unit)? = null

    override fun onError(errorCode: RongIMClient.ErrorCode) {
        _onError?.invoke(errorCode)
    }

    fun onError(listener: (RongIMClient.ErrorCode) -> Unit) {
        _onError = listener
    }

}