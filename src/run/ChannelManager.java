package run;

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
	
	public void joinChannel(String username, long channelId){
		if(getChannels().containsKey(channelId)){
			if(username.equals(getUser().getUsername())){
				getUser().addChannel(channelId);
			}
			((Channel)getChannel(channelId)).addUser(username);
			ctrl.updateUserChannels();
			ctrl.openChannelView(channelId);
		}
		else{
			ctrl.sendChannels();
		}
	}
	
	public void leaveChannel(String username, long channelId){
		if(getChannels().containsKey(channelId)){
			if(username.equals(getUser().getUsername())){
				getUser().removeChannel(channelId);
				
			}
			((Channel)getChannel(channelId)).removeUser(username);
			ctrl.updateUserChannels();
			ctrl.closeChannelView(channelId);
		}
		else{
			ctrl.sendChannels();
		}
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
	
	/*public void removeChannel(ChannelTree rmChannel){
		if(rmChannel instanceof Channel){
			if(channelTree.remove(rmChannel.getId()) != null){
				removeUserChannel(rmChannel.getId());
				ctrl.updateChannels();
			}
		}
	}*/
	
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
}