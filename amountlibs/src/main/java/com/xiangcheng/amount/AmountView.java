package com.xiangcheng.amount;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangcheng on 18/1/2.
 */

    public class AmountView extends View {
    //默认最大的额度
    private static final int MAX_AMOUNT = 3000000;
    private static final String TAG = AmountView.class.getSimpleName();
    private int amount = MAX_AMOUNT;
    private Paint radianPaint;
    private int mMinWidth = 190;
    private int mMinHeight = 120;

    private Path radianPath;
    private Path shadowPath;
    private Path shadowProgressPath;
    private int strokeWidth;
    private int shadowOffset;
    private Paint shadowPaint;
    private int shadowStrokeWidth;
    private int shadowColor = Color.parseColor("#ffa000");
    private int alphaShadowColor = Color.parseColor("#ffca28");

    private List<ValueAnimator> animators;

    private Path progressPath;

    private PathMeasure progressPathMeasure;



    private PathMeasure shadowPathMeasure;

    private String amountText;
    private Paint amountPaint;
    private Paint progressPaint;
    private String hintText = "تومان";
    private Paint hintPaint;
    private int hintOffset;
    private float textTop;
    private int[] amounts;

    //修复进度和数字不同时变动加的，add 2018/1/5
    private Path realProgressPath;
    private PathMeasure realProgressPathMeasure;

    private Path realShadowPath;
    private PathMeasure realShadowPathMeasure;
    private Paint paint;
    private Canvas arcCanvas;

    private float startAngle, endAngle;
    private RectF arcOval = new RectF();
    private float width;

    public AmountView(Context context) {
        this(context, null);
    }

    public AmountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initArgus(attrs, context);
    }

    private void initArgus(AttributeSet attrs, Context context) {

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);


        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AmountView);
        amount = array.getInt(R.styleable.AmountView_max_amount, MAX_AMOUNT);
        hintText = array.getString(R.styleable.AmountView_hint_text);
        if (TextUtils.isEmpty(hintText)) {
            hintText = context.getString(R.string.default_hint_amount);
        }
        shadowColor = array.getColor(R.styleable.AmountView_shadow_color, shadowColor);
        //透明度是33,16进制换成10进制就是3*16+3
        alphaShadowColor = Color.argb(16 * 2 + 1, Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor));
        Log.d(TAG, "amount:" + amount);
        shadowOffset = dp2px(9);
        strokeWidth = dp2px(4);
        shadowStrokeWidth = dp2px(3);
        hintOffset = dp2px(3);
        mMinWidth = dp2px(mMinWidth);
        mMinHeight = dp2px(mMinHeight);
        radianPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        radianPaint.setStrokeWidth(strokeWidth);
        radianPaint.setStyle(Paint.Style.STROKE);
        radianPaint.setColor(alphaShadowColor);
        radianPath = new Path();
        shadowPath = new Path();
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setPathEffect(new DashPathEffect(new float[]{dp2px(2.5f), dp2px(4)}, 1));
        shadowPaint.setStrokeWidth(shadowStrokeWidth);
        shadowPaint.setStyle(Paint.Style.STROKE);
        amountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        amountPaint.setColor(Color.BLACK);
        amountPaint.setTextSize(dp2sp(20));
        amountPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(Color.BLACK);
        hintPaint.setTextSize(dp2sp(10));
        hintPaint.setTextAlign(Paint.Align.CENTER);
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        initAnimator();
    }

    public void setAmount(int amount) {
        this.amount = amount;
        initAnimator();
    }

    public void setFont(Typeface font){
        amountPaint.setTypeface(font);
    }

    private void initAnimator() {
        animators = new ArrayList<>();
        String format;
        if (amount >= 1000) {
            if (Build.VERSION.SDK_INT >= 24) {
                DecimalFormat df = new DecimalFormat("#,###");
                format = df.format(amount);
            } else {
                String amountStr = String.valueOf(amount);
                Log.d(TAG, "amountStr:" + amountStr);
                StringBuilder sb = new StringBuilder();
                for (int i = amountStr.length() - 1; i >= 0; i--) {
                    sb.append(amountStr.charAt(i));
                    Log.d(TAG, "amountStr.charAt(i):" + amountStr.charAt(i));
                }
                String amountFromR2L = sb.toString();
                List<String> amountList = new ArrayList<>();

                while (amountFromR2L.length() > 3) {
                    amountList.add(amountFromR2L.substring(0, 3) + ",");
                    amountFromR2L = amountFromR2L.substring(3, amountFromR2L.length());
                }
                Log.d(TAG, "amountFromR2L:" + amountFromR2L);
                if (amountFromR2L.length() > 0 && amountFromR2L.length() < 3) {
                    amountList.add(amountFromR2L);
                }
                sb = new StringBuilder();
                for (int i = 0; i < amountList.size(); i++) {
                    sb.append(amountList.get(i));
                }
                StringBuilder finalSb = new StringBuilder();
                for (int i = sb.length() - 1; i >= 0; i--) {
                    finalSb.append(sb.toString().charAt(i));
                }
                format = finalSb.toString();
            }
        } else {
            format = amount + "";
        }
        String[] split = format.split(",");
        amounts = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            Log.d(TAG, "split[i]:" + split[i]);
            if (split[i].equals("000")) {
                split[i] = 1000 + "";
            }
            final int index = i;
            ValueAnimator animator = ValueAnimator.ofInt(0, Integer.parseInt(split[i]));
            animator.setDuration(1500);
            animator.setInterpolator(new LinearInterpolator());

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    amounts[index] = value;
                    if (index == 0) {
                        progressPath.reset();
                        shadowProgressPath.reset();
                        float percent = animation.getAnimatedFraction();
                        Log.d(TAG, "percent:" + percent);
                        realProgressPathMeasure.getSegment(0, percent * realProgressPathMeasure.getLength(), progressPath, true);
                        realShadowPathMeasure.getSegment(0, realShadowPathMeasure.getLength() * percent, shadowProgressPath, true);
                    }
                    invalidate();
                }
            });
            animators.add(animator);
        }

    }

    public void start() {
        for (int i = 0; i < animators.size(); i++) {
            ValueAnimator valueAnimator = animators.get(i);
            if (valueAnimator != null && valueAnimator.isRunning()) {
                return;
            }
            valueAnimator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        drawCircle(canvas);
        drawShadow(canvas);
        drawAmountText(canvas);
        drawProgress(canvas);
        drawHint(canvas);
    }

    private void drawHint(Canvas canvas) {
        Paint.FontMetrics fontMetrics = hintPaint.getFontMetrics();
        float allHeight = fontMetrics.descent - fontMetrics.ascent;
        canvas.drawText(hintText, getWidth() / 2, textTop - hintOffset - allHeight - fontMetrics.ascent, hintPaint);
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawPath(progressPath, progressPaint);
    }

    private void drawAmountText(Canvas canvas) {
        amountText = "";
        for (int i = 0; i < amounts.length; i++) {
            if (i > 0) {
                if ((amounts[i] + "").equals("1000")) {
                    amountText += "000,";
                } else {
                    amountText += amounts[i] + ",";
                }
            } else {
                amountText += amounts[i] + ",";
            }
        }
        amountText = amountText.substring(0, amountText.length() - 1);
        Paint.FontMetrics fontMetrics = amountPaint.getFontMetrics();
        float allHeight = fontMetrics.descent - fontMetrics.ascent;
        textTop = getHeight() - getPaddingBottom() - allHeight;
        canvas.drawText(amountText, getWidth() / 2, textTop - fontMetrics.ascent, amountPaint);
    }
    public void setStrokeWidth(float width) {
        this.width = width;
        paint.setStrokeWidth(width);
    }

    private void drawShadow(Canvas canvas) {
        canvas.drawPath(shadowProgressPath, shadowPaint);
    }

    private void drawCircle(Canvas canvas) {
        arcCanvas = canvas;
        float sweep = this.getEndAngle() - startAngle;
        if (sweep > 360){
            sweep -= 360;
        }
        arcCanvas.drawArc(arcOval, startAngle, sweep, false, paint);

    //    canvas.drawPath(radianPath, radianPaint);
    }
    public float getEndAngle(){
        return this.endAngle;
    }
    public float getStrokeWidth() {
        return this.width;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public void setEndAngle(float endAngle){
        this.endAngle = endAngle;
    }



    public void setOvalSize(float left, float top, float right, float bottom) {
        arcOval.set(left, top, right, bottom);
    }
    public RectF getOval() {
        return arcOval;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mMinWidth = mMinWidth + getPaddingLeft() + getPaddingRight();
        mMinHeight = mMinHeight + getPaddingTop() + getPaddingBottom();
        Log.d(TAG, "getPaddingLeft:" + getPaddingLeft());
        if (widthMode == MeasureSpec.AT_MOST) {
            if (heightMode == MeasureSpec.EXACTLY) {
                width = ((height - getPaddingTop() - getPaddingBottom()) * 2) + getPaddingLeft() + getPaddingRight();
            } else {
                width = mMinWidth;
                height = mMinHeight;
            }
        } else if (widthMode == MeasureSpec.EXACTLY) {
            if (heightMode == MeasureSpec.EXACTLY) {
                width = Math.min(width - getPaddingLeft() - getPaddingRight(), ((height - getPaddingTop() - getPaddingBottom()) * 2));
                height = (int) (width * 1.0 / 2) + getPaddingTop() + getPaddingBottom();
                width = width + getPaddingLeft() + getPaddingRight();
            } else {
                height = (width - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingTop() + getPaddingBottom();
            }
        } else {
            width = mMinWidth;
            height = mMinHeight;
        }
        RectF rect = new RectF();
        rect.left = getPaddingLeft() + strokeWidth;
        rect.right = width - getPaddingRight() - strokeWidth;
        rect.top = getPaddingTop() + strokeWidth;
        rect.bottom = height - getPaddingBottom();
        radianPath.addCircle(width / 2, height - getPaddingBottom(),
                height - getPaddingTop() - getPaddingBottom() - strokeWidth, Path.Direction.CW);
        shadowPath.addCircle(width / 2, height - getPaddingBottom(),
                height - getPaddingTop() - getPaddingBottom() - strokeWidth - shadowOffset, Path.Direction.CW);
        shadowPaint.setShader(new LinearGradient(rect.left, rect.bottom, rect.right, rect.bottom, alphaShadowColor,
                shadowColor, Shader.TileMode.CLAMP));
        progressPaint.setShader(new LinearGradient(rect.left, rect.bottom, rect.right, rect.bottom, alphaShadowColor,
                shadowColor, Shader.TileMode.CLAMP));
        progressPath = new Path();
        shadowProgressPath = new Path();
        progressPathMeasure = new PathMeasure();
        progressPathMeasure.setPath(radianPath, false);
        realProgressPath = new Path();
        realProgressPathMeasure = new PathMeasure();
        progressPathMeasure.getSegment(progressPathMeasure.getLength() * 0.5f, progressPathMeasure.getLength(), realProgressPath, true);
        realProgressPathMeasure.setPath(realProgressPath, false);

        shadowPathMeasure = new PathMeasure();
        shadowPathMeasure.setPath(shadowPath, false);
        realShadowPath = new Path();
        realShadowPathMeasure = new PathMeasure();
        shadowPathMeasure.getSegment(shadowPathMeasure.getLength() * 0.5f, shadowPathMeasure.getLength(), realShadowPath, true);
        realShadowPathMeasure.setPath(realShadowPath, false);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        start();
    }

    private int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private int dp2sp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < animators.size(); i++) {
            ValueAnimator valueAnimator = animators.get(i);
            if (valueAnimator != null && valueAnimator.isRunning()) {
                valueAnimator.end();
            }
        }
    }
}
