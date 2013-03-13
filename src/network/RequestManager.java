package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import run.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import data.Channel;
import data.ChannelGroup;
import data.ChannelTree;
import data.Message;
import data.Request;
import data.User;


public class RequestManager {
	
	
	/*******************************
	 * InputThread
	 */
	
	private class InputThread extends Thread{
		private InputStream is;
		private DataInputStream dis;
		private Gson gson;
		private boolean running;
		
		public InputThread(Socket newSocket){
			gson = new Gson();
			running = true;
			try {
				is = newSocket.getInputStream();
				dis = new DataInputStream(is);
			} catch (IOException e) {
				ctrl.error("Request Error", "Request Service Error - IO Exception while getting the InputStream.", 1);
				e.printStackTrace();
			}
		}
		
		public void read(String readBuffer){
			Request request = gson.fromJson(readBuffer, Request.class);
			
			switch(request.getType()){
				case "message" : 
					readMessage(request);
				break;
				
				case "channels" :
					readChannels(request);
				break;
				
				case "login" :
					readLogin(request);
				break;
				
				case "password" :
					readPassword(request);
				break;
				
				case "logout" :
					readLogout(request);
				break;
				
				case "joinned" :
					readJoinned(request);
				break;
				
				case "left" : 
					readLeft(request);
				break;
				
				case "welcome" :
					readWelcome(request);
				break;
				
				case "shutdown" :
					readShutdown(request);
				break;
				
				case "newchannel":
					readNewChannel(request);
				break;
				
				case "rmchannel":
					readRmChannel(request);
				break;
				
				default : ctrl.error("Request Error", "Request Service Error - Unkown message type :" + request.getType(), 1);
			}
		}
		
		private void readNewChannel(Request request){
			Channel channel = gson.fromJson(request.getContent(), Channel.class);
			channel.setParent(((ChannelGroup)ctrl.getChannel(channel.getParent().getId())));
			ctrl.newChannel(channel);
		}
		
		private void readRmChannel(Request request){
			long id = Long.parseLong(request.getContent());
			ctrl.removeChannel(id);
		}
		
		private void readChannels(Request request){
			boolean joinning = false;
			if(ctrl.getChannels().isEmpty())
				joinning = true;
			long welcomeChanId = 1;
			// TODO Appel à la variable channel de welcome recupéré dans readWelcome();
			
			Channel ch;
			ChannelGroup gCh;
			HashMap<Long,ChannelTree> chTree = new HashMap<Long, ChannelTree>();
			JsonParser parser = new JsonParser();
			JsonObject hashmap = parser.parse(request.getContent()).getAsJsonObject();
			for(Entry<String, JsonElement> key  : hashmap.entrySet()){
				JsonObject elem = key.getValue().getAsJsonObject();
				if(elem.has("users")){
					ch = gson.fromJson(key.getValue(), Channel.class);
					chTree.put(ch.getId(), ch);
				}
				else{
					gCh = gson.fromJson(key.getValue(), ChannelGroup.class);
					chTree.put(gCh.getId(), gCh);
				}
			}
			ctrl.setChannels(chTree);
			if(joinning && ctrl.getChannels().containsKey(welcomeChanId)){
				sendJoinned(welcomeChanId);
			}
		}
		
		private void readJoinned(Request request){
			String args[] = request.getContent().split("&");
			long id = Long.parseLong(args[1]);
			ctrl.joinChannel(args[0],id);
		}
		
		private void readLeft(Request request){
			String args[] = request.getContent().split("&");
			long id = Long.parseLong(args[1]);
			ctrl.leaveChannel(args[0],id);
		}
		
		private void readLogin(Request request){
			String args[] = request.getContent().split("&");
			switch(args[0]){
				case "user" : ctrl.usernameUsed();
				break;
				
				case "banned" : ctrl.bannedIp();
				break;
				
				case "success" : 
					logged = true;
					ctrl.loginSuccess(gson.fromJson(args[1],User.class));
				break;
				
				case "password" : ctrl.passwordAsked();
				break;
			}
		}
		
		private void readLogout(Request request){
			ctrl.kicked(request.getContent());
		}
		
		private void readMessage(Request request){
			ctrl.newMessage(gson.fromJson(request.getContent(), Message.class));
		}
		
		private void readPassword(Request request){
			// TODO gestion demande de password
		}
		
		private void readShutdown(Request request){
			ctrl.serverShutdown(request.getContent());
		}
		
		private void readWelcome(Request request){
			if(logged){
				ctrl.resetUser();
				ctrl.resetChannels();
				ctrl.closeAllChannelViews();
			}
			// TODO gestion du channel de welcome du server.
			logged = false;
			ctrl.welcome(request.getContent());
		}
		
