package com.holysmokes.data.value;

import android.os.Parcel;
import android.os.Parcelable;

public class TimerIntervalVO implements Parcelable{
	private int id;
	private String name;
	private int cookTime;
	private boolean notifcationSent;
	
	public static final Parcelable.Creator<TimerIntervalVO> CREATOR =
		new Parcelable.Creator<TimerIntervalVO>() {
		public TimerIntervalVO createFromParcel(Parcel in) {
			return new TimerIntervalVO(in);
		}

		public TimerIntervalVO[] newArray(int size) {
			return new TimerIntervalVO[size];
		}
	};

	private TimerIntervalVO(Parcel in) {
		readFromParcel(in);
	}
	
	public TimerIntervalVO() {
	}
	
	public boolean isNotifcationSent() {
		return notifcationSent;
	}

	public void setNotifcationSent(boolean notifcationSent) {
		this.notifcationSent = notifcationSent;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	


	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
		out.writeInt(cookTime);
		out.writeString(String.valueOf(notifcationSent));
	}
	
	public void readFromParcel(Parcel in) {
		id = in.readInt();
		name = in.readString();
		cookTime = in.readInt();
		notifcationSent = Boolean.getBoolean(in.readString());
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
	public int getCookTime() {
		return cookTime;
	}
	public void setCookTime(int cookTime) {
		this.cookTime = cookTime;
	}
	
	public void reset() {
		notifcationSent = false;
	}

}
