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

import de.greencity.bladenightapp.dev.android.R;

public class SslHelper {
	final static private String trustStorePassword = "ssl-password-for-development";
	final static private String keyStorePassword = "ssl-password-for-development";
	
	public static SSLSocketFactory getSSLSocketFactory(Context context) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		final InputStream trustStoreLocation = context.getResources().openRawResource(R.raw.client_truststore); 

		final InputStream keyStoreLocation = context.getResources().openRawResource(R.raw.client_keystore); 

		final KeyStore trustStore = KeyStore.getInstance("BKS");
		trustStore.load(trustStoreLocation, trustStorePassword.toCharArray());

		final KeyStore keyStore = KeyStore.getInstance("BKS");
		keyStore.load(keyStoreLocation, keyStorePassword.toCharArray());

		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);

		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyStorePassword.toCharArray());

		final SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

		return sslCtx.getSocketFactory();
	}

}
