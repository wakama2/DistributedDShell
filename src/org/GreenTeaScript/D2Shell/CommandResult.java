package org.GreenTeaScript.D2Shell;

import java.io.Serializable;

import org.GreenTeaScript.DShell.DShellException;

public class CommandResult implements Serializable {

	private static final long serialVersionUID = 200L;

	public String out;
	public String err;
	public DShellException exception;
	
	public CommandResult(String out, String err, DShellException exception) {
		this.out = out;
		this.err = err;
		this.exception = exception;
	}

}
