package run;

import java.util.Vector;

import data.User;

public class UserManager {
	private static UserManager manager;
	public static UserManager getInstance(Controller ctrl){
		if(manager != null)
			return manager;
		else{
			manager = new UserManager(ctrl);
			return manager;
		}
	}
	
	private Controller ctrl;
	private User user;
	
	public UserManager(Controller ctrl){
		this.ctrl = ctrl;
	}
	
	public User getUser(){
		return user;
	}
	
	public Vector<Long> getUserChannels(){
		return getUser().getChannels();
	}
	

	
	public void newUserChannel(long newChannelId){
		if(!getUserChannels().contains(newChannelId)){
			getUserChannels().add(newChannelId);
			ctrl.updateUserChannels();
		}
	}
	
	public void removeUserChannel(long rmChannelId){
		if(getUserChannels().contains(rmChannelId)){
			getUserChannels().removeElement(rmChannelId);
			ctrl.updateUserChannels();
		}
	}
	
	public void resetUser(){
		user = null;
	}
	
	public void setUser(User user){
		this.user = user;
	}
	
	public void setUserChannels(Vector<Long> channelsId){
		getUser().setChannels(channelsId);
		ctrl.updateUserChannels();
	}
}
