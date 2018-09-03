package com.holysmokes.util;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.holysmokes.BbqListActivity;
import com.holysmokes.R;
import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.view.TimerView;

public class TimerHandler extends Handler {
	private ListView timerListView = null;
	private NotificationManager mNotificationManager = null;
	private BbqListActivity activity = null;

	public TimerHandler(BbqListActivity bbqListActivity) {
		this.timerListView = (ListView) bbqListActivity.findViewById(R.id.TimerListView);
		this.mNotificationManager = (NotificationManager) bbqListActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		this.activity = bbqListActivity;
		

	}
	public void handleMessage(Message msg) {
		String viewTag = msg.getData().getString("viewTag");  
		TimerView timer = (TimerView) timerListView.findViewWithTag(viewTag);
		TimerBean bean = UserTimers.getInstance().getTimer(viewTag);

		if (timer != null && bean != null) {
//			bean.updateTimepassed();
			timer.invalidate();
			
			if (bean.getState() == TimerView.STATE_COMPLETE){
//				Thread myThread = bean.getThread();
//				if (myThread != null) {
//					myThread.interrupt();
//					myThread = null;
//					bean.setThread(null);
//				}
				Logger.log(this, "Timer " + bean.getId() + " " + bean.getTimerVO().getName() + " has completed");
				ActivityUtil.sendNotification(mNotificationManager, activity, bean.getTimerVO().getName() + " has completed.");
			}
			
			//handle when an interval has completed
			if (bean.isShowInterval()) {
				ExpandableListView timerListView = (ExpandableListView) activity.findViewById(R.id.TimerListView);
				TextView cookTypeView = (TextView) timerListView.findViewWithTag("TimerView" + bean.getId() + "CookType");
				TimerIntervalVO intervalVO = bean.getInterval(activity,null);
				if (intervalVO == null) {
					TextView cookTimeView = (TextView) timerListView.findViewWithTag("CookTime" + bean.getId());
					cookTimeView.setText(ActivityUtil.createTimeStr(bean.getTotalTime()));
					cookTypeView.setText(bean.getTimerVO().getName());				
				}
				else if (!cookTypeView.getText().toString().equals(intervalVO.getName())) {
					TextView cookTimeView = (TextView) timerListView.findViewWithTag("CookTime" + bean.getId());
					cookTimeView.setText(ActivityUtil.createTimeStr(intervalVO.getCookTime()));
					cookTypeView.setText(intervalVO.getName());
				}
				
				
			}

			super.handleMessage(msg);
		}
	}
}
