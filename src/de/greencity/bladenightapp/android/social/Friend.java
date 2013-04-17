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
	private Long relativeTime = null;
	private Long relativeDistance  = null;
	private Long absolutePosition  = null;
	
	public Friend(String name, FriendColor color, boolean active){
		this.name = name;
		this.color = color;
		this.active = active;
		resetDynamicData();
	}
	
	public void resetDynamicData() {
		relativeTime = null;
		relativeDistance  = null;
		absolutePosition  = null;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}
		
	public void setColor(FriendColor color){
		this.color = color;
	}

	public FriendColor getColor(){
		return color;
	}
	

	public void setActive(boolean active){
		this.active = active;
	}
	
	public boolean getActive(){
		return active;
	}
	
	public Long getRelativeTime() {
		return relativeTime;
	}

	public void setRelativeTime(Long relativeTime) {
		this.relativeTime = relativeTime;
	}

	public Long getRelativeDistance() {
		return relativeDistance;
	}

	public void setRelativeDistance(Long relativeDistance) {
		this.relativeDistance = relativeDistance;
	}

	public Long getAbsolutePosition() {
		return absolutePosition;
	}

	public void setAbsolutePosition(Long absolutePosition) {
		this.absolutePosition = absolutePosition;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(Friend another) {
		return  name.toLowerCase().compareTo(another.getName().toLowerCase());
	}

}
