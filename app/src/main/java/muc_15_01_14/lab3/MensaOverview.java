package muc_15_01_14.lab3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.widget.ImageView;

/**
 * Created by David on 21.05.2015.
 */
public class MensaOverview extends  Canvas{

    private int bg = R.drawable.mensa_map_angled_north;
    private Bitmap bitmap;

    MensaOverview(Context c) {
        Bitmap background = BitmapFactory.decodeResource(c.getResources(), R.drawable.mensa_map_angled_north);
        bitmap = Bitmap.createBitmap(background.getWidth(),background.getHeight(),Bitmap.Config.ARGB_8888);


        Paint drawPaint = new Paint();
        drawPaint.setColor( Color.BLUE);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);


        drawCircle(10,10, 20, drawPaint);
    }
}
