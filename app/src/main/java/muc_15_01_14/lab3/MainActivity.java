package muc_15_01_14.lab3;



import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements SensorEventListener, OnPostExecuteListener, OnStreamUpdateListener {

    private Canvas overlayCanvas;

    private boolean findMode;

    private SensorManager mSensor;
    private Sensor orientationSensor;
    private List<Float> orientation;

    private float myPosition;
    private boolean positionSent;
    private StreamThread streamThread;
    private String username;
    private List<Person> m_persons;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        streamThread = new StreamThread(this);
        positionSent = false;
        findMode = true;
        ((TextView)findViewById(R.id.txt_info)).setVisibility(View.INVISIBLE);
        Button btnDeletePosition = (Button) findViewById(R.id.btn_deletePosition);
        btnDeletePosition.setVisibility(View.INVISIBLE);
        btnDeletePosition.setEnabled(false);
        final Button btnSentPosition = (Button) findViewById(R.id.btn_sendPosition);
        btnSentPosition.setVisibility(View.INVISIBLE);
        btnSentPosition.setEnabled(false);


        // text flied for user name
        final EditText etxtName = (EditText) findViewById(R.id.etxt_name);
        etxtName.setVisibility(View.INVISIBLE);
        etxtName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (etxtName.getText().length() >= 4 && etxtName.getText().length() <= 12) {
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
        orientation = new ArrayList();
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
    public void onPostTaskCompleted(String s) {
        if(s.equals(String.valueOf(202))){
            //Toast.makeText(getApplicationContext(),"202 Accepted",Toast.LENGTH_LONG).show();
        } else if(s.equals(String.valueOf(404))) {
            //Toast.makeText(getApplicationContext(),"404 Not Found",Toast.LENGTH_LONG).show();
        } else if(s.equals(String.valueOf(204))) {
            //Toast.makeText(getApplicationContext(),"204 No Content",Toast.LENGTH_LONG).show();
        } else {
            try {
                JSONObject jObject = new JSONObject(s);
                JSONArray jArray = jObject.getJSONArray("list");
                if (jArray.length() > 0) {
                    //Toast.makeText(getApplicationContext(),"Response received",Toast.LENGTH_LONG).show();
                    List<Person> list = new ArrayList<Person>();
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject obj = jArray.getJSONObject(i);
                        list.add(new Person(obj.getString("user"), obj.getInt("orientation"),obj.getInt("age")));
                    }
                    displayAvailablePersons(list);
                } else {
                    //Toast.makeText(getApplicationContext(),"nothing received",Toast.LENGTH_LONG).show();
                    //test();
                    List<Person> list = new ArrayList<Person>();
                    displayAvailablePersons(list);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(!streamThread.isAlive()) {
            streamThread.interrupt();
            streamThread = new StreamThread(this);
            streamThread.start();
        }
    }

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
        if(streamThread.isAlive()){
            streamThread.interrupt();
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
                displayCurrentPositionPoint(Math.round(getAverageOrientation(orientation)), overlayCanvas);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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

    // button click on select mode
    public void onChangeMode(View view) {
        setScreenMode(this.findMode);
    }

    private void setScreenMode(boolean findMode) {
        Button btnMode = (Button) findViewById(R.id.btn_mode);
        EditText etxtName = (EditText) findViewById(R.id.etxt_name);
        Button btnSendPosition = (Button) findViewById(R.id.btn_sendPosition);
        Button btnDeletePosition = (Button) findViewById(R.id.btn_deletePosition);
        ImageView img = (ImageView) findViewById(R.id.img_background);
        ImageView imgOverlay = (ImageView) findViewById(R.id.img_overlay);
        ImageView imgIcon =(ImageView) findViewById(R.id.img_icon);
        ListView list = (ListView) findViewById(R.id.list_persons);
        TextView txtInfo = (TextView) findViewById(R.id.txt_info);


        // show components for the discoverable mode
        if (findMode) {
            this.findMode = false;
            btnMode.setText("Switch Finder");
            txtInfo.setVisibility(View.VISIBLE);
            btnSendPosition.setVisibility(View.VISIBLE);
            btnDeletePosition.setVisibility(View.VISIBLE);
            etxtName.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
            //btnSendPosition.setEnabled(false);
            img.setRotation(180);
            imgOverlay.setRotation(180);
            imgIcon.setImageResource(R.drawable.position);
        }
        // show components for the find mode
        else {
            this.findMode = true;
            btnMode.setText("Switch to Discoverable");
            txtInfo.setVisibility(View.INVISIBLE);
            btnSendPosition.setVisibility(View.INVISIBLE);
            btnDeletePosition.setVisibility(View.INVISIBLE);
            etxtName.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
            img.setRotation(0);
            imgOverlay.setRotation(0);
            imgIcon.setImageResource(R.drawable.find);

            // get the snapshot from the server
            RequestThread requestThread = new RequestThread(this);
            requestThread.execute("http://barracuda-vm9.informatik.uni-ulm.de/orientations/snapshot");
        }
    }

    // button click on send position
    public void onSendPosition(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
        myPosition = getAverageOrientation(orientation);
        positionSent = true;
        displayCurrentPositionPoint(myPosition, overlayCanvas);

        String name = ((EditText)findViewById(R.id.etxt_name)).getText().toString().trim();
        username = name;

        ((Button)findViewById(R.id.btn_deletePosition)).setEnabled(true);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        ((TextView) findViewById(R.id.txt_info)).setText("Username: " + name + "\nPosition sent at "+sdf.format(System.currentTimeMillis()));

        //popupDialog(this, "Orientation", "x: " + Float.toString(getAverageOrientation(orientation)) + "\n name: " + name);
        SendThread sendThread = new SendThread(this);
        int o = (int)getAverageOrientation(orientation);
        Log.i("DEGREE",String.valueOf(o));
        o = (int)OverlayDraw.transferLocalPositionToGlobal(o);
        Log.i("DEGREE","to "+String.valueOf(o));
        HttpPut httpPut = new HttpPut("http://barracuda-vm9.informatik.uni-ulm.de/user/"+name+"/orientation/"+Integer.toString(o));
        sendThread.execute(httpPut);
    }

    // button click on delete position
    public void  onDeletePosition(View view){
        if(positionSent){
            positionSent=false;
            myPosition=0;
        }
        ((TextView) findViewById(R.id.txt_info)).setText("");
        ((Button)findViewById(R.id.btn_deletePosition)).setEnabled(false);
        if(username!=null) {
            SendThread sendThread = new SendThread(this);
            HttpDelete httpDelete = new HttpDelete("http://barracuda-vm9.informatik.uni-ulm.de/user/" + username + "/orientation");
            sendThread.execute(httpDelete);
        }
    }

    // shows all persons in list on map and in list
    public void displayAvailablePersons(List<Person> persons){

        m_persons = persons;
        persons = OverlayDraw.addColorsToPersons(this,persons);
        final ArrayAdapter<Person> adapter = new PersonArrayAdapter(this, R.layout.person_item,persons);
        final ListView list = (ListView) findViewById(R.id.list_persons);

        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (Person p:persons){
            OverlayDraw.drawPositionOnOverlay(p.getOrientation(), overlayCanvas, p.getColor());
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                list.setAdapter(adapter);
                // set overlay invalidate to repaint image
                ((ImageView) findViewById(R.id.img_overlay)).invalidate();
            }
        });
    }



    private void displayCurrentPositionPoint(float angle, Canvas overlayCanvas) {
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


    // just for debug reasons
    private void test() {
        int x = overlayCanvas.getWidth();
        int y = overlayCanvas.getHeight();
        Log.i("Overlay Dimension (x,y)", "(" + x + "," + y + ")");

        // yellow test dot
        Paint drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setColor(Color.YELLOW);
        overlayCanvas.drawCircle(x / 1.97f, y / 1.78f, 20, drawPaint);



        List<Person> list = new ArrayList<Person>();
        list.add(new Person("Sebastian", -110, 20));
        list.add(new Person("Teresa", 20, 1020));
        list.add(new Person("David", 60, 160));
        list.add(new Person("Test", -26, 620));



        displayAvailablePersons(list);
    }

    // listens for a push notification of the server
    @Override
    public void onUpdate(Person person) {

        // checks if any thing there to update
        // else try to get a snapshot
        if(m_persons.isEmpty()){
            RequestThread requestThread = new RequestThread(this);
            requestThread.execute("http://barracuda-vm9.informatik.uni-ulm.de/orientations/snapshot");
        }

        // check if person is already in m_persons
        boolean contains = false;

        // check if person was tagged as 'logout' and needs to be removed from m_persons
        Person remove = null;
        for (Person p : m_persons) {
            if (p.getUser().equals(person.getUser())) {
                contains = true;
                if(person.getOrientation()==-99999){
                    remove = p;
                } else {

                    // update orientation
                    p.setOrientation(person.getOrientation());
                }
            }
        }
        if(remove!=null){
            m_persons.remove(remove);
        }
        if(!contains){
            m_persons.add(person);
        }

        Log.i("update","user: "+person.getUser());

        // make the changes visible in the view
        displayAvailablePersons(m_persons);
    }

    // listens for the EOF notification from the server
    // restarts the StreamThread
    @Override
    public void onEOF() {
        streamThread.interrupt();
        streamThread = new StreamThread(this);
        streamThread.start();
    }
}
