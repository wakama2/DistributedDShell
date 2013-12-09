package org.GreenTeaScript.D2Shell;

import java.io.*;
import java.util.*;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.D2Shell.Task;

public class D2ShellClient {

	static LinkedList<CommandRequest> reqs = new LinkedList<CommandRequest>();

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
		CommandRequest req = new CommandRequest(host, new String[]{ D2ShellDaemon.KILL_CMD }, "");
		Host.create(host).exec(req);
	}

	private static CommandResult Exec(CommandRequest req) {
		return HostManager.getAddrs(req.host).exec(req);
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

	public static void startup() {
	}

	public static void shutdown() {
	}

}
