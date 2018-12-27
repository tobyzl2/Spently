package com.spently.spently;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ConnectionTask extends AsyncTask {
    private final String API_KEY = "6352b9875188957";
    ConnectionResponse connectionResponse;
    Activity activity;
    String imagePath;
    ProgressDialog progress;
    public ConnectionTask(ConnectionResponse setConnectionResponse, Activity setActivity, String setImagePath) {
        this.connectionResponse = setConnectionResponse;
        this.activity = setActivity;
        this.imagePath = setImagePath;
    }
    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(activity);
        progress.setTitle("Wait while processing....");
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
        progress.show();
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(Object[] params) {
        try {
            return new JSONObject(sendPost(imagePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendPost(String imagePath) {

        try {
            URL obj = new URL("https://api.ocr.space/parse/image");
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            JSONObject params = new JSONObject();

            params.put("apikey", API_KEY);
            Log.d("absolute_path", imagePath);
            params.put("base64Image", "data:image" + File.separator + "jpg;base64," + imagePath);
            params.put("isTable", true);

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(getPostDataString(params));
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return String.valueOf(response);
        } catch(Exception e) {
            e.printStackTrace();
        }
//        con.setRequestProperty("User-Agent", "Mozilla/5.0");
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        return null;
    }

    public String getPostDataString(JSONObject params){
        try {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while (itr.hasNext()) {

                String key = itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
        connectionResponse.getResponse(o);
    }
}
