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
			D2ShellProtocol.Server sv = new D2ShellProtocol.Server(sock);
			Request r = sv.req;

			// change thread-local stream
			D2ShellClient.getStreamSet().in = sv.stdin;
			D2ShellClient.getStreamSet().out = sv.stdout;
			D2ShellClient.getStreamSet().err = sv.stderr;

			CommandResult res = r.exec();
			if(res.exception != null) {
				sv.sendException(res.exception);
			}
			sv.flush();
		} catch(ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void waitConnectionLoop() throws IOException {
		ServerSocket ss = D2ShellSocketFactory.getDefaultServerSocketFactory().createServerSocket(this.port);
		while(true) {
			final Socket socket = ss.accept();
			Thread th = new Thread() {
				public void run() {
					accept(socket);
					try {
						socket.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
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

