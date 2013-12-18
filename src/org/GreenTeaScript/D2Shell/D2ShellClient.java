package org.GreenTeaScript.D2Shell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.*;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.Task;

public class D2ShellClient {

	public static HashMap<String, Method> methods = new HashMap<String, Method>();//FIXME
	public static HashMap<String, byte[]> byteCodeMap = new HashMap<String, byte[]>(); //FIXME

	public static void ExecCommandVoid(String[]... cmds) throws DShellException {
		ExecCommand(defaultCtx.stdout, defaultCtx.stderr, cmds);
	}
	
	public static Result ExecCommand(String[]... cmds) throws DShellException {
		return ExecCommand(defaultCtx.stdout, defaultCtx.stderr, cmds);
	}
	
	public static Result ExecCommand(OutputStream defStdout, OutputStream defErr, String[]... cmds) throws DShellException {
		// FIXME: remote method invocation
		if(cmds.length == 1 && cmds[0].length >= 2) {
			Method m = methods.get(cmds[0][1]);
			if(m != null) {
				Host host = HostManager.getAddrs(cmds[0][0]);
				String cname = m.getDeclaringClass().getName();
				return host.exec(new ScriptRequest(byteCodeMap, cname, m.getName(), new Object[0]),
						defaultCtx.stdin, defStdout, defaultCtx.stderr);
			}
		}
		Result res = null;
		InputStream stdin = defaultCtx.stdin;
		for(int i=0; i<cmds.length; i++) {
			boolean isLast = i == cmds.length - 1;
			OutputStream stdout;
			OutputStream stderr;
			if(isLast) {
				stdout = defStdout;
				stderr = defaultCtx.stderr;
			} else {
				stdout = new ByteArrayOutputStream();
				stderr = defaultCtx.stderr;
			}
			String[] host_and_command = cmds[i];
			Host host = HostManager.getAddrs(host_and_command[0]);
			String[] cmd = Arrays.copyOfRange(host_and_command, 1, host_and_command.length);
			Request req = new CommandRequest(cmd);
			res = host.exec(req, stdin, stdout, stderr);
			if(res.exception != null) {
				throw res.exception;
			}
			if(!isLast) {
				ByteArrayOutputStream bo = ((ByteArrayOutputStream)stdout);
				stdin = new ByteArrayInputStream(bo.toByteArray());
			}
		}
		return res;
	}

	public static String ExecCommandString(String[]... cmds) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ExecCommand(os, defaultCtx.stderr, cmds);
		return os.toString();
	}

	public static boolean ExecCommandBool(String[]... cmds) {
		//TODO
		return false;
	}
	
	static class D2Task extends Task {
		boolean finish = false;
		Result result;
		String stdout;
		String stderr;
		
		public Object getResult() {
			return this.result.out;
		}
		
		public void join() throws DShellException {
			synchronized(this) {
				try {
					while(!this.finish) {
						this.wait();
					}
				} catch(InterruptedException e) {}
			}
			if(this.result.exception != null) {
				throw this.result.exception;
			}
		}

		@Override
		public void join(long timeout) {
			// TODO Auto-generated method stub
		}

		@Override
		public String getOutMessage() {
			return this.stdout;
		}

		@Override
		public String getErrorMessage() {
			return this.stderr;
		}

		@Override
		public int getExitStatus() {
			//TODO
			return 0;
		}
	}

	public static Task ExecCommandTask(String[]... cmds) {
		final D2Task task = new D2Task();
		// FIXME: remote method invocation
		if(cmds.length == 1 && cmds[0].length >= 2) {
			final Method m = methods.get(cmds[0][1]);
			if(m != null) {
				final Host host = HostManager.getAddrs(cmds[0][0]);
				final String cname = m.getDeclaringClass().getName();
				Thread th = new Thread() {
					public void run() {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						ByteArrayOutputStream err = new ByteArrayOutputStream();
						Result res = host.exec(new ScriptRequest(byteCodeMap, cname, m.getName(), new Object[0]),
								defaultCtx.stdin, out, err);
						task.result = res;
						task.stdout = out.toString();
						task.stderr = err.toString();
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
		if(cmds[cmds.length-1][0].equals("&")) {
			cmds = Arrays.copyOfRange(cmds, 0, cmds.length-1);
		}
		final String[][] cmds1 = cmds;
		final Thread th = new Thread(new Runnable() {
			public void run() {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ByteArrayOutputStream err = new ByteArrayOutputStream();
				task.result = ExecCommand(out, err, cmds1);
				task.stdout = out.toString();
				task.stderr = err.toString();
				synchronized(task) {
					task.finish = true;
					task.notifyAll();
				}
			}
		});
		th.start();
		return task;
	}
	
	public static D2ShellContext defaultCtx = new D2ShellContext() {{
		this.stdin = new InputStream() {
			@Override public int read() throws IOException {
				return -1;
			}
		};
		this.stdout = System.out;
		this.stderr = System.err;
	}};
	
//	private static ThreadLocal<LocalInfo> streamInfo = new ThreadLocal<LocalInfo>() {
//		@Override protected LocalInfo initialValue() {
//			return new LocalInfo();
//		}
//	};
	
//	public static LocalInfo getStreamSet() { return streamInfo.get(); }
	
	public static boolean isDaemonMode() { return true; }//streamInfo.get().daemon_mode; }
	
	public static void startup() {
//		streamInfo.get().daemon_mode = false;
	}

	public static void shutdown() {
	}

}
