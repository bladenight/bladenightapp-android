package de.greencity.bladenightapp.android.network;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.dev.android.R;

public class SslHelper {
    final static private String trustStorePassword = "ssl-password-for-development";
    final static private String keyStorePassword = "ssl-password-for-development";

    // SSL Socket Factory for javax
    public static SSLSocketFactory getSSLSocketFactory(Context context) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

        Log.d("CERT", "START");

        final InputStream trustStoreLocation = context.getResources().openRawResource(R.raw.client_truststore);
        final KeyStore trustStore = KeyStore.getInstance("BKS");
        trustStore.load(trustStoreLocation, trustStorePassword.toCharArray());
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

//      Log.d("CERT", "START DUMP");
//      X509TrustManager xtm = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
//      for (java.security.cert.X509Certificate cert : xtm.getAcceptedIssuers()) {
//          String certStr = "S:" + cert.getSubjectDN().getName() + "\nI:"
//                              + cert.getIssuerDN().getName();
//          Log.d("CERT", certStr);
//      }
//
        final InputStream keyStoreLocation = context.getResources().openRawResource(R.raw.client_keystore);
        final KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(keyStoreLocation, keyStorePassword.toCharArray());
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

        return sslContext.getSocketFactory();
    }

    // SSL Socket Factory for the Apache HTTP library
    public static org.apache.http.conn.ssl.SSLSocketFactory getSocketFactory(Context context) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

        final InputStream trustStoreLocation = context.getResources().openRawResource(R.raw.client_truststore);
        final KeyStore trustStore = KeyStore.getInstance("BKS");
        trustStore.load(trustStoreLocation, trustStorePassword.toCharArray());
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        final InputStream keyStoreLocation = context.getResources().openRawResource(R.raw.client_keystore);
        final KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(keyStoreLocation, keyStorePassword.toCharArray());
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());


        return new org.apache.http.conn.ssl.SSLSocketFactory(keyStore, keyStorePassword, trustStore);
    }


}
