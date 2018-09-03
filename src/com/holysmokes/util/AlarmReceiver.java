package com.holysmokes.util;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	public static String TIMER_NAME_PARM="timerName";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			
			String timerName = "";
			if (bundle != null) {
				timerName = bundle.getString(TIMER_NAME_PARM);
			}

			NotificationManager notifier = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            ActivityUtil.sendNotification(notifier, context, timerName + " has completed.");
            
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}
