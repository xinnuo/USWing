/**
 * created by 小卷毛, 2019/1/31
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
package com.meida.view

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.BottomSheetDialog
import android.view.WindowManager
import android.os.Bundle

class BottomSheetEditDialog : BottomSheetDialog {

    constructor(context: Context) : super(context)

    constructor(context: Context, theme: Int) : super(context, theme)

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener
    ) : super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }

}