package org.GreenTeaScript.D2Shell;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.D2Shell.Task;

public class D2ShellClient {

	static LinkedList<CommandRequest> reqs = new LinkedList<CommandRequest>();
	public static HashMap<String, Method> methods = new HashMap<String, Method>();//FIXME
	public static HashMap<String, byte[]> byteCodeMap = new HashMap<String, byte[]>(); //FIXME

//	static {
//		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			public void run() {
//				<String> hosts = new HashSet<String>();
//				for(CommandRequest r : reqs) {
//					for(String host : HostManager.getAddrs(r.host)) {
//						hosts.add(host);
//					}
//				}
//				for(String host : hosts) {
//					sendKill(host);
//				}
//		}}));
//	}

	public static void sendKill(String host) {
		CommandRequest req = new CommandRequest(new String[]{ D2ShellDaemon.KILL_CMD }, "");
		Host.create(host).exec(req);
	}

	private static CommandResult Exec(String host, Request req) {
		return HostManager.getAddrs(host).exec(req);
	}

	public static void ExecCommandVoid(String[]... cmds) throws DShellException {
		// FIXME: remote method invocation
		if(cmds.length == 1 && cmds[0].length >= 2) {
			Method m = methods.get(cmds[0][1]);
			if(m != null) {
				String host = cmds[0][0];
				String cname = m.getDeclaringClass().getName();
				Exec(host, new ScriptRequest(byteCodeMap.get(cname),
						cname, m.getName(), new Object[0]));
				return;
			}
		}
		
		String in = "";
		CommandResult res;
		for(String[] cmd : cmds) {
			String host = cmd[0];
			if(host.equals("&")) break;//FIXME
			String[] cmd2 = Arrays.copyOfRange(cmd, 1, cmd.length);
			res = Exec(host, new CommandRequest(cmd2, in));
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
			res = Exec(host, new CommandRequest(cmd2, in));
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

	public static void startup() {
	}

	public static void shutdown() {
	}

}