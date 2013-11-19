package org.GreenTeaScript.D2Shell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HostManager {
	public static HashMap<String, List<String>> addrmap = new HashMap<String, List<String>>();
	
	static {
		addrmap.put("localhost", Arrays.asList("127.0.0.1"));
	}
	
	public static void addHost(String name, List<String> addr) {
		addrmap.put(name, addr);
	}
	
	public static List<String> getAddrs(String name) {
		return addrmap.get(name);
	}
}
