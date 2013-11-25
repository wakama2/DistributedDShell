package org.GreenTeaScript.D2Shell;

public abstract class Host {
	
	public abstract CommandResult exec(CommandRequest req);
	
}

class HostGroup extends Host {
	
	private final Host[] hosts;
	
	public HostGroup(Host[] hosts) {
		this.hosts = hosts;
	}
	
	public CommandResult exec(CommandRequest req) {
		return null;
	}
	
}

class LocalHost extends Host {
	
	public CommandResult exec(CommandRequest req) {
		return null;
	}
	
}
