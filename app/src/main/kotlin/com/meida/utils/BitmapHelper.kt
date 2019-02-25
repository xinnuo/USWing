package com.meida.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.widget.ImageView

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapHelper {

    //dip转像素值
    fun Context.dip2px(d: Double) = (d * resources.displayMetrics.density + 0.5f).toInt()

    //像素值转dip
    fun Context.px2dip(pxValue: Float) = (pxValue / resources.displayMetrics.density + 0.5f).toInt()

    /***
     * 图片的缩放方法
     *
     * @param bgimage    ：源图片资源
     * @param newWidth   ：缩放后宽度
     * @param newHeight  ：缩放后高度
     */
    fun zoomImage(bgimage: Bitmap, newWidth: Double, newHeight: Double): Bitmap {

        //获取这个图片的宽和高
        val width = bgimage.width.toFloat()
        val height = bgimage.height.toFloat()

        //创建操作图片用的matrix对象
        val matrix = Matrix()

        //计算宽高缩放率
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        //缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bgimage, 0, 0, width.toInt(), height.toInt(), matrix, true)
    }

    /**
     * 根据指定宽度获取本地图片
     */
    fun getImgFromSD(file: File, width: Int): Bitmap? {
        return if (file.exists()) {
            val opts = Options()
            //读取图片尺寸，不加载到内存中
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, opts)

            //图片压缩比例
            opts.inSampleSize = opts.outWidth / width

            //读取图片，加载到内存中
            opts.inJustDecodeBounds = false
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } else null
    }

    /**
     *
     * 根据图片大小压缩图片
     *
     * @param pathString 图片绝对路径
     * @return Bitmap    压缩后图片
     */
    @Suppress("DEPRECATION")
    fun getDiskBitmap(pathString: String): Bitmap? {
        var bitmap: Bitmap? = null
        var bMapRotate: Bitmap? = null
        try {
            val file = File(pathString)
            if (file.exists()) {
                val opt = Options()
                opt.inPreferredConfig = Bitmap.Config.RGB_565
                opt.inPurgeable = true
                opt.inInputShareable = true
                opt.inTempStorage = ByteArray(1024 * 1024 * 10)
                val length = file.length()
                when {
                    length / (1024 * 1024) > 4 -> opt.inSampleSize = 16
                    length / (1024 * 1024) >= 1 -> opt.inSampleSize = 8
                    length / (1024 * 512) >= 1 -> opt.inSampleSize = 4
                    length / (1024 * 256) >= 1 -> opt.inSampleSize = 2
                    else -> opt.inSampleSize = 1
                }
                bitmap = BitmapFactory.decodeFile(pathString, opt)
                val orientation = getDegress(pathString)
                /*
				 * if(bitmap.getHeight() < bitmap.getWidth()){ orientation = 90;
				 * } else { orientation = 0; }
				 */
                bMapRotate = if (orientation != 0) {
                    val matrix = Matrix()
                    matrix.postRotate(orientation.toFloat())
                    Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else {
                    Bitmap.createScaledBitmap(bitmap!!, bitmap.width, bitmap.height, true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bMapRotate ?: bitmap
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return     degree旋转的角度
     */
    fun getDegress(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    /**
     * 旋转图片
     *
     * @param bitmap  图片
     * @param degress 度数
     * @return Bitmap  返回类型
     */
    fun rotateBitmap(bitmap: Bitmap?, degress: Int): Bitmap? {
        return if (bitmap != null) {
            val m = Matrix()
            m.postRotate(degress.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        } else null
    }

    /**
     * 图片按比例大小压缩方法（根据路径获取图片并压缩）
     *
     * @param srcPath    图片路径
     * @param mWidth     图片压缩最大宽度
     * @param mHeight    图片压缩最大高度
     * @param maxkb      一般设置为100kb
     * @return Bitmap    返回类型
     */
    fun getImage(
        srcPath: String,
        maxkb: Int = 100,
        mWidth: Float = 480f,
        mHeight: Float = 800f
    ): Bitmap? {

        val newOpts = Options()

        //开始读入图片，此时把options.inJustDecodeBounds 设回true
        newOpts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(srcPath, newOpts) //此时返回bm为空

        val w = newOpts.outWidth
        val h = newOpts.outHeight

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var be = when {
            //如果宽度大的话根据宽度固定大小缩放
            w > h && w > mWidth -> (newOpts.outWidth / mWidth).toInt()
            //如果高度高的话根据宽度固定大小缩放
            w < h && h > mHeight -> (newOpts.outHeight / mHeight).toInt()
            //be=1表示不缩放
            else -> 1
        }
        if (be <= 0) be = 1

        //设置缩放比例
        newOpts.inSampleSize = be

        //重新读入图片，注意此时把options.inJustDecodeBounds 设回false
        newOpts.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(srcPath, newOpts)
        return compressImage(bitmap, maxkb) //压缩好比例大小后再进行质量压缩
    }

    /**
     * 图片按比例大小压缩方法（根据Bitmap图片压缩）
     *
     * @param image    图片bitmap
     * @return Bitmap  返回类型
     */
    fun compressImage(
        image: Bitmap,
        maxkb: Int = 100,
        mWidth: Float = 480f,
        mHeight: Float = 800f
    ): Bitmap? {

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        //判断如果图片大于1M，进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().size / 1024 > 1024) {
            baos.reset()                                                 //重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos) //这里压缩50%，把压缩后的数据存放到baos中
        }

        image.recycle()
        var isBm = ByteArrayInputStream(baos.toByteArray())
        val newOpts = Options()

        //开始读入图片，此时把options.inJustDecodeBounds 设回true
        newOpts.inJustDecodeBounds = true
        BitmapFactory.decodeStream(isBm, null, newOpts)

        val w = newOpts.outWidth
        val h = newOpts.outHeight

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var be = when {
            //如果宽度大的话根据宽度固定大小缩放
            w > h && w > mWidth -> (newOpts.outWidth / mWidth).toInt()
            //如果高度高的话根据宽度固定大小缩放
            w < h && h > mHeight -> (newOpts.outHeight / mHeight).toInt()
            //be=1表示不缩放
            else -> 1
        }
        if (be <= 0) be = 1

        //设置缩放比例
        newOpts.inSampleSize = be

        //重新读入图片，注意此时把options.inJustDecodeBounds 设回false
        newOpts.inJustDecodeBounds = false
        isBm = ByteArrayInputStream(baos.toByteArray())
        val bitmap = BitmapFactory.decodeStream(isBm, null, newOpts)
        return compressImage(bitmap!!, maxkb) //压缩好比例大小后再进行质量压缩
    }

    /**
     * 质量压缩方法(如果图片大于指定的大小，循环压缩)
     *
     * @param image    图片bitmap
     * @param maxkb    图片大小（一般为100k）
     * @return Bitmap  返回类型
     */
    fun compressImage(image: Bitmap, maxkb: Int): Bitmap? {
        val baos = ByteArrayOutputStream()
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        var options = 100
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().size / 1024 > maxkb) {
            //重置baos即清空baos
            baos.reset()
            //每次都减少10
            options -= 10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)
        }

        image.recycle()
        val isBm = ByteArrayInputStream(baos.toByteArray())
        return BitmapFactory.decodeStream(isBm, null, null)
    }

    /**
     * 给定图片维持宽高比缩放后，截取正中间的正方形部分
     *
     * @param bitmap      原图
     * @param edgeLength  希望得到的正方形部分的边长
     * @return  缩放截取正中部分后的位图。
     */
    fun centerSquareScaleBitmap(bitmap: Bitmap?, edgeLength: Int): Bitmap? {

        if (null == bitmap || edgeLength <= 0) return null

        var result = bitmap
        val widthOrg = bitmap.width
        val heightOrg = bitmap.height

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            //压缩到一个最小长度是edgeLength的bitmap
            val longerEdge =
                edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg)
            val scaledWidth = if (widthOrg > heightOrg) longerEdge else edgeLength
            val scaledHeight = if (widthOrg > heightOrg) edgeLength else longerEdge
            val scaledBitmap: Bitmap

            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
            } catch (e: Exception) {
                return null
            }

            //从图中截取正中间的正方形部分。
            val xTopLeft = (scaledWidth - edgeLength) / 2
            val yTopLeft = (scaledHeight - edgeLength) / 2

            try {
                result =
                    Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength)
                scaledBitmap.recycle()
            } catch (e: Exception) {
                return null
            }

        }

        return result
    }

    /**
     * 把byte[] 转换 Bitmap
     */
    fun Bytes2Bitmap(b: ByteArray) = if (b.isNotEmpty()) BitmapFactory.decodeByteArray(b, 0, b.size) else null

    /**
     * 把Bitmap转换 byte[]
     */
    fun Bitmap2Bytes(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @param filePic
     * @return
     */
    fun saveBitmap(mBitmap: Bitmap?, filePic: File): String {
        if (mBitmap == null) return ""

        try {
            if (!filePic.exists()) {
                filePic.parentFile.mkdirs()
                filePic.createNewFile()
            }
            val fos = FileOutputStream(filePic)
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

        return filePic.absolutePath
    }

    /**
     * 该函数会随机选择一帧抓取，如果想要指定具体时间的缩略图，
     * 可以用函数getFrameAtTime(long timeUs), getFrameAtTime(long timeUs, int option)
     *
     * @param filePath 图片路径
     * @return bitmap
     */
    fun getVideoThumbnail(filePath: String): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            bitmap = retriever.frameAtTime
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }
}
