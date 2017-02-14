package com.ubux.quadbubblemenu.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    private static final String TAG = CommonUtil.class.getSimpleName();

    /**
     * @param fName 文件名（null或0长度的字符串不合法）
     * @return true表示文件名合法；否则不合法
     */
    public static boolean isFileNameLegal(String fName){
        if(TextUtils.isEmpty(fName)){
            return false;
        }
        Pattern p = Pattern.compile("^[^/\\\\*]*$");
        Matcher m = p.matcher(fName);
        return m.matches();
    }


    public static void quietClose(Closeable c){
        if(c!=null){
            try{c.close();}catch (IOException ioe){}
        }
    }

    /**
     * 判断参数是否为null，如果是的话转换为空串，否则原样返回
     * @param str 需要转换的字符串
     * @return 原来的str或空串（如果str为null）
     */
    public static String nullToEmpty(String str){
        return str==null?"":str;
    }

    /**
     *
     * @param t 需要检查的实例
     * @param val 如果实例t为null，将返回的结果
     * @param <T>
     * @return 如果实例t不为null，返回t；否则返回指定的val
     */
    public static <T> T ifNull(T t, T val){
        return t==null ? val : t;
    }

    /**
     * @param options 获取到的图片的参数
     * @param reqWidth 不可超出的宽度。如果值非正，按照图片的固定宽度，即options内的宽度
     * @param reqHeight 不可超出的高度。如果值非正，按照图片的固定高度，即options内的高度
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        if(reqWidth<=0){
            reqWidth = width;
        }
        if(reqHeight<=0){
            reqHeight = height;
        }
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     *
     * @param bytes Bitmap对应的字节数组
     * @param reqWidth 不可超出的宽度。如果值非正，按照图片的固定宽度，即options内的宽度
     * @param reqHeight 不可超出的高度。如果值非正，按照图片的固定高度，即options内的高度
     * @return 从bytes中解析出的Bitmap实例
     */
    public static Bitmap scaleToRequestDimension(byte[] bytes, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     *
     * @param pathName Bitmap对应的文件的绝对路径
     * @param reqWidth 不可超出的宽度。如果值非正，按照图片的固定宽度，即options内的宽度
     * @param reqHeight 不可超出的高度。如果值非正，按照图片的固定高度，即options内的高度
     * @return 从bytes中解析出的Bitmap实例
     */
    public static Bitmap scaleToRequestDimension(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * 根据原Bitmap获得对应的圆形Bitmap。如果原Bitmap不是正方形，先基于其中心截取边长最大的正方形。
     * 将该正方形圆形化。
     * @param bm 原Bitmap
     * @return 转换后的Bitmap实例
     */
    public static Bitmap roundBitmap(Bitmap bm){
        final int bmW = bm.getWidth(), bmH = bm.getHeight();
        //创建一个空白的bitmap
        Bitmap output = Bitmap.createBitmap(bmW, bmH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();

        final int k = bmW<bmH?bmW:bmH;
        canvas.drawCircle(bmW/2, bmH/2, k/2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect bounds = new Rect(0, 0, bmW, bmH);
        canvas.drawBitmap(bm, bounds, bounds, paint);
        return output;
    }

    /**
     * Transform the given string to a Date instance according to the specific format.
     * @param dateStr the given string which represents a date
     * @param format the format. See {@link SimpleDateFormat}
     * @return transformed Date instance
     */
    public static Date dateFromString(String dateStr, String format, Locale locale){
        if(locale==null){
            return null;
        }
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(format, locale);
            return dateFormat.parse(dateStr);
        }catch (ParseException pe){
            return null;
        }
    }

    public static String getMonth(int calendarMonth){
        switch (calendarMonth){
            case Calendar.JANUARY:
                return "01";
            case Calendar.FEBRUARY:
                return "02";
            case Calendar.MARCH:
                return "03";
            case Calendar.APRIL:
                return "04";
            case Calendar.MAY:
                return "05";
            case Calendar.JUNE:
                return "06";
            case Calendar.JULY:
                return "07";
            case Calendar.AUGUST:
                return "08";
            case Calendar.SEPTEMBER:
                return "09";
            case Calendar.OCTOBER:
                return "10";
            case Calendar.NOVEMBER:
                return "11";
            case Calendar.DECEMBER:
                return "12";
            default:
                return "";
        }
    }

    public static String getDay(int calendarDay){
        if(calendarDay>0 && calendarDay<10){
            return "0"+calendarDay;
        }else {
            return String.valueOf(calendarDay);
        }
    }

    /**
     * 获取类对应的带完整包名的名称，如java.lang.Object
     * @param clz
     * @return 带完整包名的名称或null（如果clz为null）
     */
    public static String getAbsName(Class<?> clz){
        if(clz == null){
            return null;
        }
        return clz.getPackage().getName()+"."+clz.getSimpleName();
    }

    public static <T> T checkNotNull(T t){
        if(t==null){
            throw new RuntimeException("Target must not be null!");
        }
        return t;
    }

    public static <T> T checkNotNull(T t, String exceptionMsg){
        if(t==null){
            throw new RuntimeException(exceptionMsg==null?
                    "Target must not be null!":exceptionMsg);
        }
        return t;
    }

    public static void checkNotNull(Object... params){
        for (Object o: params)
            if (o==null)
                throw new RuntimeException("Parameters must not be null!");
    }

    public static String getDisplayDate(Date date){
        if (date==null){
            return null;
        }
        Date now = new Date();
        long minGap = now.getTime()-date.getTime()/60000;
        long hourGap = now.getTime()-date.getTime()/3600000;
        if(minGap < 60){
            return (minGap==0?"不到1":minGap) + "分钟前";
        }else if(hourGap < 24){
            return hourGap + "小时前";
        }else {
            Calendar oldCalendar = Calendar.getInstance(),
                    curCalendar = Calendar.getInstance();
            oldCalendar.setTime(date);
            curCalendar.setTime(now);
            if(oldCalendar.get(Calendar.YEAR) == curCalendar.get(Calendar.YEAR)){
                return CommonUtil.getMonth(oldCalendar.get(Calendar.MONTH)) +
                        "-"+
                        CommonUtil.getDay(oldCalendar.get(Calendar.DAY_OF_MONTH));
            }else {
                return oldCalendar.get(Calendar.YEAR)+
                        "-" +
                        CommonUtil.getMonth(oldCalendar.get(Calendar.MONTH)) +
                        "-"+
                        CommonUtil.getDay(oldCalendar.get(Calendar.DAY_OF_MONTH));
            }
        }
    }

    private CommonUtil(){}
}
