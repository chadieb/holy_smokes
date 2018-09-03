package com.holysmokes.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.widget.Spinner;

import com.holysmokes.BbqListActivity;
import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.view.TimerView;

public class ActivityUtil {
	private static int viewId= 0x7f070000;
	private static final int HELLO_ID = 1;

	/**
	 * Create a unique id for a view
	 * @return
	 */
	public static int getViewId() {
		viewId += 0x00000001;
		return viewId;
	}

	public static void serializeTimers(BbqListActivity activity) throws IOException {
		boolean hasRunningTimers = false;
		
		UserTimers userTimers = UserTimers.getInstance();
		Iterator <TimerBean> beans = userTimers.getTimerMap().values().iterator();
		while (beans.hasNext()) {
			TimerBean bean = beans.next();
			if (bean.getState() != TimerView.STATE_COMPLETE) {
				hasRunningTimers = true;
			}
		}
		
		if (hasRunningTimers == false) {
			return;
		}
		
		File fileDir = activity.getCacheDir();
		String file = fileDir.getAbsolutePath() + "/timers.ser";
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(file)));

		
		beans = userTimers.getTimerMap().values().iterator();
		dos.writeInt(userTimers.getTimerMap().size());
		Parcel p1 = Parcel.obtain();
		while (beans.hasNext()) {
			TimerBean bean = beans.next();
			if (bean.getState() != TimerView.STATE_COMPLETE) {
				p1.writeValue(bean);
				byte[] byteArray = p1.marshall();

				dos.writeInt(byteArray.length);
				dos.write(byteArray);
			}
		}
		dos.flush();
		dos.close();
	}

	/**
	 * @param activity
	 * @throws Exception
	 */
	public static List<TimerVO> getDefaultTimers(Context context) throws Exception {
		List<TimerVO> timerList = new ArrayList<TimerVO>();
		DataInputStream in = new DataInputStream(context.getResources().openRawResource(com.holysmokes.R.raw.cook_times));
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		
		while ((line = reader.readLine()) != null) {
			String[] lineArray = line.split(",");
			
			
			TimerVO timerVO = new TimerVO();
			
			timerVO.setCategory(lineArray[0]);
			timerVO.setName(lineArray[1]);
			timerVO.setMeetCut(lineArray[2]);
			timerVO.setDefaultTimer(lineArray[3]);
			timerVO.setCookType(lineArray[4].toUpperCase());
			
			try {
				timerVO.setCookTime(Integer.parseInt(lineArray[5]));
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
			//handle intervals
			if (lineArray.length > 6) {
				List<TimerIntervalVO> list = new ArrayList<TimerIntervalVO>();
				for (int i=6;i<lineArray.length;i+=2) {
					TimerIntervalVO intervalVO = new TimerIntervalVO();
					intervalVO.setName(lineArray[i]);
					intervalVO.setCookTime(Integer.parseInt(lineArray[i+1]));
					list.add(intervalVO);
				}
				timerVO.setIntervalList(list);
			}
			timerList.add(timerVO);
		}
		
		return timerList;
	}
	
	public static void deserializeTimers(BbqListActivity activity)  {
		UserTimers userTimers = UserTimers.getInstance();

		File fileDir = activity.getCacheDir();
		String file = fileDir.getAbsolutePath() + "/timers.ser";

		File timerFile = new File(file);

		if (!timerFile.exists()) {
			return;
		}

		try {
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int size = input.readInt();
			Logger.log(ActivityUtil.class, "deserializeTimers " + size + " timers");

			Parcel p2 = Parcel.obtain();

			for (int i=0;i<size;i++) {
				int byteLen = input.readInt();
				byte[] bytes = new byte[byteLen];
				input.read(bytes, 0, bytes.length);

				p2.unmarshall(bytes, 0, bytes.length);
				p2.setDataPosition(0);
				TimerBean timer = (TimerBean) p2.readValue(TimerBean.class.getClassLoader());

				userTimers.addTimerBean(timer,
						activity);

				cancelAlarm(activity,timer);
			}
		}
		catch (Exception ex) {
			Log.e("deserializeTimers", "failed to deserialze timers",ex);
		}
		finally {
			timerFile.delete();
		}
	}

	public static void setAlarm(BbqListActivity activity,
			TimerBean timerBean) {
		
		
		int timePassed = timerBean.getTimepassed();
		int timeLeft = (timerBean.getTotalTime()*60)-timePassed;
		setAlarm(activity, timerBean.getViewTag(),timerBean.getTimerVO().getName(),timeLeft);
		
		int intervalSeconds = 0;
		for (TimerIntervalVO intervalVO : timerBean.getStartIntervalList()) {
			
			if (!intervalVO.isNotifcationSent()) {
				intervalSeconds += intervalVO.getCookTime()*60;

				if (timePassed < intervalSeconds) {
					setAlarm(activity, intervalVO.getName() + intervalVO.getId(), 
							intervalVO.getName(), intervalSeconds - timePassed);
				}
			}
		}

	}
	
	public static void setAlarm(BbqListActivity activity,
			String timerId,
			String timerName,
			int timeLeft) {
		
        Intent intent = new Intent(activity, AlarmReceiver.class);
        intent.setData(Uri.parse("custom://" + timerId));
        intent.setAction(timerId);
        intent.putExtra(AlarmReceiver.TIMER_NAME_PARM, timerName);
        
        PendingIntent sender = PendingIntent.getBroadcast(activity,0, intent,0);

        // We want the alarm to go off 30 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
		
		calendar.add(Calendar.SECOND,timeLeft);
		
        // Schedule the alarm!
		AlarmManager am = (AlarmManager) activity.getSystemService(BbqListActivity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}
	
	public static void cancelAlarm(BbqListActivity activity,
			TimerBean timerBean) {
		
		cancelAlarm(activity, timerBean.getViewTag(), timerBean.getTimerVO().getName());
		
		TimerIntervalVO currentInterval = timerBean.getInterval(null, null);
		boolean timerFound = false;
		for (TimerIntervalVO intervalVO : timerBean.getStartIntervalList()) {
			if (currentInterval == null)
				intervalVO.setNotifcationSent(true);
			
			if (currentInterval != null && !timerFound && timerBean.getState() != TimerView.STATE_COMPLETE) {
				if (currentInterval.getId() == intervalVO.getId()) {
					timerFound = true;
				}
				else {
					intervalVO.setNotifcationSent(true);
				}
			}
			cancelAlarm(activity, intervalVO.getName() + intervalVO.getId(), intervalVO.getName());
		}

	}
	
	public static void cancelAlarm(BbqListActivity activity,
			String id,
			String timerName) {
		
        Intent intent = new Intent(activity, AlarmReceiver.class);
        intent.setData(Uri.parse("custom://" + timerName));
        intent.setAction(timerName);
        
        PendingIntent sender = PendingIntent.getBroadcast(activity,0, intent, 0);
		
        // Schedule the alarm!
		AlarmManager am = (AlarmManager) activity.getSystemService(BbqListActivity.ALARM_SERVICE);
        am.cancel(sender);
	}
	
	public static void sendNotification(NotificationManager mNotificationManager,
			Context context,
			String msg) {
		
		int icon = android.R.drawable.ic_lock_idle_alarm;
		CharSequence tickerText = msg;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = "Timer Notification";
		Intent notificationIntent = new Intent(context, BbqListActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, tickerText, contentIntent);
		notification.defaults = Notification.DEFAULT_ALL;
		
		mNotificationManager.notify(HELLO_ID, notification);
	}
	
	public static String createTimeStr(int cookTimeVal) {
		int hours = cookTimeVal / 60;
		int minutes = cookTimeVal - (hours * 60);
		String timeStr = hours + " hr " + minutes + " min ";
		if (hours == 0) {
			timeStr = minutes + " min ";
		}
		
		return timeStr;
	}
	
	public static void setSpinnerSelection(Spinner categorySpinner,
			String selection) {
		for (int i=0;i<categorySpinner.getAdapter().getCount();i++) {
			String categoryName = (String) categorySpinner.getAdapter().getItem(i);
			if (categoryName.equals(selection)) {
				categorySpinner.setSelection(i);
				break;
			}
		}
	}
}
