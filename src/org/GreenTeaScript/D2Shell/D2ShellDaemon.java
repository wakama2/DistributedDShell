package org.GreenTeaScript.D2Shell;

import java.io.*;
import java.util.Arrays;
import java.net.*;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.DShellProcess;

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
			CommandRequest r = (CommandRequest) ois.readObject();
			//if(DEBUG_MODE) {
				System.out.println("[debug] " + Arrays.toString(r.command));
			//}
			CommandResult res = execCommand(r);
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
		ServerSocket ss = D2ShellSocketFactory.getServerSocketFactory().createServerSocket(this.port);
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

	public static CommandResult execCommand(CommandRequest r) {
		String out = "";
		DShellException ex = null;
		try {
			if(r.input.length() == 0) {
				out = DShellProcess.ExecCommandString(r.command);
			} else {
				String[] c = { "echo", r.input };
				out = DShellProcess.ExecCommandString(c, r.command);
			}
		} catch(DShellException e) {
			ex = e;
		}
		return new CommandResult(out, "", ex);
	}
}

