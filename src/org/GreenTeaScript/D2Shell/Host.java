package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class Host {
	
	public abstract CommandResult exec(Request req) ;
	
	public static Host create(String addr, int port) {
		return new RemoteHost(addr, port);
	}
	
	public static Host create(String addr) {
		int port = D2ShellDaemon.DEFAULT_PORT;
		int p = addr.indexOf(":");
		if(p != -1) {
			port = Integer.parseInt(addr.substring(p+1));
			addr = addr.substring(0, p);
		}
		return create(addr, port);
	}
	
	public static Host create(String...addrs) {
		if(addrs.length == 1) {
			return create(addrs[0]);
		} else {
			Host[] hosts = new Host[addrs.length];
			for(int i=0; i<addrs.length; i++) {
				hosts[i] = create(addrs[i]);
			}
			return new HostGroup(hosts);
		}
	}

}

class RemoteHost extends Host {
	private final String addr;
	private final int port;
	
	public RemoteHost(String addr, int port) {
		this.addr = addr;
		this.port = port;
	}

	@Override
	public CommandResult exec(Request req) {
		Socket sock = null;
		try {
			sock = D2ShellSocketFactory.getDefaultSocketFactory().createSocket(addr, port);
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(req);
			oos.flush();
			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			return (CommandResult) ois.readObject();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try { if(sock!=null) sock.close(); } catch(IOException e) {}
		}
		return null;
	}
}

class HostGroup extends Host {
	private final Host[] hosts;
	
	public HostGroup(Host[] hosts) {
		this.hosts = hosts;
	}
	
	@Override
	public CommandResult exec(Request req) {
		CommandResult res = null;
		String out = "";
		for(Host host : hosts) {
			res = host.exec(req);
			out += res.out;
		}
		res.out = out;
		return res;
	}
}

class LocalHost extends Host {
	@Override
	public CommandResult exec(Request r) {
		return r.exec();
	}
}
