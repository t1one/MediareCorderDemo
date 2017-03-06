package com.example.administrator.myapplication;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Toast mToast;

    public static void showToast(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), message,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public static void showToast(Context context, int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), resId,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public static void showToast(Context context, String message, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), message,
                    duration);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public static void showToast(Context context, int resId, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), resId,
                    duration);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }


}
