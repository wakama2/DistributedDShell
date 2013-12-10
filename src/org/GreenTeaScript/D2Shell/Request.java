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
	
	public Class<?> klass;
	public String funcname;
	public Object[] args = new Object[0];
	
	public ScriptRequest(Class<?> klass, String funcname, Object[] args) {
		this.klass = klass;
		this.funcname = funcname;
		this.args = args;
	}
	
	@Override
	public CommandResult exec() {
		Class<?>[] argTypes = new Class<?>[this.args.length];
		for(int i=0; i<argTypes.length; i++) {
			argTypes[i] = this.args.getClass();
		}
		Object res = null;
		DShellException ex = null;
		try {
			Method m = this.klass.getMethod(funcname, argTypes);
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
