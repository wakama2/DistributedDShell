package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.Task;

public class D2ShellClient {

	public static HashMap<String, Method> methods = new HashMap<String, Method>();//FIXME
	public static HashMap<String, byte[]> byteCodeMap = new HashMap<String, byte[]>(); //FIXME

	public static void sendKill(String host) {
		CommandRequest req = new CommandRequest(new String[]{ D2ShellDaemon.KILL_CMD }, "");
		Host.create(host).exec(req);
	}

	private static Result Exec(String host, Request req) {
		return HostManager.getAddrs(host).exec(req);
	}

	public static void ExecCommandVoid(String[]... cmds) throws DShellException {
		// FIXME: remote method invocation
		if(cmds.length == 1 && cmds[0].length >= 2) {
			Method m = methods.get(cmds[0][1]);
			if(m != null) {
				String host = cmds[0][0];
				String cname = m.getDeclaringClass().getName();
				Exec(host, new ScriptRequest(byteCodeMap, cname, m.getName(), new Object[0]));
				return;
			}
		}
		
		String in = "";
		Result res = null;
		for(String[] cmd : cmds) {
			String host = cmd[0];
			if(host.equals("&")) break;//FIXME
			String[] cmd2 = Arrays.copyOfRange(cmd, 1, cmd.length);
			res = Exec(host, new CommandRequest(cmd2, in));
			if(res.exception != null) {
				throw res.exception;
			}
			in = res.out.toString();
		}
		D2ShellClient.getStreamSet().out.print(res.out);
	}

	public static String ExecCommandString(String[]... cmds) {
		String in = "";
		Result res = null;
		for(String[] cmd : cmds) {
			String host = cmd[0];
			if(host.equals("&")) break;//FIXME
			String[] cmd2 = Arrays.copyOfRange(cmd, 1, cmd.length);
			res = Exec(host, new CommandRequest(cmd2, in));
			if(res.exception != null) {
				throw res.exception;
			}
			in = res.out.toString();
		}
		return res.out.toString();
	}

	public static boolean ExecCommandBool(String[]... cmds) {
		return false;
	}
	
	static class D2Task extends Task {
		boolean finish = false;
		Object result = "";
		DShellException exception;
		
		public Object getResult() {
			return this.result;
		}
		
		public void join() throws DShellException {
			synchronized(this) {
				try {
					while(!this.finish) {
						this.wait();
					}
				} catch(InterruptedException e) {}
			}
			if(this.exception != null) {
				throw this.exception;
			}
		}

		@Override
		public void join(long timeout) {
			// TODO Auto-generated method stub
		}

		@Override
		public String getOutMessage() {
			return this.result.toString();
		}

		@Override
		public String getErrorMessage() {
			// TODO Auto-generated method stub
			return "";
		}

		@Override
		public int getExitStatus() {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	public static Task ExecCommandTask(final String[]... cmds) {
		final D2Task task = new D2Task();
		// FIXME: remote method invocation
		if(cmds.length == 1 && cmds[0].length >= 2) {
			final Method m = methods.get(cmds[0][1]);
			if(m != null) {
				final String host = cmds[0][0];
				final String cname = m.getDeclaringClass().getName();
				Thread th = new Thread() {
					public void run() {
						Result res = Exec(host, new ScriptRequest(byteCodeMap,
								cname, m.getName(), new Object[0]));
						task.result = res.out;
						synchronized(task) {
							task.finish = true;
							task.notifyAll();
						}
					}
				};
				th.start();
				if(!cmds[0][cmds[0].length-1].equals("&")) {
					task.join();
				}
				return task;
			}
		}
		final Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					task.result = ExecCommandString(cmds);
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
	
	public static class LocalInfo {
//		public InputStream in = System.in;
		public InputStream in = new InputStream() {
			@Override public int read() throws IOException {
				return -1;
			}
		};
		public PrintStream out = System.out;
		public PrintStream err = System.err;
		public boolean daemon_mode = true;
	}
	
	private static ThreadLocal<LocalInfo> streamInfo = new ThreadLocal<LocalInfo>() {
		@Override protected LocalInfo initialValue() {
			return new LocalInfo();
		}
	};
	
	public static LocalInfo getStreamSet() { return streamInfo.get(); }
	
	public static boolean isDaemonMode() { return streamInfo.get().daemon_mode; }
	
	public static void startup() {
		streamInfo.get().daemon_mode = false;
	}

	public static void shutdown() {
	}

}
