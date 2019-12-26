package com.example.ahkazzaz.foursquare;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ahkaz on 2/25/2018.
 */

  public  class PhotoLoader extends AsyncTaskLoader<JSONObject > {
    private  JSONObject jObj = null;
    Bundle bundle;
    JSONArray itemsArray;
    JSONArray resultArray=new JSONArray();
String status,email;

    private JSONObject  finalResult;
   String news_url;
   String []id,photos;
    JSONObject image,response,item;
    JSONObject result=new JSONObject();
    String date;

    public PhotoLoader(Context context, Bundle bundle) {
        super(context);
       this.bundle=bundle;
       id=bundle.getStringArray("id");
       photos=new String [id.length];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

//You can change "yyyyMMdd_HHmmss as per your requirement

        date = sdf.format(new Date());

    }

    @Override
    public JSONObject  loadInBackground() {
        for(int i=0;i<id.length;i++) {
            news_url = " https://api.foursquare.com/v2/venues/" + id[i] + "/photos?" + "&group=venue" + "&client_id=" + "PFG3QNOFU1JAEGJSQNI1MYMACAA5U3RZ1CTKXIWJADZ3CPVW" + "&client_secret=" + "B3STKR4XNPLNP4HQOFXMQWMKHDTTZR4VAT4ASPTBGD2L0MBJ" + "&v=" + date;

            try {

                URL url = new URL(news_url);
                HttpURLConnection httpURLConnection;
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String result = "";
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

                bufferedReader.close();

                inputStream.close();
                httpURLConnection.disconnect();

                 image = new JSONObject(result);
                 response = image.getJSONObject("response");
                JSONArray itemsArray=response.getJSONObject("photos").getJSONArray("items");
                 item=itemsArray.getJSONObject(0);
                photos[i]=item.getString("prefix")+item.getString("width")+
                        "x"+item.getString("height")+item.getString("suffix");
                resultArray.put(photos[i]);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        try {
            return  result.put("res",resultArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void deliverResult(JSONObject data) {
        finalResult=data;

        super.deliverResult(data);
    }


    @Override
    protected void onStartLoading() {
        if (finalResult != null) {
            deliverResult(finalResult);
        } else {

            forceLoad();
        }



    }

}
