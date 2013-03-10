package run;

import java.net.Socket;
import java.util.HashMap;

import javax.swing.JOptionPane;

import network.ConnectionManager;
import network.RequestManager;
import view.MainView;
import data.ChannelTree;
import data.User;

public class Controller {
	
	
	public static void start(){
		new Controller();
	}
	private MainView mainView;
	private ConnectionManager connectionManager;
	private RequestManager requestManager;
	private ChannelManager channelManager;
	private User user;

	public Controller(){
		mainView = new MainView(this);
		mainView.setVisible(true);
		connectionManager = ConnectionManager.getInstance(this);
		requestManager = RequestManager.getInstance(this);
		channelManager = ChannelManager.getInstance(this);
	}
	
	public void connect(String address, int port){
		mainView.connectingState();
		connectionManager.openSocket(address, port);
	}
	
	public void connectionFailed(){
		mainView.initState();
	}
	
	public void disconnect(){
		requestManager.disconnect();
		connectionManager.closeSocket();
		mainView.initState();
	}
	
	public void error(String title, String text, int type){
		switch(type){
			case 0 : JOptionPane.showMessageDialog(mainView,
				    text,title,JOptionPane.INFORMATION_MESSAGE);
			break;
			
			case 1 : JOptionPane.showMessageDialog(mainView,
				    text,title,JOptionPane.ERROR_MESSAGE);
			break;
			
			case 2 : JOptionPane.showMessageDialog(mainView,
				    text,title,JOptionPane.WARNING_MESSAGE);
			break;
			
			default : JOptionPane.showMessageDialog(mainView,
				    text,title,JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	public void exit(){
		if(isLogged()){
			
		}
		if(requestManager.isInit()){
			requestManager.disconnect();
		}
		if(!isClosed()){
			connectionManager.closeSocket();
		}
		System.exit(0);
	}
	
	public Socket getSocket(){
		return connectionManager.getSocket();
	}
	
	public boolean isClosed(){
		return connectionManager.isClosed();
	}
	
	public boolean isConnected(){
		return connectionManager.isConnected();
	}
	
	public boolean isLogged(){
		return requestManager.isLogged();
	}
	
	public void login(String username){
		requestManager.login(username);
		mainView.loggingState();
	}
	
	public void loginFailed(){
		error("Login Failed","Couldn't log you in." , 0);
		mainView.connectedState();
	}
	
	public void loginSuccess(User user){
		setUser(user);
		mainView.loggedState();
	}
	
	public void logout(){
		requestManager.logout();
	}
	
	public void connectionSuccess(){
		requestManager.initConnection();
		mainView.connectedState();
	}
	
	public void welcome(String serverName){
		setServerName(serverName);
		mainView.setServerName(serverName);
		mainView.connectedState();
	}
	
	public void passwordAsked(){
		
	}
	
	public void setServerName(String name){
		mainView.setServerName(name);
	}

	public void serverShutdown(String reason) {
		requestManager.close();
		connectionManager.closeSocket();
		mainView.initState();
		if(reason.isEmpty())
			reason = "No reason was given.";
		error("Server shutdown",reason,0);
	}
	
	public void updateChannels(){
		mainView.updateChannels(channelManager.getChannels());
	}
	
	public void updateUserChannels(){
		//mainView.updateUserChannels();
	}
	
	public ChannelTree getChannel(long id){
		return channelManager.getChannel(id);
	}
	
	public HashMap<Long, ChannelTree> getChannels(){
		return channelManager.getChannels();
	}
	
	public void setChannels(HashMap<Long,ChannelTree> tree){
		channelManager.setChannels(tree);
	}
	
	public void joinChannel(String username, long channelId){
		//channelManager.joinChannel(username, channelId);
	}
	
	public void leaveChannel(String username, long channelId){
		//channelManager.leaveChannel(username, channelId);
	}
	
	public void setUser(User user){
		channelManager.setUser(user);
	}
	
	public User getUser(){
		return channelManager.getUser();
	}
}
