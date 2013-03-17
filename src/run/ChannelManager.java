package run;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import data.Channel;
import data.ChannelGroup;
import data.ChannelTree;

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
	
	public boolean isLinkedToChannel(long channelId) {
		return ctrl.getUser().getChannels().contains(channelId);
	}
	
	public void linkUserToChannel(String username, long channelId){
		if(getChannels().containsKey(channelId)){
			if(username.equals(ctrl.getUser().getUsername())){
				ctrl.getUser().addChannel(channelId);
				ctrl.userJoinned(channelId);
				ctrl.openChannelView(channelId);
			}
			((Channel)getChannel(channelId)).addUser(username);
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
	
	public void removeChannel(long channelId){
		if(getChannel(channelId) instanceof Channel){
			getChannel(channelId).getParent().removeChild(channelId);
			if(channelTree.remove(channelId) != null){
				ctrl.removeUserChannel(channelId);
				ctrl.updateChannels();
			}
		}
	}
	
	public void removeChannel(long channelId, boolean notify){
		removeChannel(channelId);
		if(notify)
			ctrl.sendLeft(channelId);
	}
	
	public void reset(){
		channelTree = new HashMap<Long, ChannelTree>();
		if(ctrl.getUser()!=null)
			ctrl.getUser().setChannels(new Vector<Long>());
		ctrl.resetJTrees();
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
	
	public void unlinkUserToChannel(String username, long channelId){
		if(getChannels().containsKey(channelId)){
			if(username.equals(ctrl.getUser().getUsername())){
				ctrl.getUser().removeChannel(channelId);
				ctrl.closeChannelView(channelId);
			}
			((Channel)getChannel(channelId)).removeUser(username);
			ctrl.updateUsersList(channelId);
		}
		else{
			ctrl.sendChannels();
		}
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