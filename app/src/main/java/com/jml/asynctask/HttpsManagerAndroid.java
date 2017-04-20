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
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class HttpsManagerAndroid {

    static final String TAG = "SSLConnection";


    public static String getData(Context context, String uri) {

        BufferedReader reader = null;

        try {

            HostnameVerifier nullHostNameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            //set hostname verifier to accept any certificate issued
            //to any host. by default certificate will be check
            //if the client/certificate user (in this instance the android application)
            //and the supposed to be owner of the certificate
            //since our certificate is issued for localhost and since
            //android default ip to access to localhost is 10.0.2.2
            //my machine localhost ip will be the ip4 or 127.0.0.1
            //and this is a mismatch and the exception error message
            //usually is encrypted (pun intended).
            //for production, remove this hostname verifier assignment
            //and issue a certificate to the client (forgot the syntax how)
            HttpsURLConnection.setDefaultHostnameVerifier(nullHostNameVerifier);

            //CertificateFactory cf = CertificateFactory.getInstance("X509");
            //InputStream isKeyStore = new BufferedInputStream(context.getResources().openRawResource(R.raw.tomcatclient));
            //Certificate caKeyStore = cf.generateCertificate(isKeyStore);
            //InputStream isTrustStore = new BufferedInputStream(context.getResources().openRawResource(R.raw.tomcatclienttruststore));
            //Certificate caTrustStore = cf.generateCertificate(isTrustStore);


            // Load the self-signed server certificate
            char[] passphrase = "123456".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(context.getResources().openRawResource(R.raw.tomcatclient), passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, passphrase);
            KeyManager[] keyManagers = kmf.getKeyManagers();

            KeyStore trustStore = KeyStore.getInstance("BKS");
            trustStore.load(context.getResources().openRawResource(R.raw.tomcatclienttruststore), passphrase);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            // Create a SSLContext with the certificate
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            //sslContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            sslContext.init(keyManagers, trustManagers, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // Create a HTTPS connection
            URL url = new URL(uri);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setSSLSocketFactory(sslContext.getSocketFactory());

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

            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
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



    protected SSLSocketFactory createAdditionalCertsSSLSocketFactory(Context context) {
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            // the bks file we generated above
            final InputStream in = context.getResources().openRawResource( R.raw.tomcatclient);
            try {
                // don't forget to put the password used above in strings.xml/mystore_password
                ks.load(in, context.getString( R.string.keystorepassword).toCharArray());
            } finally {
                in.close();
            }

            return new AdditionalKeyStoresSSLSocketFactory(ks);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }
}

