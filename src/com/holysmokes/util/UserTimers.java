package com.holysmokes.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.widget.ExpandableListView;

import com.holysmokes.BbqListActivity;
import com.holysmokes.R;
import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.view.ExpandableListAdapter;
import com.holysmokes.view.TimerView;

public class UserTimers {
	private static UserTimers instance = new UserTimers();
	
	private int timerId = 0;
	private Map<String, TimerBean> timerMap = new HashMap<String, TimerBean>();
	
	private UserTimers() {
		
	}
	
	public static UserTimers getInstance() {
		return instance;
	}
	
	public void addTimer(TimerBean timer) {
		timerMap.put(timer.getViewTag(),timer);
	}
	
	public void deleteTimer(TimerBean timer) {
		timerMap.remove(timer.getViewTag());
	}
	
	public void clear() {
		timerMap.clear();
	}

	public TimerBean getTimer(String viewTag) {
		return timerMap.get(viewTag);
	}
	
 	public Map<String, TimerBean> getTimerMap() {
		return timerMap;
	}

	public void setTimerMap(Map<String, TimerBean> timerMap) {
		this.timerMap = timerMap;
	}
	
	public void createTimer(TimerVO timerVO, 
			BbqListActivity activity,
			Date startTime) {
		
		String viewTag = "TimerView" + timerId;


		TimerBean timer = new TimerBean();
		timer.setId(timerId++);
		timer.setViewTag(viewTag);
		timer.setTimerVO(timerVO);
		timer.setStartTime(startTime);
		timer.setStartIntervalList(timerVO.getIntervalList());
		
		if (!timerVO.getIntervalList().isEmpty()) {
			timer.setShowInterval(true);
		}
		
		addTimerBean(timer, activity);
	}
	
	public void addTimerBean(TimerBean timer, 
			BbqListActivity activity) {
//		timer.setHandler(new TimerHandler(activity));
		ExpandableListAdapter timerItemAdapter = activity.getTimerItemAdapter();
		
		if (timerMap.get(timer.getViewTag()) == null) {
			timerMap.put(timer.getViewTag(), timer);

			timerItemAdapter.addItem(timer);
			timerItemAdapter.notifyDataSetChanged();
		}
	}
	
	public void recreateTimers(BbqListActivity activity) {
		Iterator<TimerBean> it = timerMap.values().iterator();
		ExpandableListAdapter adapter = activity.getTimerItemAdapter();
		while (it.hasNext()) {
			TimerBean bean = it.next();

			
			adapter.addItem(bean);
			adapter.notifyDataSetChanged();
			
			restartTimer(bean,
					activity);
		}
	}
	
	public void restartTimer(TimerBean bean,
			BbqListActivity activity) {
//		if (bean.getHandler() == null) {
//			bean.setHandler(new TimerHandler(activity));
//		}
		
		if (bean.getState() == TimerView.STATE_RUN) {
//			Thread myRefreshThread = bean.getThread();
			
//			if (myRefreshThread != null) {
//				myRefreshThread.interrupt();
//				myRefreshThread = null;
//				bean.setThread(null);
//			}
			
			ExpandableListView listView = (ExpandableListView) activity.findViewById(R.id.TimerListView);
			
			TimerView timerView = (TimerView) listView.findViewWithTag(bean.getViewTag());
			
			
			if (timerView != null) {
//				if (bean.getThread() != null) {
//					bean.getThread().interrupt();
//					myRefreshThread = new Thread(new CountDownRunner(timerView));
//					myRefreshThread.start();
//					bean.setThread(myRefreshThread);
//				}
			}
		}
	}
	
	public boolean hasTimers() {
		return !timerMap.isEmpty();
	}
}
