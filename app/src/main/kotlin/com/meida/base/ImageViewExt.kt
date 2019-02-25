/**
 * created by 小卷毛, 2018/3/29 0029
 * Copyright (c) 2018, 416143467@qq.com All Rights Reserved.
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
package com.meida.base

import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meida.uswing.R
import com.sunfusheng.GlideImageView

/**
 * 图片加载扩展类
 */
/*fun ImageView.setImageURL(url: String) = GlideApp.with(context)
        .load(url)
        .resourceOption(R.mipmap.default_user)
        .into(this)*/

fun ImageView.setImageURL(url: String, @DrawableRes resourceId: Int = R.mipmap.default_logo) =
    Glide.with(context).load(url)
        .apply(
            RequestOptions
                .centerCropTransform()
                .placeholder(resourceId)
                .error(resourceId)
                .dontAnimate()
        )
        .into(this)

fun GlideImageView.loadCircleImage(url: String) = loadCircle(url, R.mipmap.default_logo)

fun GlideImageView.loadRectImage(url: String) = load(url, R.mipmap.default_logo)