package com.holysmokes.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;


public class DAO {
	private static final int DATABASE_VERSION = 4; 
	public static final String DATABASE_NAME = "holySmokes.db";

	public static final String TIMER_DATABASE_TABLE = "timer";
	public static final String INTERVAL_DATABASE_TABLE = "interval";
	public static final String CATEGORY_DATABASE_TABLE = "category";

	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	// The index (key) column name for use in where clauses.
	public static final String TIMER_KEY_ID="id";
	public static final int TIMER_KEY_COLUMN= 0;
	public static final String TIMER_KEY_NAME="name";
	public static final int TIMER_NAME_COLUMN = 1;
	public static final String TIMER_KEY_CATEGORY="category";
	public static final int TIMER_CATEGORY_COLUMN = 2;
	public static final String TIMER_KEY_MEAT_CUT="meat_cut";
	public static final int TIMER_MEAT_CUT_COLUMN = 3;
	public static final String TIMER_KEY_COOK_TYPE="cook_type";
	public static final int TIMER_COOK_TYPE_COLUMN = 4;
	public static final String TIMER_KEY_COOK_TIME="cook_time";
	public static final int TIMER_COOK_TIME_COLUMN = 5;
	public static final String TIMER_KEY_DEFAULT_TIMER="isdefault";
	public static final int TIMER_DEFAULT_TIMER_COLUMN = 6;

	// The index (key) column name for use in where clauses.
	public static final String INTERVAL_KEY_ID="id";
	public static final int INTERVAL_KEY_COLUMN= 0;
	public static final String INTERVAL_KEY_TIMER_ID="timer_id";
	public static final int INTERVAL_TIMER_ID_COLUMN = 1;
	public static final String INTERVAL_KEY_NAME="name";
	public static final int INTERVAL_NAME_COLUMN = 2;
	public static final String INTERVAL_KEY_COOK_TIME="cook_time";
	public static final int INTERVAL_COOK_TIME_COLUMN = 3;

	// The index (key) column name for use in where clauses.
	public static final String CATEGORY_KEY_ID="id";
	public static final int CATEGORY_KEY_COLUMN= 0;
	public static final String CATEGORY_KEY_NAME="name";
	public static final int CATEGORY_NAME_COLUMN = 1;

	private static final String[] timer_result_columns = new String[] {TIMER_KEY_ID, 
		TIMER_KEY_NAME, 
		TIMER_KEY_CATEGORY, 
		TIMER_KEY_MEAT_CUT, 
		TIMER_KEY_COOK_TYPE,
		TIMER_KEY_COOK_TIME};

	private static final String[] interval_result_columns = new String[] {INTERVAL_KEY_ID, 
		INTERVAL_KEY_TIMER_ID, 
		INTERVAL_KEY_NAME, 
		INTERVAL_KEY_COOK_TIME};

	private static final String[] category_result_columns = new String[] {CATEGORY_KEY_ID, 
		CATEGORY_KEY_NAME};

	// SQL Statement to create a new database.
	public static final String TIMER_DATABASE_CREATE = "create table " +
	TIMER_DATABASE_TABLE + " (" + TIMER_KEY_ID +
	" integer primary key autoincrement, " +
	TIMER_KEY_NAME + " text not null," +
	TIMER_KEY_CATEGORY + " text not null," +
	TIMER_KEY_MEAT_CUT + " text," +
	TIMER_KEY_COOK_TYPE + " text not null," +
	TIMER_KEY_COOK_TIME + " double," +
	TIMER_KEY_DEFAULT_TIMER + " text);";

	public static final String INTERVAL_DATABASE_CREATE = "create table " +
	INTERVAL_DATABASE_TABLE + " (" + INTERVAL_KEY_ID +
	" integer primary key autoincrement, " +
	INTERVAL_KEY_NAME + " text not null," +
	INTERVAL_KEY_TIMER_ID + " integer not null," + 
	INTERVAL_KEY_COOK_TIME + " double);";

	public static final String CATEGORY_DATABASE_CREATE = "create table " +
	CATEGORY_DATABASE_TABLE + " (" + CATEGORY_KEY_ID +
	" integer primary key autoincrement, " +
	CATEGORY_KEY_NAME + " text not null);";

	// Variable to hold the database instance
	private SQLiteDatabase db;

	// Context of the application using the database.
	private final Context context;
	// Database open/upgrade helper
	private DbHelper dbHelper;
	
