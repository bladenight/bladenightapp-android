package de.greencity.bladenightapp.android.selection;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
	  private long id;
	  private String course;
	  private Date date;
	  private String status;
	  private String length;
	  

	  public long getId() {
	    return id;
	  }

	  public void setId(long id) {
	    this.id = id;
	  }

	  public String getCourse() {
	    return course;
	  }

	  public void setCourse(String course) {
	    this.course = course;
	  }
	  
	  public Date getDate() {
		    return date;
	  }
	  
	  public String getDateFormatted(){
		  return new SimpleDateFormat("dd.MM.yyyy").format(date);
	  }

	  public void setDate(Date date) {
	    this.date = date;
	  }
	  
	  public String getStatus() {
		    return status;
	  }

	  public void setStatus(String status) {
	    this.status = status;
	  }
	  
	  public String getLength(){
		  return length;
	  }

	  public void setLength(String length){
		  this.length = length;
	  }
	} 