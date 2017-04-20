package com.jml.asynctask;

import java.io.*;
import java.security.KeyStore;

import javax.net.ssl.*;

import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.*;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;


public class HttpsManagerApacheLib {

    static final String TAG = "SSLConnection";


    public static String getData(Context context, String uri) {

        BufferedReader reader = null;

        try {

            // setup truststore to provide trust for the server certificate

            // load truststore certificate
            InputStream clientTruststoreIs = context.getResources().openRawResource(R.raw.tomcatclienttruststore);
            KeyStore trustStore = null;
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(clientTruststoreIs, "123456".toCharArray());

            System.out.println("Loaded server certificates: " + trustStore.size());

            // initialize trust manager factory with the read truststore
            TrustManagerFactory trustManagerFactory = null;
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // setup client certificate

            // load client certificate
            InputStream keyStoreStream = context.getResources().openRawResource(R.raw.tomcatclient);
            KeyStore keyStore = null;
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(keyStoreStream, "123456".toCharArray());

            System.out.println("Loaded client certificates: " + keyStore.size());

            // initialize key manager factory with the read client certificate
            KeyManagerFactory keyManagerFactory = null;
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "123456".toCharArray());


            // initialize SSLSocketFactory to use the certificates
            SSLSocketFactory socketFactory = null;
            socketFactory = new SSLSocketFactory(SSLSocketFactory.TLS, keyStore, "123456",
                    trustStore, null, null);
            socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            // Set basic data
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams.setUserAgent(params, "Android app/1.0.0");

            // Make pool
            ConnPerRoute connPerRoute = new ConnPerRouteBean(12);
            ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
            ConnManagerParams.setMaxTotalConnections(params, 20);

            // Set timeout
            HttpConnectionParams.setStaleCheckingEnabled(params, false);
            HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
            HttpConnectionParams.setSoTimeout(params, 20 * 1000);
            HttpConnectionParams.setSocketBufferSize(params, 8192);

            // Some client params
            HttpClientParams.setRedirecting(params, false);

            // Register http/s shemas!
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", socketFactory, 8443));
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
            DefaultHttpClient sClient = new DefaultHttpClient(conMgr, params);

            HttpGet httpGet = new HttpGet(uri);
            HttpResponse response = sClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();

            InputStream is = httpEntity.getContent();
            BufferedReader read = new BufferedReader(new InputStreamReader(is));
            String query = null;
            StringBuilder sb = new StringBuilder();
            while ((query = read.readLine()) != null) {
                sb.append(query + "\n");
                System.out.println(query);
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







}

