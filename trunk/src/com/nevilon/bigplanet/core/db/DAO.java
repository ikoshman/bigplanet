package com.nevilon.bigplanet.core.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DAO {

	public static final String TABLE_GEOBOOKMARKS = "geobookmarks";

	private static final String COLUMN_OFFSETY = "offsety";

	private static final String COLUMN_OFFSETX = "offsetx";

	private static final String COLUMN_Z = "z";

	private static final String COLUMN_SOURCE = "source";

	private static final String COLUMN_TAGS = "tags";

	private static final String COLUMN_DESCRIPTION = "description";

	private static final String COLUMN_NAME = "name";

	private static final String COLUMN_ID = "id";

	public static final String TABLE_DDL =	 
	"create table " +TABLE_GEOBOOKMARKS+  
	"("+COLUMN_ID+" integer primary key autoincrement," +
	""  +COLUMN_NAME+ " text,"+
	"" +COLUMN_DESCRIPTION+ " text,"+
	"" + COLUMN_TAGS+ " text,"+
	"" +COLUMN_SOURCE+ " integer,"+
	"" +COLUMN_OFFSETX+ " integer,"+
	"" +COLUMN_OFFSETY+ " integer,"+
	"" +COLUMN_Z+ " integer"+
	");";

	
	private DBHelper dbHelper;
	
	private SQLiteDatabase db;

	
	public DAO(Context context){
		dbHelper = new DBHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	public void saveGeoBookmark(GeoBookmark bookmark){
		 ContentValues initialValues = new ContentValues();
	      //  initialValues.put(DAO.COLUMN_ID, bookmark.getId());
	        initialValues.put(DAO.COLUMN_NAME, bookmark.getName());
	        initialValues.put(DAO.COLUMN_DESCRIPTION, bookmark.getDescription());
	        initialValues.put(DAO.COLUMN_TAGS, bookmark.getTags());
	        initialValues.put(DAO.COLUMN_SOURCE, bookmark.getSource());
	        initialValues.put(DAO.COLUMN_Z, bookmark.getZ());
	        initialValues.put(DAO.COLUMN_OFFSETX, bookmark.getOffsetX());
	        initialValues.put(DAO.COLUMN_OFFSETY, bookmark.getOffsetY());
	        // save to database
	        db.insert(DAO.TABLE_GEOBOOKMARKS, null, initialValues);
	        System.out.println(getBookmarks().size());
	}
	
	public List<GeoBookmark> getBookmarks(){
		List<GeoBookmark> bookmarks = new ArrayList<GeoBookmark>();
        try {
            
        	Cursor c =
                 db.query(true, DAO.TABLE_GEOBOOKMARKS, null, null, null,
            		null, null, null, null);
            
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                GeoBookmark bookmark = new GeoBookmark();
                bookmark.setId(c.getInt(c.getColumnIndex(COLUMN_ID)));
                bookmark.setName(c.getString(c.getColumnIndex(COLUMN_NAME)));
                bookmark.setDescription(c.getString(c.getColumnIndex(COLUMN_DESCRIPTION)));
                bookmark.setOffsetX(c.getInt(c.getColumnIndex(COLUMN_OFFSETX)));
                bookmark.setOffsetY(c.getInt(c.getColumnIndex(COLUMN_OFFSETY)));
                bookmark.setSource(c.getInt(c.getColumnIndex(COLUMN_SOURCE)));
                bookmark.setTags(c.getString(c.getColumnIndex(COLUMN_TAGS)));
                bookmark.setZ(c.getInt(c.getColumnIndex(COLUMN_Z)));
                bookmarks.add(bookmark);
                c.moveToNext();
            }
            c.close();
        } catch (SQLException e) {
            Log.e("Exception on query", e.toString());
        }
        return bookmarks;
	}
	
	
	
}
