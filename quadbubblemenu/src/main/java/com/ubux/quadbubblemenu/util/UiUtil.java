package com.ubux.quadbubblemenu.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import static com.ubux.quadbubblemenu.util.CommonUtil.checkNotNull;

@SuppressWarnings("unused")
public class UiUtil {

    /**
     * Use {@link FragmentManager} in the FragmentActivity
     * by {@link FragmentActivity#getSupportFragmentManager()}.
     */
    public static void replaceFragment(FragmentActivity fa, int containerId, Fragment newF){
        replaceFragment(fa.getSupportFragmentManager(), containerId, newF, false, null);
    }

    /**
     * Use {@link FragmentManager} in the Fragment by
     * {@link Fragment#getChildFragmentManager()}
     */
    public static void replaceFragment(Fragment containerF, int containerId, Fragment newF){
        replaceFragment(containerF.getChildFragmentManager(), containerId, newF, false, null);
    }

    public static void replaceFragmentToBackStack(FragmentActivity fa, int containerId,
                                                  Fragment newF, String stateName){
        replaceFragment(fa.getSupportFragmentManager(), containerId, newF, true, stateName);
    }

    private static void replaceFragment(FragmentManager fm, int containerId,
                                        Fragment newF, boolean addToBackStack, String stateName){
        FragmentTransaction transaction = fm.beginTransaction()
                .replace(containerId, newF);
        if (addToBackStack){
            transaction.addToBackStack(stateName);
        }
        transaction.commit();
    }

    /**
     * Convenient method for View#findById().
     */
    public static <T extends View> T find(View root, int resId){
        //noinspection unchecked
        return (T)root.findViewById(resId);
    }

    /**
     * Convenient method for View#findById().
     */
    public static <T extends View> T find(Activity a, int resId){
        //noinspection unchecked
        return (T)a.findViewById(resId);
    }

    /**
     * Replace the old view with a new one. If the old view doesn't have a parent,
     * nothing will change.
     */
    public static void replaceView(@NonNull View oldV, @NonNull View newV){
        checkNotNull(oldV);
        checkNotNull(newV);
        ViewGroup vg = (ViewGroup)oldV.getParent();
        if (vg!=null){
            int index = vg.indexOfChild(oldV);
            vg.removeViewAt(index);
            vg.addView(newV, index);
        }
    }

    /**
     * Convert a drawable instance to a Bitmap instance.
     * @param d must not be null
     */
    public static @NonNull Bitmap fromDrawable(@NonNull Drawable d){
        checkNotNull(d);
        Bitmap bm;
        if (d instanceof BitmapDrawable && (bm=((BitmapDrawable) d).getBitmap())!=null){
            return bm;
        }else if (d.getIntrinsicWidth()<=0 || d.getIntrinsicHeight()<=0){
            bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }else {
            bm = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bm);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bm;
    }

    public static void setOnClickListenerForChildren(@NonNull ViewGroup root,
                                                     @Nullable View.OnClickListener listener){
        for (int i=0; i<root.getChildCount(); ++i){
            View v = root.getChildAt(i);
            if (v instanceof ViewGroup){
                setOnClickListenerForChildren((ViewGroup)v, listener);
            }else {
                v.setOnClickListener(listener);
            }
        }
    }

    /**
     * Get status bar height on platform of pre-lollipop.
     * @return
     */
    public static int getStatusBarHeight(Resources res) {
        int result = 0;
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static Drawable getDrawable(Context c, int resId){
        if (Build.VERSION.SDK_INT < 22){
            //noinspection deprecation
            return c.getResources().getDrawable(resId);
        }else {
            return c.getResources().getDrawable(resId, c.getTheme());
        }
    }

    public static int getColor(Context c, int colorResId){
        if (Build.VERSION.SDK_INT < 23){
            //noinspection deprecation
            return c.getResources().getColor(colorResId);
        }else {
            return c.getResources().getColor(colorResId, c.getTheme());
        }
    }

}
