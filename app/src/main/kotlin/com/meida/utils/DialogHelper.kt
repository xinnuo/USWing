package com.meida.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bigkoo.svprogresshud.SVProgressHUD
import com.flyco.dialog.widget.ActionSheetDialog
import com.flyco.dialog.widget.popup.BubblePopup
import com.meida.base.BaseDialog
import com.meida.base.BottomDialog
import com.meida.base.inflate
import com.meida.uswing.IntegralChargeActivity
import com.meida.uswing.R
import com.meida.uswing.WalletChargeActivity
import com.ruanmeng.utils.KeyboardHelper
import com.weigan.loopview.LoopView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

object DialogHelper {

    @SuppressLint("StaticFieldLeak")
    private var mSVProgressHUD: SVProgressHUD? = null

    fun showDialog(context: Context, hint: String) {
        dismissDialog()

        mSVProgressHUD = SVProgressHUD(context)
        mSVProgressHUD!!.showWithStatus(hint)
    }

    fun dismissDialog() {
        if (mSVProgressHUD != null && mSVProgressHUD!!.isShowing)
            mSVProgressHUD!!.dismissImmediately()
    }

    fun Fragment.showHintDialog(title: String, hint: String, listener: (String) -> Unit) {
        showHintDialog(title, hint, "取消", "确定", false, listener)
    }

    fun Fragment.showHintDialog(
        title: String,
        hint: String,
        cancel: String,
        sure: String,
        listener: (String) -> Unit
    ) {
        showHintDialog(title, hint, cancel, sure, false, listener)
    }

