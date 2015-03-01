package af.beaconfinder.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import af.beaconfinder.Beacon.BeaconFilter;
import af.beaconfinder.Beacon.Trilateration;

/**
 * Created by hugo on 01/03/15.
 */
public class CanvasView extends View {

    private static final String TAG = "CanvasView";

    // setup initial color
    private final int paintColor = Color.BLACK;
    private final int paintPosColor = Color.GREEN;
    private final int paintBeaconColor = Color.BLUE;

    private DashPathEffect dashPath = new DashPathEffect(new float[]{5,5}, (float)1.0);

    // defines paint and canvas
    private Paint drawPaint, textPaint, circlePaint;
    private Trilateration.Point mPosition = null;

    private double mOWidth = 5.0; // Max X
    private double mOHeight = 5.0; // Max Y

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();
    }

    // Setup paint with color and stroke styles
    private void setupPaint() {
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(2);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.ROUND);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circlePaint.setPathEffect(dashPath);
        circlePaint.setColor(paintColor);

        textPaint = new Paint();
        textPaint.setTextSize(20);
        textPaint.setColor(paintColor);
        textPaint.setAntiAlias(true);
    }

    private void drawPoint(Canvas canvas, float x, float y, float r, int color) {

        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setColor(color);
        canvas.drawCircle(x, y, r, drawPaint);

        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(paintColor);
        canvas.drawCircle(x, y, r, drawPaint);
    }

    private void drawCircle(Canvas canvas, float x, float y, float r, int color) {
        //if(r > 2000) {
        //    return;
        //}
        Log.d(TAG, "Drawing cricle at x: " + x + " y:" + y + " r:" +r);
        circlePaint.setColor(color);
        canvas.drawCircle(x, y, r, circlePaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {


        Log.d(TAG, "Drawing on canvas " + canvas.getWidth() + ":" + canvas.getHeight());

        Double xScale = (canvas.getWidth()/mOWidth);
        Double yScale = (canvas.getHeight()/mOHeight);
        Double yMiddle = yScale/2;
        Double xMiddle = xScale/2;

        float xM = (xScale.floatValue() * 0.5f);
        float yM = (yScale.floatValue() * 0.5f);


        Log.d(TAG, "Scale: " + xScale.doubleValue()+ ":" + yScale.doubleValue());

        drawPaint.setColor(paintColor);
        for(Trilateration.Beacon b : Trilateration.mBeacons.values()) {
            float sX = (xScale.floatValue() * b.x().floatValue());
            float sY = (yScale.floatValue() * b.y().floatValue());

            Log.d(TAG, "NoMargin " + b.getId() + " on x:" + sX + " y:" + sY);

            if(sX < xMiddle) sX += xM; else sX -= xM;
            if(sY < yMiddle) sY += yM; else sY -= yM;

            Log.d(TAG, "Drawing " + b.getId() + " on x:" + sX + " y:" + sY);
            drawPoint(canvas, sX, sY, 40,b.getColor());

            if(b.getDistance() != null) {
                Log.d(TAG, "Distance:" + (b.getDistance().floatValue()*yScale.floatValue()));
                drawCircle(canvas, sX, sY, (b.getDistance().floatValue()*yScale.floatValue()), b.getColor());
            }

            canvas.drawText(b.getId(), sX+80, sY+10, textPaint);
        }

        if(mPosition != null) {
            float spX = (xScale.floatValue() * mPosition.x.floatValue());
            float spY = (yScale.floatValue() * mPosition.y.floatValue());

            if(spX < xMiddle) spX += xM; else spX -= xM;
            if(spY < yMiddle) spY += yM; else spY -= yM;

            Log.d(TAG, "Drawing position x:" + spX + " y:" + spY);
            drawPoint(canvas, spX, spY, 40, paintPosColor);
        }
    }

    public void updatePositionPoint(Trilateration.Point p) {
        mPosition = p;
        invalidate();
    }
}
