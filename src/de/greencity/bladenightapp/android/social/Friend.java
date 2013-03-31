package de.greencity.bladenightapp.android.social;

public class Friend {

	public enum FriendColor {
		ORANGE,
		RED,
		BLUE,
		GREEN,
		GREEN_LIGHT
	}
	
	private String name;
	private boolean active;
	private FriendColor color;
	
	public Friend(String name, FriendColor color, boolean active){
		this.name = name;
		this.color = color;
		this.active = active;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setColor(FriendColor color){
		this.color = color;
	}
	
	public void setActive(boolean active){
		this.active = active;
	}
	
	public String getName(){
		return name;
	}
	
	public FriendColor getColor(){
		return color;
	}
	
	public boolean getActive(){
		return active;
	}
	
	public String toString(){
		return name;
	}
}
