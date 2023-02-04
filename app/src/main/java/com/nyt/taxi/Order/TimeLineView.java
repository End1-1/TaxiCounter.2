package com.nyt.taxi.Order;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class TimeLineView extends View {

    private float mTotalLength = 0;
    private float mPastLength = 0;
    private String mTimeLeft = "";
    private String mArrivalTime = "";

    public TimeLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLenght(float l) {
        mTotalLength = l;
        invalidate();
    }

    public void setPastLength(float l) {
        mPastLength = l;
        if (mPastLength > mTotalLength) {
            mPastLength = mTotalLength;
        }
        invalidate();
    }

    public void setTimeLeft(String t) {
        mTimeLeft = t;
        invalidate();
    }

    public void setArrivalTime(String t) {
        mArrivalTime = t;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float middleHeight = (getHeight() / 2) + 10;

        RectF rbgs = new RectF(3, 3, getWidth() + 2, getHeight() + 2);
        Paint lbg = new Paint();
        lbg.setStyle(Paint.Style.FILL);
        lbg.setColor(Color.parseColor("#e0e0e0"));
        //canvas.drawRoundRect(rbgs, 20, 20, lbg);

        RectF rbg = new RectF(2, 2, getWidth() - 5, getHeight() - 5);

        lbg.setStyle(Paint.Style.FILL);
        lbg.setColor(Color.WHITE);
        canvas.drawRoundRect(rbg, 10, 10, lbg);

        lbg.setStyle(Paint.Style.STROKE);
        lbg.setColor(Color.parseColor("#e5e5e5"));
        canvas.drawRoundRect(rbg, 10, 10, lbg);

        float left = (getWidth() / mTotalLength) * mPastLength;
        if (left > getWidth() - 46) {
            left = getWidth() - 48;
        }

        Paint lp = new Paint();
        lp.setColor(Color.parseColor("#54FF17"));
        lp.setStrokeWidth(5);
        canvas.drawLine(4, middleHeight, getWidth() - 10, middleHeight, lp);
        lp.setColor(Color.parseColor("#a1a1a1"));
        canvas.drawLine(4, middleHeight, left + 4, middleHeight, lp);

        Path triangle = new Path();
        triangle.moveTo(left + 4, middleHeight - 25);
        triangle.lineTo(left + 4, middleHeight + 25);
        triangle.lineTo(left + 25, middleHeight);
        triangle.close();

        Paint ltf = new Paint();
        ltf.setStyle(Paint.Style.FILL);
        ltf.setColor(Color.YELLOW);
        canvas.drawPath(triangle, ltf);

        Paint ltb = new Paint();
        ltb.setStyle(Paint.Style.STROKE);
        ltb.setStrokeWidth(2);
        ltb.setColor(Color.BLACK);
        canvas.drawPath(triangle, ltb);

        Paint ltext = new Paint();
        ltext.setStyle(Paint.Style.FILL);
        ltext.setColor(Color.BLACK);
        ltext.setTextSize(50);
        canvas.drawText(String.format("%.1f%s", (mTotalLength - mPastLength) / 1000, "km"), 4, 2 + ltext.getTextSize(), ltext);
        canvas.drawText(mTimeLeft, getWidth() - ltext.measureText(mTimeLeft) - 14, 2 + ltext.getTextSize(), ltext);
        ltext.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(mArrivalTime, (getWidth() / 2) - (ltext.measureText(mArrivalTime) / 2), 2 + ltext.getTextSize(), ltext);

        super.onDraw(canvas);
    }
}
