package com.jml.asynctask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class HttpsManagerLocalhostMutualAuthentication {

    static final String TAG = "SSLConnection";


    public static String getDataBksMutualAuthentication(Context context, String uri) {

        BufferedReader reader = null;

        try {

            // setup truststore to provide trust for the server certificate

            // load truststore certificate
            InputStream clientTruststoreIs = context.getResources().openRawResource(R.raw.tomcatclienttruststore);
            KeyStore trustStore = null;
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(clientTruststoreIs, "MyPassword".toCharArray());

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
            SSLSocketFactory sslSocketFactory = null;
            sslSocketFactory = new SSLSocketFactory(SSLSocketFactory.TLS, keyStore, "123456",
                    trustStore, null, null);

            URL url = new URL(uri);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });





            //conn.setSSLSocketFactory(sslSocketFactory);

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

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

            InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.tomcatclient));
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca certificate data\n\n\n");
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
            SSLContext sslContext = SSLContext.getInstance("TLS");
            //sslcontext.init(null, tmf.getTrustManagers(), null);
            sslContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());


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

            /**HostnameVerifier hostnameVerifier = new HostnameVerifier() {

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
            };*/


            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            //urlConnection.setHostnameVerifier(hostnameVerifier);
            //urlConnection.setDefaultHostnameVerifier(hostnameVerifier);
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            return urlConnection;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }



    @SuppressLint("SdCardPath")
    public static HttpsURLConnection setUpHttpsConnectionForBKS(Context context, String urlString) {

        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.tomcatclient));
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca certificate data\n\n\n");
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
            SSLContext sslContext = SSLContext.getInstance("TLS");
            //sslcontext.init(null, tmf.getTrustManagers(), null);
            sslContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());


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

            /**HostnameVerifier hostnameVerifier = new HostnameVerifier() {

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
            };*/


            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            //urlConnection.setHostnameVerifier(hostnameVerifier);
            //urlConnection.setDefaultHostnameVerifier(hostnameVerifier);
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            return urlConnection;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }


}

