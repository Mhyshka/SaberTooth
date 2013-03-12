package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import run.Controller;

public class ConnectionManager {
	private class Connecter extends Thread{
		String address;
		int port;
		public Connecter(String address, int port){
			this.address = address;
			this.port = port;
		}
		
		@Override
		public void run(){
			try {
				socket = new Socket();
				try {
					socket.setReuseAddress(true);
				} catch (SocketException e) {
					// TODO
					// Apparait lorsque le port est déjà utilisé par 
					// exemple : le server et le client lancé sur le même PC essayant de se connecter sur 127.0.0.1.
					e.printStackTrace();
				}
				socket.connect(new InetSocketAddress(address, port), 5000);
				if(isConnected()){
					ctrl.connectionSuccess();
				}
			} catch (UnknownHostException e) {
				ctrl.error("Connexion Error", "UnknowHost Exception while connecting to : " + address + ":" + port, 1);
				ctrl.connectionFailed();
				e.printStackTrace();
			} catch(SocketTimeoutException e){
				ctrl.error("Connexion Error", "Connection Timed-out.", 1);
				ctrl.connectionFailed();
				e.printStackTrace();
			} catch (IOException e) {
				ctrl.error("Connexion Error", "I/O Exception while connecting to : " + address + ":" + port, 1);
				ctrl.connectionFailed();
				e.printStackTrace();
			} 
		}
	}
	
	private Controller ctrl;
	private static ConnectionManager manager;
	private Socket socket;
	
	public static ConnectionManager getInstance(Controller newCtrl){
		if(manager !=null){
			return manager;
		}
		else{
			manager = new ConnectionManager(newCtrl);
			return manager;
		}
	}
	
	private ConnectionManager(Controller newCtrl){
		ctrl = newCtrl;
	}
	
	public void openSocket(String address, int port){
		new Connecter(address, port).start();
	}
	
	public void closeSocket(){
		try {
			socket.close();
		} catch (IOException e) {
			ctrl.error("Connexion Error", "I/O Exception while closing the socket", 1);
			e.printStackTrace();
		}
	}
	
	public Socket getSocket(){
		return socket;
	}
	
	public boolean isConnected(){
		return socket.isConnected();
	}
	
	public boolean isClosed(){
		if(socket == null)
			return true;
		else
			return socket.isClosed();
	}
}
