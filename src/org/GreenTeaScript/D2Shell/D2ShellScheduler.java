package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.D2Shell.Task;

public class D2ShellScheduler {
	
	private static Socket connect(String addr) throws IOException {
		int port = D2ShellDaemon.DEFAULT_PORT;
		int p = addr.indexOf(":");
		if(p != -1) {
			port = Integer.parseInt(addr.substring(p+1));
			addr = addr.substring(0, p);
		}
		return new Socket(addr, port);
	}

//	public CommandResult run(String[] cmd, boolean bg) {
//		if(bg) {
//			return runAsync(cmd);
//		} else {
//			return runSync(cmd);
//		}
//	}
	
//	public CommandResult runAsync(final String[] cmd) {
//		return new CommandResult() {
//			Object result;
//			boolean finish = false;
//			Object self = this;
//			{
//				Thread th = new Thread(new Runnable() {
//					public void run() {
//						result = runSync(cmd);
//						synchronized(self) {
//							finish = true;
//							self.notifyAll();
//						}
//					}
//				});
//				th.start();
//			}
//			@Override public String getResult() {
//				join();
//				return result.toString();
//			}
//			@Override public void join() {
//				synchronized(self) {
//					while(!finish) {
//						try { self.wait(); } catch(Exception e) { e.printStackTrace(); }
//					}
//				}
//			}
//			@Override public boolean isFinished() {
//				return finish;
//			}
//		};
//	}
	
	public static CommandResult runSync(Socket sock, CommandRequest r) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(r);
			oos.flush();

			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			return (CommandResult) ois.readObject();
		} catch(IOException e) {
			throw new ConnectionException();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static CommandResult Exec(CommandRequest req) {
		List<String> addrs = HostManager.getAddrs(req.host);
		CommandResult res = null;
		String out = "";
		for(String addr : addrs) {
			Socket sock = null;
			try {
				sock = connect(addr);
				res = runSync(sock, req);
				out += res.out;
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				try { if(sock!=null) sock.close(); } catch(Exception e) {}
			}
		}
		res.out = out;
		return res;
	}

	public static void ExecCommandVoid(String[]... cmds) throws DShellException {
		String in = "";
		CommandResult res;
		for(String[] cmd : cmds) {
			String host = cmd[0];
			if(host.equals("&")) break;//FIXME
			String[] cmd2 = Arrays.copyOfRange(cmd, 1, cmd.length);
			res = Exec(new CommandRequest(host, cmd2, in));
			if(res.exception != null) {
				throw res.exception;
			}
			in = res.out;
		}
	}

	public static String ExecCommandString(String[]... cmds) {
		String in = "";
		CommandResult res = null;
		for(String[] cmd : cmds) {
			String host = cmd[0];
			if(host.equals("&")) break;//FIXME
			String[] cmd2 = Arrays.copyOfRange(cmd, 1, cmd.length);
			res = Exec(new CommandRequest(host, cmd2, in));
			if(res.exception != null) {
				throw res.exception;
			}
			in = res.out;
		}
		return res.out;
	}

	public static boolean ExecCommandBool(String[]... cmds) {
		return false;
	}
	
	static class D2Task extends Task {
		boolean finish = false;
		String output = "";
		DShellException exception;
		
		public String getOutput() {
			return this.output;
		}
		
		public void join() {
			synchronized(this) {
				while(!this.finish) {
					try {this.wait();} catch(Exception e) {}
				}
			}
			if(this.exception != null) {
				throw this.exception;
			}
		}
	}

	public static Task ExecCommandTask(final String[]... cmds) {
		final D2Task task = new D2Task();
		final Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					task.output = ExecCommandString(cmds);
				} catch(DShellException e) {
					task.exception = e;
				}
				synchronized(task) {
					task.finish = true;
					task.notifyAll();
				}
			}
		});
		th.start();
		return task;
	}
	
	private static Thread localDaemon = new Thread(new Runnable() {
		public void run() {
			try {
				D2ShellDaemon.main_self();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	});
	
	public static void startup() {
		localDaemon.start();
		while(!D2ShellDaemon.ready) try { Thread.sleep(1); } catch(Exception e) {}
	}

	public static void shutdown() {
		D2ShellDaemon.close();
//		while(localDaemon.isAlive()) try { Thread.sleep(1); } catch(Exception e) {}
	}

}
