package muc_15_01_14.lab3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

/**
 * Created by David on 21.05.2015.
 */
public class OverlayDraw {

    public static final int BLUEDOT = 0;
    public static final int REDDOT = 1;
    public static final int GREENDOT = 2;
    public static final int CYANDOT = 3;
    public static final int CURDISCOVERABLEDOT=100;

    public static void drawPositionOnOverlay(int angle,Canvas canvas, int paintMode) {

        int size= 50;
        int bgWidth = canvas.getWidth();
        int bgHeight = canvas.getHeight();
        Point p = null;


        double y = Math.cos((2*Math.PI/360)*angle);
        double x = Math.sin((2 * Math.PI / 360) * angle);

            int radius = Math.round(bgHeight * getRealtivRadius(angle));
            p = new Point((int)Math.round(getNullPoint(canvas).x + x * radius),(int)Math.round( getNullPoint(canvas).y - y * radius));


        Paint drawPaint = getPaintMode(paintMode);
        canvas.drawCircle(p.x - (size / 2.0f), p.y - (size / 2.0f), size, drawPaint);
    }


    private static Point getNullPoint(Canvas canvas){
        return new Point(Math.round(canvas.getWidth() / 1.97f), Math.round(canvas.getHeight() / 1.78f));
    }

    private static Paint getPaintMode(int mode){
        Paint drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        switch (mode){
            case BLUEDOT:
                drawPaint.setColor(Color.BLUE);
                break;
            case REDDOT:
                drawPaint.setColor(Color.RED);
                break;
            case CYANDOT:
                drawPaint.setColor(Color.CYAN);
                break;
            case GREENDOT:
                drawPaint.setColor(Color.GREEN);
                break;
            case CURDISCOVERABLEDOT:
                drawPaint.setColor(Color.MAGENTA);
                break;

        }
        return drawPaint;
    }

    private static float getRealtivRadius(int angle){
        return (float)(0.000028f*(Math.pow((angle+14),2.0)+10714.3f));

    }
}
