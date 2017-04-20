package com.jml.asynctask;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public final class SSLUtils {

    private SSLUtils() { //non instantiable
    }

    public static String[] getCipherSuitesWhiteList(String[] cipherSuites) {
        List<String> whiteList = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        for (String suite : cipherSuites) {
            String s = suite.toLowerCase();
            if (s.contains("anon") || //reject no anonymous
                    s.contains("export") || //reject no export
                    s.contains("null") || //reject no encryption
                    s.contains("md5") || //reject MD5 (weaknesses)
                    s.contains("_des") || //reject DES (key size too small)
                    s.contains("krb5") || //reject Kerberos: unlikely to be used
                    s.contains("ssl") || //reject ssl (only tls)
                    s.contains("empty")) {    //not sure what this one is
                rejected.add(suite);
            } else {
                whiteList.add(suite);
            }
        }
        Log.i("Rejected Cipher Suites ", rejected.toString());
        return whiteList.toArray(new String[whiteList.size()]);
    }




}