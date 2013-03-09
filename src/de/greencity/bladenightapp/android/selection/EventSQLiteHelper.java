package de.greencity.bladenightapp.android.selection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventSQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_EVENTS = "events";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_COURSE = "course";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_LENGTH = "length";


	private static final String DATABASE_NAME = "events.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_EVENTS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_COURSE
			+ " text not null, "+ COLUMN_DATE + " text not null, "
			+ COLUMN_STATUS + " text not null, " + COLUMN_LENGTH 
			+ " text not null );";

	public EventSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(EventSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		onCreate(db);
	}

} 
