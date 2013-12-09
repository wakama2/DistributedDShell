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
	
	static SocketFactory createSocketFactory() {
		return SocketFactory.getDefault();
	}
	
	static ServerSocketFactory createServerSocketFactory() {
		return ServerSocketFactory.getDefault();
	}
	
	static final String keyStorePath = "ext/d2shell.keystore";
	static final char[] pass = { 'k', 'o', 'n', 'o', 'h', 'a' };
	
	static SSLSocketFactory createSSLSocketFactory() throws GeneralSecurityException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keyStorePath), pass);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(keyStore);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		return context.getSocketFactory();
	}

	static SSLServerSocketFactory createSSLServerSocketFactory() throws GeneralSecurityException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keyStorePath), pass);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keyStore, pass);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(kmf.getKeyManagers(), null, null);
		SSLServerSocketFactory ssf = context.getServerSocketFactory();
		return ssf;
	}
	
	private static SocketFactory sf;
	private static ServerSocketFactory ssf;
	
	static {
		try {
			sf = createSSLSocketFactory();
			ssf = createSSLServerSocketFactory();
//			sf = getSocketFactory();
//			ssf = getServerSocketFactory();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SocketFactory getDefaultSocketFactory() {
		return sf;
	}
	
	public static ServerSocketFactory getDefaultServerSocketFactory() {
		return ssf;
	}
	
}
