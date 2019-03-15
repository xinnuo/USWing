package com.meida.utils

import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat

/**
 * <p>
 * 文件大小格式工具类
 * </p>
 */
object FileSizeHelper {

    /**
     * 获取文件指定文件的指定单位的大小
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型为B、KB、MB、GB
     * @return double值的大小
     */
    fun getFileOrFilesSize(filePath: String, sizeType: String) = kotlin.run {
        val file = File(filePath)
        FormetFileSize(if (file.isDirectory) getFileSizes(file) else getFileSize(file), sizeType)
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    fun getAutoFileOrFilesSize(filePath: String) = kotlin.run {
        val file = File(filePath)
        FormetFileSize(if (file.isDirectory) getFileSizes(file) else getFileSize(file))
    }

    /**
     * 获取指定文件大小
     */
    fun getFileSize(file: File) = kotlin.run {
        var size = 0L
        if (file.exists()) size = FileInputStream(file).channel.size()
        else file.createNewFile()
        size
    }

    /**
     * 获取指定文件夹
     */
    fun getFileSizes(file: File) = kotlin.run {
        var size = 0L
        file.listFiles().forEach {
            size += if (it.isDirectory) getFileSizes(it) else getFileSize(it)
        }
        size
    }

    /**
     * 转换文件大小
     */
    fun FormetFileSize(size: Long) = kotlin.run {
        val format = DecimalFormat("#.00")
        when {
            size == 0L -> "0B"
            size < 1024 -> "${format.format(size.toDouble())}B"
            size < 1048576 -> "${format.format(size.toDouble() / 1024)}KB"
            size < 1073741824 -> "${format.format(size.toDouble() / 1048576)}MB"
            else -> "${format.format(size.toDouble() / 1073741824)}GB"
        }
    }

    /**
     * 转换文件大小,指定转换的类型
     */
    fun FormetFileSize(size: Long, sizeType: String) = kotlin.run {
        val format = DecimalFormat("#.00")
        when (sizeType) {
            "B" -> format.format(size.toDouble()).toDouble()
            "KB" -> format.format(size.toDouble() / 1024).toDouble()
            "MB" -> format.format(size.toDouble() / 1048576).toDouble()
            "GB" -> format.format(size.toDouble() / 1073741824).toDouble()
            else -> 0.0
        }
    }

}