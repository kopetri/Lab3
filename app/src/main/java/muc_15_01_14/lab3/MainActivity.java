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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private Canvas overlayCanvas;

    private boolean findMode;

    private SensorManager mSensor;
    private Sensor orientationSensor;
    private List<Float> orientation;

    private float myPosition;
    private boolean positionSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        positionSent = false;
        findMode = true;
        final Button btnSentPosition = (Button) findViewById(R.id.btn_sendPosition);
        btnSentPosition.setVisibility(View.INVISIBLE);
        btnSentPosition.setEnabled(false);
        final EditText etxtName = (EditText) findViewById(R.id.etxt_name);
        etxtName.setVisibility(View.INVISIBLE);
        etxtName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (etxtName.getText().length() >= 2) {
                    btnSentPosition.setEnabled(true);
                } else {
                    btnSentPosition.setEnabled(false);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // create Overlay
        Bitmap backgroundBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.mensa_map_angled_north);
        Bitmap overlayBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);


        // attach the canvas to the ImageView
        ImageView img_overlay = (ImageView) findViewById(R.id.img_overlay);
        img_overlay.setImageDrawable(new BitmapDrawable(getResources(), overlayBitmap));

        // initialise sensor
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
        // register sensor
        if (orientationSensor != null) {
            mSensor.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("Sensor", "No orientation available");
            this.finish();
        }
        setScreenMode(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor
        if (orientationSensor != null) {
            mSensor.unregisterListener(this);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            // set the last 3 values to the list to build an average value
            if (orientation.size() >= 3) {
                orientation.remove(0);
            }
            orientation.add(OverlayDraw.transferGlobalPositionToLocal(event.values[0]));

            // if discovered mode is on displays own position on map
            if (!findMode) {
                // draw current position
                drawCurrentPositionPoint(Math.round(getAverageOrientation(orientation)), overlayCanvas);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private void drawCurrentPositionPoint(float angle, Canvas overlayCanvas) {
        // clears the overlay image
        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // select der right dot representation
        if (positionSent) {
            float angleDifference = Math.abs(myPosition - angle);
            if (angleDifference < 5) {
                // draw sent position
                OverlayDraw.drawPositionOnOverlay(Math.round(myPosition), overlayCanvas, OverlayDraw.DISCOVERABLE_DOT_VALID);
            } else if (angleDifference < 15) {
                // draw current position
                OverlayDraw.drawPositionOnOverlay(Math.round(getAverageOrientation(orientation)), overlayCanvas, OverlayDraw.CURRENT_DISCOVERABLE_DOT);
                // draw sent position
                OverlayDraw.drawPositionOnOverlay(Math.round(myPosition), overlayCanvas, OverlayDraw.DISCOVERABLE_DOT_INVALID_0);
            } else {
                // draw current position
                OverlayDraw.drawPositionOnOverlay(Math.round(getAverageOrientation(orientation)), overlayCanvas, OverlayDraw.CURRENT_DISCOVERABLE_DOT);
                // draw sent position
                OverlayDraw.drawPositionOnOverlay(Math.round(myPosition), overlayCanvas, OverlayDraw.DISCOVERABLE_DOT_INVALID_1);
            }
        } else {
            // draw current position
            OverlayDraw.drawPositionOnOverlay(Math.round(getAverageOrientation(orientation)), overlayCanvas, OverlayDraw.CURRENT_DISCOVERABLE_DOT);
        }

        // set overlay invalidate to repaint image
        ((ImageView) findViewById(R.id.img_overlay)).invalidate();
    }

    // calculates an average orientation of values in the list
    private static float getAverageOrientation(List<Float> list) {
        float result = 0;
        float i = 0;
        for (Float f : list) {
            i++;
            result += f;
        }
        if (i != 0) {

            return result / i;
        }
        return 0;
    }

    public void onChangeMode(View view) {
        setScreenMode(this.findMode);
    }

    private void setScreenMode(boolean findMode) {
        Button btnMode = (Button) findViewById(R.id.btn_mode);
        EditText etxtName = (EditText) findViewById(R.id.etxt_name);
        Button btnSendPosition = (Button) findViewById(R.id.btn_sendPosition);
        ImageView img = (ImageView) findViewById(R.id.img_background);
        ImageView imgOverlay = (ImageView) findViewById(R.id.img_overlay);
        ImageView imgIcon =(ImageView) findViewById(R.id.img_icon);

        // show components for the find mode
        if (findMode) {
            this.findMode = false;
            btnMode.setText("Start Finder");
            btnSendPosition.setVisibility(View.VISIBLE);
            etxtName.setVisibility(View.VISIBLE);
            //btnSendPosition.setEnabled(false);
            img.setRotation(180);
            imgOverlay.setRotation(180);
            imgIcon.setImageResource(R.drawable.position);

        }
        // show components for the discoverable mode
        else {
            this.findMode = true;
            btnMode.setText("Start Discoverable");
            btnSendPosition.setVisibility(View.INVISIBLE);
            etxtName.setVisibility(View.INVISIBLE);
            img.setRotation(0);
            imgOverlay.setRotation(0);
            imgIcon.setImageResource(R.drawable.find);
            test();
        }
    }

    public void onSendPosition(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
        myPosition = getAverageOrientation(orientation);
        positionSent = true;
        drawCurrentPositionPoint(myPosition, overlayCanvas);

        String name = ((EditText)findViewById(R.id.etxt_name)).getText().toString().trim();

        popupDialog(this, "Orientation", "x: " + Float.toString(getAverageOrientation(orientation)) + "\n name: "+name);
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


    public void availablePersons(List<Person> persons){


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


        OverlayDraw.drawPositionOnOverlay(0, overlayCanvas, OverlayDraw.BLUE_DOT);
        OverlayDraw.drawPositionOnOverlay(-30, overlayCanvas, OverlayDraw.RED_DOT);
        OverlayDraw.drawPositionOnOverlay(60, overlayCanvas, OverlayDraw.CYAN_DOT);
        OverlayDraw.drawPositionOnOverlay(-110, overlayCanvas, OverlayDraw.GREEN_DOT);

        this.getWindow().getDecorView().invalidate();
    }
}
