package de.greencity.bladenightapp.android.social;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import android.graphics.Color;

public class Friend implements Comparable<Friend>, Serializable {

    private static final long serialVersionUID = 3261795661099426411L;

    private String name;
    private boolean isActive;
    private int color;
    transient private Long relativeTime = null;
    transient private Long relativeDistance  = null;
    transient private Long absolutePosition  = null;
    private boolean isOnline = false;
    private boolean isValid = false;
    private long requestId;

    public Friend(String name){
        this.name = name;
        this.color = Color.BLACK;
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

    public void setColor(int color){
        this.color = color;
    }

    public int getColor(){
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

}
