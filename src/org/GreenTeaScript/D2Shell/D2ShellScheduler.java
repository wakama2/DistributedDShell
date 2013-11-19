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
			} catch(Exception e) {
				
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

	public static Task ExecCommandTask(final String[]... cmds) {
		final boolean[] finish = new boolean[1];
		final String[] output = new String[1];
		final Thread th = new Thread(new Runnable() {
			public void run() {
				output[0] = ExecCommandString(cmds);
				synchronized(finish) {
					finish[0] = true;
					finish.notifyAll();
				}
			}
		});
		th.start();
		return new Task() {
			public String getOutput() {
				return output[0];
			}
			public void join() {
				synchronized(finish) {
					if(!finish[0]) {
						try {finish.wait();} catch(Exception e) {}
					}
				}
			}
		};
	}
	
	private static Thread localDaemon = new Thread(new Runnable() {
		public void run() {
			try {
				D2ShellDaemon.main(new String[0]);
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
