package com.meida.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static java.util.TimeZone.getDefault;

public class TimeHelper {

    /**
     * 1s==1000ms
     */
    private final static int TIME_MILLISECONDS = 1000;
    /**
     * 时间中的分、秒最大值均为60
     */
    private final static int TIME_NUMBERS = 60;
    /**
     * 时间中的小时最大值
     */
    private final static int TIME_HOURSES = 24;

    public static TimeHelper getInstance() {
        return new TimeHelper();
    }

    /**
     * 毫秒转化时分秒毫秒
     */
    public static String formatSecondTime(Long ms) {
        Integer mi = TIME_MILLISECONDS * TIME_NUMBERS;
        Integer hh = TIME_MILLISECONDS * TIME_NUMBERS;
        Integer dd = TIME_MILLISECONDS * TIME_HOURSES;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / TIME_MILLISECONDS;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * TIME_MILLISECONDS;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append("天");
        } else {
            sb.append("0天");
        }
        if (hour > 0) {
            sb.append(hour).append("时");
        } else {
            sb.append("0时");
        }
        if (minute > 0) {
            sb.append(minute).append("分");
        } else {
            sb.append("0分");
        }
        if (second > 0) {
            sb.append(second).append("秒");
        } else {
            sb.append("0秒");
        }
        /*if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }*/
        return sb.toString();
    }

    /**
     * 将总秒数转换为时分秒表达形式
     *
     * @param seconds 任意秒数
     * @return %s时%s分%s秒
     */
    public static String formatTime(long seconds) {
        long hh = seconds / TIME_NUMBERS / TIME_NUMBERS;
        long mm = (seconds - hh * TIME_NUMBERS * TIME_NUMBERS) > 0 ? (seconds - hh * TIME_NUMBERS * TIME_NUMBERS) / TIME_NUMBERS : 0;
        long ss = seconds < TIME_NUMBERS ? seconds : seconds % TIME_NUMBERS;
        return (hh == 0 ? "" : (hh < 10 ? "0" + hh : hh) + "时")
                + (mm == 0 ? "" : (mm < 10 ? "0" + mm : mm) + "分")
                + (ss == 0 ? "" : (ss < 10 ? "0" + ss : ss) + "秒");
    }

    /**
     * 将GMT（格林尼治标准时间）日期格式化为系统默认时区的日期字符串表达形式
     *
     * @param gmtUnixTime GTM（格林尼治标准时间）时间戳
     * @param format      格式化字符串
     * @return 日期字符串，如"yyyy-MM-dd HH:mm:ss"
     */
    public static String formatGMTUnixTime(long gmtUnixTime, String format) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(gmtUnixTime + getDefault().getRawOffset());
    }

    /**
     * 将系统默认时区的Unix时间戳转换为GMT（格林尼治标准时间）Unix时间戳
     *
     * @param unixTime unix时间戳
     * @return GMT（格林尼治标准时间）Unix时间戳
     */
    public static long getGMTUnixTime(long unixTime) {
        return unixTime - getDefault().getRawOffset();
    }

    /**
     * 将GMT（格林尼治标准时间）Unix时间戳转换为系统默认时区的Unix时间戳
     *
     * @param gmtUnixTime GMT（格林尼治标准时间）Unix时间戳
     * @return 系统默认时区的Unix时间戳
     */
    public static long getCurrentTimeZoneUnixTime(long gmtUnixTime) {
        return gmtUnixTime + getDefault().getRawOffset();
    }

    /**
     * 获取当前时间的GMT（格林尼治标准时间）Unix时间戳
     *
     * @return 当前的GMT（格林尼治标准时间）Unix时间戳
     */
    public static long getGMTUnixTimeByCalendar() {
        Calendar calendar = Calendar.getInstance();
        // 获取当前时区下日期时间对应的时间戳
        long unixTime = calendar.getTimeInMillis();
        // 获取标准格林尼治时间下日期时间对应的时间戳
        return unixTime - getDefault().getRawOffset();
    }

    /**
     * 获取当前时区的Unix时间戳
     *
     * @return 当前的Unix时间戳
     */
    public static long getUnixTimeByCalendar() {
        Calendar calendar = Calendar.getInstance();
        // 获取当前时区下日期时间对应的时间戳
        return calendar.getTimeInMillis();
    }

    /**
     * 获取更改时区后的时间
     *
     * @param date    时间
     * @param oldZone 旧时区
     * @param newZone 新时区
     * @return 时间
     */
    public static Date changeTimeZone(Date date, TimeZone oldZone, TimeZone newZone) {
        Date dateTmp = null;
        if (date != null) {
            int timeOffset = oldZone.getRawOffset() - newZone.getRawOffset();
            dateTmp = new Date(date.getTime() - timeOffset);
        }
        return dateTmp;
    }

    /**
     * 获取当前时间距离指定日期时差的大致表达形式
     *
     * @param milliseconds 日期毫秒
     * @return 时差的大致表达形式
     */
    public static String getDiffTime(long milliseconds) {
        String strTime;
        long time = Math.abs(new Date().getTime() - milliseconds);
        // 一分钟以内
        if (time < TIME_NUMBERS * TIME_MILLISECONDS) {
            strTime = time / TIME_MILLISECONDS + "秒前";
        } else {
            int min = (int) (time / TIME_MILLISECONDS / TIME_NUMBERS);
            if (min < TIME_NUMBERS) {
                strTime = min + "分钟前";
            } else {
                int hh = min / TIME_NUMBERS;
                if (hh < TIME_HOURSES) {
                    strTime = hh + "小时前";
                } else {
                    int days = hh / TIME_HOURSES;
                    if (days <= 30) {
                        if (days == 1) strTime = "昨天";
                        else if (days == 2) strTime = "前天";
                        else strTime = days + "天前";
                    } else {
                        if (days / 365 < 1) {
                            strTime = days / 30 + "月前";
                        } else {
                            strTime = days / 365 + "年前";
                        }
                    }
                }
            }
        }

        return strTime;
    }

    /**
     * 判断是否润年
     */
    public boolean isLeapYear(String ddate) {
        /**
         * 详细设计：
         *     1.被400整除是闰年
         *     2.不能被4整除则不是闰年
         *     3.能被4整除同时不能被100整除则是闰年
         */
        Date d = strToDate(ddate);
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(d);
        int year = gc.get(Calendar.YEAR);
        return (year % 400) == 0 || (year % 4) == 0 && (year % 100) != 0;
    }

    /**
     * 获取现在时间
     */
    public Date getNow() {
        return new Date();
    }

    /**
     * 获取现在时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public Date getNowDate() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String dateString = formatter.format(getNow());
        ParsePosition pos = new ParsePosition(8);
        return formatter.parse(dateString, pos);
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间格式 yyyy-MM-dd
     */
    public Date getNowDateShort() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = formatter.format(getNow());
        ParsePosition pos = new ParsePosition(8);
        return formatter.parse(dateString, pos);
    }

    /**
     * 获取现在时间
     *
     * @return 返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public String getStringDate() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(getNow());
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd
     */
    public String getStringDateShort() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(getNow());
    }

    /**
     * 获取现在时间  小时:分:秒 HH:mm:ss
     */
    public String getTimeShort() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(getNow());
    }

    /**
     * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写。
     */
    public String getNowTime(String pattern) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(getNow());
    }

    /**
     * 根据用户传入的GMT Unix时间秒，返回指定的时间格式
     */
    public String getGMTFormatedDateTime(String pattern, long dateTime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(getCurrentTimeZoneUnixTime(dateTime * 1000)));
    }

    /**
     * 根据用户传入的默认时区秒，返回指定的时间格式
     */
    public String getFormatedDateTime(String pattern, long dateTime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(dateTime * 1000));
    }

    /**
     * 根据用户传入的毫秒，返回指定的时间格式
     */
    public String getSecondFormatedDateTime(String pattern, long dateTime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(dateTime));
    }

    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     */
    public Date strToDateLong(String strDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }

    /**
     * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
     */
    public String dateToStrLong(Date dateDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(dateDate);
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     */
    public Date strToDate(String strDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }

    /**
     * 将短时间格式时间转换为字符串 yyyy-MM-dd
     */
    public String dateToStr(Date dateDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(dateDate);
    }

    /**
     * 根据用户传入的时间，返回指定时间的格式
     *
     * @param format  转换前的时间格式
     * @param nowTime 需要转换的时间
     */
    public String stringToString(String format, String nowTime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf1 = new SimpleDateFormat(format);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf1.parse(nowTime);
            return sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return nowTime;
        }
    }

    /**
     * 根据用户传入指定格式的时间，返回指定时间的格式
     *
     * @param sFormat 转换前的时间格式
     * @param eFormat 转换后的时间格式
     * @param nowTime 需要转换的时间
     */
    public String stringToString(String sFormat, String eFormat, String nowTime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf1 = new SimpleDateFormat(sFormat);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf2 = new SimpleDateFormat(eFormat);
        try {
            Date date = sdf1.parse(nowTime);
            return sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return nowTime;
        }
    }

    /**
     * 根据用户传入的时间，返回指定时间的毫秒值
     *
     * @param nowtime 指定时间
     * @return long   返回类型
     */
    public long stringToLong(String nowtime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time;
        try {
            Date date = sdf.parse(nowtime);
            time = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return time;
    }

    /**
     * 根据用户传入的时间和格式，返回指定时间的毫秒值
     *
     * @param format  格式
     * @param nowtime 指定时间
     * @return long   返回类型
     */
    public long stringToLong(String format, String nowtime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        long time;
        try {
            Date date = sdf.parse(nowtime);
            time = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return time;
    }

    /**
     * 根据用户传入的时间，返回指定时间的秒值
     *
     * @param nowtime 指定时间
     * @return long   返回类型
     */
    public long stringToLongSecond(String nowtime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time;
        try {
            Date date = sdf.parse(nowtime);
            time = date.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return time;
    }

    /**
     * 根据用户传入的时间和格式，返回指定时间的秒值
     *
     * @param format  格式
     * @param nowtime 指定时间
     * @return long   返回类型
     */
    public long stringToLongSecond(String format, String nowtime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        long time;
        try {
            Date date = sdf.parse(nowtime);
            time = date.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return time;
    }

    /**
     * 二个小时时间间的差值,必须保证二个时间都是"HH:MM"的格式，返回字符型的分钟
     */
    public String getTwoHour(String st1, String st2) {
        String[] kk;
        String[] jj;
        kk = st1.split(":");
        jj = st2.split(":");
        if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0]))
            return "0";
        else {
            double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1]) / 60;
            double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1]) / 60;
            if ((y - u) > 0)
                return y - u + "";
            else
                return "0";
        }
    }

    /**
     * 返回指定时间前（后）推几分钟的时间,其中JJ表示分钟.
     */
    public String getPreTime(String sj1, String jj) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mydate1 = "";
        try {
            Date date1 = format.parse(sj1);
            long Time = (date1.getTime() / 1000) + Integer.parseInt(jj) * 60;
            date1.setTime(Time * 1000);
            mydate1 = format.format(date1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mydate1;
    }

    /**
     * 得到一个时间前（后）移几天的时间,nowdate为时间 yyyy-MM-dd,delay为前（后）移的日期
     */
    public String getNextDay(String nowdate, String delay) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String mdate;
            Date d = strToDate(nowdate);
            long myTime = (d.getTime() / 1000) + Integer.parseInt(delay) * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            mdate = format.format(d);
            return mdate;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 得到一个时间前（后）移几天的时间,nowdate为时间 yyyy-MM-dd,delay为前（后）移的日期
     */
    public String getNextDay(String nowdate, int day) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            Date d = strToDate(nowdate);
            long myTime = (d.getTime() / 1000) + day * 24 * 60 * 60;
            d.setTime(myTime * 1000);

            return format.format(d);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 得到一个时间前（后）移几天的时间，nowdate为时间指定格式的时间，delay为前（后）移的日期
     */
    public String getNextDay(String nowdate, int day, String pattern) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            Date d = format.parse(nowdate, new ParsePosition(0));

            long myTime = (d.getTime() / 1000) + day * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            return format.format(d);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获得指定几个月后（前）的日期 参数负值为月前
     */
    public String getAfterMonth(String str, int month) {
        Calendar c = Calendar.getInstance(); //获得一个日历的实例
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(str); //初始日期
        } catch (Exception e) {
            e.printStackTrace();
        }
        c.setTime(date); //设置日历时间
        c.add(Calendar.MONTH, month);
        return sdf.format(c.getTime());
    }

    /**
     * 获得指定格式的几个月后（前）的日期 参数负值为月前
     */
    public String getAfterMonth(String str, int month, String pattern) {
        Calendar c = Calendar.getInstance(); //获得一个日历的实例
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(str); //初始日期
        } catch (Exception e) {
            e.printStackTrace();
        }
        c.setTime(date); //设置日历时间
        c.add(Calendar.MONTH, month);
        return sdf.format(c.getTime());
    }

    /**
     * 得到两个日期间的间隔天数
     */
    public String getTwoDay(String start, String end) {
        if (start == null || TextUtils.isEmpty(start)) return "";
        if (end == null || TextUtils.isEmpty(end)) return "";

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        long day;
        try {
            Date date = myFormatter.parse(start);
            Date mydate = myFormatter.parse(end);
            day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            return "";
        }
        return day + "";
    }

    /**
     * 两个时间之间的天数
     */
    public long getDays(String date_start, String date_end) {
        if (date_start == null || TextUtils.isEmpty(date_start)) return 0;
        if (date_end == null || TextUtils.isEmpty(date_end)) return 0;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date_s = null;
        Date date_e = null;
        try {
            date_s = formatter.parse(date_start);
            date_e = formatter.parse(date_end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert date_s != null;
        assert date_e != null;
        return (date_e.getTime() - date_s.getTime()) / (24 * 60 * 60 * 1000);
    }

    /**
     * 根据一个日期，返回是星期几的字符串
     */
    @SuppressLint("SimpleDateFormat")
    public String getWeek(String sdate) {
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // int hour=c.get(Calendar.DAY_OF_WEEK);
        // hour 中存的就是星期几了，其范围 1~7
        // 1=星期日 7=星期六，其他类推
        return new SimpleDateFormat("EEEE").format(c.getTime());
    }

    /**
     * 返回当天和本周星期一相差的天数
     * 日：1，一：0，二：-1，三：-2，四：-3，五：-4，六：-5
     */
    private int getMondayPlus() {
        Calendar cd = Calendar.getInstance();
        // 获得今天是一周的第几天，星期日是第一天，星期一是第二天......
        // 因为按中国礼拜一作为第一天所以这里减1
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1;
        return dayOfWeek == 1 ? 0 : 1 - dayOfWeek;
    }

    /**
     * 获得当天是本周的第几天
     * 0：日，1：一，2：二，3：三，4：四，5：五，6：六
     */
    public int getDayOfWeek() {
        Calendar cd = Calendar.getInstance();
        // 获得今天是一周的第几天，星期日是第一天，星期一是第二天......
        // 因为按中国礼拜一作为第一天所以这里减1
        return cd.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 返回本周一的日期
     */
    public String getMondayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回本周二的日期
     */
    public String getTuesdayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 1);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回本周三的日期
     */
    public String getWednesdayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 2);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回本周四的日期
     */
    public String getThursdayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 3);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回本周五的日期
     */
    public String getFridayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 4);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回本周六的日期
     */
    public String getSaturdayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 5);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回本周日的日期（中国日历）
     */
    public String getSundayOFWeek() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 6);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期一的日期
     */
    public String getAnyWeekMonday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期二的日期
     */
    public String getAnyWeekTuesday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 1);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期三的日期
     */
    public String getAnyWeekWednesday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 2);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期四的日期
     */
    public String getAnyWeekThursday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 3);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期五的日期
     */
    public String getAnyWeekFriday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 4);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期六的日期
     */
    public String getAnyWeekSaturday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 5);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期日的日期（中国日历）
     */
    public String getAnyWeekSunday(int weeks) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 6);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回几周前（后）星期几的日期（0一~6日）（中国日历）
     */
    public String getAnyWeekDay(int weeks, int day) {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + day);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回上周日的日期
     */
    public String getPreviousWeekSunday() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus - 1);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回上周一的日期
     */
    public String getPreviousWeekday() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus - 7);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回下周一的日期
     */
    public String getNextMonday() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 返回下周日的日期
     */
    public String getNextSunday() {
        int mondayPlus = this.getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 + 6);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(monday);
    }

    /**
     * 获得指定日期的年份
     */
    public int getYearOfDate(String time) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(time);

            cd.setTime(date);
            return cd.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获得指定日期的年份
     */
    public int getYearOfDate(String time, String pattern) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(time);

            cd.setTime(date);
            return cd.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获得指定日期的月份
     */
    public int getMonthOfDate(String time) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(time);

            cd.setTime(date);
            return cd.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获得指定日期的月份
     */
    public int getMonthOfDate(String time, String pattern) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(time);

            cd.setTime(date);
            return cd.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获得指定日期的天数
     */
    public int getDayOfDate(String time) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(time);

            cd.setTime(date);
            return cd.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获得指定日期的天数
     */
    public int getDayOfDate(String time, String pattern) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(time);

            cd.setTime(date);
            return cd.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获得本月某一天的日期
     */
    public String getDayOfMonth(String day) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.set(Calendar.DATE, Integer.parseInt(day)); //设为当前月的某天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得本月某一天的日期
     */
    public String getDayOfMonth(int day) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.set(Calendar.DATE, day); //设为当前月的某天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得本月第一天的日期
     */
    public String getDayOfMonthFirst() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.set(Calendar.DATE, 1); //设为当前月的1号

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得本月最后一天的日期
     */
    public String getDayOfMonthEnd() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.set(Calendar.DATE, 1);            //设为当前月的1号
        lastDate.roll(Calendar.DATE, -1); //日期回滚一天，也就是本月最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得上个月第一天的日期
     */
    public String getPreviousMonthFirst() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, -1); //减一个月
        lastDate.set(Calendar.DATE, 1);            //把日期设置为当月第一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得上个月最后一天的日期
     */
    public String getPreviousMonthEnd() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, -1); //减一个月
        lastDate.set(Calendar.DATE, 1);            //把日期设置为当月第一天
        lastDate.roll(Calendar.DATE, -1); //日期回滚一天，也就是本月最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得下个月第一天的日期
     */
    public String getNextMonthFirst() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, 1); //加一个月
        lastDate.set(Calendar.DATE, 1);           //把日期设置为当月第一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得下个月最后一天的日期
     */
    public String getNextMonthEnd() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, 1);  //加一个月
        lastDate.set(Calendar.DATE, 1);            //把日期设置为当月第一天
        lastDate.roll(Calendar.DATE, -1); //日期回滚一天，也就是本月最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得指定几个月后（前）的第一天日期
     */
    public String getAnyMonthFirst(int amount) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, amount); //正数为月后，负数为月前
        lastDate.set(Calendar.DATE, 1);       //把日期设置为当月第一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得指定几个月后（前）的当天天日期
     */
    public String getAnyMonthNow(int amount) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, amount); //正数为月后，负数为月前

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得指定几个月后（前）的最后一天日期
     */
    public String getAnyMonthEnd(int amount) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.MONTH, amount);      //正数为月后，负数为月前
        lastDate.set(Calendar.DATE, 1);            //把日期设置为当月第一天
        lastDate.roll(Calendar.DATE, -1); //日期回滚一天，也就是本月最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得去年第一天的日期
     */
    public String getPreviousYearFirst() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.YEAR, -1); //减一年
        lastDate.set(Calendar.DAY_OF_YEAR, 1);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得去年最后一天的日期
     */
    public String getPreviousYearEnd() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.YEAR, -1);         //减一年
        lastDate.set(Calendar.DAY_OF_YEAR, 1);            //把日期设置为当年第一天
        lastDate.roll(Calendar.DAY_OF_YEAR, -1); //日期回滚一天，也就是本年最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得明年第一天的日期
     */
    public String getNextYearFirst() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.YEAR, 1); //加一年
        lastDate.set(Calendar.DAY_OF_YEAR, 1);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得明年最后一天的日期
     */
    public String getNextYearEnd() {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.YEAR, 1);          //加一个年
        lastDate.set(Calendar.DAY_OF_YEAR, 1);            //把日期设置为当年第一天
        lastDate.roll(Calendar.DAY_OF_YEAR, -1); //日期回滚一天，也就是本年最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得指定几年后（前）第一天的日期
     */
    public String getAnyYearFirst(int amount) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.YEAR, amount); //正数为年后，负数为年前
        lastDate.set(Calendar.DAY_OF_YEAR, 1);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得指定几年后（前）最后一天的日期
     */
    public String getAnyYearEnd(int amount) {
        Calendar lastDate = Calendar.getInstance();

        lastDate.add(Calendar.YEAR, amount);              //正数为年后，负数为年前
        lastDate.set(Calendar.DAY_OF_YEAR, 1);            //把日期设置为当年第一天
        lastDate.roll(Calendar.DAY_OF_YEAR, -1); //日期回滚一天，也就是本年最后一天

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastDate.getTime());
    }

    /**
     * 获得指定日期几年后（前）的日期
     */
    public String getAnyYear(String time, int amount) {
        try {
            Calendar cd = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(time);
            cd.setTime(date);
            cd.add(Calendar.YEAR, amount); //正数为年后，负数为年前

            return sdf.format(cd.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 获得本年有多少天
     */
    public int getMaxDaysOfYear() {
        Calendar cd = Calendar.getInstance();
        cd.set(Calendar.DAY_OF_YEAR, 1);            //把日期设为当年第一天
        cd.roll(Calendar.DAY_OF_YEAR, -1); //把日期回滚一天。
        return cd.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取某年某月的最后一天
     *
     * @param year  年
     * @param month 月
     * @return 最后一天
     */
    private int getLastDayOfMonth(int year, int month) {
        if (month == 1
                || month == 3
                || month == 5
                || month == 7
                || month == 8
                || month == 10
                || month == 12) {
            return 31;
        }
        if (month == 4
                || month == 6
                || month == 9
                || month == 11) {
            return 30;
        }
        if (month == 2) {
            return isLeapYear(year) ? 29 : 28;
        }
        return 0;
    }

    /**
     * 是否闰年
     */
    public boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
