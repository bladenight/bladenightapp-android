package de.greencity.bladenightapp.android.social;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Friend implements Comparable<Friend> {

	public enum FriendColor {
		ORANGE,
		RED,
		BLUE,
		GREEN,
		GREEN_LIGHT,
		BLACK
	}
	
	private String name;
	private boolean active;
	private FriendColor color;
	private int time_rel;
	private int distance_rel;
	private int time_abs;
	private int distance_abs;
	
	public Friend(String name, FriendColor color, boolean active){
		this.name = name;
		this.color = color;
		this.active = active;
	}
	
	public void setActionData(int time_rel, int distance_rel, int time_abs, int distance_abs){
		this.time_rel = time_rel;
		this.distance_rel = distance_rel;
		this.time_abs = time_abs;
		this.distance_abs = distance_abs;
	}
	
	public int getTimeRel(){
		return time_rel;
	}
	
	public int getDistanceRel(){
		return distance_rel;
	}
	
	public int getTimeAbs(){
		return time_abs;
	}
	
	public int getDistanceAbs(){
		return distance_abs;
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
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(Friend another) {
		return  name.toLowerCase().compareTo(another.getName().toLowerCase());
	}
}
