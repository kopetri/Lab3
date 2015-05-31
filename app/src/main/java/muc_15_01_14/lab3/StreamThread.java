package muc_15_01_14.lab3;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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
    private OnStreamUpdateListener listener = null;


    // this thread opens the stream until EOF
    // it then tells its listeners that it reached EOF
    public StreamThread(OnStreamUpdateListener listener){
        this.listener = listener;
    }

    @Override
    public void run() {
        Log.i("StreamThread","started");
        long start = System.currentTimeMillis();
        try {

            //open connection and stuff
            url = new URL("http://barracuda-vm9.informatik.uni-ulm.de/orientations/stream");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setChunkedStreamingMode(0);
            if(urlConnection != null) {
                while(!isInterrupted()) {

                    // if 200 OK response from server
                    if (urlConnection.getResponseMessage().equals("OK")) {

                        // open buffered input stream to read from stream
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        byte[] data = new byte[1024];
                        int bytesRead = in.read(data);

                        //read until EOFException
                        while (bytesRead != -1) {
                            String str = new String(data, "UTF-8");
                            JSONObject jObject = null;
                            try {

                                // response string to json object
                                jObject = new JSONObject(str);
                                Person person = null;
                                // check for update attribute
                                if (jObject.has("update")) {
                                    jObject = jObject.getJSONObject("update");

                                    // extract attributes
                                    int o = jObject.getInt("orientation");
                                    person = new Person(jObject.getString("user"),o, 0);

                                // check for logout attribute
                                } else if (jObject.has("logout")) {
                                    jObject = jObject.getJSONObject("logout");
                                    person = new Person(jObject.getString("user"), -99999, 0);
                                }

                                // inform listener about the update
                                listener.onUpdate(person);
                            } catch (JSONException e) {/*ignore and continue*/}
                            bytesRead = in.read(data);
                        }
                        in.close();
                    }
                }
            }
            // return, so it doesn't create a infinite loop
            return;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // ignore
        } finally {
            if(urlConnection!=null)
                    urlConnection.disconnect();
            long stop = System.currentTimeMillis();
            Log.i("StreamThread","stoped after " +(Math.abs(stop - start))/1000 +" seconds");
            //tell listeners that it is done
            listener.onEOF();
        }

    }
}
