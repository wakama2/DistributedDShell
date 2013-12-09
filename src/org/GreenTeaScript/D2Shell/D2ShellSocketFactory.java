package org.GreenTeaScript.D2Shell;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class D2ShellSocketFactory {
	
	static final String keyStorePath = "ext/d2shell.keystore";
	static final char[] pass = "konoha".toCharArray();
	
	static SSLSocketFactory getSSLSocketFactory() throws GeneralSecurityException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keyStorePath), pass);
		TrustManagerFactory kmf = TrustManagerFactory.getInstance("SunX509");
		kmf.init(keyStore);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, kmf.getTrustManagers(), null);
		return context.getSocketFactory();
	}

	static SSLServerSocketFactory getSSLServerSocketFactory() throws GeneralSecurityException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keyStorePath), pass);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keyStore, pass);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(kmf.getKeyManagers(), null, null);
		SSLServerSocketFactory ssf = context.getServerSocketFactory();
		return ssf;
	}
	
	private static SocketFactory sf = null;
	private static ServerSocketFactory ssf = null;
	
	public static SocketFactory getSocketFactory() {
		if(sf == null) {
			try {
				sf = getSSLSocketFactory();
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return sf;
	}
	
	public static ServerSocketFactory getServerSocketFactory() {
		if(ssf == null) {
			try {
				ssf = getSSLServerSocketFactory();
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return ssf;
	}
	
}
