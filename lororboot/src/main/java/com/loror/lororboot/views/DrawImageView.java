package com.loror.lororboot.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.loror.lororUtil.image.BitmapUtil;
import com.loror.lororboot.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DrawImageView extends View {

    @IntDef(value = {Direct.LEFT, Direct.RIGHT})
    @Retention(RetentionPolicy.CLASS)
    public @interface Direct {
        int LEFT = 0;
        int RIGHT = 1;
    }

    private Matrix matrix = new Matrix();//对图片进行移动和缩放变换的矩阵
    private Bitmap sourceBitmap;//待展示的Bitmap对象
    private int width;//DrawImageView控件的宽度
    private int height;//DrawImageView控件的高度
    private float currentRatio;//记录图片当前的缩放比例
    private float currentBitmapWidth;//记录当前图片的宽度，图片被缩放时，这个值会一起变动
    private float currentBitmapHeight;//记录当前图片的高度，图片被缩放时，这个值会一起变动
    private float totalTranslateX;//记录图片在矩阵上的横向偏移值
    private float totalTranslateY;//记录图片在矩阵上的纵向偏移值
    private int drawColor = 0xFFFF0000;
    private int drawWidth = 10;

    private Paint paint = new Paint();
    private List<List<Point>> pointsGroup = new LinkedList<>();

    public Bitmap viewShort() {
        setDrawingCacheEnabled(true);
        Bitmap drawingCache = getDrawingCache();
        drawingCache = Bitmap.createBitmap(drawingCache);
        setDrawingCacheEnabled(false);
        return drawingCache;
    }

    private class Point {
        private float x;
        private float y;
        private int color;
    }

    /**
     * DrawImageView构造函数
     *
     * @param context
     */
    public DrawImageView(Context context) {
        this(context, null);
    }

    /**
     * DrawImageView构造函数，将当前操作状态设为STATUS_INIT。
     *
     * @param context
     * @param attrs
     */
    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(drawColor);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DrawImageView);
            drawWidth = array.getDimensionPixelSize(R.styleable.DrawImageView_drawWidth, 6);
            array.recycle();
        } else {
            drawWidth = 6;
        }
    }

    /*将待展示的图片设置进来。
     * @param bitmap
     * 待展示的Bitmap对象
     */
    public void setImageBitmap(Bitmap bitmap) {
        sourceBitmap = bitmap;
        invalidate();
    }

    /**
     * 设置标记颜色
     */
    public void setDrawColor(@ColorInt int drawColor) {
        this.drawColor = drawColor;
    }

    /**
     * 设置涂鸦宽度
     */
    public void setDrawWidth(int drawWidth) {
        this.drawWidth = drawWidth;
    }

    /**
     * 获取旋转图片后的缩放
     */
    private float getNextRatio() {
        float ratio = 0;
        if (sourceBitmap != null) {
            int bitmapWidth = sourceBitmap.getWidth();
            int bitmapHeight = sourceBitmap.getHeight();
            if (bitmapWidth > width || bitmapHeight > height) {
                if (bitmapWidth - width > bitmapHeight - height) {
                    // 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
                    ratio = width / (bitmapWidth * 1.0f);
                } else {
                    // 当图片高度大于屏幕高度时，将图片等比例压缩，使它可以完全显示出来
                    ratio = height / (bitmapHeight * 1.0f);

                }
            } else {
                // 当图片的宽高都小于屏幕宽高时，使长或宽顶满
                float widthScale = width * 1.0F / sourceBitmap.getWidth();
                //尝试将宽度顶满，高度会超出控件，按高度缩放
                if (widthScale * sourceBitmap.getHeight() > height) {
                    ratio = height * 1.0f / sourceBitmap.getHeight();
                } else {
                    ratio = width * 1.0f / sourceBitmap.getWidth();
                }
            }
        }
        return ratio;
    }

    public void rotateBitmap(@Direct int direct) {
        if (sourceBitmap == null) {
            return;
        }
        if (direct == Direct.RIGHT) {
            sourceBitmap = BitmapUtil.rotateBitmapByDegree(sourceBitmap, 90);
        } else {
            sourceBitmap = BitmapUtil.rotateBitmapByDegree(sourceBitmap, -90);
        }
        float ratio = getNextRatio();
        float scale = ratio / currentRatio;
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        if (pointsGroup.size() > 0) {
            for (List<Point> points : pointsGroup) {
                for (Point point : points) {
                    float x = point.x;
                    float y = point.y;
                    //旋转
                    if (direct == Direct.RIGHT) {
                        point.x = -(y - centerY) + centerX;
                        point.y = (x - centerX) + centerY;
                    } else {
                        point.x = (y - centerY) + centerX;
                        point.y = -(x - centerX) + centerY;
                    }

                    //缩放
                    if (scale != 1.0f) {
                        point.x = (point.x - centerX) * scale + centerX;
                        point.y = (point.y - centerY) * scale + centerY;
                    }
                }
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * 还原一次操作
     */
    public void revertDraw() {
        if (pointsGroup.size() > 0) {
            pointsGroup.remove(pointsGroup.size() - 1);
            invalidate();
        }
    }

    /**
     * 清空轨迹
     */
    public void clearDraw() {
        if (pointsGroup.size() > 0) {
            pointsGroup.clear();
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            // 分别获取到ZoomImageView的宽度和高度
            width = getWidth();
            height = getHeight();
        }
    }

    private List<Point> points;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 一个手指时候记录位置
        if (event.getPointerCount() == 1) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    points = new ArrayList<>();
                    pointsGroup.add(points);
                    break;
                case MotionEvent.ACTION_MOVE:
                    Point point = new Point();
                    point.x = event.getX();
                    point.y = event.getY();
                    point.color = drawColor;
                    if (point.x < totalTranslateX || point.x > totalTranslateX + currentBitmapWidth) {
                        return false;
                    }
                    if (point.y < totalTranslateY || point.y > totalTranslateY + currentBitmapHeight) {
                        return false;
                    }
                    points.add(point);
                    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                default:
                    invalidate();
                    break;
            }
        }
        return true;
    }

    /*根据currentStatus的值来决定对图片进行什么样的绘制操作。*/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
        drawPoints(canvas);
    }

    /**
     * 绘制点
     *
     * @param canvas
     */
    private void drawPoints(Canvas canvas) {
        for (List<Point> points : pointsGroup) {
            int size = points.size();
            for (int i = 0; i < size - 1; i++) {
                Point start = points.get(i);
                Point end = points.get(i + 1);

                if (Integer.toHexString(start.color).length() >= 7) {
                    paint.setColor(start.color);
                    paint.setStrokeWidth(drawWidth);
                    canvas.drawLine(start.x, start.y, end.x, end.y, paint);
                }
            }
        }
    }

    /**
     * 绘制图片
     *
     * @param canvas
     */
    private void drawBitmap(Canvas canvas) {
        if (sourceBitmap != null) {
            matrix.reset();
            int bitmapWidth = sourceBitmap.getWidth();
            int bitmapHeight = sourceBitmap.getHeight();
            if (bitmapWidth > width || bitmapHeight > height) {
                if (bitmapWidth - width > bitmapHeight - height) {
                    // 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
                    float ratio = width / (bitmapWidth * 1.0f);
                    matrix.postScale(ratio, ratio);
                    float translateY = (height - (bitmapHeight * ratio)) / 2f;
                    // 在纵坐标方向上进行偏移，以保证图片居中显示
                    matrix.postTranslate(0, translateY);
                    totalTranslateY = translateY;
                    currentRatio = ratio;
                } else {
                    // 当图片高度大于屏幕高度时，将图片等比例压缩，使它可以完全显示出来
                    float ratio = height / (bitmapHeight * 1.0f);
                    matrix.postScale(ratio, ratio);
                    float translateX = (width - (bitmapWidth * ratio)) / 2f;
                    // 在横坐标方向上进行偏移，以保证图片居中显示
                    matrix.postTranslate(translateX, 0);
                    totalTranslateX = translateX;
                    currentRatio = ratio;
                }
                currentBitmapWidth = bitmapWidth * currentRatio;
                currentBitmapHeight = bitmapHeight * currentRatio;
            } else {
                float ratio = 0;
                float translateX = 0;
                float translateY = 0;
                // 当图片的宽高都小于屏幕宽高时，使长或宽顶满
                float widthScale = width * 1.0F / sourceBitmap.getWidth();
                //尝试将宽度顶满，高度会超出控件，按高度缩放
                if (widthScale * sourceBitmap.getHeight() > height) {
                    ratio = height * 1.0f / sourceBitmap.getHeight();
                    translateX = (width - (ratio * sourceBitmap.getWidth())) / 2.0f;
                    translateY = 0;
                } else {
                    ratio = width * 1.0f / sourceBitmap.getWidth();
                    translateX = 0;
                    translateY = (height - (ratio * sourceBitmap.getHeight())) / 2.0f;
                }
                matrix.postScale(ratio, ratio);
                matrix.postTranslate(translateX, translateY);
                totalTranslateX = translateX;
                totalTranslateY = translateY;
                currentRatio = ratio;
                currentBitmapWidth = bitmapWidth * currentRatio;
                currentBitmapHeight = bitmapHeight * currentRatio;
            }
            canvas.drawBitmap(sourceBitmap, matrix, null);
        }
    }


}
