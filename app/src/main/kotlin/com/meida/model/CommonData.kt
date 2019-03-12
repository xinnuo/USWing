/**
 * created by 小卷毛, 2019/1/25
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
package com.meida.model

import java.io.Serializable

/**
 * 项目名称：USWing
 * 创建人：小卷毛
 * 创建时间：2019-01-25 11:20
 */
data class CommonData(
    //好友列表
    var letter: String = "",
    var friend_id: String = "",
    var fuser_id: String = "",

    //群组列表
    var groupchat_id: String = "",
    var groupchat_name: String = "",
    var groupchatId: String = "",
    var groupchatName: String = "",
    var ls: ArrayList<String>? = ArrayList(),

    //区域列表
    var areaCode: String = "",
    var areaName: String = "",
    var lng: String = "",
    var lat: String = "",

    //收支列表
    var amount_type: String = "",
    var opt_amount: String = "",
    var create_date: String = "",
    var reward_sum: String = "",

    //充值记录
    var recharge_sum: String = "",
    var recharge_way: String = "",

    //提现记录
    var carno: String = "",
    var card_number: String = "",
    var withdraw_sum: String = "",
    var wstatus: String = "",

    //积分列表
    var integral_num: String = "",
    var integral_date: String = "",
    var integral_type: String = "",
    var nick_name: String = "",

    //签到列表
    var signin_day: String = "",
    var signin_sum: String = "",

    //圈子列表
    var circle_id: String = "",
    var send_user: String = "",
    var circle_type: String = "",
    var vtype: String = "",
    var user_head: String = "",
    var create_date_time: String = "",
    var coach: String = "",
    var circle_title: String = "",
    var circle_imgs: String = "",
    var fctn: String = "",
    var lctn: String = "",
    var comment_ctn: String = "",
    var like_ctn: String = "",
    var reward_ctn: String = "",
    var likes: ArrayList<CommonData>? = ArrayList(),
    var comments: ArrayList<CommonData>? = ArrayList(),

    //视频宽高
    var width: String = "",
    var height: String = "",

    //点赞列表
    var user_info_id: String = "",

    //评论列表
    var comment_circle_id: String = "",
    var comment_id: String = "",
    var comment_info: String = "",
    var comment_nick_name: String? = "",
    var comment_user: String? = "",

    //举报列表
    var reportinfoId: String = "",
    var reportInfo: String = "",

    //消息列表
    var msg_receive_id: String = "",
    var send_date: String = "",
    var title: String = "",
    var mome: String = "",
    var astatus: String = "",
    var type: String = "",

    //轮播图
    var sliderId: String = "",
    var sliderImg: String = "",
    var href: String = "",

    //教练列表
    var certification_id: String = "",
    var certification_img: String = "",
    var follow_id: String = "",
    var recommend: String? = "",
    var teach_age: String = "",
    var gender: String? = "",
    var ucity: String = "",
    var uprovince: String = "",
    var follow_ctn: String = "",
    var follows: String = "",
    var friend: String = "",
    var collection: String = "",
    var introduction: String = "",
    var telephone: String = "",
    var specialty: String = "",
    var honors: ArrayList<CommonData>? = ArrayList(),

    //荣誉列表
    var honorId: String = "",
    var honorInfo: String = "",

    //学院列表
    var collegeId: String = "",
    var collegeName: String = "",

    //魔频列表
    var magicvoide_id: String = "",
    var theme_title: String = "",
    var labels_id: String? = "",
    var labels_name: String = "",
    var positive_voide: String = "",
    var positive_img: String = "",
    var negative_voide: String = "",
    var negative_img: String = "",
    var address: String = "",

    //标签列表
    var labelsId: String = "",
    var labelsName: String = "",

    var isChecked: Boolean = false
) : Serializable