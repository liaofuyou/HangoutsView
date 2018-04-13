package me.ajax.hangoutsview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import static me.ajax.hangoutsview.utils.GeometryUtils.polarX;
import static me.ajax.hangoutsview.utils.GeometryUtils.polarY;

/**
 * Created by aj on 2018/4/2
 */

public class HangoutsView extends View {

    Paint mPaint = new Paint();
    Paint linePaint = new Paint();

    RectF rectF = new RectF();
    Path path1 = new Path();
    Path path2 = new Path();

    int animationRepeat = 2;
    float animatedFraction = 0;

    int shadowLength = dp2Dx(150);
    ValueAnimator animator;


    public HangoutsView(Context context) {
        super(context);
        init();
    }

    public HangoutsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HangoutsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速

        //画笔
        mPaint.setColor(0xFFFF00FF);
        mPaint.setStrokeWidth(dp2Dx(2));
        mPaint.setStyle(Paint.Style.FILL);

        linePaint.setColor(0xFF0000FF);
        linePaint.setStrokeWidth(dp2Dx(10));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setShader(new LinearGradient(
                0, 0, shadowLength, shadowLength,
                0xFF1C6F3F, 0XFF259A55, Shader.TileMode.CLAMP));

        //mPaint.setPathEffect(new CornerPathEffect(dp2Dx(50)));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animationRepeat = 1;

                animator = ValueAnimator.ofFloat(0, 1);
                animator.setDuration(600);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.setRepeatCount(1);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        animation.setDuration(400);
                        animationRepeat++;
                    }
                });
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        animatedFraction = animation.getAnimatedFraction();
                        invalidateView();
                    }
                });
                animator.start();
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                performClick();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);

        //大逗号
        canvas.save();
        mPaint.setColor(0XFF259A55);
        if (animationRepeat == 1) {
            canvas.scale(animatedFraction, animatedFraction, 0, dp2Dx(100) + dp2Dx(100) / 2.5F);
        }
        canvas.drawPath(bigCommaPath(path1, dp2Dx(100)), mPaint);
        canvas.restore();

        //小逗号及其阴影的绘制
        if (animationRepeat == 2) {

            //限定绘制范围
            canvas.clipPath(path1);

            //小逗号1
            canvas.save();
            mPaint.setColor(0XFFFFFFFF);
            canvas.translate(-dp2Dx(35), dp2Dx(100) * (1 - animatedFraction));
            //阴影
            float r = dp2Dx(22);
            linePaint.setStrokeWidth((float) Math.sqrt(Math.abs((2 * r) * (2 * r) + (-2 * r) * (-2 * r))));
            canvas.drawLine(0, 0, polarX(shadowLength, 45), polarY(shadowLength, 45), linePaint);
            canvas.drawPath(smallCommaPath(path2, r), mPaint);

            //小逗号2
            canvas.translate(dp2Dx(69), 0);
            //阴影
            canvas.drawLine(0, 0, polarX(shadowLength, 45), polarY(shadowLength, 45), linePaint);
            canvas.drawPath(smallCommaPath(path2, r), mPaint);
            canvas.restore();
        }

        canvas.restore();
    }

    //大逗号路径
    Path bigCommaPath(Path path, float radius) {
        path.reset();
        path.addCircle(0, 0, radius, Path.Direction.CW);
        path.moveTo(radius, 0);

        float m = radius / 2.5F;
        path.cubicTo(radius, (radius + m) / 2, radius / 2, (radius + m), 0, radius + m);
        path.lineTo(0, 0);

        return path;
    }

    //小逗号路径
    Path smallCommaPath(Path path, float radius) {
        path.addRect(-radius, -radius, radius, radius, Path.Direction.CW);
        path.moveTo(radius, radius);
        float m = radius / 1F;
        path.cubicTo(radius, (2 * radius + m) / 2, radius / 2, radius + m, 0, radius + m);
        path.lineTo(0, 0);
        return path;
    }


    int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }


    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimAndRemoveCallbacks();
    }

    private void stopAnimAndRemoveCallbacks() {

        if (animator != null) animator.end();

        Handler handler = this.getHandler();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
