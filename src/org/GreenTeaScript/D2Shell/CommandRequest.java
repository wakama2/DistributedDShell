package org.GreenTeaScript.D2Shell;

import java.io.Serializable;

public class CommandRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String host;
	public String[] command;
	public String input;
	
	public CommandRequest(String host, String[] cmd, String in) {
		this.host = host;
		this.command = cmd;
		this.input = in;
	}

}
