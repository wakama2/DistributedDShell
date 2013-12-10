package org.GreenTeaScript.D2Shell;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.DShellProcess;

public abstract class Request {
	public abstract CommandResult exec();
}

class CommandRequest extends Request implements Serializable {
	private static final long serialVersionUID = -3762794483693239635L;
	
	public String[] command;
	public String input;
	
	public CommandRequest(String[] cmd, String in) {
		this.command = cmd;
		this.input = in;
	}
	
	public CommandResult exec() {
		//if(DEBUG_MODE) {
			System.out.println("[debug] " + Arrays.toString(this.command));
		//}
		String out = "";
		DShellException ex = null;
		try {
			if(this.input.length() == 0) {
				out = DShellProcess.ExecCommandString(this.command);
			} else {
				String[] c = { "echo", this.input };//FIXME use InputStream
				out = DShellProcess.ExecCommandString(c, this.command);
			}
		} catch(DShellException e) {
			ex = e;
		}
		return new CommandResult(out, "", ex);
	}
}

class ScriptRequest extends Request implements Serializable {
	private static final long serialVersionUID = 4280102515448720707L;
	
	public Map<String, byte[]> bcmap;
	public String cname;
	public String fname;
	public Object[] args = new Object[0];
	
	public ScriptRequest(Map<String, byte[]> bcmap, String cname, String funcname, Object[] args) {
		this.bcmap = bcmap;
		this.cname = cname;
		this.fname = funcname;
		this.args = args;
	}
	
	class RemoteClassLoader extends ClassLoader {
		@Override protected Class<?> findClass(String name) {
			byte[] bytecode = bcmap.get(name);
			if(bytecode != null) {
				return this.defineClass(name, bytecode, 0, bytecode.length);
			}
			return null;
		}
	}
	
	@Override
	public CommandResult exec() {
		ClassLoader cl = new RemoteClassLoader();
		Class<?>[] argTypes = new Class<?>[this.args.length];
		for(int i=0; i<argTypes.length; i++) {
			argTypes[i] = this.args.getClass();
		}
		Object res = "";
		DShellException ex = null;
		try {
			Class<?> klass = cl.loadClass(cname);
			Method m = klass.getMethod(fname, argTypes);
			res = m.invoke(null, args);
		} catch(DShellException e) {
			ex = e;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new CommandResult(res, "", ex);
	}
}
