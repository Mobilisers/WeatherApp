package com.example.nav.serviceslibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by nav on 3/8/16.
 */
public class NetworkServices {
    Context context;
    final static String INTERNET_NOT_AVAILABLE = "Internet connection not available";

    /**
     * Class that provides network services.
     *
     * @param context pass the context in which to run the network services. Preferably the application context or the activity context.
     */
    public NetworkServices(Context context) {
        this.context = context;
    }

    /**
     * Use this method to make async network calls and returns result string.
     * To get the results of an async request listen to the callback via NetworkServicesListener.
     *
     * @param url      the url to request
     * @param callback the callback which implements NetworkServicesListener
     */
    public void runAsyncNetworkTask(String url, final NetworkServicesInterface callback) {
        if (isNetworkAvailable()) {
            if (callback != null & url != null) {
                String urls[] = new String[1];
                urls[0] = url;
                new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... urls) {
                        HttpURLConnection urlConnection = null;
                        StringBuilder result = new StringBuilder();
                        InputStream in = null;
                        BufferedReader reader = null;
                        try {
                            URL url = new URL(urls[0]);
                            //fix for escaping spaces that results in malformed url
                            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                            url = uri.toURL();
                            Log.e(getClass().getSimpleName(), "making async call to " + url);
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setInstanceFollowRedirects(true);
                            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                in = new BufferedInputStream(urlConnection.getInputStream());
                                reader = new BufferedReader(new InputStreamReader(in));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    result.append(line);
                                }

                            } else {
                                Log.e(getClass().getSimpleName(), "HTTP ERROR CODE " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());
                                in = new BufferedInputStream(urlConnection.getErrorStream());
                                reader = new BufferedReader(new InputStreamReader(in));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    result.append(line);
                                }
                                Log.e(getClass().getSimpleName(), "Error: " + line);
                                urlConnection.getErrorStream().close();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return result.toString();
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        if (result.length() > 0) {
                            callback.onResult(result);
                        }
                    }
                }.execute(urls);
            }
        }
    }

    /**
     * Get Bitmap from url.
     *
     * @param img_url the url from which to retrieve the bitmap
     * @return the Bitmap retrieved
     */
    public Bitmap getBitmapFromUrl(final String img_url) {
        if (isNetworkAvailable()) {
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
        } else {
            Toast.makeText(context, INTERNET_NOT_AVAILABLE, Toast.LENGTH_LONG).show();
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
        } else {
            if (context != null) {
                Toast.makeText(context, INTERNET_NOT_AVAILABLE, Toast.LENGTH_LONG).show();
            } else {
                Log.e(getClass().getSimpleName(), INTERNET_NOT_AVAILABLE);
            }
            return false;
        }
    }
}
