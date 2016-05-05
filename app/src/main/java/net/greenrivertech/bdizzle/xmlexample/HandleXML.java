package net.greenrivertech.bdizzle.xmlexample;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HandleXML {
    // We don't use namespaces
    private static final String ns = null;

    private String country = "county";
    private String temperature = "temperature";
    private String humidity = "humidity";
    private String pressure = "pressure";
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    public HandleXML(String url){
        this.urlString = url;
    }

    public String getCountry(){
        return country;
    }

    public String getTemperature(){
        return temperature;
    }

    public String getHumidity(){
        return humidity;
    }

    public String getPressure(){
        return pressure;
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser) throws XmlPullParserException, IOException {

            String text = null;

            myParser.require(XmlPullParser.START_TAG, ns, "current");


            while (myParser.next() != XmlPullParser.END_DOCUMENT) {

                if (myParser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                if (myParser.getEventType() == XmlPullParser.TEXT) {
                    text = myParser.getText();
                }

                String name = myParser.getName();
                if (name.equals("city")){

                    country = myParser.getAttributeValue(null, "name");

                 } else if (name.equals("temperature")) {

                    temperature = myParser.getAttributeValue(null, "value");

                } else if (name.equals("humidity")) {

                    humidity = myParser.getAttributeValue(null, "value");

                } else if (name.equals("clouds")) {

                    pressure = myParser.getAttributeValue(null, "name");

                } else {
                    skip(myParser);
                }

                parsingComplete = false;
            }

    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public void fetchXML(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream stream = conn.getInputStream();

                    XmlPullParser myparser = Xml.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    //Skip global tag
                    myparser.nextTag();

                    parseXMLAndStoreIt(myparser);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}