package com.meida.uswing

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.meida.RongCloudContext
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.GroupModel
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.DialogHelper.showRewardDialog
import com.meida.utils.hideSoftInput
import com.meida.utils.toTextInt
import io.rong.imkit.RongIM
import io.rong.imkit.fragment.ConversationFragment
import io.rong.imkit.model.GroupUserInfo
import io.rong.imlib.MessageTag
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Group
import io.rong.imlib.model.UserInfo
import io.rong.imlib.typingmessage.TypingStatus
import io.rong.message.TextMessage
import io.rong.message.VoiceMessage
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ConversationActivity : BaseActivity() {

    private lateinit var mConversationType: Conversation.ConversationType
    private var mTargetId = ""
    private var mTitle = ""
    private var mIntegral = 0
    private val listMember = ArrayList<CommonData>()

    private val TextTypingTitle = "对方正在输入..."
    private val VoiceTypingTitle = "对方正在讲话..."

    private val SET_TEXT_TYPING_TITLE = 1
    private val SET_VOICE_TYPING_TITLE = 2
    private val SET_TARGET_ID_TITLE = 0

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SET_TEXT_TYPING_TITLE -> tvTitle.text = TextTypingTitle
                SET_VOICE_TYPING_TITLE -> tvTitle.text = VoiceTypingTitle
                SET_TARGET_ID_TITLE -> tvTitle.text = mTitle
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val uri = intent.data
        mTargetId = uri?.getQueryParameter("targetId") ?: ""
        mTitle = uri?.getQueryParameter("title") ?: ""
        mConversationType = Conversation.ConversationType.valueOf(
            uri?.lastPathSegment?.toUpperCase(Locale.US) ?: ""
        )

        init_title(mTitle)
        isPushMessage()

        when (mConversationType) {
            Conversation.ConversationType.PRIVATE -> getUserData(mTargetId)
            Conversation.ConversationType.GROUP -> {
                EventBus.getDefault().post(RefreshMessageEvent("群聊消息", mTargetId))
                getGroupData(mTargetId)
            }
            else -> getUserData(mTargetId)
        }
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    override fun init_title() {
        super.init_title()
        when (mConversationType) {
            Conversation.ConversationType.PRIVATE -> {
                tvRight.visible()
                tvRight.text = "打赏"
            }
            Conversation.ConversationType.GROUP -> {
                tvRight.visible()
                tvRight.text = "查看成员"
            }
            else -> tvRight.gone()
        }

        RongIMClient.setTypingStatusListener { _, targetId, typingStatusSet ->
            //当输入状态的会话类型和targetID与当前会话一致时，才需要显示
            if (targetId == mTargetId) {
                val count = typingStatusSet.size
                //count表示当前会话中正在输入的用户数量，目前只支持单聊，所以判断大于0就可以给予显示了
                if (count > 0) {
                    val iterator = typingStatusSet.iterator()
                    val status = iterator.next() as TypingStatus
                    val objectName = status.typingContentType

                    val textTag =
                        TextMessage::class.java.getAnnotation(MessageTag::class.java)?.value ?: ""
                    val voiceTag =
                        VoiceMessage::class.java.getAnnotation(MessageTag::class.java)?.value ?: ""

                    //匹配对方正在输入的是文本消息还是语音消息
                    when (objectName) {
                        textTag -> handler.sendEmptyMessage(SET_TEXT_TYPING_TITLE)
                        voiceTag -> handler.sendEmptyMessage(SET_VOICE_TYPING_TITLE)
                    }
                } else {
                    //当前会话没有用户正在输入，标题栏仍显示原来标题
                    handler.sendEmptyMessage(SET_TARGET_ID_TITLE)
                }
            }
        }

        // 发送消息监听，结束activity时要设置监听器null，防止内存泄漏
        /*RongCloudContext.getInstance().setOnMessageExtraListener {
            when (mConversationType) {
                Conversation.ConversationType.GROUP -> it.extra = mTitle
                Conversation.ConversationType.PRIVATE -> it.extra = getString("nickName")
                else -> it.extra = getString("nickName")
            }
        }*/

        tvRight.oneClick {
            when (mConversationType) {
                Conversation.ConversationType.PRIVATE -> {
                    showRewardDialog(mIntegral) {
                        if (it.isEmpty() || it.toTextInt() <= 0) {
                            toast("请输入打赏积分数")
                            return@showRewardDialog
                        }

                        if (it.toTextInt() > mIntegral) {
                            toast("当前积分不足")
                            return@showRewardDialog
                        }

                        /* 打赏用户 */
                        OkGo.post<String>(BaseHttp.add_reward_user)
                            .tag(this@ConversationActivity)
                            .headers("token", getString("token"))
                            .params("rewardSum", it)
                            .params("rewardUser", mTargetId)
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                                    toast(msg)
                                    mIntegral -= it.toTextInt()
                                    hideSoftInput()
                                }

                            })
                    }
                }
                Conversation.ConversationType.GROUP -> {
                    if (listMember.isNotEmpty()) {
                        startActivity<ContactMemberActivity>(
                            "list" to listMember
                        )
                    }
                }
                else -> return@oneClick
            }
        }
    }

    /**
     * 判断是否是 Push 消息，判断是否需要做 connect 操作
     */
    private fun isPushMessage() {
        if (intent != null
            && intent.data != null
            && intent.data?.scheme == "rong"
            && intent.data?.getQueryParameter("isFromPush") != null
            && intent.data?.getQueryParameter("isFromPush") == "true"
        ) {
            RongIM.connect(getString("rongToken"), object : RongIMClient.ConnectCallback() {
                /*
                 * 连接融云成功，返回当前 token 对应的用户 id
                 */
                override fun onSuccess(userid: String) {
                    OkLogger.i("融云连接成功， 用户ID：$userid")
                    OkLogger.i(RongIMClient.getInstance().currentConnectionStatus.message)

                    RongCloudContext.getInstance().connectedListener()

                    enterFragment(mConversationType, mTargetId)
                }

                /*
                 * 连接融云失败 errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                override fun onError(errorCode: RongIMClient.ErrorCode) {
                    OkLogger.e("融云连接失败，错误码：" + errorCode.message)
                }

                /*
                 * Token 错误。可以从下面两点检查
                 * 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                 * 2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                 */
                override fun onTokenIncorrect() {
                    OkLogger.e("融云token错误！！！")
                }
            })
        } else {
            enterFragment(mConversationType, mTargetId)
        }
    }

    /**
     * 加载会话页面 ConversationFragment
     *
     * @param mConversationType 会话类型
     * @param mTargetId         目标 Id
     */
    private fun enterFragment(mConversationType: Conversation.ConversationType, mTargetId: String) {

        val fragment =
            supportFragmentManager.findFragmentById(R.id.conversation) as ConversationFragment

        val uri = Uri.parse("rong://" + applicationInfo.packageName).buildUpon()
            .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
            .appendQueryParameter("targetId", mTargetId).build()

        fragment.uri = uri
    }

    /* 积分查询 */
    override fun getData() {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@ConversationActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    mIntegral = obj.optString("integral", "0").toInt()
                }

            })
    }

    /* 获取用户信息 */
    private fun getUserData(userId: String) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_user_heads)
            .tag(this@ConversationActivity)
            .headers("token", getString("token"))
            .params("userInfoids", userId)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    val items = ArrayList<CommonData>()
                    items.addItems(response.body().data)

                    if (items.isNotEmpty()) {
                        mTitle = items[0].nick_name
                        tvTitle.text = mTitle

                        RongIM.getInstance().refreshUserInfoCache(
                            UserInfo(
                                userId,
                                items[0].nick_name,
                                Uri.parse(BaseHttp.baseImg + items[0].user_head)
                            )
                        )
                    }

                }

            })
    }

    /* 获取群组信息 */
    private fun getGroupData(groupId: String) {
        OkGo.post<BaseResponse<GroupModel>>(BaseHttp.find_groupchat_users)
            .tag(this@ConversationActivity)
            .headers("token", getString("token"))
            .params("groupchatId", groupId)
            .execute(object : JacksonDialogCallback<BaseResponse<GroupModel>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<GroupModel>>) {

                    val imgs = ArrayList<String>()
                    val groupData = response.body().data.groupchat ?: CommonData()
                    mTitle = groupData.groupchatName
                    tvTitle.text = mTitle

                    listMember.addItems(response.body().data.ls)
                    listMember.mapTo(imgs) { BaseHttp.baseImg + it.user_head }

                    RongIM.getInstance().refreshGroupInfoCache(
                        Group(
                            groupId,
                            groupData.groupchatName,
                            Uri.parse(imgs.joinToString(","))
                        )
                    )

                    listMember.forEach {
                        RongIM.getInstance().refreshGroupUserInfoCache(
                            GroupUserInfo(
                                groupId,
                                it.user_info_id,
                                it.nick_name
                            )
                        )
                    }

                }

            })
    }

}
