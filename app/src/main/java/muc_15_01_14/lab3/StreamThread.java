package muc_15_01_14.lab3;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sebastian on 26.05.2015.
 */
public class StreamThread extends Thread {
    private URL url = null;
    private HttpURLConnection urlConnection = null;

    public StreamThread(){
        try {
            url = new URL("http://barracuda-vm9.informatik.uni-ulm.de/orientations/stream");
        } catch (MalformedURLException e) {
            Log.e("ERROR","ERROR");
            e.printStackTrace();
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e("ERROR","ERROR");
            e.printStackTrace();
        }


    }
    @Override
    public void run() {
        Log.i("test","test");
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] data      = new byte[1024];
            int    bytesRead = in.read(data);
            while(bytesRead!=-1){
                String str = new String(data, "UTF-8");
                bytesRead = in.read(data);
                Log.i("test",str);
                break;
            }
            Log.i("break","done");
            in.close();
            urlConnection.disconnect();
        } catch (IOException e) {
            Log.e("ERROR","ERROR");
            e.printStackTrace();
        } finally {
            Log.i("done","with test");
        }

    }

}
