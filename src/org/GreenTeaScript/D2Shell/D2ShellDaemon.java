package org.GreenTeaScript.D2Shell;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.DShellProcess;

public class D2ShellDaemon {

	public static final int DEFAULT_PORT = 10000;
	public static final String KILL_CMD = "<kill>";
	
	public static final int WORKERS = 4;
	
	public boolean DEAMON_MODE = false;
	
	private final LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue<Task>();
	private final Worker[] workers = new Worker[WORKERS];
	private final int port;

	abstract class Task implements Runnable {
		Socket socket;
		public Task(Socket socket) {
			this.socket = socket;
		}
	}
	
	class Worker extends Thread {
		public void run() {
			while(true) {
				Task task;
				try {
					task = taskQueue.take();
					if(task == null) return;
					try {
						task.run();
					} catch(Exception e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public D2ShellDaemon(int port) throws IOException {
		this.port = port;
		for(int i=0; i<WORKERS; i++) {
			Worker w = new Worker();
			workers[i] = w;
			w.start();
		}
	}

	public void accept(final Socket sock) throws IOException {
		taskQueue.offer(new Task(sock) {
			@Override public void run() {
				try {
					accept(sock, sock.getInputStream(), sock.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void accept(Socket sock, InputStream is, OutputStream os) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(is);
		try {
			CommandRequest r = (CommandRequest) ois.readObject();
			//if(DEAMON_MODE) {
				System.out.println("[debug] " + Arrays.toString(r.command));
			//}
			CommandResult res = execCommand(r);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(res);
			oos.flush();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		sock.close();
	}
	
	public void waitConnectionLoop() throws IOException {
		ServerSocket ss = D2ShellSocketFactory.getServerSocketFactory().createServerSocket(this.port);
		while(true) {
			Socket s = ss.accept();
			this.accept(s);
		}	
	}

	public static void main(String[] args) throws Exception {
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

