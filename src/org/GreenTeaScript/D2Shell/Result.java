package org.GreenTeaScript.D2Shell;

import java.io.Serializable;
import java.util.ArrayList;

import org.GreenTeaScript.DShell.DShellException;
import org.GreenTeaScript.DShell.MultipleException;

public class Result implements Serializable {
	private static final long serialVersionUID = 200L;

	public Object out; // exit status or return value
	public DShellException exception;
	
	public Result(Object out, DShellException exception) {
		this.out = out;
		this.exception = exception;
	}
	
	public static Result merge(Result[] res) {
		Object[] outs = new Object[res.length];
		ArrayList<DShellException> elist = new ArrayList<>();
		for(int i=0; i<res.length; i++) {
			outs[i] = res[i].out;
			if(res[i].exception != null) {
				elist.add(res[i].exception);
			}
		}
		DShellException e;
		if(elist.size() == 0) {
			e = null;
		} else if(elist.size() == 1) {
			e = elist.get(0);
		} else {
			e = new MultipleException("", elist.toArray(new DShellException[0]));
		}
		return new Result(outs, e);
	}
}
