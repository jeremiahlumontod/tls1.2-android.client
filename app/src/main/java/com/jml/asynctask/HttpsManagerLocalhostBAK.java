package com.jml.asynctask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;


public class HttpsManagerLocalhostBAK {

    static final String TAG = "SSLConnection";

    public static String getData(Context context, String uri) {

        BufferedReader reader = null;

        try {
            URL url = new URL(uri);
            HttpsURLConnection con = (HttpsURLConnection) setUpHttpsConnection(context,uri);

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }

    @SuppressLint("SdCardPath")
    public static HttpsURLConnection setUpHttpsConnection(Context context, String urlString) {

        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            // My CRT file that I put in the assets folder
            // I got this file by following these steps:
            // * Go to https://littlesvr.ca using Firefox
            // * Click the padlock/More/Security/View Certificate/Details/Export
            // * Saved the file as littlesvr.crt (type X.509 Certificate (PEM))
            // The MainActivity.context is declared as:
            // public static Context context;
            // And initialized in MainActivity.onCreate() as:
            // MainActivity.context = getApplicationContext();
            //InputStream caInput = new BufferedInputStream(context.getAssets().open("littlesvrca.crt"));
            InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.tomcatservercert));
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, tmf.getTrustManagers(), null);


            // Create an HostnameVerifier that hardwires the expected hostname.
            // Note that is different than the URL's hostname:
            // example.com versus example.org
            /**HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("https://10.0.2.2:8443", session);
                }
            };*/

            HostnameVerifier nullHostNameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("https://10.0.2.2:8443", session);
                }
            };

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.i(TAG, "HOST NAME " + hostname);
                    if (hostname.contentEquals("10.0.2.2")) {
                        Log.i(TAG, "Approving certificate for host " + hostname);
                        System.out.println("Approving certificate for host " + hostname);
                        return true;
                    }else{
                        Log.i(TAG, "Denying certificate for host " + hostname);
                        System.out.println("Denying certificate for host " + hostname);
                    }
                    return false;
                }
            };

            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            //urlConnection.setHostnameVerifier(hostnameVerifier);
            urlConnection.setDefaultHostnameVerifier(hostnameVerifier);
            urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());

            return urlConnection;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }

}

