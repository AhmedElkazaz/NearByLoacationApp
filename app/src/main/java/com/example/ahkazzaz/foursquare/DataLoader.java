package com.example.ahkazzaz.foursquare;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ahkaz on 2/25/2018.
 */

  public  class DataLoader extends AsyncTaskLoader<JSONObject > {
    private  JSONObject jObj = null;
    Bundle bundle;
    InputStream inputStream;
    String id;
    String [] image=new String[1];

String status,email;

    private JSONObject  finalResult;
   String sort="40.74224,-73.99386";
   String news_url,date;

    public DataLoader(Context context, Bundle bundle) {
        super(context);
       this.bundle=bundle;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

//You can change "yyyyMMdd_HHmmss as per your requirement

        date = sdf.format(new Date());

    }

    @Override
    public JSONObject  loadInBackground() {
      //  news_url = "http://192.168.1.113/eea.txt";
      // news_url= "https://api.foursquare.com/v2/venues/explore?client_secret=B3STKR4XNPLNP4HQOFXMQWMKHDTTZR4VAT4ASPTBGD2L0MBJ&client_id=PFG3QNOFU1JAEGJSQNI1MYMACAA5U3RZ1CTKXIWJADZ3CPVW&v=20191225&limit=10&near=San Francisco, CA&section=food";
      news_url= "https://api.foursquare.com/v2/venues/explore?ll="  + bundle.getString("ll") +"&client_id="+"PFG3QNOFU1JAEGJSQNI1MYMACAA5U3RZ1CTKXIWJADZ3CPVW"+"&client_secret="+"B3STKR4XNPLNP4HQOFXMQWMKHDTTZR4VAT4ASPTBGD2L0MBJ"+"&v="+date+"&offset="+"1"+ "&radius="+"1000"+"&limit="+"10" ;

            try {

                URL url = new URL(news_url);
                HttpURLConnection httpURLConnection;
                httpURLConnection = (HttpURLConnection)url.openConnection();
               //httpURLConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
              //  httpURLConnection.addRequestProperty("Authorization", "bearer " + bundle.getString("token"));
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
                String result = "";
                String line=null;
                while((line = bufferedReader.readLine())!= null) {
                    result+=line;
                }

                bufferedReader.close();

                inputStream.close();
                httpURLConnection.disconnect();
                JSONObject image=new JSONObject(result);


                return  image;

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
