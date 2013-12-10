package org.GreenTeaScript.D2Shell;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.DShellProcess;

public interface Request {
	public CommandResult exec();
}

class CommandRequest implements Request, Serializable {
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

class ScriptRequest implements Request, Serializable {
	private static final long serialVersionUID = 4280102515448720707L;
	
	public byte[] bytecode;
	public String cname;
	public String fname;
	public Object[] args = new Object[0];
	
	public ScriptRequest(byte[] bytecode, String cname, String funcname, Object[] args) {
		this.bytecode = bytecode;
		this.cname = cname;
		this.fname = funcname;
		this.args = args;
	}
	
	@Override
	public CommandResult exec() {
		ClassLoader cl = new ClassLoader() {
			@Override protected Class<?> findClass(String name) {
				if(cname.equals(name)) return this.defineClass(name, bytecode, 0, bytecode.length);
				return null;
			}
		};
		Class<?>[] argTypes = new Class<?>[this.args.length];
		for(int i=0; i<argTypes.length; i++) {
			argTypes[i] = this.args.getClass();
		}
		Object res = null;
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
		String out = res != null ? res.toString() : "";
		return new CommandResult(out, "", ex);
	}
}
