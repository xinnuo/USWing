package com.meida.uswing

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.base.loadRectImage
import com.meida.share.BaseHttp
import com.meida.utils.dp2px
import com.meida.utils.getScreenWidth
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_code.*

class CodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)
        init_title("我的二维码")
    }

    @SuppressLint("CheckResult")
    override fun init_title() {
        super.init_title()

        code_img.loadRectImage(BaseHttp.baseImg + getString("userHead"))
        code_name.text = getString("nickName")

        Flowable.just("token:${getString("token")}")
            .map {
                return@map QRCodeEncoder.syncEncodeQRCode(
                    it,
                    getScreenWidth() - dp2px(150f),
                    Color.BLACK
                )
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                code_qr.setImageBitmap(it)
            }
    }
}
