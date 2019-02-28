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
import io.rong.imlib.model.Conversation

fun RongIM.startChatRoomChat(context: Context, chatRoomId: String, chatRoomName: String, createIfNotExist: Boolean) =
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