	public DAO(Context _context) {
		context = _context;
		dbHelper = new DbHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}
	

	public void setDB(SQLiteDatabase db) {
		this.db = db;
	}
	
	public DAO open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (db != null)
			db.close();
	}

	public long insertTimer(TimerVO timerVO) {
		if (db == null || !db.isOpen()) {
			open();
		}

		ContentValues value = new ContentValues();
		value.put(TIMER_KEY_NAME, timerVO.getName());
		value.put(TIMER_KEY_CATEGORY, timerVO.getCategory());
		value.put(TIMER_KEY_MEAT_CUT, timerVO.getMeetCut());
		value.put(TIMER_KEY_COOK_TYPE, timerVO.getCookType());
		value.put(TIMER_KEY_COOK_TIME,timerVO.getCookTime());
		value.put(TIMER_KEY_DEFAULT_TIMER, "N");

		long retVal = db.insert(TIMER_DATABASE_TABLE, null, value);
		close();

		for (TimerIntervalVO intervalVO : timerVO.getIntervalList()) {
			insertInterval(String.valueOf(retVal),intervalVO);
		}
		return retVal;
	}

	public long updateTimer(TimerVO timerVO) {
		if (db == null || !db.isOpen()) {
			open();
		}

		ContentValues value = new ContentValues();
		value.put(TIMER_KEY_NAME, timerVO.getName());
		value.put(TIMER_KEY_CATEGORY, timerVO.getCategory());
		value.put(TIMER_KEY_MEAT_CUT, timerVO.getMeetCut());
		value.put(TIMER_KEY_COOK_TYPE, timerVO.getCookType());
		value.put(TIMER_KEY_COOK_TIME,timerVO.getCookTime());

		long retVal = db.update(TIMER_DATABASE_TABLE, value, "id=" + timerVO.getId(),null);
		close();

		//		for (TimerIntervalVO intervalVO : timerVO.getIntervalList()) {
		//			insertInterval(String.valueOf(retVal),intervalVO);
		//		}
		return retVal;
	}

	public long insertInterval(String timerId, 
			TimerIntervalVO timerIntervalVO) {
		if (db == null || !db.isOpen()) {
			open();
		}

		ContentValues value = new ContentValues();
		value.put(INTERVAL_KEY_NAME, timerIntervalVO.getName());
		value.put(INTERVAL_KEY_TIMER_ID, timerId);
		value.put(INTERVAL_KEY_COOK_TIME,timerIntervalVO.getCookTime());

		long retVal = db.insert(INTERVAL_DATABASE_TABLE, null, value);
		close();
		return retVal;
	}

	public long insertCategory(String category) {
		if (db == null || !db.isOpen()) {
			open();
		}

		ContentValues value = new ContentValues();
		value.put(CATEGORY_KEY_NAME, category);

		long retVal = db.insert(CATEGORY_DATABASE_TABLE, null, value);
		close();
		return retVal;
	}

	public boolean deleteTimer(long _rowIndex) {
		if (db == null || !db.isOpen()) {
			open();
		}
		boolean retVal =  db.delete(TIMER_DATABASE_TABLE, TIMER_KEY_ID +
				"=" + _rowIndex, null) > 0;

				close();
				return retVal;
	}

	public boolean deleteInterval(long _rowIndex) {
		if (db == null || !db.isOpen()) {
			open();
		}
		boolean retVal =  db.delete(INTERVAL_DATABASE_TABLE, INTERVAL_KEY_ID +
				"=" + _rowIndex, null) > 0;

				close();
				return retVal;
	}

	public boolean deleteCategory(String category) throws Exception {
		List<TimerVO> timerList = getTimerByCategory(category);
		if (!timerList.isEmpty()) {
			throw new Exception ("Can not delete category that has timers associated with it.  Please delete the timers first then delete the category.");
		}
		
		if (db == null || !db.isOpen()) {
			open();
		}
		
		boolean retVal = false;
		try {
			retVal =  db.delete(CATEGORY_DATABASE_TABLE, CATEGORY_KEY_NAME +
					"='" + category + "'", null) > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		close();
		return retVal;
	}

	public List<TimerVO> getAllTimers() {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				null, null, null, null, TIMER_KEY_CATEGORY);

		List<TimerVO> resultList = new ArrayList<TimerVO>();
		if(cursor.moveToFirst()) {
			do {
				TimerVO timerVO = createTimerVO(cursor);
				List <TimerIntervalVO> intervalList = getIntervalsTimers(timerVO.getId());

				if (intervalList != null && !intervalList.isEmpty()) {
					timerVO.setIntervalList(intervalList);
				}
				resultList.add(timerVO);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return resultList;
	}

	/**
	 * Get all the timers...add an extra timer for categories
	 * @return
	 */
	public List<TimerVO> getAllTimersCategory() {
		if (db == null || !db.isOpen()) {
			open();
		}
		Set<String> categorySet = new HashSet<String>();
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				null, null, null, null, TIMER_KEY_CATEGORY);

		List<TimerVO> resultList = new ArrayList<TimerVO>();
		if(cursor.moveToFirst()) {
			do {
				TimerVO timerVO = createTimerVO(cursor);
				List <TimerIntervalVO> intervalList = getIntervalsTimers(timerVO.getId());

				if (intervalList != null && !intervalList.isEmpty()) {
					timerVO.setIntervalList(intervalList);
				}

//				if (!categorySet.contains(timerVO.getCategory())) {
//					TimerVO categoryTimerVO = new TimerVO();
//					categoryTimerVO.setCategory(timerVO.getCategory());
//					categoryTimerVO.setIsCategory(true);
//					categorySet.add(timerVO.getCategory());
//					resultList.add(categoryTimerVO);
//				}
				resultList.add(timerVO);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return resultList;
	}

	public List<TimerIntervalVO> getIntervalsTimers(int timerID) {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(INTERVAL_DATABASE_TABLE, interval_result_columns,
				INTERVAL_KEY_TIMER_ID + "=" + timerID, null, null, null, null);

		List<TimerIntervalVO> resultList = new ArrayList<TimerIntervalVO>();
		if(cursor.moveToFirst()) {
			do {
				resultList.add(createIntervalVO(cursor));
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return resultList;
	}

	public List<String> getAllCategories() {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(CATEGORY_DATABASE_TABLE, category_result_columns,
				null, null, null, null, null);

		List<String> resultList = new ArrayList<String>();
		if(cursor.moveToFirst()) {
			do {
				resultList.add(cursor.getString(CATEGORY_NAME_COLUMN));
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return resultList;
	}

	public Map<String,List<TimerVO>> getCategoryTimerMap() {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				null, null, null, null, TIMER_KEY_CATEGORY);

		Map<String,List<TimerVO>> resultMap = new HashMap<String,List<TimerVO>>();
		if(cursor.moveToFirst()) {
			do {
				TimerVO timerVO = createTimerVO(cursor);
				List <TimerIntervalVO> intervalList = getIntervalsTimers(timerVO.getId());

				if (intervalList != null && !intervalList.isEmpty()) {
					timerVO.setIntervalList(intervalList);
				}
				List<TimerVO> timerList = resultMap.get(timerVO.getCategory());
				if (timerList == null) {
					timerList = new ArrayList<TimerVO>();
					resultMap.put(timerVO.getCategory(), timerList);
				}
				timerList.add(timerVO);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return resultMap;
	}

	public List<TimerVO> getTimerList() {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				null, null, null, null, TIMER_KEY_CATEGORY + "," + TIMER_KEY_NAME + "," + TIMER_KEY_MEAT_CUT + " DESC");

		List<TimerVO> resultList = new ArrayList<TimerVO>();
		if(cursor.moveToFirst()) {
			do {
				TimerVO timerVO = createTimerVO(cursor);
				List <TimerIntervalVO> intervalList = getIntervalsTimers(timerVO.getId());

				if (intervalList != null && !intervalList.isEmpty()) {
					timerVO.setIntervalList(intervalList);
				}
				resultList.add(timerVO);

			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return resultList;
	}

	public List<TimerVO> getTimerByCategory(String category) {
		if (db == null || !db.isOpen()) {
			open();
		}
		
		List<TimerVO> timerList = new ArrayList<TimerVO>();
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				TIMER_KEY_CATEGORY + "='" + category + "'", null, null, null, null);

		if(cursor.moveToFirst()) {
			do {
				TimerVO timerVO = createTimerVO(cursor);
				List <TimerIntervalVO> intervalList = getIntervalsTimers(timerVO.getId());

				if (intervalList != null && !intervalList.isEmpty()) {
					timerVO.setIntervalList(intervalList);
				}


				timerList.add(timerVO);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return timerList;
	}
	
	public TimerVO getTimer(long id) {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				TIMER_KEY_ID + "=" + id, null, null, null, null);
		TimerVO TimerVO = null;
		if(cursor.moveToFirst()) {
			TimerVO = createTimerVO(cursor);
		}
		cursor.close();
		db.close();
		return TimerVO;
	}

	public TimerVO getInterval(long id) {
		if (db == null || !db.isOpen()) {
			open();
		}
		Cursor cursor =  db.query(TIMER_DATABASE_TABLE, timer_result_columns,
				TIMER_KEY_ID + "=" + id, null, null, null, null);
		TimerVO TimerVO = null;
		if(cursor.moveToFirst()) {
			TimerVO = createTimerVO(cursor);
		}
		cursor.close();
		db.close();
		return TimerVO;
	}

	public void initDB() {
		insertCategory("BEEF");
		insertCategory("CHICKEN");
		insertCategory("VEGETABLES");

		List<TimerVO> timerList = new ArrayList<TimerVO>();
		TimerVO timerCatVO = new TimerVO();
		timerCatVO.setCategory("BEEF");
		timerCatVO.setIsCategory(true);
		timerList.add(timerCatVO);
		//
		TimerVO timerVO = new TimerVO();
		timerVO.setCategory("BEEF");
		timerVO.setCookTime(25);
		timerVO.setName("London Broil");
		timerVO.setMeetCut("4 inches thick");
		timerVO.setCookType("Direct");
		timerVO.setId(1);
		timerList.add(timerVO);
		//		
		TimerVO timerVO2 = new TimerVO();
		timerVO2.setCategory("BEEF");
		timerVO2.setCookTime(150);
		timerVO2.setName("Ribs");
		timerVO2.setMeetCut("4 LBS");
		timerVO2.setCookType("Indirect");
		timerVO2.setId(2);
		//		
		List<TimerIntervalVO> intervalList = new ArrayList<TimerIntervalVO>();
		TimerIntervalVO interval1 = new TimerIntervalVO();
		interval1.setCookTime(120);
		interval1.setName("Smoke");
		intervalList.add(interval1);

		TimerIntervalVO interval2 = new TimerIntervalVO();
		interval2.setCookTime(15);
		interval2.setName("Mop Sauce Top");
		intervalList.add(interval2);
		//		
		TimerIntervalVO interval3 = new TimerIntervalVO();
		interval3.setCookTime(15);
		interval3.setName("Mop Sauce Bottom");
		intervalList.add(interval3);
		timerVO2.setIntervalList(intervalList);
		timerList.add(timerVO2);

		timerCatVO = new TimerVO();
		timerCatVO.setCategory("CHICKEN");
		timerCatVO.setIsCategory(true);
		timerList.add(timerCatVO);

		TimerVO timerVO3 = new TimerVO();
		timerVO3.setCategory("CHICKEN");
		timerVO3.setCookTime(25);
		timerVO3.setName("Chicken Breast");
		timerVO3.setMeetCut("4 inches thick");
		timerVO3.setCookType("Direct");
		timerVO3.setId(3);
		timerList.add(timerVO3);

		insertTimer(timerVO);
		insertTimer(timerVO2);
		insertTimer(timerVO3);
	}

	private TimerVO createTimerVO(Cursor cursor) {
		TimerVO timerVO = new TimerVO();
		timerVO.setId(cursor.getInt(TIMER_KEY_COLUMN));
		timerVO.setName(cursor.getString(TIMER_NAME_COLUMN));
		timerVO.setCategory(cursor.getString(TIMER_CATEGORY_COLUMN));
		timerVO.setCookTime(cursor.getInt(TIMER_COOK_TIME_COLUMN));
		timerVO.setCookType(cursor.getString(TIMER_COOK_TYPE_COLUMN));
		
		String cut = cursor.getString(TIMER_MEAT_CUT_COLUMN);
		if (cut != null && cut.trim().length() > 0)
			timerVO.setMeetCut(cut);

		return timerVO;
	}

	private TimerIntervalVO createIntervalVO(Cursor cursor) {
		TimerIntervalVO intervalVO = new TimerIntervalVO();
		intervalVO.setId(cursor.getInt(INTERVAL_KEY_COLUMN));
		intervalVO.setName(cursor.getString(INTERVAL_NAME_COLUMN));
		intervalVO.setCookTime(cursor.getInt(INTERVAL_COOK_TIME_COLUMN));

		return intervalVO;
	}



}
