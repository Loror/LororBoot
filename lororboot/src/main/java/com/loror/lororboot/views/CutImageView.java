package com.loror.lororboot.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.loror.lororboot.R;

public class CutImageView extends View {

    public static final int STATUS_INIT = 1;//常量初始化
    public static final int STATUS_ZOOM_OUT = 2;//图片放大状态常量
    public static final int STATUS_ZOOM_IN = 3;//图片缩小状态常量
    public static final int STATUS_MOVE = 4;//图片拖动状态常量

    private Matrix matrix = new Matrix();//对图片进行移动和缩放变换的矩阵
    private Bitmap sourceBitmap;//待展示的Bitmap对象

    private int currentStatus;//记录当前操作的状态，可选值为STATUS_INIT、STATUS_ZOOM_OUT、STATUS_ZOOM_IN和STATUS_MOVE
    private int width;//CutImageView控件的宽度
    private int height;//CutImageView控件的高度
    private float currentBitmapWidth;//记录当前图片的宽度，图片被缩放时，这个值会一起变动
    private float currentBitmapHeight;//记录当前图片的高度，图片被缩放时，这个值会一起变动

    private float centerPointX;//记录两指同时放在屏幕上时，中心点的横坐标值
    private float centerPointY;//记录两指同时放在屏幕上时，中心点的纵坐标值
    private float lastXMove = -1;//记录上次手指移动时的横坐标
    private float lastYMove = -1;//记录上次手指移动时的纵坐标
    private float movedDistanceX;//记录手指在横坐标方向上的移动距离
    private float movedDistanceY;//记录手指在纵坐标方向上的移动距离
    private float totalTranslateX;//记录图片在矩阵上的横向偏移值
    private float totalTranslateY;//记录图片在矩阵上的纵向偏移值
    private float totalRatio;//记录图片在矩阵上的总缩放比例
    private float scaledRatio;//记录手指移动的距离所造成的缩放比例
    private float initRatio;//记录图片初始化时的缩放比例
    private float maxRatio = 4;//最大缩放比例
    private double lastFingerDis;//记录上次两指之间的距离
    private int borderShadowColor;//边框颜色
    private int cutButtonWidth;//裁剪框拉动按钮大小
    private int cutButtonColor;//裁剪框拉动按钮颜色
    private boolean showCutButton;//显示拖动按钮
    private boolean cutDragEnable;//允许拖动

    private float scale;//裁剪框宽高比例
    private float topBorder = -1, bottomBorder = -1;//临时边距

    private Paint paint = new Paint();
    private int border;

    /**
     * CutImageView构造函数，将当前操作状态设为STATUS_INIT。
     *
     * @param context
     */
    public CutImageView(Context context) {
        this(context, null);
    }

