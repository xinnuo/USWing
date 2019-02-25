/**
 * created by 小卷毛, 2018/03/06
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

@file:Suppress("NOTHING_TO_INLINE")

package com.meida.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import com.meida.utils.PreferencesUtils
import java.io.Serializable

inline fun <reified T : Activity> Fragment.startActivityEx(vararg params: Pair<String, Any?>) =
    startActivity(Intent(this.activity, T::class.java).apply {
        if (params.isNotEmpty()) {
            params.forEach {
                val value = it.second
                when (value) {
                    null -> putExtra(it.first, null as Serializable?)
                    is Int -> putExtra(it.first, value)
                    is Long -> putExtra(it.first, value)
                    is CharSequence -> putExtra(it.first, value)
                    is String -> putExtra(it.first, value)
                    is Float -> putExtra(it.first, value)
                    is Double -> putExtra(it.first, value)
                    is Char -> putExtra(it.first, value)
                    is Short -> putExtra(it.first, value)
                    is Boolean -> putExtra(it.first, value)
                    is Serializable -> putExtra(it.first, value)
                    is Bundle -> putExtra(it.first, value)
                    is Parcelable -> putExtra(it.first, value)
                    is Array<*> -> when {
                        value.isArrayOf<CharSequence>() -> putExtra(it.first, value)
                        value.isArrayOf<String>() -> putExtra(it.first, value)
                        value.isArrayOf<Parcelable>() -> putExtra(it.first, value)
                        else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                    }
                    is IntArray -> putExtra(it.first, value)
                    is LongArray -> putExtra(it.first, value)
                    is FloatArray -> putExtra(it.first, value)
                    is DoubleArray -> putExtra(it.first, value)
                    is CharArray -> putExtra(it.first, value)
                    is ShortArray -> putExtra(it.first, value)
                    is BooleanArray -> putExtra(it.first, value)
                    else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                }
                return@forEach
            }
        }
    })

inline fun Fragment.getString(key: String, defaultValue: String = ""): String =
    PreferencesUtils.getString(this.activity, key, defaultValue)

inline fun Fragment.putString(key: String, vaule: String) =
    if (vaule == "null") PreferencesUtils.putString(this.activity, key, "")
    else PreferencesUtils.putString(this.activity, key, vaule)

inline fun Fragment.getBoolean(key: String, defaultValue: Boolean = false): Boolean =
    PreferencesUtils.getBoolean(this.activity, key, defaultValue)

inline fun Fragment.putBoolean(key: String, vaule: Boolean) =
    PreferencesUtils.putBoolean(this.activity, key, vaule)