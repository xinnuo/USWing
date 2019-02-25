/**
 * created by 小卷毛, 2019/1/10
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
package com.meida.base

import android.text.Editable
import android.text.TextWatcher

open class _TextWatcher : TextWatcher {

    private var _afterTextChanged: ((Editable) -> Unit)? = null

    override fun afterTextChanged(s: Editable) {
        _afterTextChanged?.invoke(s)
    }

    fun afterTextChanged(listener: (Editable) -> Unit) {
        _afterTextChanged = listener
    }

    private var _beforeTextChanged: ((CharSequence, Int, Int, Int) -> Unit)? = null

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        _beforeTextChanged?.invoke(s, start, count, after)
    }

    fun beforeTextChanged(listener: (CharSequence, Int, Int, Int) -> Unit) {
        _beforeTextChanged = listener
    }

    private var _onTextChanged: ((CharSequence, Int, Int, Int) -> Unit)? = null

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _onTextChanged?.invoke(s, start, before, count)
    }

    fun onTextChanged(listener: (CharSequence, Int, Int, Int) -> Unit) {
        _onTextChanged = listener
    }

}