package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;


public abstract class Host {
	
	public abstract Result exec(Request req) ;
	
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
	private final SocketFactory sf;
	
	public RemoteHost(String addr, int port) {
		this(addr, port, D2ShellSocketFactory.getDefaultSocketFactory());
	}
	
	public RemoteHost(String addr, int port, SocketFactory sf) {
		this.addr = addr;
		this.port = port;
		this.sf = sf;
	}

	@Override
	public Result exec(Request req) {
		try(Socket sock = this.sf.createSocket(addr, port)) {
			D2ShellProtocol.Client p = new D2ShellProtocol.Client(sock, req, D2ShellClient.getStreamSet().in);
			return p.run();
		} catch(IOException e) {
			e.printStackTrace();
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
	public Result exec(Request req) {
		Result res = null;
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
	public Result exec(Request r) {
		return r.exec();
	}
}
