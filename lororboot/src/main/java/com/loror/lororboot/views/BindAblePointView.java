package com.loror.lororboot.views;

import com.loror.commonview.utils.DpPxUtil;
import com.loror.lororboot.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BindAblePointView extends View {

    private int index, count;
    private int height, width;
    private Paint p1, p2;
    private int backColor;
    private int foreColor;

    public BindAblePointView(Context context) {
        this(context, null);
    }

    public BindAblePointView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BindAblePointView);
        backColor = array != null ? array.getColor(R.styleable.BindAblePointView_backColor, Color.GRAY) : Color.GRAY;
        foreColor = array != null ? array.getColor(R.styleable.BindAblePointView_foreColor, Color.BLUE) : Color.BLUE;
        if (array != null) {
            array.recycle();
        }
        p1 = new Paint();
        p2 = new Paint();
        p1.setColor(backColor);
        p2.setColor(foreColor);
        p1.setAntiAlias(true);
        p2.setAntiAlias(true);
    }

    public void setIndex(int index) {
        this.index = index;
        invalidate();
    }

    public void setCount(int count) {
        this.count = count;
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                params.height = DpPxUtil.Dp2Px(getContext(), 5);
            }
            params.width = params.height * 2 * count;
            setLayoutParams(params);
        }
        invalidate();
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
        p1.setColor(backColor);
        invalidate();
    }

    public void setForeColor(int foreColor) {
        this.foreColor = foreColor;
        p2.setColor(foreColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        height = getHeight();
        width = getWidth();
        int max = count * 3 - 1;
        for (int i = 0; i < count; i++) {
            canvas.drawCircle(width * (1 + (3 * i)) / max, height / 2, height / 2, p1);
        }
        canvas.drawCircle(width * (1 + (3 * index)) / max, height / 2, height / 2, p2);
    }
}
