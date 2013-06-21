package com.panda.menu;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity {

    private static final String DEBUG_TAG = "Main";
    private TextView textView;
    private InputStream stream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        //call the my connection method here

        myConnection();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void myConnection() {
        String stringUrl = "http://10.0.2.2:8080/";

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");

        }


    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
    }


    private String downloadUrl(String myurl) throws IOException {
        InputStream stream = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);

            stream = conn.getInputStream();
            return startParse(stream);
        }
        finally {
            if(stream != null){
                stream.close();
            }
        }
    }



    private String startParse(InputStream stream){
        XmlPullParserFactory pullParserFactory;

        try {
            pullParserFactory = XmlPullParserFactory.newInstance();

            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);

            return parseXML(parser);

        } catch (XmlPullParserException e) {
            return "There was an XML Pull Parser Exception in startParse()";

        } catch (IOException e) {
            // TODO Auto-generated catch block
            return "There was an IO Exception in startParse()";
        }
    }


    private String parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
    {
        String testName = null;
        ArrayList<Restaurant> restaurants = null;
        int eventType = parser.getEventType();
        Restaurant currentProduct = null;
        testName = "before starting loop";
        while (eventType != XmlPullParser.END_DOCUMENT){
            testName += " (" + eventType + ") ";
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    restaurants = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    testName += " " + name + " ";
                    if (name.equals("restaurant")){
                        currentProduct = new Restaurant();
                    } else if (currentProduct != null){
                        if (name.equals("name")){
                            currentProduct.restaurantName = parser.nextText();
                        }
                    }
                    break;
//                    case XmlPullParser.TEXT:
//                        testName += " ["  + parser.getText() + "] ";
//                        break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("restaurant") && currentProduct != null){
                        restaurants.add(currentProduct);
                    }
            }
            eventType = parser.next();
        }
        String theName = getRestoName(restaurants);
        return theName;
    }
    private String getRestoName(ArrayList<Restaurant> restaurants)
    {
        String theName = "";
        Iterator<Restaurant> it = restaurants.iterator();
        while(it.hasNext())
        {
            Restaurant currProduct  = it.next();
            theName = currProduct.restaurantName;
        }
        return theName;
    }
}
