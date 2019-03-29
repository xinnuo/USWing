/**
 * created by 小卷毛, 2018/12/27
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
package com.meida.share

import com.meida.uswing.BuildConfig

object BaseHttp {

    @Suppress("MayBeConstant")
    private val baseUrl = BuildConfig.API_HOST
    private val baseIp = "$baseUrl/api"
    val baseImg = "$baseUrl/"
    val circleImg = "$baseUrl:8092/"

    val identify_get = "$baseIp/identify_get.rm"                 //注册验证码
    val register_sub = "$baseIp/register_sub.rm"                 //注册
    val identify_getbyforget = "$baseIp/identify_getbyforget.rm" //忘记验证码
    val pwd_forget_sub = "$baseIp/pwd_forget_sub.rm"             //忘记密码
    val login_sub = "$baseIp/login_sub.rm"                       //登录
    val identify_get2 = "$baseIp/identify_get2.rm"               //第三方登录验证码

    val user_msg_data = "$baseIp/user_msg_data.rm"                     //个人资料
    val userinfo_uploadhead_sub = "$baseIp/userinfo_uploadhead_sub.rm" //上传头像
    val update_userInfo = "$baseIp/update_userInfo.rm"                 //更新资料
    val update_mobile = "$baseIp/update_mobile.rm"                     //更新手机号
    val password_change_sub = "$baseIp/password_change_sub.rm"         //修改密码
    val identify_get_mobile = "$baseIp/identify_get_mobile.rm"         //更改手机验证码
    val find_user_details = "$baseIp/find_user_details.rm"             //用户详情
    val find_amount_List = "$baseIp/find_amount_List.rm"               //收支列表
    val add_signin = "$baseIp/add_signin.rm"                           //签到
    val find_signin_list = "$baseIp/find_signin_list.rm"               //签到列表
    val find_collection_list = "$baseIp/find_collection_list.rm"       //收藏列表
    val find_follow_list = "$baseIp/find_follow_list.rm"               //关注列表

    val add_recharge = "$baseIp/add_recharge.rm"                                   //充值
    val find_recharge_list = "$baseIp/find_recharge_list.rm"                       //充值列表
    val add_withdraw = "$baseIp/add_withdraw.rm"                                   //提现
    val find_withdraw_list = "$baseIp/find_withdraw_list.rm"                       //提现列表
    val find_integral_list = "$baseIp/find_integral_list.rm"                       //积分列表
    val signin_proportion = "$baseIp/signin_proportion.rm"                         //积分比例
    val add_recharge_integral = "$baseIp/add_recharge_integral.rm"                 //积分充值
    val add_recharge_integral_balance = "$baseIp/add_recharge_integral_balance.rm" //积分余额充值
    val add_withdraw_integral = "$baseIp/add_withdraw_integral.rm"                 //积分提现

    val add_circle = "$baseIp/add_circle.rm"                     //圈子图片
    val add_circle_voides = "$baseIp/add_circle_voides.rm"       //圈子视频
    val find_circle_list = "$baseIp/find_circle_list.rm"         //圈子列表
    val find_circle_details = "$baseIp/find_circle_details.rm"   //圈子详情
    val delete_circle = "$baseIp/delete_circle.rm"               //删除圈子
    val add_likes = "$baseIp/add_likes.rm"                       //点赞
    val delete_likes = "$baseIp/delete_likes.rm"                 //取消点赞
    val add_comment = "$baseIp/add_comment.rm"                   //评论
    val add_reward_user = "$baseIp/add_reward_user.rm"           //打赏用户
    val add_reward = "$baseIp/add_reward.rm"                     //打赏圈子
    val find_reportinfo_list = "$baseIp/find_reportinfo_list.rm" //举报信息
    val add_report = "$baseIp/add_report.rm"                     //举报
    val add_coach_follow = "$baseIp/add_coach_follow.rm"         //关注
    val delete_follow = "$baseIp/delete_follow.rm"               //取消关注

    val msg_list_data = "$baseIp/msg_list_data.rm"               //消息列表
    val find_msg_details = "$baseIp/find_msg_details.rm"         //消息详情
    val find_friend_list = "$baseIp/find_friend_list.rm"         //好友列表
    val find_groupchat_list = "$baseIp/find_groupchat_list.rm"   //群组列表
    val update_application = "$baseIp/update_application.rm"     //处理申请
    val add_apply_friend = "$baseIp/add_apply_friend.rm"         //好友申请
    val add_groupchat = "$baseIp/add_groupchat.rm"               //创建群组
    val find_groupchat_users = "$baseIp/find_groupchat_users.rm" //群成员
    val find_user_heads = "$baseIp/find_user_heads.rm"           //成员头像

    val index_data = "$baseIp/index_data.rm"                           //首页
    val find_court_list = "$baseIp/find_court_list.rm"                 //试炼场列表
    val find_court_detils = "$baseIp/find_court_detils.rm"             //试炼场详情
    val certification_list = "$baseIp/certification_list.rm"           //教练列表
    val certification_near_list = "$baseIp/certification_near_list.rm" //附近教练
    val coach_details = "$baseIp/coach_details.rm"                     //教练详情
    val find_news_list = "$baseIp/find_news_list.rm"                   //资讯列表
    val find_news_details = "$baseIp/find_news_details.rm"             //资讯详情
    val add_collection = "$baseIp/add_collection.rm"                   //收藏
    val delete_collection = "$baseIp/delete_collection.rm"             //取消收藏

    val delete_honor = "$baseIp/delete_honor.rm"                 //删除荣誉
    val add_honor = "$baseIp/add_honor.rm"                       //新增荣誉
    val upadte_certification = "$baseIp/upadte_certification.rm" //修改简介
    val upadate_specialty = "$baseIp/upadate_specialty.rm"       //修改特长
    val find_college_list = "$baseIp/find_college_list.rm"       //学院列表
    val add_certification = "$baseIp/add_certification.rm"       //认证教练

    val add_startboot = "$baseIp/add_startboot.rm"                   //开机
    val find_startboot_price = "$baseIp/find_startboot_price.rm"     //开机价格
    val add_magicvoide = "$baseIp/add_magicvoide.rm"                 //获取魔频
    val find_magicvoide_list = "$baseIp/find_magicvoide_list.rm"     //魔频列表
    val delete_magicvoide = "$baseIp/delete_magicvoide.rm"           //删除魔频
    val labels_all = "$baseIp/labels_all.rm"                         //标签
    val edit_magicvoide = "$baseIp/edit_magicvoide.rm"               //编辑魔频
    val add_circle_share = "$baseIp/add_circle_share.rm"             //分享魔频
    val find_magicvoide_deatls = "$baseIp/find_magicvoide_deatls.rm" //魔频详情
    val add_magicvoide_coach = "$baseIp/add_magicvoide_coach.rm"     //上传魔频
    val add_voide_coach = "$baseIp/add_voide_coach.rm"               //教练上传
    val find_voide_coach = "$baseIp/find_voide_coach.rm"             //教练视频

    val leave_message_sub = "$baseIp/leave_message_sub.rm" //意见反馈
    val find_area_parent = "$baseIp/find_area_parent.rm"   //父级区域
    val find_area_level = "$baseIp/find_area_level.rm"     //级别区域
    val find_html_info = "$baseIp/find_html_info.rm"       //字典详情
    val find_startimg = "$baseIp/find_startimg.rm"         //启动页
    val update_oosition = "$baseIp/update_oosition.rm"     //更新位置

    val share_video = "${baseImg}forend/share_video.hm?magicvoideId=" //分享
}