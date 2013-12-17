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
	static final int HEAD_EXCEPTION = 3;
	
	public static class Client {
		Socket socket;
		Request req;
		InputStream stdin;
		
		//-----
		DShellException exception;
		
		public Client(Socket socket, Request req, InputStream stdin) {
			this.socket = socket;
			this.req = req;
			this.stdin = stdin;
		}
		
		public void run() {
			try {
				// send request
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(req);
				oos.flush();
				
				// start sender thread
				PipeInputStream p = new PipeInputStream(D2ShellClient.getStreamSet().in, oos);
				p.start();
				
				// receive
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				OutputStream stdout = D2ShellClient.getStreamSet().out;
				OutputStream stderr = D2ShellClient.getStreamSet().err;
				while(true) {
					int header = in.read();
					if(header == HEAD_STDOUT) {
						stdout.write(in.read());
					} else if(header == HEAD_STDERR) {
						stderr.write(in.read());
					} else if(header == HEAD_EXCEPTION) {
						this.exception = (DShellException) in.readObject();
					} else {
						break;
					}
				}
				try{
					p.join();
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
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
			this.stdin = is;
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
			}
		}
		
		public void sendErr(int b) throws IOException {
			synchronized(os) {
				os.write(HEAD_STDERR);
				os.write(b);
			}
		}
		
		public void sendException(DShellException e) throws IOException {
			synchronized(os) {
				os.write(HEAD_EXCEPTION);
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
	public PipeInputStream(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}
	@Override public void run() {
		byte[] buf = new byte[256];
		try {
			while(true) {
				int len = this.is.read(buf);
				if(len != -1) {
					this.os.write(buf, 0, len);
				} else {
					break;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
//			try{this.os.close();} catch(IOException e) {}
		}
	}
}