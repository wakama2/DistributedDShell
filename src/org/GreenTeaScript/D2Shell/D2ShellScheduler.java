package org.GreenTeaScript.D2Shell;

import org.GreenTeaScript.DShell.Task;

public class D2ShellScheduler {

	public static void ExecCommandVoid(String[]... cmds) {
		System.out.println(cmds);
	}

	public static String ExecCommandString(String[]... cmds) {
		return null;
	}

	public static boolean ExecCommandBool(String[]... cmds) {
		return false;
	}

	public static Task ExecCommandTask(String[]... cmds) {
		return null;
	}

}
