package com.holysmokes.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.holysmokes.view.TimerView;

public 	class CountDownRunner implements Runnable{
	private String viewTag = null;
	private Handler viewUpdateHandler = null;
	TimerView view = null;
	public CountDownRunner(String viewTag, Handler viewUpdateHandler) {
		this.viewTag = viewTag;
		this.viewUpdateHandler = viewUpdateHandler;
	}
	
	public CountDownRunner(TimerView view) {
		this.view = view;
	}
	
	public void setView(TimerView view) {
		this.view = view;
	}
	
	public void run() {
		while(!Thread.currentThread().isInterrupted()){
			
//			Message m = viewUpdateHandler.obtainMessage();
//			Bundle b = new Bundle();
//			b.putString("viewTag", viewTag);
//			m.setData(b);
//			viewUpdateHandler.sendMessage(m);
			view.postInvalidate();

			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
