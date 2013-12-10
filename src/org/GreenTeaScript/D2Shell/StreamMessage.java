package org.GreenTeaScript.D2Shell;

import java.io.Serializable;

import org.GreenTeaScript.DShell.DShellException;

public class StreamMessage {
	
	public class Stdin extends StreamMessage implements Serializable {
		private static final long serialVersionUID = 5213530169721455100L;
		public byte[] data;
		public Stdin(byte[] data) {
			this.data = data;
		}
	}

	public class Stderr extends StreamMessage implements Serializable {
		private static final long serialVersionUID = -2999128667588301706L;
		public byte[] data;
		public Stderr(byte[] data) {
			this.data = data;
		}
	}
		
	public class Stdout extends StreamMessage implements Serializable {
		private static final long serialVersionUID = -7380307024992918256L;
		public byte[] data;
		public Stdout(byte[] data) {
			this.data = data;
		}
	}
	
	public class ExceptionMsg extends StreamMessage implements Serializable {
		private static final long serialVersionUID = 5439736855801035636L;
		public DShellException e;
		public ExceptionMsg(DShellException e) {
			this.e = e;
		}
	}

}