    fun Fragment.showHintDialog(
        title: String,
        hint: String,
        cancel: String,
        sure: String,
        isForced: Boolean,
        listener: (String) -> Unit
    ) {

        val dialog = object : BaseDialog(activity) {

            override fun onCreateView(): View {
                widthScale(0.7f)
                val view = activity!!.inflate<View>(R.layout.dialog_handle_hint)

                val tvTitle = view.findViewById<TextView>(R.id.dialog_title)
                val tvHint = view.findViewById<TextView>(R.id.dialog_hint)
                val btCancel = view.findViewById<TextView>(R.id.dialog_cancel)
                val btSure = view.findViewById<TextView>(R.id.dialog_sure)

                tvTitle.text = title
                tvHint.text = hint
                btCancel.text = cancel
                btSure.text = sure

                btCancel.onClick {
                    dismiss()
                    listener.invoke("取消")
                }

                btSure.onClick {
                    dismiss()
                    listener.invoke("确定")
                }

                return view
            }
        }

        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) return@setOnKeyListener isForced
            else return@setOnKeyListener false
        }
        dialog.setCanceledOnTouchOutside(!isForced)
        dialog.show()
    }

    fun Context.showHintDialog(title: String, hint: String, listener: (String) -> Unit) {
        showHintDialog(title, hint, "取消", "确定", false, listener)
    }

    fun Context.showHintDialog(
        title: String,
        hint: String,
        cancel: String,
        sure: String,
        listener: (String) -> Unit
    ) {
        showHintDialog(title, hint, cancel, sure, false, listener)
    }

    fun Context.showHintDialog(
        title: String,
        hint: String,
        cancel: String,
        sure: String,
        isForced: Boolean,
        listener: (String) -> Unit
    ) {

        val dialog = object : BaseDialog(this) {

            override fun onCreateView(): View {
                widthScale(0.7f)
                val view = inflate<View>(R.layout.dialog_handle_hint)

                val tvTitle = view.findViewById<TextView>(R.id.dialog_title)
                val tvHint = view.findViewById<TextView>(R.id.dialog_hint)
                val btCancel = view.findViewById<TextView>(R.id.dialog_cancel)
                val btSure = view.findViewById<TextView>(R.id.dialog_sure)

                tvTitle.text = title
                tvHint.text = hint
                btCancel.text = cancel
                btSure.text = sure

                btCancel.onClick {
                    dismiss()
                    listener.invoke("取消")
                }

                btSure.onClick {
                    dismiss()
                    listener.invoke("确定")
                }

                return view
            }
        }

        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) return@setOnKeyListener isForced
            else return@setOnKeyListener false
        }
        dialog.setCanceledOnTouchOutside(!isForced)
        dialog.show()
    }

    fun Context.showCompareDialog(listener: (String) -> Unit) {

        val dialog = object : BottomDialog(this) {

            override fun onCreateView(): View {
                val view = inflate<View>(R.layout.dialog_compare_bottom)

                val ivCoach = view.findViewById<ImageView>(R.id.dialog_compare_coach)
                val ivMine = view.findViewById<ImageView>(R.id.dialog_compare_mine)
                val ivCollect = view.findViewById<ImageView>(R.id.dialog_compare_collect)
                val ivClose = view.findViewById<ImageView>(R.id.dialog_compare_close)

                ivCoach.onClick {
                    dismiss()
                    listener.invoke("教练魔频")
                }

                ivMine.onClick {
                    dismiss()
                    listener.invoke("我的魔频")
                }

                ivCollect.onClick {
                    dismiss()
                    listener.invoke("我的收藏")
                }

                ivClose.onClick {
                    dismiss()
                    listener.invoke("关闭")
                }

                return view
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun Context.showShareDialog(listener: (String) -> Unit) {
        val view = inflate<View>(R.layout.dialog_share_bottom)
        val mQQ = view.findViewById<LinearLayout>(R.id.dialog_share_qq)
        val mWechat = view.findViewById<LinearLayout>(R.id.dialog_share_wechat)
        val mCircle = view.findViewById<LinearLayout>(R.id.dialog_share_circle)
        val mSina = view.findViewById<LinearLayout>(R.id.dialog_share_sina)
        val mAnswer = view.findViewById<LinearLayout>(R.id.dialog_share_answer)
        val mComment = view.findViewById<LinearLayout>(R.id.dialog_share_comment)
        val cancel = view.findViewById<Button>(R.id.dialog_share_cancel)
        val dialog = object : BottomSheetDialog(this, R.style.BottomSheetDialogStyle) {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, context.getScreenHeight())
            }

        }

        mQQ.onClick {
            dialog.dismiss()
            listener.invoke("QQ")
        }

        mWechat.onClick {
            dialog.dismiss()
            listener.invoke("微信")
        }

        mCircle.onClick {
            dialog.dismiss()
            listener.invoke("朋友圈")
        }

        mSina.onClick {
            dialog.dismiss()
            listener.invoke("新浪")
        }

        mAnswer.onClick {
            dialog.dismiss()
            listener.invoke("问答")
        }

        mComment.onClick {
            dialog.dismiss()
            listener.invoke("点评")
        }

        cancel.onClick { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    fun Activity.showCommentDialog(listener: (String) -> Unit) {

        val dialog = object : BottomDialog(this) {

            override fun onCreateView(): View {
                val view = inflate<View>(R.layout.dialog_state_input)

                val etInput = view.findViewById<EditText>(R.id.input_hint)
                val tvSend = view.findViewById<TextView>(R.id.input_send)

                etInput.apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()
                }

                tvSend.onClick {
                    dismiss()
                    listener.invoke(etInput.text.trimString())
                }

                return view
            }
        }

        dialog.setOnShowListener { KeyboardHelper.showSoftInput(this) }
        dialog.show()
    }

    fun Context.showRightPopup(
        anchor: View,
        top: String = "关注",
        bottom: String = "举报",
        listener: (String) -> Unit
    ) {

        val view = inflate<View>(R.layout.popu_state_right)

        val appoint = view.findViewById<TextView>(R.id.popu_appoint)
        val report = view.findViewById<TextView>(R.id.popu_report)

        appoint.text = top
        report.text = bottom

        BubblePopup(this, view).apply {

            anchorView(anchor)
            @Suppress("DEPRECATION")
            bubbleColor(resources.getColor(R.color.black))
            cornerRadius(5f)
            triangleWidth(12f)
            triangleHeight(7f)
            gravity(Gravity.BOTTOM)
            showAnim(null)
            dismissAnim(null)
            dimEnabled(false)
            show()

            appoint.onClick {
                dismiss()
                listener.invoke(top)
            }
            report.onClick {
                dismiss()
                listener.invoke(bottom)
            }
        }

    }

    fun Fragment.showRightPopup(
        anchor: View,
        top: String = "关注",
        bottom: String = "举报",
        listener: (String) -> Unit
    ) {

        val view = activity!!.inflate<View>(R.layout.popu_state_right)

        val appoint = view.findViewById<TextView>(R.id.popu_appoint)
        val report = view.findViewById<TextView>(R.id.popu_report)

        appoint.text = top
        report.text = bottom

        BubblePopup(activity, view).apply {

            anchorView(anchor)
            @Suppress("DEPRECATION")
            bubbleColor(resources.getColor(R.color.black))
            cornerRadius(5f)
            triangleWidth(12f)
            triangleHeight(7f)
            gravity(Gravity.BOTTOM)
            showAnim(null)
            dismissAnim(null)
            dimEnabled(false)
            show()

            appoint.onClick {
                dismiss()
                listener.invoke(top)
            }
            report.onClick {
                dismiss()
                listener.invoke(bottom)
            }
        }

    }

    fun Fragment.showCommentDialog(listener: (String) -> Unit) {

        val dialog = object : BottomDialog(activity) {

            override fun onCreateView(): View {
                val view = activity!!.inflate<View>(R.layout.dialog_state_input)

                val etInput = view.findViewById<EditText>(R.id.input_hint)
                val tvSend = view.findViewById<TextView>(R.id.input_send)

                etInput.apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()
                }

                tvSend.onClick {
                    dismiss()
                    listener.invoke(etInput.text.trimString())
                }

                return view
            }
        }

        dialog.setOnShowListener { KeyboardHelper.showSoftInput(activity!!) }
        dialog.show()
    }

    fun Context.showRewardDialog(num: Int = 0, title: String = "打赏", listener: (String) -> Unit) {

        val dialog = object : BaseDialog(this, true) {

            override fun onCreateView(): View {
                val view = inflate<View>(R.layout.dialog_reward_center)

                val ivClose = view.findViewById<ImageView>(R.id.dialog_close)
                val tvTitle = view.findViewById<TextView>(R.id.dialog_title)
                val etInput = view.findViewById<EditText>(R.id.dialog_input)
                val tvNum = view.findViewById<TextView>(R.id.dialog_num)
                val charge = view.findViewById<LinearLayout>(R.id.dialog_charge)
                val btSure = view.findViewById<Button>(R.id.dialog_sure)

                tvTitle.text = title
                tvNum.text = num.toString()

                charge.onClick {
                    dismiss()
                    startActivity<IntegralChargeActivity>()
                }

                btSure.onClick {
                    dismiss()
                    listener.invoke(etInput.text.toString())
                }

                ivClose.onClick { dismiss() }

                return view
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun Fragment.showRewardDialog(num: Int = 0, title: String = "打赏", listener: (String) -> Unit) {

        val dialog = object : BaseDialog(activity, true) {

            override fun onCreateView(): View {
                val view = activity!!.inflate<View>(R.layout.dialog_reward_center)

                val ivClose = view.findViewById<ImageView>(R.id.dialog_close)
                val tvTitle = view.findViewById<TextView>(R.id.dialog_title)
                val etInput = view.findViewById<EditText>(R.id.dialog_input)
                val tvNum = view.findViewById<TextView>(R.id.dialog_num)
                val charge = view.findViewById<LinearLayout>(R.id.dialog_charge)
                val btSure = view.findViewById<Button>(R.id.dialog_sure)

                tvTitle.text = title
                tvNum.text = num.toString()

                charge.onClick {
                    dismiss()
                    startActivity<IntegralChargeActivity>()
                }

                btSure.onClick {
                    dismiss()
                    listener.invoke(etInput.text.toString())
                }

                ivClose.onClick { dismiss() }

                return view
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    @Suppress("DEPRECATION")
    fun Context.showItemDialog(vararg params: String, listener: (Int) -> Unit) {

        ActionSheetDialog(this, params, null).apply {
            isTitleShow(false)
            lvBgColor(resources.getColor(R.color.white))
            itemTextColor(resources.getColor(R.color.black))
            itemHeight(45f)
            itemTextSize(15f)
            dividerHeight(0.5f)
            dividerColor(resources.getColor(R.color.divider))
            cancelText(resources.getColor(R.color.light))
            cancelTextSize(15f)
            layoutAnimation(null)
            show()

            setOnOperItemClickL { _, _, index, _ ->
                dismiss()
                listener.invoke(index)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun Fragment.showItemDialog(vararg params: String, listener: (Int) -> Unit) {

        ActionSheetDialog(activity, params, null).apply {
            isTitleShow(false)
            lvBgColor(resources.getColor(R.color.white))
            itemTextColor(resources.getColor(R.color.black))
            itemHeight(45f)
            itemTextSize(15f)
            dividerHeight(0.5f)
            dividerColor(resources.getColor(R.color.divider))
            cancelText(resources.getColor(R.color.light))
            cancelTextSize(15f)
            layoutAnimation(null)
            show()

            setOnOperItemClickL { _, _, index, _ ->
                dismiss()

                Completable.timer(350, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { listener.invoke(index) }
            }
        }
    }

    fun Context.showGroupDialog(listener: (String) -> Unit) {

        val dialog = object : BaseDialog(this, true) {

            override fun onCreateView(): View {
                val view = inflate<View>(R.layout.dialog_group_center)

                val ivClose = view.findViewById<ImageView>(R.id.dialog_close)
                val etInput = view.findViewById<EditText>(R.id.dialog_input)
                val btSure = view.findViewById<Button>(R.id.dialog_sure)

                btSure.onClick {
                    dismiss()
                    listener.invoke(etInput.text.toString())
                }

                ivClose.onClick { dismiss() }

                return view
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun Context.showSchoolDialog(
        title: String = "所属学院",
        position: Int,
        items: List<String>,
        listener: (Int, String) -> Unit) {

        val dialog = object : BottomDialog(this) {

            private lateinit var loopView: LoopView

            override fun onCreateView(): View {
                val view = inflate<View>(R.layout.dialog_select_one)

                val tvTitle = view.findViewById<TextView>(R.id.tv_dialog_select_title)
                val tvCancel = view.findViewById<TextView>(R.id.tv_dialog_select_cancle)
                val tvOk = view.findViewById<TextView>(R.id.tv_dialog_select_ok)
                loopView = view.findViewById(R.id.lv_dialog_select_loop)

                tvTitle.text = title
                loopView.setTextSize(15f)
                @Suppress("DEPRECATION")
                loopView.setDividerColor(context.resources.getColor(R.color.divider))
                loopView.setNotLoop()

                tvCancel.onClick { dismiss() }

                tvOk.onClick {
                    dismiss()
                    listener.invoke(loopView.selectedItem, items[loopView.selectedItem])
                }

                return view
            }

            override fun setUiBeforShow() {
                loopView.setItems(items)
                loopView.setInitPosition(position)
            }

        }

        dialog.show()
    }

    fun Context.showPayDialog(price: String, remain: String, listener: (String) -> Unit) {

        val dialog = object : BaseDialog(this, true) {

            @SuppressLint("SetTextI18n")
            override fun onCreateView(): View {
                val view = inflate<View>(R.layout.dialog_scan_center)

                val ivClose = view.findViewById<ImageView>(R.id.dialog_close)
                val tvHint = view.findViewById<TextView>(R.id.dialog_price)
                val tvNum = view.findViewById<TextView>(R.id.dialog_num)
                val charge = view.findViewById<LinearLayout>(R.id.dialog_charge)
                val btSure = view.findViewById<Button>(R.id.dialog_sure)

                tvHint.text = price
                tvNum.text = "${remain}元"

                charge.onClick {
                    dismiss()
                    listener.invoke("取消")
                    startActivity<WalletChargeActivity>()
                }

                btSure.onClick {
                    val mPrice = price.toNotDouble()
                    val mRemain = remain.toNotDouble()

                    if (mPrice > mRemain) {
                        toast("余额不足，请充值！")
                        return@onClick
                    }

                    dismiss()
                    listener.invoke("确定")
                }

                ivClose.onClick {
                    dismiss()
                    listener.invoke("取消")
                }

                return view
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

}