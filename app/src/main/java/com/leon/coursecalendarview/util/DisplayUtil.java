package com.leon.coursecalendarview.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * dp(dip): device independent pixels(设备独立像素). 不同设备有不同的显示效果,这个和设备硬件有关，一般我们为了支持WVGA、HVGA和QVGA 推荐使用这个，不依赖像素。
 * dp也就是dip，这个和sp基本类似。如果设置表示长度、高度等属性时可以使用dp 或sp。但如果设置字体，需要使用sp。dp是与密度无关，sp除了与密度无关外，还与scale无关。如果屏幕密度为160，这时dp和sp和px是一 样的。1dp=1sp=1px，但如果使用px作单位，如果屏幕大小不变（假设还是3.2寸），而屏幕密度变成了320。那么原来TextView的宽度 设成160px，在密度为320的3.2寸屏幕里看要比在密度为160的3.2寸屏幕上看短了一半。但如果设置成160dp或160sp的话。系统会自动 将width属性值设置成320px的。也就是160 * 320 / 160。其中320 / 160可称为密度比例因子。也就是说，如果使用dp和sp，系统会根据屏幕密度的变化自动进行转换。
 * px: pixels(像素). 不同设备显示效果相同，一般我们HVGA代表320x480像素，这个用的比较多。
 * pt: point，是一个标准的长度单位，1pt＝1/72英寸，用于印刷业，非常简单易用；
 * sp: scaled pixels(放大像素). 主要用于字体显示best for textsize。
 * 转换代码如下
 */

/**
 * dp、sp 转换为 px 的工具类
 */
public class DisplayUtil {

    private static float density;
    private static float scaledDensity;

    private static void setDensity(Context context) {
        if (density <= 0 || scaledDensity <= 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            density = dm.density;
            scaledDensity = dm.scaledDensity;
        }
    }

    /**
     * 获取屏幕的密度
     *
     * @return 屏幕的密度
     */
    public static float getScreenDensity(Context context) {

        setDensity(context);

        return density;
    }

    public static float getScaledDensity(Context context) {

        setDensity(context);

        return scaledDensity;
    }

    /**
     * 获取屏幕的宽度
     *
     * @return 屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
        scaledDensity = dm.scaledDensity;
        int screenWidth = dm.widthPixels;
        return screenWidth;
    }

    /**
     * 获取屏幕的高度
     *
     * @return 屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
        scaledDensity = dm.scaledDensity;
        int screenHeight = dm.heightPixels;
        return screenHeight;
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 将dip转换为px
     *
     * @param dip
     * @return dip转换为px
     */
    public static int dip2px(Context context, int dip) {
        if (density <= 0) {
            setDensity(context);
        }
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 将dip转换为px
     *
     * @param dip
     * @return dip转换为px
     */
    public static float dip2px(Context context, float dip) {
        if (density <= 0) {
            setDensity(context);
        }
        return dip * density + 0.5f * (dip >= 0 ? 1 : -1);

    }

    /**
     * 将px转换为dip
     *
     * @param px
     * @return px转换为dip
     */
    public static int px2dip(Context context, int px) {
        if (density <= 0) {

            setDensity(context);
        }
        return (int) (px / density + 0.5f * (px >= 0 ? 1 : -1));
    }

    /**
     * sp 转换为 px
     *
     * @param sp
     * @return sp 转换为 px
     */
    public static int sp2px(Context context, int sp) {
        if (scaledDensity <= 0) {

            setDensity(context);
        }
        return (int) (sp * scaledDensity + 0.5f * (sp >= 0 ? 1 : -1));
    }

    /**
     * sp 转换为 px
     *
     * @param sp
     * @return sp 转换为 px
     */
    public static float sp2px(Context context, float sp) {
        if (scaledDensity <= 0) {

            setDensity(context);
        }
        return sp * scaledDensity + 0.5f * (sp >= 0 ? 1 : -1);
    }

    /**
     * px 转换为 sp
     *
     * @param px
     * @return px 转换为 sp
     */
    public static int px2sp(Context context, int px) {
        if (scaledDensity <= 0) {

            setDensity(context);
        }
        return (int) (px / scaledDensity + 0.5f * (px >= 0 ? 1 : -1));
    }

    /***************************************************/
    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @return
     * @paramscale （DisplayMetrics类中属性density）
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     * @paramfontScale （DisplayMetrics类中属性scaledDensity）
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

}