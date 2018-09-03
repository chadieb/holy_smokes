package com.holysmokes.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.util.ActivityUtil;


public class DbHelper extends SQLiteOpenHelper {
	private Context dbContext;
	public DbHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.dbContext = context;
	}


	// Called when no database exists in
	// disk and the helper class needs
	// to create a new one.
	@Override
	public void onCreate(SQLiteDatabase _db) {
		_db.execSQL(DAO.TIMER_DATABASE_CREATE);
		_db.execSQL(DAO.INTERVAL_DATABASE_CREATE);
		_db.execSQL(DAO.CATEGORY_DATABASE_CREATE);
		
		List<TimerVO> timerList;
		try {
			timerList = ActivityUtil.getDefaultTimers(dbContext);
			Set<String> categorySet = new HashSet<String>();
			for (TimerVO timerVO : timerList) {
				if (!categorySet.contains(timerVO.getCategory())) {
					insertCategory(_db,timerVO.getCategory());
					categorySet.add(timerVO.getCategory());
				}
				insertTimer(_db,timerVO);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	// Called when there is a database version mismatch meaning that
	// the version of the database on disk needs to be upgraded to
	// the current version.
	@Override
	public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
			int _newVersion) {
		// Log the version upgrade.
		Log.w("TaskDBAdapter", "Upgrading from version " +
				_oldVersion + " to " +
				_newVersion +
		", which will destroy all old data");

		// Upgrade the existing database to conform to the new version.
		// Multiple previous versions can be handled by comparing
		// _oldVersion and _newVersion values.

		// The simplest case is to drop the old table and create a
		// new one.
		_db.execSQL("DROP TABLE IF EXISTS " + DAO.TIMER_DATABASE_TABLE);
		_db.execSQL("DROP TABLE IF EXISTS " + DAO.INTERVAL_DATABASE_TABLE);
		_db.execSQL("DROP TABLE IF EXISTS " + DAO.CATEGORY_DATABASE_TABLE);
		// Create a new one.
		onCreate(_db);
	}
	
	private long insertCategory(SQLiteDatabase _db,
			String category) {
		ContentValues value = new ContentValues();
		value.put(DAO.CATEGORY_KEY_NAME, category);

		long retVal = _db.insert(DAO.CATEGORY_DATABASE_TABLE, null, value);
		return retVal;
	}
	
	private long insertInterval(SQLiteDatabase _db,
			String timerId, 
			TimerIntervalVO timerIntervalVO) {

		ContentValues value = new ContentValues();
		value.put(DAO.INTERVAL_KEY_NAME, timerIntervalVO.getName());
		value.put(DAO.INTERVAL_KEY_TIMER_ID, timerId);
		value.put(DAO.INTERVAL_KEY_COOK_TIME,timerIntervalVO.getCookTime());

		long retVal = _db.insert(DAO.INTERVAL_DATABASE_TABLE, null, value);
		return retVal;
	}
	
	public long insertTimer(SQLiteDatabase _db,
			TimerVO timerVO) {
		ContentValues value = new ContentValues();
		value.put(DAO.TIMER_KEY_NAME, timerVO.getName());
		value.put(DAO.TIMER_KEY_CATEGORY, timerVO.getCategory());
		value.put(DAO.TIMER_KEY_MEAT_CUT, timerVO.getMeetCut());
		value.put(DAO.TIMER_KEY_COOK_TYPE, timerVO.getCookType());
		value.put(DAO.TIMER_KEY_COOK_TIME,timerVO.getCookTime());
		value.put(DAO.TIMER_KEY_DEFAULT_TIMER, "Y");

		long retVal = _db.insert(DAO.TIMER_DATABASE_TABLE, null, value);

		for (TimerIntervalVO intervalVO : timerVO.getIntervalList()) {
			insertInterval(_db,
					String.valueOf(retVal),
					intervalVO);
		}
		return retVal;
	}
}
