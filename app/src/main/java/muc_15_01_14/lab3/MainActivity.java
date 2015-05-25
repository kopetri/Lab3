package muc_15_01_14.lab3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements SensorEventListener{

    private Canvas overlayCanvas;

    private boolean findModus;

    private SensorManager mSensor;
    private Sensor orientationSensor;
    private List<Float> orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findModus = true;
        ((Button)findViewById(R.id.btn_sendPosition)).setVisibility(View.INVISIBLE);


        // Create Overlay
        Bitmap backgroundBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.mensa_map_angled_north);
        Bitmap overlayBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);



        //Attach the canvas to the ImageView
        ImageView img_overlay = (ImageView) findViewById(R.id.img_overlay);
        img_overlay.setImageDrawable(new BitmapDrawable(getResources(), overlayBitmap));


        orientation = new ArrayList<>();
        mSensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationSensor = mSensor.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            popupDialog(this, "About", "Mobile & Ubiquitous Computing\nAssignment 3 - Mensafinder\n\nTheresa Hirzle\nSebastian Hardwig\nDavid Lehr");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(orientationSensor != null){
            mSensor.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("Sensor","No orientation available");
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(orientationSensor != null){
            mSensor.unregisterListener(this);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){

            if(orientation.size()>0){
            orientation.remove(0);
            }
            orientation.add(event.values[0]);

            if(!findModus){
                overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            OverlayDraw.drawPositionOnOverlay(Math.round(getAverageOrientation(orientation)), overlayCanvas, OverlayDraw.CURDISCOVERABLEDOT);

                this.getWindow().getDecorView().invalidate();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private static float getAverageOrientation(List<Float> list){
        float result = 0;
        float i=0;
        for(Float f:list){
            i++;
            result += f;
        }
        if(i!=0){

            return result/i;
        }
        return 0;
    }

    public void onChangeMode(View view) {
        Button btnMode = (Button)findViewById(R.id.btn_mode);
        Button btnSendPosition =(Button)findViewById(R.id.btn_sendPosition);
        TextView txt = (TextView) findViewById(R.id.txt_headline);
        ImageView img = (ImageView) findViewById(R.id.img_background);
        ImageView img_overlay = (ImageView) findViewById(R.id.img_overlay);
        if(findModus){
            findModus=false;
            Log.i("findModus",""+findModus);
            btnMode.setText("Start Finder");
            txt.setText("Discoverable");
            btnSendPosition.setVisibility(View.VISIBLE);
            //btnSendPosition.setEnabled(false);
            img.setRotation(180);
            img_overlay.setRotation(180);

        }else{
            Log.i("findModus",""+findModus);
            findModus=true;
            btnMode.setText("Start Discoverable");
            txt.setText("Available Persons");
            btnSendPosition.setVisibility(View.INVISIBLE);
            img.setRotation(0);
            img_overlay.setRotation(0);
            test();
        }
    }

    public void onSendPosition(View view) {
        popupDialog(this,"Orientation","x: "+Float.toString(getAverageOrientation(orientation)));
    }


    // Popup Dialog
    public static void popupDialog(Context context, String title, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void test() {

        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int x = overlayCanvas.getWidth();
        int y = overlayCanvas.getHeight();
        Log.i("Overlay Dimension (x,y)", "(" + x + "," + y + ")");

        Paint drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setColor(Color.YELLOW);
        overlayCanvas.drawCircle(x / 1.97f, y / 1.78f, 20, drawPaint);


        OverlayDraw.drawPositionOnOverlay(0, overlayCanvas, OverlayDraw.BLUEDOT);
        OverlayDraw.drawPositionOnOverlay(-30, overlayCanvas,  OverlayDraw.REDDOT);
        OverlayDraw.drawPositionOnOverlay(60, overlayCanvas,  OverlayDraw.CYANDOT);
        OverlayDraw.drawPositionOnOverlay(-110, overlayCanvas, OverlayDraw.GREENDOT);

        this.getWindow().getDecorView().invalidate();
    }
}
