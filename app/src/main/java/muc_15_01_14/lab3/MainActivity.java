package muc_15_01_14.lab3;

import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import muc_15_01_14.lab3.interfaces.OnPostExecuteListener;


public class MainActivity extends ActionBarActivity implements OnPostExecuteListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RequestThread requestThread = new RequestThread();
        final SendThread sendThread = new SendThread();
        requestThread.setListener(this);
        Button searchButton = (Button) findViewById(R.id.state_searching_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestThread.execute("http://barracuda-vm9.informatik.uni-ulm.de/orientations/snapshot");
                //sendThread.execute("http://barracuda-vm9.informatik.uni-ulm.de/user/testpilot/orientation/133");
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostTaskCompleted(String s) {

        try {
            JSONObject jObject = new JSONObject(s);
            JSONArray jArray = jObject.getJSONArray("list");
            for(int i=0;i<jArray.length();i++){
                JSONObject obj = jArray.getJSONObject(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
