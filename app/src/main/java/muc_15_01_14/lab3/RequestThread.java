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

/**
 * Created by Sebastian on 25.05.2015.
 */
public class RequestThread extends AsyncTask<String,String,String> {
    private OnPostExecuteListener listener;

    public RequestThread(OnPostExecuteListener listener){this.listener = listener;}

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
            //ignore
        } catch (IOException e) {
            //ignore
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        String tmp = "{time:2015-05-26T20:32:27.364Z,list:[]}";
        if(s != null){
            super.onPostExecute(s);
            tmp = s;
        }
        if(listener != null){

            // inform listeners
            listener.onPostTaskCompleted(tmp);
        }
    }
}
