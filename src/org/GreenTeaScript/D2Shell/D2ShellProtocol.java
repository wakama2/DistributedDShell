package org.GreenTeaScript.D2Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.GreenTeaScript.DShell.DShellException;

class D2ShellProtocol {
	static final int HEAD_STDOUT = 1;//FIXME
	static final int HEAD_STDERR = 2;
	static final int HEAD_RESULT = 3;
	
	static final int HEAD_STDIN = 1;
	static final int HEAD_CLOSE = 2;
	
	public static class Client {
		Socket socket;
		Request req;
		InputStream stdin;
		OutputStream stdout;
		OutputStream stderr;
		
		//-----
		DShellException exception;
		
		public Client(Socket socket, Request req, InputStream stdin, OutputStream stdout, OutputStream stderr) {
			this.socket = socket;
			this.req = req;
			this.stdin = stdin;
			this.stdout = stdout;
			this.stderr = stderr;
		}
		
		public Result run() {
			Result res = null;
			try {
				// send request
				final
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(req);
				oos.flush();
				
				// start sender thread
				new Thread() {
					public void run() {
						try{
							while(true) {
								int b = stdin.read();
								if(b != -1) {
									oos.write(HEAD_STDIN);
									oos.write(b);
									oos.flush();
								} else break;
							}
							oos.write(HEAD_CLOSE);
							oos.flush();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}.start();
//				PipeInputStream p = new PipeInputStream(stdin, oos);
//				p.start();
				
				// receive
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				while(true) {
					int header = in.read();
					if(header == HEAD_STDOUT) {
						stdout.write(in.read());
					} else if(header == HEAD_STDERR) {
						stderr.write(in.read());
					} else if(header == HEAD_RESULT) {
						res = (Result) in.readObject();
					} else {
						break;
					}
				}
//				try{
//					p.join();
//				}catch(InterruptedException e) {
//					e.printStackTrace();
//				}
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			return res;
		}
	}
	
	public static class Server {
		Socket socket;
		
		Request req;
		InputStream stdin;
		PrintStream stdout;
		PrintStream stderr;
		
		ObjectInputStream is;
		ObjectOutputStream os;
		
		public Server(Socket socket) throws IOException, ClassNotFoundException {
			this.socket = socket;
			this.is = new ObjectInputStream(socket.getInputStream());
			this.os = new ObjectOutputStream(socket.getOutputStream());
			// receive request
			this.req = (Request) this.is.readObject();
			// set streams
//			this.stdin = is;
			this.stdin = new InputStream() {
				boolean closed = false;
				@Override
				public int read() throws IOException {
					if(closed) return -1;
					int h = is.read();
					if(h == HEAD_STDIN) {
						int b = is.read();
						return b;
					}
					closed = true;
					return -1;
				}
			};
			this.stdout = new PrintStream(new OutputStream() {
				@Override public void write(int b) throws IOException {
					sendOut(b);
				}
				@Override public void close() throws IOException {}
			});
			this.stderr = new PrintStream(new OutputStream() {
				@Override public void write(int b) throws IOException {
					sendErr(b);
				}
				@Override public void close() throws IOException {}
			});
		}
		
		public void sendOut(int b) throws IOException {
			synchronized(os) {
				os.write(HEAD_STDOUT);
				os.write(b);
				os.flush();
			}
		}
		
		public void sendErr(int b) throws IOException {
			synchronized(os) {
				os.write(HEAD_STDERR);
				os.write(b);
				os.flush();
			}
		}
		
		public void sendResult(Result e) throws IOException {
			synchronized(os) {
				os.write(HEAD_RESULT);
				os.writeObject(e);
				os.flush();
			}
		}
		
		public void flush() throws IOException {
			os.flush();
		}
	}
}

class PipeInputStream extends Thread {
	InputStream is;
	OutputStream os;
	boolean closeOs;
	public PipeInputStream(InputStream is, OutputStream os) {
		this(is, os, false);
	}
	public PipeInputStream(InputStream is, OutputStream os, boolean closeOs) {
		this.is = is;
		this.os = os;
		this.closeOs = closeOs;
	}
	@Override public void run() {
		byte[] buf = new byte[256];
		try {
			while(true) {
				int len = this.is.read(buf);
				if(len != -1) {
					this.os.write(buf, 0, len);
					this.os.flush();
				} else {
					break;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(closeOs) {
				try{this.os.close();} catch(IOException e) {}
			}
		}
	}
}