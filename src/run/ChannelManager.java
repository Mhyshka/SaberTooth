package run;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import data.Channel;
import data.ChannelGroup;
import data.ChannelTree;
import data.User;

public class ChannelManager {
	private static ChannelManager manager;
	public static ChannelManager getInstance(Controller ctrl){
		if(manager != null)
			return manager;
		else{
			manager = new ChannelManager(ctrl);
			return manager;
		}
	}
	private HashMap<Long,ChannelTree> channelTree;
	private Controller ctrl;
	
	
	private User user;
	
	private ChannelManager(Controller ctrl){
		this.ctrl = ctrl;
		channelTree = new HashMap<Long,ChannelTree>();
		System.out.println("Channel Service - initialized.");
	}
	
	public ChannelTree getChannel(long id){
		return channelTree.get(id);
	}
	
	public HashMap<Long, ChannelTree> getChannels(){
		return channelTree;
	}
	
	public User getUser(){
		return user;
	}
	
	public Vector<Long> getUserChannels(){
		return getUser().getChannels();
	}
	
	public boolean isLinkedToChannel(long channelId) {
		return user.getChannels().contains(channelId);
	}
	
	public void linkUserToChannel(String username, long channelId){
		if(getChannels().containsKey(channelId)){
			if(username.equals(getUser().getUsername())){
				getUser().addChannel(channelId);
				ctrl.openChannelView(channelId);
			}
			((Channel)getChannel(channelId)).addUser(username);
			ctrl.updateUserChannels();
			ctrl.updateUsersList(channelId);
		}
		else{
			ctrl.sendChannels();
		}
	}
	
	public void unlinkUserToChannel(String username, long channelId){
		if(getChannels().containsKey(channelId)){
			if(username.equals(getUser().getUsername())){
				getUser().removeChannel(channelId);
				ctrl.closeChannelView(channelId);
			}
			((Channel)getChannel(channelId)).removeUser(username);
			ctrl.updateUserChannels();
			ctrl.updateUsersList(channelId);
		}
		else{
			ctrl.sendChannels();
		}
	}
	
	public Vector<String> listUsernames(long channelId) {
		Vector<String> usernames;
		if(getChannel(channelId) instanceof ChannelGroup){
			usernames = new Vector<String>();
			ChannelGroup cg = (ChannelGroup)getChannel(channelId);
			for(long id : cg.getChildren()){
				vectorUsers(id,usernames);
			}
		}
		else{
			usernames = ((Channel)getChannel(channelId)).getUsers();
		}
		Collections.sort(usernames);
		return usernames;
	}
	
	public void newChannel(ChannelTree newChannel){
		channelTree.put(newChannel.getId(), newChannel);
		ctrl.updateChannels();
	}
	
	public void newChannel(ChannelTree newChannel, boolean notify){
		newChannel(newChannel);
		if(notify)
			ctrl.sendNewChannel(newChannel);
	}
	
	public void newUserChannel(long newChannelId){
		if(!getUserChannels().contains(newChannelId)){
			getUserChannels().add(newChannelId);
			ctrl.updateUserChannels();
		}
	}
	
	public void removeChannel(long channelId){
		if(getChannel(channelId) instanceof Channel){
			getChannel(channelId).getParent().removeChild(channelId);
			if(channelTree.remove(channelId) != null){
				removeUserChannel(channelId);
				ctrl.updateChannels();
			}
		}
	}
	
	public void removeChannel(long channelId, boolean notify){
		removeChannel(channelId);
		if(notify)
			ctrl.sendLeft(channelId);
	}
	
	public void removeUserChannel(long rmChannelId){
		if(getUserChannels().contains(rmChannelId)){
			getUserChannels().removeElement(rmChannelId);
			ctrl.updateUserChannels();
		}
	}
	
	public void reset(){
		channelTree = new HashMap<Long, ChannelTree>();
		if(getUser()!=null)
			getUser().setChannels(new Vector<Long>());
		ctrl.resetJTrees();
	}
	
	public void resetUser(){
		user = null;
	}
	
	public void rmChannel(long rmChannelId){
		if(getChannel(rmChannelId) instanceof ChannelGroup){
			for(long cid : ((ChannelGroup)getChannel(rmChannelId)).getChildren())
					getChannel(cid).setParent((ChannelGroup)getChannel(getChannel(rmChannelId).getParent().getId()));
			channelTree.remove(rmChannelId);
		}
		else{
			((Channel)getChannel(rmChannelId)).getParent().removeChild(rmChannelId);
			channelTree.remove(rmChannelId);
		}
	}
	
	public void setChannels(HashMap<Long,ChannelTree> chTree){
		channelTree = chTree;
		ctrl.updateChannels();
	}

	public void setUser(User user){
		this.user = user;
	}
	
	public void setUserChannels(Vector<Long> channelsId){
		getUser().setChannels(channelsId);
		ctrl.updateUserChannels();
	}
	
	private void vectorUsers(long chanId, Vector<String> users){
		ChannelTree ct = getChannel(chanId);
		if(ct instanceof ChannelGroup){
			ChannelGroup cg = (ChannelGroup)ct;
			for(long id : cg.getChildren()){
				vectorUsers(id,users);
			}
		}
		else{
			Channel ch = (Channel)ct;
			Vector<String>chUsers = ch.getUsers();
			for(String username : chUsers){
				if(!users.contains(username)){
					users.add(username);
				}
			}
		}
	}
}