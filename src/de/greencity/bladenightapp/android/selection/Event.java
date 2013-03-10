package de.greencity.bladenightapp.android.selection;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Event {
	private long id;
	private String course;
	private DateTime date;
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

	public DateTime getDate() {
		return date;
	}

	public String getDateFormatted(){
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy");
		return fmt.print(date);
	}

	public void setDate(DateTime date) {
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