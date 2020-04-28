package com.example.doubleselectsidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
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
    private float startLineX;
    private float endLineX;
    private float indicatorRadius;
    private DoubleSelectIndicator minIndicator, maxIndicator;
    private int markedColor, unMarkedColor;
    private int lineWidth;
    private Paint mPaint;
    private int startX, endX;
    private Bitmap indicatorBitmap;
    private Rect indicatorBitmapRect;
    private Bitmap tipBitmap;
    private Rect tipBitmapRect;
    private float minValue;
    private float maxValue;
    private TextBean minValueText;
    private TextBean maxValueText;
    // 文字与指示器之间的间隔
    private int TextVerticalSpace;
    private int indicatorTipHeight;
    private int indicatorTipWidth;
    private int indicatorTipVerticalSpace;
    private TextBean minIndicatorValueText;
    private TextBean maxIndicatorValueText;
    private int baseLineBottom;
    // 最大值不限的情况
    private float unlimitedArea;
    // 最大最小值差
    private float valueStep;
    // 数值精度
    private int numDegree = 1;
    // 取舍值（四舍五入）
    private int numStep = 0;

    public DoubleSelectSeekBar(Context context) {
        this(context, null);
    }

    public DoubleSelectSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleSelectSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(context, attrs);
        setClickable(true);
    }

    private void init(Context context) {
        minIndicator = new DoubleSelectIndicator();
        maxIndicator = new DoubleSelectIndicator();
        indicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eh_ic_indicator);
        indicatorBitmapRect = new Rect(0, 0, indicatorBitmap.getWidth(), indicatorBitmap.getHeight());
        tipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eh_ic_indicator_tip);
        tipBitmapRect = new Rect(0, 0, tipBitmap.getWidth(), tipBitmap.getHeight());
        mPaint = new Paint();
        minValueText = new TextBean();
        maxValueText = new TextBean();
        minIndicatorValueText = new TextBean();
        maxIndicatorValueText = new TextBean();
        indicatorTipVerticalSpace = TextVerticalSpace = DensityUtil.dpToPx(getContext(), 6);
        indicatorTipHeight = DensityUtil.dpToPx(getContext(), 30);
        //设置基准线的高度
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(DensityUtil.dpToPx(getContext(), 12));
        baseLineBottom = (int) mPaint.getFontMetrics().bottom;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DoubleSelectSeekBar);
            indicatorRadius = (int) (typedArray.getDimension(R.styleable.DoubleSelectSeekBar_indicator_width, DensityUtil.dpToPx(context, 20)) / 2);
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
        int verticalPadding = getPaddingTop() + getPaddingBottom();
        int horizontalPadding = getPaddingStart() + getPaddingEnd();
        int textValueHeight = Math.max(minValueText.getRect().height(), maxValueText.getRect().height());
        int finalWidth;
        int finalHeight;
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            finalWidth = wrapWidth + horizontalPadding;
            finalHeight = wrapHeight + verticalPadding + textValueHeight + TextVerticalSpace + indicatorTipVerticalSpace + baseLineBottom + indicatorTipHeight;
        } else if (widthMode == MeasureSpec.EXACTLY) {
            finalWidth = widthSize - horizontalPadding;
            finalHeight = wrapHeight + verticalPadding + textValueHeight + TextVerticalSpace + indicatorTipVerticalSpace + baseLineBottom + indicatorTipHeight;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            finalWidth = wrapWidth + horizontalPadding;
            finalHeight = heightSize - verticalPadding;
        } else {
            finalWidth = widthSize;
            finalHeight = heightSize;
        }
        startX = getPaddingStart();
        endX = finalWidth - getPaddingEnd();
        startLineX = startX + minIndicator.getIndicatorRadius();
        endLineX = endX - maxIndicator.getIndicatorRadius();
        //确定不限的区域为末尾留出的10px的区域
        unlimitedArea = 10f;
        //确定指示器位置信息
        updateIndicatorPosition(minIndicator, startLineX, getPaddingTop() + minIndicator.getIndicatorRadius() + indicatorTipVerticalSpace + indicatorTipHeight);
        updateIndicatorPosition(maxIndicator, endLineX, getPaddingTop() + minIndicator.getIndicatorRadius() + indicatorTipVerticalSpace + indicatorTipHeight);
        setMeasuredDimension(finalWidth, finalHeight);
    }

    private void updateIndicatorPosition(DoubleSelectIndicator indicator, float x, float y) {
        indicator.setIndicatorX(x);
        indicator.setIndicatorY(y);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制未选中线
        drawUnMarkedLined(canvas);
        // 绘制选中线
        drawMarkedLined(canvas);
        // 绘制指示器
        drawIndicator(canvas);
        // 绘制最大最小刻度
        drawTextMaxMinValue(canvas);
        // 绘制指示器文字提示
        drawIndicatorTips(canvas);
    }

    private void drawIndicatorTips(Canvas canvas) {
        // 绘制最小指示器文字提示
        drawIndicatorTip(minIndicator, canvas);
        // 绘制最大指示器文字提示
        drawIndicatorTip(maxIndicator, canvas);
    }

    private float tipSpace = DensityUtil.dpToPx(getContext(), 5);

    private String tipContent;
    private Rect tipRect;

    private void drawIndicatorTip(DoubleSelectIndicator indicator, Canvas canvas) {
        mPaint.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(DensityUtil.dpToPx(getContext(), 12));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setDither(true);
        if (indicator == minIndicator) {
            tipContent = minIndicator.getNum();
            mPaint.getTextBounds(tipContent, 0, tipContent.length(), minIndicatorValueText.getRect());
            indicatorTipWidth = minIndicatorValueText.getRect().width() + 2 * (DensityUtil.dpToPx(getContext(), 9));
            tipRect = new Rect(
                    (int) minIndicator.getIndicatorX() - indicatorTipWidth / 2,
                    (int) minIndicator.getIndicatorRange()[TOP] - (indicatorTipHeight + indicatorTipVerticalSpace),
                    (int) minIndicator.getIndicatorX() + indicatorTipWidth / 2,
                    (int) minIndicator.getIndicatorRange()[TOP] - indicatorTipVerticalSpace);
        } else {
            tipContent = maxIndicator.getNum();
            mPaint.getTextBounds(tipContent, 0, tipContent.length(), maxIndicatorValueText.getRect());
            indicatorTipWidth = maxIndicatorValueText.getRect().width() + 2 * (DensityUtil.dpToPx(getContext(), 12));
            tipRect = new Rect(
                    (int) maxIndicator.getIndicatorX() - indicatorTipWidth / 2,
                    (int) maxIndicator.getIndicatorRange()[TOP] - (indicatorTipHeight + indicatorTipVerticalSpace),
                    (int) maxIndicator.getIndicatorX() + indicatorTipWidth / 2,
                    (int) maxIndicator.getIndicatorRange()[TOP] - indicatorTipVerticalSpace);
        }
        canvas.drawBitmap(tipBitmap, tipBitmapRect, tipRect, mPaint);
        drawTipText(indicator, canvas, tipContent, tipRect);
    }

    private float tipX;
    private float tipY;

    private void drawTipText(DoubleSelectIndicator indicator, Canvas canvas, String content, Rect rect) {
        mPaint.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        mPaint.setTextSize(DensityUtil.dpToPx(getContext(), 12));
        tipY = rect.top - mPaint.getFontMetrics().ascent + tipSpace;
        if (indicator == minIndicator) {
            tipX = minIndicator.indicatorX - minIndicatorValueText.getRect().width() / 2f;
        } else {
            tipX = maxIndicator.indicatorX - maxIndicatorValueText.getRect().width() / 2f;
        }
        canvas.drawText(content, tipX, tipY, mPaint);
    }

    private void drawTextMaxMinValue(Canvas canvas) {
        mPaint.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.textColorGray));
        mPaint.setTextSize(DensityUtil.dpToPx(getContext(), 12));
        mPaint.setTextAlign(Paint.Align.CENTER);
        // 绘制最小值刻度文字
        canvas.drawText(
                minValueText.getText(),
                startLineX,
                minIndicator.getIndicatorRange()[BOTTOM] + minValueText.getRect().height() + baseLineBottom + TextVerticalSpace,
                mPaint);
        // 绘制最大值刻度文字
        canvas.drawText(
                maxValueText.getText(),
                endLineX - maxValueText.getRect().width() / 2f,
                maxIndicator.getIndicatorRange()[BOTTOM] + minValueText.getRect().height() + baseLineBottom + TextVerticalSpace,
                mPaint);
    }

    private void drawIndicator(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        RectF minRect = new RectF(
                minIndicator.getIndicatorRange()[LEFT],
                minIndicator.getIndicatorRange()[TOP] - DensityUtil.dpToPx(getContext(), 2.5f),
                minIndicator.getIndicatorRange()[RIGHT],
                minIndicator.getIndicatorRange()[BOTTOM] + DensityUtil.dpToPx(getContext(), 2.5f));
        RectF maxRect = new RectF(
                maxIndicator.getIndicatorRange()[LEFT],
                maxIndicator.getIndicatorRange()[TOP] - DensityUtil.dpToPx(getContext(), 2.5f),
                maxIndicator.getIndicatorRange()[RIGHT],
                maxIndicator.getIndicatorRange()[BOTTOM] + DensityUtil.dpToPx(getContext(), 2.5f));
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
                startLineX,
                minIndicator.getIndicatorY(),
                endLineX,
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
                minIndicator.getIndicatorX(),
                minIndicator.getIndicatorY(),
                maxIndicator.getIndicatorX(),
                maxIndicator.getIndicatorY(),
                mPaint);
    }

    // 增加触碰的响应范围
    private float touchPadding = 0;

    private boolean checkIsTouchIndicator(DoubleSelectIndicator indicator, float x, float y) {
        float[] range = indicator.getIndicatorRange();
        return x >= range[LEFT] - touchPadding && x <= range[RIGHT] + touchPadding && y >= range[TOP] - touchPadding && y <= range[BOTTOM] + touchPadding;
    }

    public float getTouchPadding() {
        return touchPadding;
    }

    public void setTouchPadding(float touchPadding) {
        this.touchPadding = touchPadding;
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
                if (lastX == event.getX()) {
                    return super.onTouchEvent(event);
                }
                // MotionEvent.ACTION_DOWN 满足同时触摸两个指示器
                if (isMinIndicatorTouched && isMaxIndicatorTouched) {
                    isMaxIndicatorTouched = currentX - lastX > 0;
                    isMinIndicatorTouched = currentX - lastX < 0;
                }
                if (isMinIndicatorTouched) {
                    minIndicatorMoveToPosition(currentX);
                } else if (isMaxIndicatorTouched) {
                    maxIndicatorMoveToPosition(currentX);
                }
                lastX = currentX;
                break;
            case MotionEvent.ACTION_UP:
                isMinIndicatorTouched = false;
                isMaxIndicatorTouched = false;
                if (minIndicator.getNum().endsWith("+") && maxIndicator.getNum().endsWith("+")) {
                    setPosition(maxValue * 2, maxValue * 2);
                } else if (maxIndicator.getNum().endsWith("+")) {
                    setPosition(Float.parseFloat(minIndicator.getNum()), maxValue * 2);
                } else {
                    setPosition(Float.parseFloat(minIndicator.getNum()), Float.parseFloat(maxIndicator.getNum()));
                }
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    private void maxIndicatorMoveToPosition(float x) {
        maxIndicator.setIndicatorX(Math.max(minIndicator.indicatorX, Math.min(x, endLineX)));
        invalidate();
    }

    private void minIndicatorMoveToPosition(float x) {
        minIndicator.setIndicatorX(Math.max(startLineX, Math.min(x, maxIndicator.indicatorX)));
        invalidate();
    }


    public void setPosition(float value1, float value2) {
        if (maxValue == 0) {
            throw new IllegalArgumentException("请设置最大范围");
        }
        //当设置的值超过最大值则直接将其设置为最大值的平方，确保定位到endLineX
        value1 = value1 > maxValue ? maxValue * maxValue : value1;
        value2 = value2 > maxValue ? maxValue * maxValue : value2;
        float x1 = calculatePositionByNum(value1);
        float x2 = calculatePositionByNum(value2);
        x1 = Math.max(startLineX, Math.min(x1, endLineX));
        x2 = Math.max(startLineX, Math.min(x2, endLineX));
        float minX = value1 < value2 ? x1 : x2;
        float maxX = value1 < value2 ? x2 : x1;
        minIndicator.setIndicatorX(minX);
        maxIndicator.setIndicatorX(maxX);
        invalidate();
    }

    public float calculatePositionByNum(float value) {
        return value / valueStep * (endLineX - startLineX - unlimitedArea) + startLineX;
    }

    public float calculateNumByPosition(float positionX) {
        return (positionX - startLineX) * (maxValue - minValue) / (endLineX - startLineX - unlimitedArea);
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        minValueText.setText(String.valueOf((int) minValue));
        mPaint.getTextBounds(minValueText.getText(), 0, minValueText.getText().length(), minValueText.getRect());
        this.minValue = minValue;
        valueStep = maxValue - minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        maxValueText.setText(maxValue + "+");
        mPaint.getTextBounds(maxValueText.getText(), 0, maxValueText.getText().length(), maxValueText.getRect());
        this.maxValue = maxValue;
        valueStep = maxValue - minValue;
    }

    public void setDegree(int degree) {
        if (degree <= 0) {
            degree = 1;
        }
        this.numDegree = degree;
    }

    public int getNumDegree() {
        return numDegree;
    }

    public int getNumStep() {
        return numStep;
    }

    public void setNumStep(int numStep) {
        this.numStep = numStep;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!indicatorBitmap.isRecycled()) {
            indicatorBitmap.recycle();
        }
        if (!tipBitmap.isRecycled()) {
            tipBitmap.recycle();
        }
    }

    private class DoubleSelectIndicator {

        private float indicatorRadius;
        private float[] indicatorRange = new float[4];
        // 当前所在XY轴的位置（中心点）
        private float indicatorX;
        private float indicatorY;
        private String num;

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
            indicatorRange[0] = indicatorX - indicatorRadius;
            indicatorRange[2] = indicatorX + indicatorRadius;
            updateNum(this.indicatorX);
        }

        public float getIndicatorY() {
            return indicatorY;
        }

        public void setIndicatorY(float indicatorY) {
            this.indicatorY = indicatorY;
            indicatorRange[1] = indicatorY - indicatorRadius;
            indicatorRange[3] = indicatorY + indicatorRadius;
        }

        public String getNum() {
            return num;
        }

        public void setNum(int num) {
            if (num % numDegree == 0) {
                this.num = String.valueOf(num);
            } else {
                // 例如numDegree精度为50，numStep为25，则每当数值大于25则记为50，大于75则记为100
                num += numStep;
                this.num = String.valueOf(num / numDegree * numDegree);
            }
        }

        public void setNum(String num) {
            this.num = num;
        }

        private void updateNum(float x) {
            if (x > endLineX - unlimitedArea) {
                setNum(maxValue + "+");
            } else {
                x += 0.5f;
                int currentNum = (int) calculateNumByPosition(x);
                setNum(currentNum);
            }
        }
    }


    private static class TextBean {
        private Rect rect = new Rect();
        private String text;

        public Rect getRect() {
            return rect;
        }

        public void setRect(Rect rect) {
            this.rect = rect;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
