package com.meida.uswing

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.lzy.okgo.utils.OkLogger
import com.meida.RongCloudContext
import com.meida.base.*
import io.rong.imkit.RongIM
import io.rong.imkit.fragment.ConversationFragment
import io.rong.imlib.MessageTag
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.typingmessage.TypingStatus
import io.rong.message.TextMessage
import io.rong.message.VoiceMessage
import java.util.*

class ConversationActivity : BaseActivity() {

    private lateinit var mConversationType: Conversation.ConversationType
    private var mTargetId = ""
    private var mTitle = ""

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
    }

    override fun init_title() {
        super.init_title()
        when (mConversationType) {
            Conversation.ConversationType.PRIVATE -> {
                tvRight.visible()
                tvRight.text = "打赏"
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

                    val textTag = TextMessage::class.java.getAnnotation(MessageTag::class.java)?.value ?: ""
                    val voiceTag = VoiceMessage::class.java.getAnnotation(MessageTag::class.java)?.value ?: ""

                    //匹配对方正在输入的是文本消息还是语音消息
                    when(objectName) {
                        textTag -> handler.sendEmptyMessage(SET_TEXT_TYPING_TITLE)
                        voiceTag -> handler.sendEmptyMessage(SET_VOICE_TYPING_TITLE)
                    }
                } else {
                    //当前会话没有用户正在输入，标题栏仍显示原来标题
                    handler.sendEmptyMessage(SET_TARGET_ID_TITLE)
                }
            }
        }

        tvRight.oneClick {

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
}
