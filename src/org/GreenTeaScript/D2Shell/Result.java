package org.GreenTeaScript.D2Shell;

import java.io.Serializable;

import org.GreenTeaScript.DShell.DShellException;

public class Result implements Serializable {
	private static final long serialVersionUID = 200L;

	public Object out; // exit status or return value
	public DShellException exception;
	
	public Result(Object out, DShellException exception) {
		this.out = out;
		this.exception = exception;
	}
}
