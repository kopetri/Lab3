package muc_15_01_14.lab3;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Sebastian on 26.05.2015.
 */
public class SendThread extends AsyncTask<HttpUriRequest,String,String> {
    private OnPostExecuteListener listener;
    @Override
    protected String doInBackground(HttpUriRequest... httpUriRequests) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(httpUriRequests[0]);
            StatusLine statusLine = response.getStatusLine();
            return String.valueOf(statusLine.getStatusCode());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(listener != null && s != null){
            listener.onPostTaskCompleted(s);
        }
    }

    public void setListener(OnPostExecuteListener listener) {
        this.listener = listener;
    }
}
