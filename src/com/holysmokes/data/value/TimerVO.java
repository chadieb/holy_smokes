package com.holysmokes.data.value;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class TimerVO implements Parcelable {
	private int id;
	private String name;
	private String category;
	private String meetCut;
	private String cookType; //direct or indirect
	private int cookTime;
	private String defaultTimer;
	private boolean isCategory;

	private List<TimerIntervalVO> intervalList = new ArrayList<TimerIntervalVO>();


	public static final Parcelable.Creator<TimerVO> CREATOR =
		new Parcelable.Creator<TimerVO>() {
		public TimerVO createFromParcel(Parcel in) {
			return new TimerVO(in);
		}

		public TimerVO[] newArray(int size) {
			return new TimerVO[size];
		}
	};


	private TimerVO(Parcel in) {
		readFromParcel(in);
	}

	public TimerVO() {
		
	}
	public String getDefaultTimer() {
		return defaultTimer;
	}

	public void setDefaultTimer(String defaultTimer) {
		this.defaultTimer = defaultTimer;
	}
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeString(category);
		out.writeString(meetCut);
		out.writeString(cookType);
		out.writeInt(cookTime);
		out.writeInt(isCategory ? 1 : 0);
		out.writeTypedList(intervalList);
	}

	public void readFromParcel(Parcel in) {
		id = in.readInt();
		name = in.readString();
		category = in.readString();
		meetCut = in.readString();
		cookType = in.readString();
		cookTime = in.readInt();
		isCategory = in.readInt() == 1;
		in.readTypedList(intervalList, TimerIntervalVO.CREATOR);
	}


	public boolean isCategory() {
		return isCategory;
	}
	public void setIsCategory(boolean isCategory) {
		this.isCategory = isCategory;
	}
	public String getCookType() {
		return cookType;
	}
	public void setCookType(String cookType) {
		this.cookType = cookType;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMeetCut() {
		return meetCut;
	}
	public void setMeetCut(String meetCut) {
		this.meetCut = meetCut;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getCookTime() {
		return cookTime;
	}
	public void setCookTime(int cookTime) {
		this.cookTime = cookTime;
	}
	public List<TimerIntervalVO> getIntervalList() {
		return intervalList;
	}
	public void setIntervalList(List<TimerIntervalVO> intervalList) {
		this.intervalList = intervalList;
	}


	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

}
