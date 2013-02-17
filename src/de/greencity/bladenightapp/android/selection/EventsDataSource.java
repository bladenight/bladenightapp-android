package de.greencity.bladenightapp.android.selection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EventsDataSource {

  // Database fields
  private SQLiteDatabase database;
  private EventSQLiteHelper dbHelper;
  private String[] allColumns = { EventSQLiteHelper.COLUMN_ID,
      EventSQLiteHelper.COLUMN_COURSE, EventSQLiteHelper.COLUMN_DATE,
      EventSQLiteHelper.COLUMN_STATUS,EventSQLiteHelper.COLUMN_LENGTH};

  public EventsDataSource(Context context) {
    dbHelper = new EventSQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Event createEvent(String course, String date, String status, String length) {
    ContentValues values = new ContentValues();
    values.put(EventSQLiteHelper.COLUMN_COURSE, course);
    values.put(EventSQLiteHelper.COLUMN_DATE, date);
    values.put(EventSQLiteHelper.COLUMN_STATUS, status);
    values.put(EventSQLiteHelper.COLUMN_LENGTH, length);
    long insertId = database.insert(EventSQLiteHelper.TABLE_EVENTS, null,
        values);
    Cursor cursor = database.query(EventSQLiteHelper.TABLE_EVENTS,
        allColumns, EventSQLiteHelper.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Event newEvent = cursorToEvent(cursor);
    cursor.close();
    return newEvent;
  }

  public void deleteComment(Event event) {
    long id = event.getId();
    System.out.println("Event deleted with id: " + id);
    database.delete(EventSQLiteHelper.TABLE_EVENTS, EventSQLiteHelper.COLUMN_ID
        + " = " + id, null);
  }

  public LinkedList<Event> getAllEvents() {
	  LinkedList<Event> events = new LinkedList<Event>();

    Cursor cursor = database.query(EventSQLiteHelper.TABLE_EVENTS,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Event event = cursorToEvent(cursor);
      events.add(event);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return events;
  }

  private Event cursorToEvent(Cursor cursor) {
    Event event = new Event();
    event.setId(cursor.getLong(0));
    event.setCourse(cursor.getString(1));
    try {
		event.setDate(new SimpleDateFormat("dd.MM.yyyy").parse(cursor.getString(2)));
	} catch (ParseException e) {
		e.printStackTrace();
	}
    event.setStatus(cursor.getString(3));
    event.setLength(cursor.getString(4));
    return event;
  }
} 
