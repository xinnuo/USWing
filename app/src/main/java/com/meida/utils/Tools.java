package com.meida.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Tools {

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * Gets the content:// URI  from the given corresponding path to a file
     *
     * @param context   上下文
     * @param imageFile path
     * @return content  Uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");

            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取版本名称
     */
    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取软件版本号
     */
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            verCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return verCode;
    }

    /**
     * 根据包名启动一个App
     *
     * @param context     上下文
     * @param packagename 参数
     */
    public static void openAppWithPackageName(Context context, String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (packageinfo == null) return;

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    /**
     * 结束正在运行服务的方法
     *
     * @param mContext     上下文
     * @param serviceClass 服务的类名
     */
    public static void stopService(Context mContext, Class<?> serviceClass) {
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() > 0) {
            for (int i = 0; i < myList.size(); i++) {
                String mName = myList.get(i).service.getClassName();
                if (mName.equals(serviceClass.getName())) {
                    mContext.stopService(new Intent(mContext, serviceClass));
                    break;
                }
            }
        }
    }

    /**
     * 图片转字符串
     *
     * @param bitmap 图片
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) try {
                baos.close();
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * 图片转字符串
     *
     * @param bitmap 图片
     */
    public static String bitmapToBase64(Bitmap bitmap, int quality) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) try {
                baos.close();
            } catch (Exception ignored) {
            }
            if (bitmap != null) bitmap.recycle();
        }
        return result;
    }

    /**
     * 根据图片路径转字符串
     *
     * @param path 图片路径
     */
    public static String bitmapToBase64(String path) {
        String result = null;
        ByteArrayOutputStream baos = null;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                Log.i("toByteArray", baos.toByteArray().length + "");
                int options = 100;
                //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                while (baos.toByteArray().length / 1024 > 100) {
                    Log.i("toByteArray", baos.toByteArray().length + "");
                    //重置baos即清空baos
                    baos.reset();
                    //每次都减少10
                    options -= 10;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                }
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) try {
                baos.flush();
                baos.close();
            } catch (Exception ignored) {
            }
            if (bitmap != null) bitmap.recycle();
        }
        return result;
    }

    /**
     * 根据图片路径转字符串
     *
     * @param path    图片路径
     * @param quality 图片质量
     */
    public static String bitmapToBase64(String path, int quality) {
        String result = null;
        ByteArrayOutputStream baos = null;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) bitmap.recycle();
        }
        return result;
    }

    /**
     * 将base64字符串转换成Bitmap类型
     *
     * @param str base64字符串
     */
    public static Bitmap strToBitmap(String str) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(str, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 根据view来生成bitmap图片，可用于截图功能
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);
        // 能画缓存就返回false
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) v.destroyDrawingCache();
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

    /**
     * 将文件转为String对象
     *
     * @param path 文件路径
     */
    public static String readStream(String path) {
        InputStream inStream = null;
        ByteArrayOutputStream outStream = null;
        String mImage = null;
        try {
            File file = new File(path);
            inStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            outStream = new ByteArrayOutputStream();
            try {
                while ((len = inStream.read(buffer)) != -1)
                    outStream.write(buffer, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = outStream.toByteArray();
            mImage = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) try {
                outStream.close();
            } catch (Exception ignored) {
            }
            if (inStream != null) try {
                inStream.close();
            } catch (Exception ignored) {
            }
        }
        return mImage;
    }

    /**
     * 将中文转为Unicode
     *
     * @param gbString 中文字符串
     */
    public static String gbEncoding(final String gbString) {
        if (TextUtils.isEmpty(gbString)) return "";

        char[] utfBytes = gbString.toCharArray();
        StringBuilder unicodeBytes = new StringBuilder();
        for (char utfByte : utfBytes) {
            String hexB = Integer.toHexString(utfByte); //转换为16进制整型字符串
            if (hexB.length() == 3) hexB = "0" + hexB;
            if (hexB.length() <= 2) hexB = "00" + hexB;
            unicodeBytes.append("\\u").append(hexB);
        }
        return unicodeBytes.toString();
    }

    /**
     * 将Unicode转为中文
     *
     * @param dataStr Unicode字符串
     */
    public static String decodeUnicode(final String dataStr) {
        if (TextUtils.isEmpty(dataStr)) return "";

        int start = 0, end;
        final StringBuilder buffer = new StringBuilder();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = dataStr.substring(start + 2, end == -1 ? dataStr.length() : end);
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(Character.valueOf(letter).toString());
            start = end;
        }
        return buffer.toString();
    }

    /**
     * 改变TabLayout下标宽度，仅适用于v27及以下
     */
    public static void setIndicator(TabLayout tabs, int leftDip, int rightDip) {
        Class<?> tabLayout = tabs.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayout.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        if (tabStrip == null) return;
        tabStrip.setAccessible(true);
        LinearLayout llTab = null;
        try {
            llTab = (LinearLayout) tabStrip.get(tabs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip, Resources.getSystem().getDisplayMetrics());
        int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip, Resources.getSystem().getDisplayMetrics());

        if (llTab == null) return;
        for (int i = 0; i < llTab.getChildCount(); i++) {
            View child = llTab.getChildAt(i);
            child.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            params.leftMargin = left;
            params.rightMargin = right;
            child.setLayoutParams(params);
            child.invalidate();
        }
    }

    public static ArrayList<String> jsonArrayToList(String json) {

        ArrayList<String> list = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) list.add(arr.getString(i));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

}
