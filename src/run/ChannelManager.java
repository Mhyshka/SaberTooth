package run;

import java.util.HashMap;
import java.util.Vector;

import data.Channel;
import data.ChannelGroup;
import data.ChannelTree;
import data.User;

public class ChannelManager {
	private static ChannelManager manager;
	private HashMap<Long,ChannelTree> channelTree;
	private Controller ctrl;
	private User user;
	
	
	public static ChannelManager getInstance(Controller ctrl){
		if(manager != null)
			return manager;
		else{
			manager = new ChannelManager(ctrl);
			return manager;
		}
	}
	
	private ChannelManager(Controller ctrl){
		this.ctrl = ctrl;
		channelTree = new HashMap<Long,ChannelTree>();
		System.out.println("Channel Service - initialized.");
	}
	
	/*public void addUserChannel(long newChannelId){
		if(!getUserChannels().contains(newChannelId)){
			getUserChannels().add(newChannelId);
			getChannel(newChannelId)
			if(channelTree.containsKey(newChannelId)){
				
			}
			else{
				ctrl.missingChannel();
			}
		}
	}*/
	
	public ChannelTree getChannel(long id){
		return channelTree.get(id);
	}
	
	public HashMap<Long, ChannelTree> getChannels(){
		return channelTree;
	}
	
	public Vector<Long> getUserChannels(){
		return ctrl.getUser().getChannels();
	}
	
	private void newChannel(ChannelTree newChannel){
		channelTree.put(newChannel.getId(), newChannel);
		ctrl.updateChannels();
	}
	
	private void removeChannel(ChannelTree rmChannel){
		if(rmChannel instanceof Channel){
			channelTree.remove(rmChannel.getId());
			ctrl.updateChannels();
		}
	}
	
	public void removeUserChannel(long rmChannelId){
		if(getUserChannels().contains(rmChannelId)){
			getUserChannels().removeElement(rmChannelId);
		}
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
	
	public void setUserChannels(Vector<Long> channelsId){
		ctrl.getUser().setChannels(channelsId);
		ctrl.updateUserChannels();
	}
	
	public void setUser(User user){
		this.user = user;
	}
	
	public User getUser(){
		return user;
	}
}