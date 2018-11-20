package com.loror.demo;

import android.content.Context;
import android.graphics.Bitmap;

import com.loror.lororUtil.image.BitmapConverter;
import com.loror.lororUtil.image.BitmapUtil;

public class RoundBitmapConverter implements BitmapConverter {
    @Override
    public Bitmap convert(Context context, Bitmap bitmap) {
        return BitmapUtil.centerRoundCorner(bitmap);
    }
}
