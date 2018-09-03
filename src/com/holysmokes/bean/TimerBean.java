package com.holysmokes.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.util.ActivityUtil;
import com.holysmokes.view.TimerView;

public class TimerBean implements Parcelable {
	private int id;
	private int state;
	private String viewTag;
	private int pauseTimeLeft = 0;
	private TimerVO timerVO = null;
	private int totalTime;
	private int intervalTime;
	private Date startTime;

	private List<TimerIntervalVO> startIntervalList = new ArrayList<TimerIntervalVO>();
	boolean showInterval;
	
	private TimerBean(Parcel in) {
		id = in.readInt();
		state = in.readInt();
		viewTag = in.readString();
		pauseTimeLeft = in.readInt();
		timerVO = in.readParcelable(TimerVO.class.getClassLoader());	
		totalTime = in.readInt();
		startTime = new Date(in.readLong());
		startIntervalList = in.readArrayList(TimerIntervalVO.class.getClassLoader());
		showInterval = Boolean.parseBoolean(in.readString());
	}

	public TimerBean() {
	}
	
	@Override
	public String toString() {
		return " id:"+id + " name:" + timerVO.getName() + " state" + state; 
	}
	
	public int getIntervalTime() {
		return intervalTime;
	}

	
	public int getPauseTimeLeft() {
		return pauseTimeLeft;
	}

	public void setPauseTimeLeft(int pauseTimeLeft) {
		this.pauseTimeLeft = pauseTimeLeft;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public static final Parcelable.Creator<TimerBean> CREATOR = new Parcelable.Creator<TimerBean>() {
		public TimerBean createFromParcel(Parcel in) {
			return new TimerBean(in);
		}

		public TimerBean[] newArray(int size) {
			return new TimerBean[size];
		}
	};
	

	public Date getStartTime() {
		return startTime;
	}


	public List<TimerIntervalVO> getStartIntervalList() {
		return startIntervalList;
	}

	public void setStartIntervalList(List<TimerIntervalVO> startIntervalList) {
		this.startIntervalList = startIntervalList;
	}

	public boolean isShowInterval() {
		return showInterval;
	}

	public void setShowInterval(boolean showInterval) {
		this.showInterval = showInterval;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}
	public int getTimepassed() {
		Date currentTime = new Date();
		long timeLeft = (currentTime.getTime() - getStartTime().getTime()) / 1000; //inseconds
		return (int) timeLeft;
	}


	public String getViewTag() {
		return viewTag;
	}

	public void setViewTag(String viewTag) {
		this.viewTag = viewTag;
	}

	public TimerVO getTimerVO() {
		return timerVO;
	}

	public void setTimerVO(TimerVO timerVO) {
		this.totalTime = timerVO.getCookTime();
		this.startIntervalList.addAll(timerVO.getIntervalList());
		this.timerVO = timerVO;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void reset() {
		state = TimerView.STATE_BEGIN;
		startTime = new Date();
		
		for (TimerIntervalVO interval :startIntervalList) {
			interval.reset();
		}
	}

	public void toggleState() {
		switch (state) {
		case TimerView.STATE_BEGIN :
			state = TimerView.STATE_RUN;
			break;
		case TimerView.STATE_RUN :
			state = TimerView.STATE_PAUSE;
			break;
		case TimerView.STATE_PAUSE :
			state = TimerView.STATE_RUN;
			break;
		}
	}

	public int getState() {
		return state;
	}


	public int describeContents() {
		return 0;
	}

	public TimerIntervalVO getInterval(Context context,
			TextView textView) {
		TimerIntervalVO returnVal = null;
		if (!startIntervalList.isEmpty() && showInterval) {
			
			int intervalSeconds = 0;
			int cookTimeSeconds = getTimepassed();
			for (TimerIntervalVO intervalVO : startIntervalList) {
				intervalSeconds += intervalVO.getCookTime() * 60;
				if (cookTimeSeconds < intervalSeconds) {
					returnVal = intervalVO;
					intervalTime = intervalSeconds - cookTimeSeconds;
					break;
				}
				else {
					if (!intervalVO.isNotifcationSent() && context != null) {
						ActivityUtil.sendNotification((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), 
								context, 
								intervalVO.getName() + " has completed.");
						intervalVO.setNotifcationSent(true);
					}
				}
			}
		}
		String labelText = returnVal == null ? timerVO.getName() : returnVal.getName();
		if (textView != null &&  !textView.getText().equals(labelText)) {
			textView.setText(labelText);
			textView.invalidate();
		}
		return returnVal;
	}

	public int calculateMaxIntervalTime() {
		int maxIntervalTime = totalTime;
		for (TimerIntervalVO interval : startIntervalList) {
			maxIntervalTime -= interval.getCookTime();
		}
		return maxIntervalTime;
	}
	
	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(id);
		out.writeInt(state);
		out.writeString(viewTag);
		out.writeInt(pauseTimeLeft);
		out.writeParcelable(timerVO, 0);
		out.writeInt(totalTime);
		out.writeLong(startTime.getTime());	
		out.writeList(startIntervalList);
		out.writeString(String.valueOf(showInterval));
	}

	public void readFromParcel(Parcel in) {
		id = in.readInt();
		state = in.readInt();
		viewTag = in.readString();
		pauseTimeLeft = in.readInt();
		timerVO = in.readParcelable(null);
		totalTime = in.readInt();
		startTime = new Date(in.readLong());
		startIntervalList = in.readArrayList(startIntervalList.getClass().getClassLoader());
		showInterval = Boolean.parseBoolean(in.readString());
	}
}
