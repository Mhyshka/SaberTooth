package run;

import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JOptionPane;

import network.ConnectionManager;
import network.RequestManager;
import view.MainView;
import data.Channel;
import data.ChannelTree;
import data.Message;
import data.User;

public class Controller {
	
	
	public static void start(){
		new Controller();
	}
	private MainView mainView;
	private ConnectionManager connectionManager;
	private RequestManager requestManager;
	private ChannelManager channelManager;
	private UserManager userManager;

	public Controller(){
		mainView = new MainView(this);
		mainView.setVisible(true);
		connectionManager = ConnectionManager.getInstance(this);
		requestManager = RequestManager.getInstance(this);
		channelManager = ChannelManager.getInstance(this);
		userManager = UserManager.getInstance(this);
	}
	
	public void bannedIp(){
		error("Login error","Your IP is banned." , 1);
		mainView.initState();
	}
	
	public void closeAllChannelViews(){
		mainView.closeAllChannelViews();
	}
	
	public void closeChannelView(long channelId){
		mainView.closeChannelView(channelId);
	}
	
	public void connect(String address, int port){
		mainView.connectingState();
		connectionManager.openSocket(address, port);
	}
	
	public void connectionFailed(){
		mainView.initState();
	}
	
	public void connectionSuccess(){
		requestManager.initConnection();
		mainView.connectedState();
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
	
	public ChannelTree getChannel(long id){
		return channelManager.getChannel(id);
	}
	
	public HashMap<Long, ChannelTree> getChannels(){
		return channelManager.getChannels();
	}
	
	public Socket getSocket(){
		return connectionManager.getSocket();
	}
	
	public User getUser(){
		return userManager.getUser();
	}
	
	public boolean isClosed(){
		return connectionManager.isClosed();
	}
	
	public boolean isConnected(){
		return connectionManager.isConnected();
	}
	
	public boolean isLinkedToChannel(long channelId) {
		return channelManager.isLinkedToChannel(channelId);
	}
	
	public boolean isLogged(){
		return requestManager.isLogged();
	}

	public void joinChannel(String username, long channelId){
		channelManager.linkUserToChannel(username, channelId);
	}
	
	public void kicked(String reason){
		if(reason.isEmpty())
			reason = "No reason was given.";
		error("Kicked","You've been kicked for : " + reason, 1);
		mainView.connectedState();
		mainView.closeAllChannelViews();
	}
	
	public void leaveChannel(String username, long channelId){
		channelManager.unlinkUserToChannel(username, channelId);
	}
	
	public Vector<String> listUsernames(long channelId) {
		return channelManager.listUsernames(channelId);
	}
	
	public void login(String username){
		requestManager.sendLogin(username);
		mainView.loggingState();
	}
	
	public void loginSuccess(User user){
		setUser(user);
		mainView.loggedState();
	}
	
	public void logout(){
		requestManager.sendLogout();
	}
	
	public void newChannel(Channel channel){
		channelManager.newChannel(channel);
	}
	
	public void newMessage(Message message){
		//TODO gestion d'un new message
	}
	
	public void openChannelView(long channelId){
		mainView.openChannelView(channelId);
	}
	
	public void passwordAsked(){
		// TODO 
		// Ouverture de fenetre du password si nécéssaire.
	}
	
	public void removeChannel(long channelId){
		channelManager.removeChannel(channelId);
	}
	
	public void removeUserChannel(long channelId) {
		userManager.removeUserChannel(channelId);
	}
	
	public void resetChannels(){
		channelManager.reset();
	}
	
	public void resetJTrees(){
		mainView.resetJTrees();
	}
	public void resetUser(){
		userManager.resetUser();
	}
	
	public void sendChannels(){
		requestManager.sendChannels();
	}
	
	public void sendJoinned(long channelId){
		requestManager.sendJoinned(channelId);
	}
	
	public void sendLeft(long channelId){
		requestManager.sendLeft(channelId);
	}
	
	public void sendMessage(String text, long channelId){
		Message message = new Message(getUser().getUsername(),channelId, text);
		message.setBold(getUser().isBold());
		message.setColor(getUser().getColor());
		message.setItalic(getUser().isItalic());
		
		requestManager.sendMessage(message);
	}
	
	public void sendNewChannel(ChannelTree newChannel) {
		requestManager.sendNewChannel(newChannel);
	}
	
	public void serverShutdown(String reason) {
		requestManager.close();
		connectionManager.closeSocket();
		mainView.initState();
		if(reason.isEmpty())
			reason = "No reason was given.";
		error("Server shutdown",reason,0);
	}
	
	public void setChannels(HashMap<Long,ChannelTree> tree){
		channelManager.setChannels(tree);
	}
	
	public void setServerName(String name){
		mainView.setServerName(name);
	}
	
	public void setUser(User user){
		userManager.setUser(user);
	}

	public void updateChannels(){
		mainView.updateChannels();
	}

	public void updateUsersList(long channelId) {
		boolean parent = false;
		ChannelTree chan = getChannel(channelId);
		while(chan.getParent()!= null && !parent){
			if(chan.getParent().getId() == mainView.getSelectedChannelId())
				parent = true;
			else
				chan = chan.getParent();
		}
		if(parent || mainView.getSelectedChannelId() == channelId || mainView.getSelectedChannelId() == 0)
			mainView.updateUsers(listUsernames(mainView.getSelectedChannelId()));
	}

	public void usernameUsed(){
		error("Login error","The username you choose is already in use." , 1);
		mainView.initState();
	}

	public void welcome(String serverName){
		setServerName(serverName);
		mainView.setServerName(serverName);
		mainView.connectedState();
	}
	
	public void userJoinned(long channelId){
		//mainView.setJoinned(channelId);
	}
}
