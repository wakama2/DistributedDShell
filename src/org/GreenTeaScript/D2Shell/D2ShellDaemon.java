package org.GreenTeaScript.D2Shell;

import java.io.*;
import java.net.*;

public class D2ShellDaemon {

	public static final int DEFAULT_PORT = 10000;
	public static final String KILL_CMD = "<kill>";
	
	private final int port;

	public D2ShellDaemon(int port) throws IOException {
		this.port = port;
	}

	public void accept(Socket sock) {
		try {
			InputStream is = sock.getInputStream();
			OutputStream os = sock.getOutputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			Request r = (Request) ois.readObject();
			CommandResult res = r.exec();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(res);
			oos.flush();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try { sock.close(); } catch(IOException e) {}
		}
	}
	
	public void waitConnectionLoop() throws IOException {
		ServerSocket ss = D2ShellSocketFactory.getDefaultServerSocketFactory().createServerSocket(this.port);
		while(true) {
			final Socket socket = ss.accept();
			Thread th = new Thread() {
				public void run() {
					accept(socket);
				}
			};
			th.start();
		}	
	}

	public static void main(String[] args) throws IOException {
		int port = DEFAULT_PORT;
		if(args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		D2ShellDaemon dm = new D2ShellDaemon(port);
		dm.waitConnectionLoop();
	}

}

