package com.holysmokes.util;

import android.view.View;
import android.widget.ListView;

import com.holysmokes.bean.TimerBean;
import com.holysmokes.view.TimerView;

public class TimerClickListener implements View.OnClickListener {

	private TimerView view = null;
	ListView timerListView = null;
	private String viewTag = null;

	public TimerClickListener(ListView timerListView, 
			String viewTag) {
		this.viewTag = viewTag;
		this.timerListView = timerListView;
	}
	
	public void onClick(View arg0) {
		view = (TimerView) timerListView.findViewWithTag(viewTag);
		TimerBean bean = UserTimers.getInstance().getTimer(viewTag);
		
		bean.toggleState();
//		Thread myRefreshThread = bean.getThread();
//		if (myRefreshThread == null) {
//			myRefreshThread = new Thread(new CountDownRunner(viewTag,bean.getHandler()));
//			myRefreshThread.start();
//			bean.setThread(myRefreshThread);
//		}
//		else {
//			myRefreshThread.interrupt();
//			view.invalidate();
//			myRefreshThread = null;
//			bean.setThread(null);
//		}
	}	
}
