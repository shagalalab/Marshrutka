package com.shagalalab.marshrutka.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.shagalalab.marshrutka.R;

import java.util.ArrayList;

/**
 * Created by aziz on 7/16/15.
 */
public class PathDrawer extends View {
    Paint mCirclePaint, mLinePaint;
    ArrayList<Point> mPoints = new ArrayList<>();
    private final static int RADIUS_DP = 10;
    private final static int LINE_STROKE_LENGTH_DP = 5;
    private float mRadius;
    private float mLineStrokeLength;

    public PathDrawer(Context context) {
        this(context, null);
    }

    public PathDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mRadius = convertDpToPx(RADIUS_DP);
        mLineStrokeLength = convertDpToPx(LINE_STROKE_LENGTH_DP);

        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(getResources().getColor(R.color.theme_accent_1));

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(mLineStrokeLength);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(getResources().getColor(R.color.theme_accent_1));
    }

    private float convertDpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void initDrawing(LinearLayout linearLayout) {
        // we already know that linearLayout has only TextView-type children

        int childCount = linearLayout.getChildCount();
        for (int i=0; i<childCount; i++) {
            View textView = linearLayout.getChildAt(i);
            int top = textView.getTop();
            int bottom = textView.getBottom();
            mPoints.add(new Point(getMeasuredWidth()/2, (top + bottom) / 2));
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mPoints.size() > 0) {
            int count = mPoints.size();
            for (int i=0; i<count; i++) {
                Point point = mPoints.get(i);
                canvas.drawCircle(point.x, point.y, mRadius, mCirclePaint);
                if (i > 0) {
                    Point prevPoint = mPoints.get(i-1);
                    canvas.drawLine(prevPoint.x, prevPoint.y, point.x, point.y, mLinePaint);
                }
            }
        }
    }
}