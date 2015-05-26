package muc_15_01_14.lab3;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import muc_15_01_14.lab3.interfaces.OnPostExecuteListener;

/**
 * Created by Sebastian on 25.05.2015.
 */
public class RequestThread extends AsyncTask<String,String,String> {
    private OnPostExecuteListener listener;

    @Override
    protected String doInBackground(String[] uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        String tmp = "{time:2011-05-31T14:24:34.020Z," +
                "list:[" +
                "{user:florian,age:6,orientation:120}," +
                "{user:benjamin,age:4,orientation:165}," +
                "{user:bastian,age:2,orientation:123}" +
                "]}";
        tmp = "ERROR";
        if(s != null){
            super.onPostExecute(s);
            //Log.i("RESPONSE", s);
            tmp = s;
        } else {
            //Log.i("RESPONSE",tmp);
        }
        if(listener != null){
            listener.onPostTaskCompleted(tmp);
        }
    }

    public void setListener(OnPostExecuteListener listener) {
        this.listener = listener;
    }
}
