package com.example.doubleselectsidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * @author: 37745
 * @date: 2020/4/26 9:02
 * @desc:
 */
public class DoubleSelectSeekBar extends View {
    private static final String TAG = "DoubleSelectSeekBar";
    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    private float indicatorRadius;
    private DoubleSelectIndicator minIndicator, maxIndicator;
    private int markedColor, unMarkedColor;
    private int lineWidth;
    private Paint mPaint;
    private int startX, endX;
    private Bitmap indicatorBitmap;
    private Rect indicatorBitmapRect;


    public DoubleSelectSeekBar(Context context) {
        this(context, null);
    }

    public DoubleSelectSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleSelectSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        minIndicator = new DoubleSelectIndicator();
        maxIndicator = new DoubleSelectIndicator();
        indicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_icon);
        indicatorBitmapRect = new Rect(0, 0, indicatorBitmap.getWidth(), indicatorBitmap.getHeight());
        initAttrs(context, attrs);
        initPaint();
        setClickable(true);
    }

    private void initPaint() {
        mPaint = new Paint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DoubleSelectSeekBar);
            indicatorRadius = (int) typedArray.getDimension(R.styleable.DoubleSelectSeekBar_indicator_width, DensityUtil.dpToPx(context, 20)) / 2;
            minIndicator.setIndicatorRadius(indicatorRadius);
            maxIndicator.setIndicatorRadius(indicatorRadius);
            markedColor = typedArray.getColor(R.styleable.DoubleSelectSeekBar_marked_color, ContextCompat.getColor(context, R.color.marked_color));
            unMarkedColor = typedArray.getColor(R.styleable.DoubleSelectSeekBar_unmarked_color, ContextCompat.getColor(context, R.color.unmarked_color));
            lineWidth = (int) typedArray.getDimension(R.styleable.DoubleSelectSeekBar_line_width, DensityUtil.dpToPx(context, 2));
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int wrapWidth = DensityUtil.dpToPx(getContext(), 200);
        int wrapHeight = 2 * (int) (indicatorRadius + DensityUtil.dpToPx(getContext(), 2));
        int verticalPadding = getPaddingStart() + getPaddingEnd();
        int horizontalPadding = getPaddingTop() + getPaddingBottom();
        int finalWidth;
        int finalHeight;
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            finalWidth = wrapWidth + horizontalPadding;
            finalHeight = wrapHeight + verticalPadding;
        } else if (widthMode == MeasureSpec.EXACTLY) {
            finalWidth = widthSize - horizontalPadding;
            finalHeight = wrapHeight + verticalPadding;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            finalWidth = wrapWidth + horizontalPadding;
            finalHeight = heightSize - verticalPadding;
        } else {
            finalWidth = widthSize;
            finalHeight = heightSize;
        }
        startX = getPaddingStart();
        endX = finalWidth - getPaddingEnd();
        updateIndicatorPosition(minIndicator, startX + minIndicator.getIndicatorRadius(), finalHeight / 2f);
        updateIndicatorPosition(maxIndicator, endX - maxIndicator.getIndicatorRadius(), finalHeight / 2f);
        setMeasuredDimension(finalWidth, finalHeight);
    }

    private void updateIndicatorPosition(DoubleSelectIndicator indicator, float x, float y) {
        indicator.setIndicatorX(x);
        indicator.setIndicatorY(y);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUnMarkedLined(canvas);
        drawMarkedLined(canvas);
        drawIndicator(canvas);
    }

    private void drawIndicator(Canvas canvas) {
        mPaint.reset();
        RectF minRect = new RectF(
                minIndicator.getIndicatorRange()[LEFT],
                minIndicator.getIndicatorRange()[TOP],
                minIndicator.getIndicatorRange()[RIGHT],
                minIndicator.getIndicatorRange()[BOTTOM]);
        RectF maxRect = new RectF(
                maxIndicator.getIndicatorRange()[LEFT],
                maxIndicator.getIndicatorRange()[TOP],
                maxIndicator.getIndicatorRange()[RIGHT],
                maxIndicator.getIndicatorRange()[BOTTOM]);

        canvas.drawBitmap(indicatorBitmap, indicatorBitmapRect, minRect, mPaint);
        canvas.drawBitmap(indicatorBitmap, indicatorBitmapRect, maxRect, mPaint);
    }

    private void drawUnMarkedLined(Canvas canvas) {

        mPaint.reset();
        mPaint.setColor(unMarkedColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(lineWidth);
        // 绘制起点到终点
        canvas.drawLine(
                startX,
                minIndicator.getIndicatorY(),
                endX,
                maxIndicator.getIndicatorY(),
                mPaint);
    }

    private void drawMarkedLined(Canvas canvas) {
        mPaint.reset();
        mPaint.setColor(markedColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(lineWidth);
        // 绘制指示器之间
        canvas.drawLine(
                minIndicator.getIndicatorRange()[RIGHT],
                minIndicator.getIndicatorY(),
                maxIndicator.getIndicatorRange()[LEFT],
                maxIndicator.getIndicatorY(),
                mPaint);
    }

    private boolean checkIsTouchIndicator(DoubleSelectIndicator indicator, float x, float y) {
        float[] range = indicator.getIndicatorRange();
        return x >= range[LEFT] && x <= range[RIGHT] && y >= range[TOP] && y <= range[BOTTOM];
    }

    private boolean isMinIndicatorTouched;
    private boolean isMaxIndicatorTouched;
    private float lastX;
    private float currentX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = currentX = event.getX();
                if (checkIsTouchIndicator(minIndicator, event.getX(), event.getY())) {
                    isMinIndicatorTouched = true;
                }
                if (checkIsTouchIndicator(maxIndicator, event.getX(), event.getY())) {
                    isMaxIndicatorTouched = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                // MotionEvent.ACTION_DOWN 同时满足触摸两个指示器
                if (isMinIndicatorTouched && isMaxIndicatorTouched) {
                    isMaxIndicatorTouched = currentX - lastX > 0;
                    isMinIndicatorTouched = currentX - lastX < 0;
                }
                if (isMinIndicatorTouched) {
                    minIndicatorMoveToPosition(currentX);
                }
                if (isMaxIndicatorTouched) {
                    maxIndicatorMoveToPosition(currentX);
                }
                lastX = currentX;
                break;
            case MotionEvent.ACTION_UP:
                isMinIndicatorTouched = false;
                isMaxIndicatorTouched = false;
                break;
            default:
        }
        return super.onTouchEvent(event);
    }


    private void maxIndicatorMoveToPosition(float x) {
        if (x + maxIndicator.indicatorRadius > endX) {
            maxIndicator.setIndicatorX(endX - maxIndicator.indicatorRadius);
        } else if (x - maxIndicator.indicatorRadius < minIndicator.indicatorRange[LEFT]) {
            maxIndicator.setIndicatorX(minIndicator.indicatorRange[LEFT] + maxIndicator.indicatorRadius);
        } else {
            maxIndicator.setIndicatorX(x);
        }
        invalidate();
    }

    private void minIndicatorMoveToPosition(float x) {
        if (x - minIndicator.indicatorRadius < startX) {
            minIndicator.setIndicatorX(startX + minIndicator.indicatorRadius);
        } else if (x + minIndicator.indicatorRadius > maxIndicator.indicatorRange[RIGHT]) {
            minIndicator.setIndicatorX(maxIndicator.indicatorRange[RIGHT] - minIndicator.indicatorRadius);
        } else {
            minIndicator.setIndicatorX(x);
        }
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!indicatorBitmap.isRecycled()) {
            indicatorBitmap.recycle();
        }
    }

    private static class DoubleSelectIndicator {

        private float indicatorRadius;
        private float[] indicatorRange = new float[4];
        // 当前所在XY轴的位置（中心点）
        private float indicatorX;
        private float indicatorY;
        private int num;
        // 数值精度
        private int numDegree;

        public float getIndicatorRadius() {
            return indicatorRadius;
        }

        public void setIndicatorRadius(float indicatorRadius) {
            this.indicatorRadius = indicatorRadius;
        }

        public float[] getIndicatorRange() {
            return indicatorRange;
        }

        public void setMinIndicatorRange(float left, float top, float right, float bottom) {
            this.indicatorRange[0] = left;
            this.indicatorRange[1] = top;
            this.indicatorRange[2] = right;
            this.indicatorRange[3] = bottom;
        }

        public float getIndicatorX() {
            return indicatorX;
        }

        public void setIndicatorX(float indicatorX) {
            this.indicatorX = indicatorX;
            setMinIndicatorRange(
                    indicatorX - indicatorRadius,
                    indicatorRange[1],
                    indicatorX + indicatorRadius,
                    indicatorRange[3]);
        }

        public float getIndicatorY() {
            return indicatorY;
        }

        public void setIndicatorY(float indicatorY) {
            this.indicatorY = indicatorY;
            setMinIndicatorRange(
                    indicatorRange[0],
                    indicatorY - indicatorRadius,
                    indicatorRange[2],
                    indicatorY + indicatorRadius);
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            if (num % numDegree == 0) {
                this.num = num;
            }
        }

        public int getNumDegree() {
            return numDegree;
        }

        public void setNumDegree(int numDegree) {
            this.numDegree = numDegree;
        }
    }

}