    /**
     * CutImageView构造函数
     *
     * @param context
     * @param attrs
     */
    public CutImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentStatus = STATUS_INIT;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CutImageView);
            scale = array.getFloat(R.styleable.CutImageView_cutScale, 1.0f);
            cutDragEnable = array.getBoolean(R.styleable.CutImageView_cutDragEnable, true);
            border = array.getDimensionPixelSize(R.styleable.CutImageView_borderPadding, 6);
            cutButtonWidth = array.getDimensionPixelSize(R.styleable.CutImageView_cutButtonWidth, 80);
            cutButtonColor = array.getColor(R.styleable.CutImageView_cutButtonColor, 0xCC33AA00);
            borderShadowColor = array.getColor(R.styleable.CutImageView_borderShadowColor, 0x88000000);
            array.recycle();
        } else {
            scale = 1.0f;
            cutDragEnable = true;
            border = 6;
            cutButtonWidth = 80;
            cutButtonColor = 0xCC33AA00;
            borderShadowColor = 0x88000000;
        }
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    /*将待展示的图片设置进来。
     * @param bitmap
     * 待展示的Bitmap对象
     */
    public void setImageBitmap(Bitmap bitmap) {
        sourceBitmap = bitmap;
        currentStatus = STATUS_INIT;
        invalidate();
    }

    /**
     * 设置拖动按钮颜色
     **/
    public void setCutButtonColor(@ColorInt int cutButtonColor) {
        this.cutButtonColor = cutButtonColor;
    }

    /**
     * 设置边框颜色
     **/
    public void setBorderShadowColor(int borderShadowColor) {
        this.borderShadowColor = borderShadowColor;
        invalidate();
    }

    /**
     * 设置边框宽度
     **/
    public void setBorder(int border) {
        this.border = border;
        currentStatus = STATUS_INIT;
        invalidate();
    }

    /**
     * 设置宽高比
     **/
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * 设置是否允许拖动
     **/
    public void setCutDragEnable(boolean cutDragEnable) {
        this.cutDragEnable = cutDragEnable;
    }

    /**
     * 获取源图片
     **/
    public Bitmap getSourceBitmap() {
        return sourceBitmap;
    }

    /**
     * 获取剪切后的Bitmap
     */
    public Bitmap getCutBitmap() {
        Bitmap bitmap = sourceBitmap;
        int pictureWidth = bitmap.getWidth();
        int pictureHeight = bitmap.getHeight();
        RectF rect = getCutRect();
        float left = pictureWidth * rect.left;
        float top = pictureHeight * rect.top;
        float right = pictureWidth * rect.right;
        float bottom = pictureHeight * rect.bottom;
        return Bitmap.createBitmap(bitmap, (int) left, (int) top, (int) (right - left), (int) (bottom - top));
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (sourceBitmap == null) {
            return false;
        }
        if (initRatio == totalRatio) {
            getParent().requestDisallowInterceptTouchEvent(false);
        } else {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    // 当有两个手指按在屏幕上时，计算两指之间的距离
                    lastFingerDis = distanceBetweenFingers(event);
                }
                break;
            case MotionEvent.ACTION_DOWN: {
                currentStatus = 0;
                float xMove = event.getX();
                float yMove = event.getY();

                float bottomArea = getHeight() - getTopBorder();
                float centerX = getWidth() / 2.0f;
                //点击到裁剪框下边界中心点
                if (cutDragEnable && Math.sqrt(Math.pow(centerX - xMove, 2) + Math.pow(bottomArea - yMove, 2)) < cutButtonWidth * 3) {
                    showCutButton = true;
                    topBorder = getTopBorder();
                    invalidate();
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float xMove = event.getX();
                    float yMove = event.getY();
                    //点击到裁剪框下边界中心点
                    if (showCutButton) {
                        float top = getTopBorder();
                        float bottom = currentBitmapHeight + totalTranslateY;
                        if (yMove > top + (currentBitmapHeight > 50 ? 50 : currentBitmapHeight) && yMove < bottom) {
                            bottomBorder = getHeight() - yMove;
                        }
                    } else {
                        // 只有单指按在屏幕上移动时，为拖动状态
                        if (lastXMove == -1 && lastYMove == -1) {
                            lastXMove = xMove;
                            lastYMove = yMove;
                        }
                        currentStatus = STATUS_MOVE;
                        movedDistanceX = xMove - lastXMove;
                        movedDistanceY = yMove - lastYMove;
                        // 进行边界检查，不允许将图片拖出边界
                        if (totalTranslateX + movedDistanceX > getLeftBorder()) {
                            movedDistanceX = 0;
                        } else if (width - (totalTranslateX + movedDistanceX + getLeftBorder()) > currentBitmapWidth) {
                            movedDistanceX = 0;
                        }
                        if (totalTranslateY + movedDistanceY > getTopBorder()) {
                            movedDistanceY = 0;
                        } else if (height - (totalTranslateY + movedDistanceY + getBottomBorder()) > currentBitmapHeight) {
                            movedDistanceY = 0;
                        }
                        lastXMove = xMove;
                        lastYMove = yMove;
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (showCutButton) {
                            topBorder = -1;
                            bottomBorder = -1;
                            showCutButton = false;
                        }
                    }
                    // 调用onDraw()方法绘制图片
                    invalidate();
                } else if (event.getPointerCount() == 2) {
                    if (!showCutButton) {
                        // 有两个手指按在屏幕上移动时，为缩放状态
                        centerPointBetweenFingers(event);
                        double fingerDis = distanceBetweenFingers(event);
                        if (fingerDis > lastFingerDis) {
                            currentStatus = STATUS_ZOOM_OUT;
                        } else {
                            currentStatus = STATUS_ZOOM_IN;
                        }
                        // 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
                        if ((currentStatus == STATUS_ZOOM_OUT && totalRatio < maxRatio * initRatio)
                                || (currentStatus == STATUS_ZOOM_IN && totalRatio > initRatio)) {
                            float oldScaledRatio = scaledRatio;
                            float oldTotalRatio = totalRatio;
                            scaledRatio = (float) (fingerDis / lastFingerDis);
                            totalRatio = totalRatio * scaledRatio;
                            if (totalRatio > maxRatio * initRatio) {
                                totalRatio = maxRatio * initRatio;
                            } else if (totalRatio < initRatio) {
                                totalRatio = initRatio;
                            }
                            if (!canZoom()) {
                                scaledRatio = oldScaledRatio;
                                totalRatio = oldTotalRatio;
                            }
                            // 调用onDraw()方法绘制图片
                            invalidate();
                            lastFingerDis = fingerDis;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    // 手指离开屏幕时将临时值还原
                    lastXMove = -1;
                    lastYMove = -1;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 手指离开屏幕时将临时值还原
                lastXMove = -1;
                lastYMove = -1;
                if (showCutButton) {
                    currentStatus = STATUS_MOVE;
                    //裁剪框重绘时图片跟随移动
                    float oldCutHeight = getHeight() - 2 * topBorder;
                    float newCutHeight = getHeight() - bottomBorder - topBorder;
                    movedDistanceY = (oldCutHeight - newCutHeight) / 2;
                    scale = (getWidth() - getLeftBorder() * 2) / newCutHeight;
                    topBorder = -1;
                    bottomBorder = -1;
                    showCutButton = false;
                    invalidate();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /*根据currentStatus的值来决定对图片进行什么样的绘制操作。*/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (currentStatus) {
            case STATUS_ZOOM_OUT:
            case STATUS_ZOOM_IN:
                zoom(canvas);
                break;
            case STATUS_MOVE:
                move(canvas);
                break;
            case STATUS_INIT:
                initBitmap(canvas);
            default:
                if (sourceBitmap != null) {
                    canvas.drawBitmap(sourceBitmap, matrix, null);
                }
                break;
        }
        int width = getWidth();
        int height = getHeight();
        paint.setColor(borderShadowColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, getTopBorder(), paint);
        canvas.drawRect(0, getTopBorder(), getLeftBorder(), height - getBottomBorder(), paint);
        canvas.drawRect(0, height - getBottomBorder(), width, height, paint);
        canvas.drawRect(width - getLeftBorder(), getTopBorder(), width, height - getBottomBorder(), paint);
        if (showCutButton) {
            paint.setColor(cutButtonColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(width / 2, height - getBottomBorder(), cutButtonWidth / 2.0f, paint);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(width / 2, height - getBottomBorder(), cutButtonWidth * 1.2f / 2.0f, paint);
        }
    }

    /**
     * 获取左右边距
     */
    private float getLeftBorder() {
        int width = getWidth();
        int height = getHeight();

        float cutHeight = (width - 2 * border) / scale;
        //边框应用到宽度方向上时，高边框不足
        if (height - cutHeight < 2 * border) {
            return (width - (height - 2 * border) * scale) / 2.0f;
        } else {
            return border;
        }
    }

    /**
     * 获取上边距
     */
    private float getTopBorder() {
        if (topBorder != -1) {
            return topBorder;
        }
        int width = getWidth();
        int height = getHeight();
        float cutHeight = (width - 2 * border) / scale;
        //边框应用到宽度方向上时，高边框不足
        if (height - cutHeight < 2 * border) {
            return border;
        } else {
            return (height - (width - 2 * border) / scale) / 2.0f;
        }
    }

    /**
     * 获取下边距
     */
    private float getBottomBorder() {
        if (bottomBorder != -1) {
            return bottomBorder;
        }
        int width = getWidth();
        int height = getHeight();
        float cutHeight = (width - 2 * border) / scale;
        //边框应用到宽度方向上时，高边框不足
        if (height - cutHeight < 2 * border) {
            return border;
        } else {
            return (height - (width - 2 * border) / scale) / 2.0f;
        }
    }

    /**
     * 是否可以缩放
     */
    private boolean canZoom() {
        float scaledWidth = sourceBitmap.getWidth() * totalRatio;
        float scaledHeight = sourceBitmap.getHeight() * totalRatio;
        float translateX = 0f;
        float translateY = 0f;
        // 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
        if (currentBitmapWidth < width) {
            translateX = (width - scaledWidth) / 2f;
        } else {
            translateX = totalTranslateX * scaledRatio + centerPointX
                    * (1 - scaledRatio);
        }
        // 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
        if (currentBitmapHeight < height) {
            translateY = (height - scaledHeight) / 2f;
        } else {
            translateY = totalTranslateY * scaledRatio + centerPointY
                    * (1 - scaledRatio);
        }
        if (currentStatus == STATUS_ZOOM_IN && (
                translateX > getLeftBorder() ||
                        width - (translateX + getLeftBorder()) > currentBitmapWidth
                        || translateY > getTopBorder() ||
                        height - (translateY + getBottomBorder()) > currentBitmapHeight
        )) {
            return false;
        }
        return true;
    }

    /**
     * 对图片进行缩放处理。
     *
     * @param canvas
     */
    private void zoom(Canvas canvas) {
        float scaledWidth = sourceBitmap.getWidth() * totalRatio;
        float scaledHeight = sourceBitmap.getHeight() * totalRatio;
        float translateX = 0f;
        float translateY = 0f;
        // 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
        if (currentBitmapWidth < width) {
            translateX = (width - scaledWidth) / 2f;
        } else {
            translateX = totalTranslateX * scaledRatio + centerPointX
                    * (1 - scaledRatio);
            // 进行边界检查，保证图片缩放后在水平方向上不会偏移出中心框
            if (translateX > getLeftBorder()) {
                translateX = getLeftBorder();
            } else if (width - translateX - getLeftBorder() > scaledWidth) {
                translateX = width - scaledWidth - getLeftBorder();
            }
        }
        // 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
        if (currentBitmapHeight < height) {
            translateY = (height - scaledHeight) / 2f;
        } else {
            translateY = totalTranslateY * scaledRatio + centerPointY
                    * (1 - scaledRatio);
            // 进行边界检查，保证图片缩放后在垂直方向上不会偏移出中心框
            if (translateY > getTopBorder()) {
                translateY = getTopBorder();
            } else if (height - translateY - getBottomBorder() > scaledHeight) {
                translateY = height - scaledHeight - getTopBorder();
            }
        }
        matrix.reset();
        // 将图片按总缩放比例进行缩放
        matrix.postScale(totalRatio, totalRatio);
        // 缩放后对图片进行偏移，以保证缩放后中心点位置不变
        matrix.postTranslate(translateX, translateY);
        totalTranslateX = translateX;
        totalTranslateY = translateY;
        currentBitmapWidth = scaledWidth;
        currentBitmapHeight = scaledHeight;
        canvas.drawBitmap(sourceBitmap, matrix, null);
    }

    /**
     * 对图片进行平移处理
     *
     * @param canvas
     */
    private void move(Canvas canvas) {
        matrix.reset();
        // 根据手指移动的距离计算出总偏移值
        float translateX = totalTranslateX + movedDistanceX;
        float translateY = totalTranslateY + movedDistanceY;
        // 先按照已有的缩放比例对图片进行缩放
        matrix.postScale(totalRatio, totalRatio);
        // 再根据移动距离进行偏移
        matrix.postTranslate(translateX, translateY);
        totalTranslateX = translateX;
        totalTranslateY = translateY;
        canvas.drawBitmap(sourceBitmap, matrix, null);
    }

    /**
     * 对图片进行初始化操作，包括让图片居中，以及当图片大于屏幕宽高时对图片进行压缩。
     *
     * @param canvas
     */
    private void initBitmap(Canvas canvas) {
        if (sourceBitmap != null) {
            matrix.reset();
            int bitmapWidth = sourceBitmap.getWidth();
            int bitmapHeight = sourceBitmap.getHeight();
            //中心宽
            float centerWidth = width - 2 * getLeftBorder();
            float ratio;
            float widthRatio = centerWidth / bitmapWidth;
            //尝试宽度定到裁剪框时高度低于裁剪框
            if (widthRatio * bitmapHeight < height - 2 * getTopBorder()) {
                ratio = (height - 2 * getTopBorder()) / bitmapHeight;
            } else {
                ratio = (width - 2 * getLeftBorder()) / bitmapWidth;
            }
            matrix.postScale(ratio, ratio);
            float translateX = (width - (bitmapWidth * ratio)) / 2f;
            float translateY = (height - (bitmapHeight * ratio)) / 2f;
            matrix.postTranslate(translateX, translateY);
            totalRatio = initRatio = ratio;
            currentBitmapWidth = bitmapWidth * initRatio;
            currentBitmapHeight = bitmapHeight * initRatio;
            totalTranslateX = translateX;
            totalTranslateY = translateY;

            if (ratio > 4) {
                maxRatio = ratio + 1;
            }

            canvas.drawBitmap(sourceBitmap, matrix, null);
        }
    }

    /**
     * 重置
     */
    public void reset() {
        showCutButton = false;
        currentStatus = STATUS_INIT;
        invalidate();
    }

    /**
     * 获取选中区域
     */
    public RectF getCutRect() {
        float left = getLeftBorder() - totalTranslateX;
        float right = left + (getWidth() - 2 * getLeftBorder());
        float top = getTopBorder() - totalTranslateY;
        float bottom = top + (getHeight() - 2 * getTopBorder());
        return new RectF(left / currentBitmapWidth, top / currentBitmapHeight, right / currentBitmapWidth, bottom / currentBitmapHeight);
    }

    /**
     * 计算两个手指之间的距离。
     *
     * @param event
     * @return 两个手指之间的距离
     */
    private double distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 计算两个手指之间中心点的坐标。
     *
     * @param event
     */
    private void centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        centerPointX = (xPoint0 + xPoint1) / 2;
        centerPointY = (yPoint0 + yPoint1) / 2;
    }

}
