package data;

import java.util.Vector;

public class Channel extends ChannelTree{
	private Vector<String> users;
	private Vector<Message> messages;
	private String password;
	private boolean whisper;
	private User owner;
	private Vector<String> moderators;
	private boolean joinned;
	
	
	public Channel(long id, String name, ChannelGroup parent, boolean isWhisper){
		setName(name);
		setId(id);
		setParent(parent);
		whisper = isWhisper;
		users = new Vector<String>();
		moderators = new Vector<String>();
		messages = new Vector<Message>();
		joinned = false;
	}
	
	public boolean isJoinned(){
		return joinned;
	}
	
	public void setJoinned(boolean state){
		joinned = state;
	}
	
	public void addMessage(Message message){
		this.messages.add(message);
	}
	public void addModerator(String newModeratorName){
		this.moderators.add(newModeratorName);
	}
	public void addUser(String newUsername){
		this.users.add(newUsername);
	}
	public Vector<Message> getMessages() {
		return messages;
	}
	
	public Vector<String> getModerators() {
		return moderators;
	}
	public User getOwner() {
		return owner;
	}
	public String getPassword() {
		return password;
	}
	public Vector<String> getUsers() {
		return users;
	}
	public boolean isWhisper() {
		return whisper;
	}
	
	public void removeMessage(int index){
		this.messages.removeElementAt(index);
	}
	public void removeMessage(Message rmMessage){
		this.messages.remove(rmMessage);
	}
	
	public void removeModerator(String rmModeratorName){
		this.moderators.remove(rmModeratorName);
	}
	public void removeUser(String rmUsername){
		this.users.remove(rmUsername);
	}
	
	public void setMessages(Vector<Message> messages) {
		this.messages = messages;
	}
	public void setModerators(Vector<String> moderators) {
		this.moderators = moderators;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setUsers(Vector<String> usernames) {
		this.users = usernames;
	}
	public void setWhisper(boolean whisper) {
		this.whisper = whisper;
	}
	
}