		@Override
		public void run(){
			String readBuffer = "";
			while(running){
				try {
					if(dis.available() != 0){
						readBuffer = dis.readUTF();
						read(readBuffer);
					}
					else{
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							ctrl.error("Request Error", "InputThread was interrupted while sleeping.", 1);
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					ctrl.error("Request Error","Request Service Error - IOException while reading the buffer of the InputStream.\nRead : "+ readBuffer,1);
					e.printStackTrace();
				}
			}
		}
		
		public void turnOff(){
			running = false;
		}
	}
	
	/*******************************
	 * OutputThread
	 */
	
	
	private class OutputThread extends Thread{
		private OutputStream os;
		private DataOutputStream dos;
		private LinkedList<Request> stack;
		private Gson gson;
		private boolean running;
		private boolean closing;
		
		public OutputThread(Socket newSocket){
			try {
				os = newSocket.getOutputStream();
			} catch (IOException e) {
				ctrl.error("Request Error", "IO Exception Exception while getting the OutputStream.", 1);
				e.printStackTrace();
			}
			dos = new DataOutputStream(os);
			stack = new LinkedList<Request>();
			gson = new Gson();
			running = true;
			closing = false;
		}
		
		public boolean isClosing(){
			return closing;
		}
		
		public boolean isStacked(){
			return !stack.isEmpty();
		}
		
		@Override
		public void run(){
			while(running){
				if(stack.isEmpty())
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						ctrl.error("Request Error", "OutputThread was interrupted while sleeping.", 1);
						e.printStackTrace();
					}
				else{
					synchronized(stack){
						send(stack.pop());
					}
				}
			}
		}
		
		private void send(Request request){
			try {
				dos.writeUTF(gson.toJson(request));
				if(request.getType().equals("goodbye")){
					closing = false;
					turnOff();
				}
			} catch (IOException e) {
				ctrl.error("Request Error", "IO Exception while sending request to the server.", 1);
				e.printStackTrace();
			}
		}
		
		public void stackRequest(Request request){
			if(request.getType().equals("goodbye"))
				closing = true;
			synchronized(stack){
				stack.add(request);
			}
		}
		
		public void turnOff(){
			running = false;
		}
	}
	
	/*******************************
	 * MANAGER
	 */
	
	
	private static RequestManager manager;
	public static RequestManager getInstance(Controller newCtrl){
		if(manager != null)
			return manager;
		else{
			manager = new RequestManager(newCtrl);
			return manager;
		}
	}
	private InputThread inputThread;
	private OutputThread outputThread;
	private Controller ctrl;
	
	private boolean logged;
	
	public RequestManager(Controller newCtrl){
		ctrl = newCtrl;
		logged = false;
		System.out.println("Request Service - Initialized.");
	}
	
	public void close(){
		outputThread.turnOff();
		inputThread.turnOff();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		outputThread = null;
		inputThread = null;
	}
	
	public void disconnect(){
		Request request = new Request("goodbye", "" , "");
		logged = false;
		outputThread.stackRequest(request);
		inputThread.turnOff();
		while(outputThread.isClosing()){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		inputThread.interrupt();
		outputThread.interrupt();
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		inputThread = null;
		outputThread = null;
	}
	
	public void initConnection(){
		inputThread = new InputThread(ctrl.getSocket());
		outputThread = new OutputThread(ctrl.getSocket());
		inputThread.start();
		outputThread.start();
	}
	
	public boolean isInit(){
		return (inputThread != null && outputThread != null);
	}
	
	public boolean isLogged(){
		return logged;
	}
	
	public boolean isStacked(){
		return outputThread.isStacked();
	}
	
	public void sendLogin(String username){
		sendRequest(new Request("login",username,""));
	}
	
	public void sendLogout(){
		sendRequest(new Request("logout","",""));
	}
	
	public void sendChannels(){
		sendRequest(new Request("channels","",""));
	}

	public void sendJoinned(long channelId){
		sendRequest(new Request("joinned",""+channelId,""));
	}
	
	public void sendLeft(long channelId){
		sendRequest(new Request("left",""+channelId,""));
	}
	
	public void sendMessage(Message message) {
		sendRequest(new Request("message",new Gson().toJson(message),""));
	}
	
	public void sendRequest(Request request){
		outputThread.stackRequest(request);
	}
	
	public void sendNewChannel(Channel newChannel){
		Request request = new Request("newchannel",new Gson().toJson(newChannel),"");
		sendRequest(request);
	}
	
	public void sendRmChannel(long channelId){
		Request request = new Request("rmchannel",""+channelId,"");
		sendRequest(request);
	}

	public void sendNewChannel(ChannelTree newChannel) {
		Request request = new Request("newchannel",new Gson().toJson(newChannel),"");
		sendRequest(request);
	}
}
