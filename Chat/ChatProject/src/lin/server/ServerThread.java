package lin.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{

	private ServerSocket serverSocket;
	private boolean status = false;
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public ServerThread(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	public void run() {
		while(!status) {
			try {
				Socket socket = serverSocket.accept();
				//不做人数限制  这样方便一点
				//ClientThread client = new ClientThread(socket);
				new ClientThread(socket);
				//client.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
