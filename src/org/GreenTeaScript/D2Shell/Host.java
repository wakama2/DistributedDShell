package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import javax.net.SocketFactory;


public abstract class Host {
	
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

class MultiInputStream {
	private final InputStream in;
	private final InputStream[] in2;
	private final DataBlock[] ident;
	private final int[] idseek;
	private DataBlock last;

	static class DataBlock {
		public final byte[] buf = new byte[256];
		public int size;
		public DataBlock next;
	}

	class SubInputStream extends InputStream {
		private final int id;
		public SubInputStream(int id) {
			this.id = id;
		}
		public int read() throws IOException {
			MultiInputStream self = MultiInputStream.this;
			return self.read(id);
		}
	}

	public MultiInputStream(InputStream in, int n) throws IOException {
		this.in = in;
		this.in2 = new InputStream[n];
		this.ident = new DataBlock[n];
		this.idseek = new int[n];
		this.last = new DataBlock();
		for(int i=0; i<n; i++) {
			this.ident[i] = this.last;
			this.idseek[i] = 0;
			this.in2[i] = new SubInputStream(i);
		}
		this.last.size = in.read(this.last.buf);
	}

	public InputStream get(int id) {
		return this.in2[id];
	}

	private void readNextBlock() throws IOException {
		synchronized(this) {
			if(this.last.next == null) {
				DataBlock e = new DataBlock();
				e.size = this.in.read(e.buf);
				if(e.size != -1) {
					this.last.next = e;
					this.last = e;
				}
			}
		}
	}

	private int read(int id) throws IOException {
		DataBlock e = ident[id];
		int seek = idseek[id];
		if(seek < e.size) {
			idseek[id] += 1;
			return e.buf[seek];
		} else {
			if(e.next != null) {
				ident[id] = e.next;
				idseek[id] = 0;
				return read(id);
			} else {
				readNextBlock();
				if(e.next == null) {
					return -1;
				}
				return read(id);
			}
		}
	}
}

class HostGroup extends Host {
	private final Host[] hosts;
	
	public HostGroup(Host[] hosts) {
		this.hosts = hosts;
	}
	
	@Override
	public Result exec(final Request req, InputStream stdin, final OutputStream stdout, final OutputStream stderr) {
		try {
			MultiInputStream in = new MultiInputStream(stdin, hosts.length);
			Thread[] threads = new Thread[hosts.length];
			final Result[] results = new Result[hosts.length];
			for(int i=0; i<hosts.length; i++) {
				final int id = i;
				final Host host = hosts[i];
				final InputStream is = in.get(i);
				threads[i] = new Thread() {
					public void run() {
						results[id] = host.exec(req, is, stdout, stderr);
					}
				};
				threads[i].start();
			}
			
			for(int i=0; i<hosts.length; i++) {
				try {
					threads[i].join();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			return Result.merge(results);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
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
