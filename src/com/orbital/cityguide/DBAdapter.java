package com.orbital.cityguide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.orbital.cityguide.model.Tag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBAdapter {

	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DBAdapter";
	
	// Database Name
	private static final String DATABASE_NAME = "cityGuideSingapore";

	// Common column names
	private static final String KEY_ID = "id";

	private static final String TABLE_TAG = "tags";
	private static final String KEY_TAG_NAME = "tag_name";

	private static final String TABLE_PLANNERLIST = "plannerList";
	public static final String KEY_ATTR_ID = "attr_id";
	public static final String KEY_TAG_ID = "tag_id";
	private static final String KEY_CREATED_AT = "created_at";

	private static final String CREATE_TABLE_TAG = "CREATE TABLE " + TABLE_TAG
			+ "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TAG_NAME + " TEXT"
			+ ")";

	private static final String CREATE_TABLE_PLANNERLIST = "CREATE TABLE "
			+ TABLE_PLANNERLIST + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
			+ KEY_ATTR_ID + " TEXT," + KEY_TAG_ID + " TEXT,"
			+ KEY_CREATED_AT + " DATETIME" + ")";

	Context context;
	DatabaseHelper DBHelper;
	SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(CREATE_TABLE_TAG);
				db.execSQL(CREATE_TABLE_PLANNERLIST);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_TAG);
			db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_PLANNERLIST);
			onCreate(db);
		}
	}

	// opens the database
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	// close the database
	public void close() {
		DBHelper.close();
	}

	 /* Creating tag*/
	public long createTag(Tag tag) {
		db = DBHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TAG_NAME, tag.getTagName());

		return db.insert(TABLE_TAG, null, values);
	}
	
	// insert a item into the insertPlannerList
	public long insertPlannerList(String attr_id, String tag_id) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ATTR_ID, attr_id);
		initialValues.put(KEY_TAG_ID, tag_id);
		initialValues.put(KEY_CREATED_AT, getDateTime());
		
		return db.insert(TABLE_PLANNERLIST, null, initialValues);	
	}

	// delete a ListItem
	public boolean deletePlannerItem(String key) {
		return db.delete(TABLE_PLANNERLIST, KEY_ATTR_ID + "=" + key, null) > 0;
	}

	// retrieve all the ListItem
	public Cursor getAllPlanner() {
		return db.query(TABLE_PLANNERLIST, new String[] { KEY_ID, KEY_ATTR_ID,
				KEY_TAG_ID, KEY_CREATED_AT }, null, null, null, null,
				KEY_TAG_ID + " ASC");
	}
	
	// retrieve all attr_id
	public Cursor getAttrID() {
		Cursor mCursor = null;
		if(db != null ){
			mCursor = db.rawQuery("SELECT attr_id FROM plannerList", null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
		} else {
			Log.d("Testing", "gdhhhhhhhhhhhhhhh");
		}
		return mCursor;
	}

	// retrieve a particular ListItem
	public Cursor getPlannerItem(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, TABLE_PLANNERLIST, new String[] {
				KEY_ID, KEY_ATTR_ID, KEY_TAG_ID, KEY_CREATED_AT }, KEY_ID
				+ "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// update ListItem
	public boolean updatePlannerItem(long rowId, long attr_id, long tag_id) {
		ContentValues args = new ContentValues();
		args.put(KEY_ATTR_ID, attr_id);
		args.put(KEY_TAG_ID, tag_id);
		args.put(KEY_CREATED_AT, getDateTime());
		return db.update(TABLE_PLANNERLIST, args, KEY_ID + "=" + rowId, null) > 0;
	}

	private String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/*public void insertTag(){
		Tag tag1 = new Tag("Waiting List");
		Tag tag2 = new Tag("Day 1");
		Tag tag3 = new Tag("Day 2");
		Tag tag4 = new Tag("Day 3");
		Tag tag5 = new Tag("Day 4");
		Tag tag6 = new Tag("Day 5");
		Tag tag7 = new Tag("Day 6");
		Tag tag8 = new Tag("Day 7");
		Tag tag9 = new Tag("Day 8");
		Tag tag10 = new Tag("Day 9");
		
		// Inserting tags in db
		createTag(tag1);
		createTag(tag2);
		createTag(tag3);
		createTag(tag4);
		createTag(tag5);
		createTag(tag6);
		createTag(tag7);
		createTag(tag8);
		createTag(tag9);
		createTag(tag10);
	}*/
	
}
