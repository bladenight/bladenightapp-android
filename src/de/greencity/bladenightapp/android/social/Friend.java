package de.greencity.bladenightapp.android.social;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import de.greencity.bladenightapp.android.R;

public class Friend implements Comparable<Friend>, Serializable {

	private static final long serialVersionUID = 3261795661099426411L;

	public enum FriendColor {
		COLOR1,
		COLOR2,
		COLOR3,
		COLOR4,
		COLOR5,
		BLACK
	}
	
	private String name;
	private boolean isActive;
	private FriendColor color;
	private Long relativeTime = null;
	private Long relativeDistance  = null;
	private Long absolutePosition  = null;
	private boolean isOnline = false;
	private boolean isValid = false;
	private long requestId;
	
	public Friend(String name, FriendColor color){
		this.name = name;
		this.color = color;
		this.isActive = true;
		resetPositionData();
	}
	
	public void resetPositionData() {
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

	public int getColorInt(){
		return colorToInt(color);
	}
	
	public FriendColor getColor(){
		return color;
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

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public long getRequestId() {
		return requestId;
	}

	public boolean isActive() {
		return isActive;
	}

	public void isActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void isOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public boolean isValid() {
		return isValid;
	}

	public void isValid(boolean isValid) {
		this.isValid = isValid;
	}


	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(Friend another) {
		return  name.toLowerCase().compareTo(another.getName().toLowerCase());
	}
	
	public int colorToInt(FriendColor color){
		int exit = 0;
		if(color.equals(FriendColor.COLOR1)) exit = R.color.new_friend1;
		if(color.equals(FriendColor.COLOR2)) exit = R.color.new_friend2;
		if(color.equals(FriendColor.COLOR3)) exit = R.color.new_friend3;
		if(color.equals(FriendColor.COLOR4)) exit = R.color.new_friend4;
		if(color.equals(FriendColor.COLOR5)) exit = R.color.new_friend5;
		if(color.equals(FriendColor.BLACK)) exit = R.color.black;
		return exit;
	}

}
