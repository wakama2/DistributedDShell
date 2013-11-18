package org.GreenTeaScript.D2Shell;

import java.util.HashMap;
import java.util.List;

public class HostManager {
	public static HashMap<String, List<String>> addrmap = new HashMap<String, List<String>>();
	
	public static void addHost(String name, List<String> addr) {
		addrmap.put(name, addr);
	}
	
	public static List<String> getAddrs(String name) {
		return addrmap.get(name);
	}
}
