package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import javax.net.SocketFactory;


public abstract class Host {
	
//	public Result exec(Request req) {
//		return exec(req, null, null, null);//FIXME
//	}
	
	public abstract Result exec(Request req, InputStream stdin, OutputStream stdout, OutputStream stderr);
	
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
	public Result exec(Request req, InputStream stdin, OutputStream stdout, OutputStream stderr) {
		try(Socket sock = this.sf.createSocket(addr, port)) {
			D2ShellProtocol.Client p = new D2ShellProtocol.Client(sock, req, stdin, stdout, stderr);
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
	public Result exec(Request req, InputStream stdin, OutputStream stdout, OutputStream stderr) {
		Result res = null;
		String out = "";
		for(Host host : hosts) {
			res = host.exec(req, stdin, stdout, stderr);
			out += res.out;
		}
		res.out = out;
		return res;
	}
}

class LocalHost extends Host {
	@Override
	public Result exec(Request req, InputStream stdin, OutputStream stdout, OutputStream stderr) {
		D2ShellContext ctx = new D2ShellContext();
		ctx.stdin = stdin;
		ctx.stdout = new PrintStream(stdout);
		ctx.stderr = new PrintStream(stderr);
		return req.exec(ctx);
	}
}
