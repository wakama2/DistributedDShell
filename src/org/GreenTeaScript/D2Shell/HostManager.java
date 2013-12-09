package org.GreenTeaScript.D2Shell;

import java.util.*;

public class HostManager {
	public static HashMap<String, Host> addrmap = new HashMap<String, Host>();
	
	static {
		addrmap.put("localhost", new LocalHost());
	}
	
	public static void addHost(String name, List<String> addr) {
		addrmap.put(name, Host.create(addr.toArray(new String[0])));
	}
	
	public static Host getAddrs(String name) {
		return addrmap.get(name);
	}
}
