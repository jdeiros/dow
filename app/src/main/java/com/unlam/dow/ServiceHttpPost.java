package com.unlam.dow;

import android.app.IntentService;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.Nullable;

public class ServiceHttpPost extends IntentService {

    private Exception mException = null;
    private HttpURLConnection httpConnection;
    private URL mUrl;

    public ServiceHttpPost() {
        super("ServiceHttpPost");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("LOG_SERVICE", "Service onCreate()");
    }

    //onHandleIntent genera un thread aparte.
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            String uri = intent.getExtras().getString("uri");
            JSONObject jsonData = new JSONObject(intent.getExtras().getString("jsonData"));

            runPost(uri, jsonData);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("LOG_SERVICE", "ERROR");
        }
    }

    private void runPost(String uri, JSONObject jsonData) {
        String result = POST(uri, jsonData);
        if(result == null){
            Log.e("LOG_SERVICE", "Error en POST:\n" + mException.toString());
            return;
        }
        if(result == "NO_OK"){
            Log.e("LOG_SERVICE", "Se recibió respuesta NO_OK");
            return;
        }

        Intent intent = new Intent("com.unlam.intentservice.intent.action.RESPONSE_OPERATION");
        intent.putExtra("jsonData", result);
        sendBroadcast(intent);
    }

    private String POST(String uri, JSONObject jsonData) {
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            URL mUrl = new URL(uri);
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("POST");

            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.write(jsonData.toString().getBytes("UTF-8"));
            Log.i("LOG_SERVICE", "Se envía al servidor: " + jsonData.toString());
            dataOutputStream.flush();

            urlConnection.connect();
            //aqui se queda esperando respuesta, esto es bloqueante por eso se hace en un hilo aparte.
            int responseCode = urlConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                result = inputStreamReader.toString();
            }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getErrorStream());
                result = inputStreamReader.toString();
            }else{
                result = "NO_OK";
            }

            mException = null;
            dataOutputStream.close();
            urlConnection.disconnect();
            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            mException = e;
        } catch (IOException e) {
            e.printStackTrace();
            mException = e;
        }finally {
            return result;
        }
    }
}
