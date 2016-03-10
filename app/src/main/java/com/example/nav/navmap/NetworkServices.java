package com.example.nav.navmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by nav on 3/8/16.
 */
public class NetworkServices extends AsyncTask<String, String, String> {

    HttpURLConnection urlConnection;
    Context context;

    /**
     * Class that provides network services.
     *
     * @param context pass the context in which to run the network services. Preferably the application context or the activity context.
     */
    public NetworkServices(Context context) {
        this.context = context;
    }

    /**
     * Use this method to make async network calls.
     * To get the results of an async request listen to the callback via NetworkServicesListener.
     *
     * @param url      the url to request
     * @param callback the callback which implements NetworkServicesListener
     */
    public void runAsyncNetworkTask(String url, NetworkServicesInterface callback) {
        if (callback != null & url != null) {
            String urls[] = new String[1];
            urls[0] = url;
            try {
                callback.Result(execute(url).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        if (isNetworkAvailable()) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(params[0]);
                Log.e(getClass().getSimpleName(), "making async call to " + url);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }


            return result.toString();
        } else {

            Toast.makeText(context, "NetworkServices Connection Not Availble", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }

    /**
     * Get Bitmap from url.
     *
     * @param img_url the url from which to retrieve the bitmap
     * @return the Bitmap retrieved
     */
    public Bitmap getBitmapFromUrl(final String img_url) {
        try {
            return new AsyncTask<Void, Bitmap, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bmp = null;
                    try {
                        URL url = new URL(img_url);
                        bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return bmp;
                }

                @Override
                protected void onPostExecute(Bitmap aVoid) {
                    super.onPostExecute(aVoid);
                }

            }.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Class that check for network availability.
     *
     * @return
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
