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
	
	public static boolean DEAMON_MODE = false;
	
	public LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue<Task>();
	public Worker[] workers = new Worker[WORKERS];
	
	public static boolean ready = false;
	
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

	public static String runCommand(CommandRequest r) throws Exception {
		if(r.input.length() == 0) {
			return DShellProcess.ExecCommandString(r.command);
		} else {
			String[] c = { "echo", r.input };
			return DShellProcess.ExecCommandString(c, r.command);
		}
	}
	
	public void init() {
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
	
	public static void accept(Socket sock, InputStream is, OutputStream os) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(is);
		try {
			CommandRequest r = (CommandRequest) ois.readObject();
			String out = "";
			DShellException ex = null;
			try {
				if(DEAMON_MODE) {
					System.out.println("[debug] " + Arrays.toString(r.command));
				}
				out = runCommand(r);
			} catch(DShellException e) {
				ex = e;
			} catch(Exception e) {
				e.printStackTrace();
			}
			CommandResult res = new CommandResult(out, ex);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(res);
			oos.flush();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		sock.close();
	}
	
	private static ServerSocket ss;
	private static D2ShellDaemon dm;

	public static void close() {
		System.exit(0);
	}
	
	public static void main_self() throws Exception {
		dm = new D2ShellDaemon();
		dm.init();
		ss = new ServerSocket(DEFAULT_PORT);
		ready = true;
		while(true) {
			Socket s = ss.accept();
			dm.accept(s);
		}	
	}

	public static void main(String[] args) throws Exception {
		int port = DEFAULT_PORT;
		if(args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		DEAMON_MODE = true;
		dm = new D2ShellDaemon();
		dm.init();
		ss = new ServerSocket(port);
		ready = true;
		while(true) {
			Socket s = ss.accept();
			dm.accept(s);
		}
	}
}

