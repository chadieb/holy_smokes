package com.holysmokes;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.util.ActivityUtil;
import com.holysmokes.util.Logger;
import com.holysmokes.util.NotificationService;
import com.holysmokes.util.UserTimers;
import com.holysmokes.view.ExpandableListAdapter;
import com.holysmokes.view.TimerCreateItemAdapater;
import com.holysmokes.view.TimerView;


public class BbqListActivity extends Activity {
	private static final String MY_AD_UNIT_ID="TEST";
	private static final String ROTATION_CHANGE="change";
	private static final int DIALOG_UPDATE_ID = 0;
	private static final int DIALOG_INSTANT_ID = 1;
	private static final int DIALOG_WELCOME_ID = 2;
	private static final int DIALOG_LOG_ID = 3;
	
	private static final String TIMER_TAG_PREFIX = "TimerView";
	
	/** Called when the activity is first created. */
	protected static final int DEFAULTSECONDS = 5 * 2;

	private ExpandableListAdapter timerItemAdapter = null;

	private UserTimers userTimers = UserTimers.getInstance();
	
	private static TimePicker timePicker = null;
	private static int instantHours = 0;
	private static int instantMinutes = 0;
	private static TimerBean updateTimer;
	
	
	
	private static boolean isStarted = false;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.log(this, "onCreate");
		
		setContentView(R.layout.main);

		if (!isStarted) {
			Logger.log(this, "doBindService");
			doBindService();
			
		}
		
		ExpandableListView timerListView = (ExpandableListView) this.findViewById(R.id.TimerListView);
		if (timerListView.getAdapter() == null) {
			timerItemAdapter = new  ExpandableListAdapter(this, new ArrayList<String>(),
					R.layout.timer_category_layout,
					new ArrayList<ArrayList<TimerBean>>(),
					R.layout.timer_child_timer_layout);

			timerListView.setAdapter(timerItemAdapter);
		}
		else {
			timerItemAdapter = (ExpandableListAdapter) timerListView.getAdapter();
		}
		
		Intent activtyIntent = getIntent();
		
		if (userTimers.hasTimers()) {
			Logger.log(this, " recreate timers");
			userTimers.recreateTimers(this);
			
			//expand the category
			Iterator<TimerBean> it = UserTimers.getInstance().getTimerMap().values().iterator();
			
			while (it.hasNext()) {
				TimerBean bean = it.next();
				int groupId = timerItemAdapter.getGroupId(bean.getTimerVO().getCategory());
				timerListView.expandGroup(groupId);
			}
		}
		
		try {
			ActivityUtil.deserializeTimers(this);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		List<TimerVO> timerList = null;
		
		//this value will always be null unless their is an orienation change
		String change = (String) getLastNonConfigurationInstance();
		if (activtyIntent.getExtras() != null && change == null) {
			timerList = activtyIntent.getExtras().getParcelableArrayList("timerList");
			
			if (timerList != null) {
				for (TimerVO timer : timerList) {
					userTimers.createTimer(timer,
							this,
							new Date());
					
					int groupId = timerItemAdapter.getGroupId(timer.getCategory());
					timerListView.expandGroup(groupId);
				}
			}
			
			TimerVO timer = activtyIntent.getExtras().getParcelable("timer");
			if (timer != null) {
				userTimers.createTimer(timer,
						this,
						new Date());
				int groupId = timerItemAdapter.getGroupId(timer.getCategory());
				timerListView.expandGroup(groupId);
			}
			activtyIntent.getExtras().putParcelable("timer", null);
		}		
		
		timerListView.setOnCreateContextMenuListener(this);
		
		
		
		Logger.log(this, "timerItemAdapter.isEmpty()" + timerItemAdapter.isEmpty());
		if (timerItemAdapter.isEmpty()) {
			showDialog(DIALOG_WELCOME_ID);
		}
		else {
			timerItemAdapter.notifyDataSetChanged();
		}
		
	}
	
	void doBindService() {
		Intent notifcationIntent = new Intent(this, NotificationService.class);
		if (!isStarted) {
			startService(notifcationIntent);
			isStarted = true;
		}
	}
	
	void doUnbindService() {
		if (isStarted) {
			stopService(new Intent(this, NotificationService.class));
			isStarted = false;
		}
	}


	@Override
	public Object onRetainNonConfigurationInstance() {
		Logger.log(this, "onRetainNonConfigurationInstance");
		return ROTATION_CHANGE;
	}
	
	public ExpandableListAdapter getTimerItemAdapter() {
		return timerItemAdapter;
	}



	
	/**
	 * Create the context menu for a long click for a list
	 */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListView.ExpandableListContextMenuInfo exandableMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		TimerBean timerBean = null;
		if (ExpandableListView.getPackedPositionType (exandableMenuInfo.packedPosition) ==  ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPosition = ExpandableListView.getPackedPositionGroup(exandableMenuInfo.packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(exandableMenuInfo.packedPosition);
			
			timerBean = (TimerBean) timerItemAdapter.getChild(groupPosition, childPosition);
		}
		
		if (timerBean == null)
			return;
		
		MenuInflater inflater = this.getMenuInflater();    
		inflater.inflate(R.menu.timer_context_menu, menu);  
		menu.setHeaderTitle("Configuration Menu");	
		
		
		if (timerBean != null) {
			if (timerBean.getState() == TimerView.STATE_RUN) {
				MenuItem menuItem = (MenuItem) menu.findItem(R.id.timer_context_start);
				menuItem.setVisible(false);
			}
			else {
				MenuItem menuItem = (MenuItem) menu.findItem(R.id.timer_context_pause);
				menuItem.setVisible(false);			
			}
		}
	}
	
	public void toggleMenu(Menu menu) {
		if (timerItemAdapter != null && !timerItemAdapter.isEmpty()) {	
			Map<String, TimerBean> timerMap = userTimers.getTimerMap();
			Iterator<TimerBean> it = timerMap.values().iterator();
			boolean isStarted = false;
			while (it.hasNext()) {
				TimerBean timer = it.next();
				if (timer.getState() == TimerView.STATE_RUN) {
					isStarted = true;
					break;
				}
			}
			
			if (isStarted) {
				menu.findItem(R.id.bbq_play).setVisible(false);
				menu.findItem(R.id.bbq_pause).setVisible(true);
			}
			else {
				menu.findItem(R.id.bbq_play).setVisible(true);
				menu.findItem(R.id.bbq_pause).setVisible(false);
			}
		}
		else {
			menu.findItem(R.id.bbq_pause).setVisible(false);
			menu.findItem(R.id.bbq_play).setVisible(false);
		}
	}
	
	/**
	 * Loop through all the timers and change thier state either to paused or play
	 * 
	 * @param pauseTimers if true pause the times
	 */
	private void toggleTimers(boolean pauseTimers) {
		Map<String, TimerBean> timerMap = userTimers.getTimerMap();
		Iterator<TimerBean> it = timerMap.values().iterator();
		ListView listView = (ListView) findViewById(R.id.TimerListView);
		

		while (it.hasNext()) {
			TimerBean timerVO = it.next();
			String viewTag = timerVO.getViewTag();
			TimerView timerView = (TimerView) listView.findViewWithTag(viewTag);
			if (pauseTimers) {
				pauseTimer(timerView, timerVO);
			}
			else {
				startTimer(timerView, timerVO);
			}
		}
	}
	
	/**
	 * Start the timer
	 * 
	 * @param timerView
	 * @param timerVO
	 */
	private void startTimer(TimerView timerView,
			TimerBean timerVO) {
//		Thread myRefreshThread = null;
		if (timerVO.getState() == TimerView.STATE_PAUSE || timerVO.getState() == TimerView.STATE_BEGIN) {
			timerVO.toggleState();
			timerView.invalidate();
			
//			myRefreshThread = new Thread(new CountDownRunner(timerView));
//			myRefreshThread.start();
//			timerVO.setThread(myRefreshThread);
		}			
	}
	/**
	 * pause a timer
	 * 
	 * @param timerView
	 * @param timerVO
	 */
	private void pauseTimer(TimerView timerView,
			TimerBean timerVO) {
		if (timerVO.getState() == TimerView.STATE_RUN) {
			timerVO.toggleState();

//			if (timerVO.getThread() != null) {
//				timerVO.getThread().interrupt();
//			}
			timerView.invalidate();
//			timerVO.setThread(null);	
		}
	}
	/**
	 * Handle long click of list
	 */
	public boolean onContextItemSelected(MenuItem item)
	{
		ExpandableListView.ExpandableListContextMenuInfo exandableMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		
		
		TimerBean timerBean = null;
		int groupPosition = -1;
		int childPosition = -1;
		if (ExpandableListView.getPackedPositionType (exandableMenuInfo.packedPosition) ==  ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPosition = ExpandableListView.getPackedPositionGroup(exandableMenuInfo.packedPosition);
			childPosition = ExpandableListView.getPackedPositionChild(exandableMenuInfo.packedPosition);
			
			timerBean = (TimerBean) timerItemAdapter.getChild(groupPosition, childPosition);
		}
		
		if (timerBean == null) {
			return true;
		}
		
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.TimerListView);
		
		
		String viewTag = TIMER_TAG_PREFIX + timerBean.getId();
		TimerView timerView = (TimerView) listView.findViewWithTag(viewTag);

		switch (item.getItemId()) {
			case R.id.timer_context_pause: {
				pauseTimer(timerView,timerBean);
				break;
			}
			case R.id.timer_context_start: {
				startTimer(timerView,timerBean);
				break;
			}
			case R.id.timer_context_restart: {
				if (timerBean.getState() == TimerView.STATE_RUN) {
					pauseTimer(timerView,timerBean);
				}
				
				timerBean.reset();
				timerView.invalidate();
				
				break;
			}
			case R.id.timer_context_delete: {
				pauseTimer(timerView,timerBean);
				
				timerItemAdapter.remove(groupPosition,childPosition);
				timerItemAdapter.notifyDataSetChanged();
				
				userTimers.deleteTimer(timerBean);

				if (timerItemAdapter.isEmpty()) {
					showDialog(DIALOG_WELCOME_ID);
				}
				break;
			}
			case R.id.timer_context_update_time: {
				updateTimer = timerBean;
				showDialog(DIALOG_UPDATE_ID);
				
			}
		}
		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();    
		inflater.inflate(R.menu.bbqlist_menu, menu); 
		
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toggleMenu(menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.bbq_edit: {
				Intent intent = new Intent(this, TimerMaintainActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.bbq_add: {
				Intent intent = new Intent(this, TimerSelectActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.bbq_create: {
				Intent intent = new Intent(this, TimerCreateActivity.class);
				intent.putExtra("previousIntent", BbqListActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.bbq_instant: {
				showDialog(DIALOG_INSTANT_ID);
				break;
			}
			case R.id.bbq_pause: {
				toggleTimers(true);
				break;
			}
			case R.id.bbq_play: {
				toggleTimers(false);
				break;
			}
			case R.id.bbq_log: {
				showDialog(DIALOG_LOG_ID);
				break;
			}
		}
		return true;
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id) {
			case DIALOG_UPDATE_ID: {
				if (updateTimer == null) {
					break;
				}
				final Dialog tempDialog = new Dialog(this);
				tempDialog.setContentView(R.layout.timer_instant);
				
				
				
				ExpandableListView timerListView = (ExpandableListView) this.findViewById(R.id.TimerListView);
				
				String viewTag = updateTimer.getViewTag();
				final TimerView timerView = (TimerView) timerListView.findViewWithTag(viewTag);
				final TextView timerIntervalTxt = (TextView) timerListView.findViewWithTag("CookTime" + updateTimer.getId());
				
				timePicker = (TimePicker) tempDialog.findViewById(R.id.instant_timePicker);
				tempDialog.findViewById(R.id.instant_nameEditText).setVisibility(View.GONE);
				tempDialog.findViewById(R.id.instant_nameTextView).setVisibility(View.GONE);

				//if we are doing updating an interval..the interval can't go past the total time
				int cookTime = updateTimer.getTotalTime();
				int maxIntervalTime = updateTimer.getTotalTime();
				
				String timerName = updateTimer.getTimerVO().getName();
				if (updateTimer.isShowInterval()) {
					TimerIntervalVO intervalVO = updateTimer.getInterval(this,null);
					if (intervalVO != null) {
						maxIntervalTime = updateTimer.calculateMaxIntervalTime();
						maxIntervalTime += intervalVO.getCookTime();
						timerName = intervalVO.getName();
						cookTime = intervalVO.getCookTime();	
					}
				}
				
				final int maxIntervalHours = maxIntervalTime / 60;
				final int maxIntervalMinutes = maxIntervalTime - (maxIntervalHours * 60);
				
				tempDialog.setTitle("Update " + timerName);
				
				instantHours = cookTime / 60;
				instantMinutes = cookTime - (instantHours * 60);
				
				timePicker.setIs24HourView(true);
				
				timePicker.setCurrentHour(instantHours);
				timePicker.setCurrentMinute(instantMinutes);
				
				
				timePicker.invalidate();
				
				
				
				timePicker.setOnTimeChangedListener( new OnTimeChangedListener() {

					public void onTimeChanged(TimePicker view, int hourOfDay,int minute) {
						if (updateTimer.isShowInterval()) {
							int maxIntervalTime = maxIntervalHours * 60 + maxIntervalMinutes;
							int currentIntervalTime = hourOfDay * 60 + minute;

							if (currentIntervalTime > maxIntervalTime) {
								view.setCurrentMinute(maxIntervalMinutes);
								view.setCurrentHour(maxIntervalHours);
							}
						}
					}
				});
				
				Button okBtn = (Button)tempDialog.findViewById(R.id.instant_okBtn);
				okBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int cookTime = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
						
						if (cookTime > 0 ) {
							if (updateTimer.isShowInterval()) {
								TimerIntervalVO intervalVO = updateTimer.getInterval(v.getContext(),null);
								intervalVO.setCookTime(cookTime);
							}
							else {
								updateTimer.setTotalTime(cookTime);
								updateTimer.getTimerVO().setCookTime(cookTime);
							}
							timerView.invalidate();
							
							//update the interval time shown
							int hours = cookTime / 60;
							int minutes = cookTime - (hours * 60);
							String cookTimeStr = hours + " hr " + minutes + " min ";
							if (hours == 0) {
								cookTimeStr = minutes + " min ";
							}
							
							timerIntervalTxt.setText(cookTimeStr);
							
							removeDialog(DIALOG_UPDATE_ID);
						}
					}
				});
				
				Button cancelBtn = (Button) tempDialog.findViewById(R.id.instant_cancelBtn);
				cancelBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						removeDialog(DIALOG_UPDATE_ID);
					}
					
				});
				dialog=tempDialog;
				break;
				
			}
			case DIALOG_INSTANT_ID: {
				final Dialog tempDialog = new Dialog(this);
				tempDialog.setContentView(R.layout.timer_instant);
				tempDialog.setTitle("Instant Timer");

				timePicker = (TimePicker) tempDialog.findViewById(R.id.instant_timePicker);
				timePicker.setIs24HourView(true);

				timePicker.setCurrentHour(new Integer(0));
				timePicker.setCurrentMinute(0);
				
				
				timePicker.invalidate();
				
				final ExpandableListView timerListView = (ExpandableListView) this.findViewById(R.id.TimerListView);
				
				Button okBtn = (Button)tempDialog.findViewById(R.id.instant_okBtn);
				okBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {						
						int cookTime = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
						
						if (cookTime == 0) {
							AlertDialog.Builder errorDialog = new AlertDialog.Builder(BbqListActivity.this);
							errorDialog.setTitle("Error");
							errorDialog.setMessage("Please select a time.");
							
							errorDialog.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									
								}
							});
							
							errorDialog.show();
						}
						else {
							TimerVO timer = new TimerVO();
							timer.setCategory(TimerCreateItemAdapater.INSTANT_TYPE.toUpperCase());
							timer.setCookTime(timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute());

							EditText editView = (EditText) tempDialog.findViewById(R.id.instant_nameEditText);
							String name = editView.getText().toString();
							if (name == null) {
								name = TimerCreateItemAdapater.INSTANT_TYPE;
							}
							timer.setName(name);
							timer.setId(ActivityUtil.getViewId());

							userTimers.createTimer(timer, 
									BbqListActivity.this,
									new Date());

							int groupId = timerItemAdapter.getGroupId(timer.getCategory());
							timerListView.expandGroup(groupId);
							removeDialog(DIALOG_INSTANT_ID);
						}
					}
					
				});
				Button cancelBtn = (Button) tempDialog.findViewById(R.id.instant_cancelBtn);
				cancelBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						removeDialog(DIALOG_INSTANT_ID);
					}
					
				});
				dialog=tempDialog;
				break;
				
			}
			case DIALOG_WELCOME_ID: {
				final Dialog tempDialog = new Dialog(this);
				tempDialog.setContentView(R.layout.timer_welcome);
				tempDialog.setTitle("Welcome");
			
				
				OnClickListener selectListener = new OnClickListener() {
					public void onClick(View v) {
						tempDialog.dismiss();
						Intent intent = new Intent(v.getContext(), TimerSelectActivity.class);
						startActivity(intent);
					}
				};
				
				OnClickListener createListener = new OnClickListener() {
					public void onClick(View v) {
						tempDialog.dismiss();
						Intent intent = new Intent(v.getContext(), TimerCreateActivity.class);
						intent.putExtra("previousIntent", BbqListActivity.class);
						startActivity(intent);
					}
				};
				
				OnClickListener instantListener = new OnClickListener() {
					public void onClick(View v) {
						tempDialog.dismiss();
						showDialog(DIALOG_INSTANT_ID);
					}
				};
				
				OnClickListener maintainListener = new OnClickListener() {
					public void onClick(View v) {
						tempDialog.dismiss();
						Intent intent = new Intent(v.getContext(), TimerMaintainActivity.class);
						startActivity(intent);
					}
				};
				
				TableRow tableRowCreate = (TableRow) tempDialog.findViewById(R.id.welcome_row_create);
				tableRowCreate.setOnClickListener(createListener);
				
				ImageButton welcomeBtn = (ImageButton) tempDialog.findViewById(R.id.welcome_createBtn);
				welcomeBtn.setOnClickListener(createListener);
				
				TableRow tableRowInstant = (TableRow) tempDialog.findViewById(R.id.welcome_row_instant);
				tableRowInstant.setOnClickListener(instantListener);
				
				ImageButton instantBtn = (ImageButton) tempDialog.findViewById(R.id.welcome_instantBtn);
				instantBtn.setOnClickListener(instantListener);
				
				TableRow tableRowMaintain = (TableRow) tempDialog.findViewById(R.id.welcome_row_maintain);
				tableRowMaintain.setOnClickListener(maintainListener);

				ImageButton maintainBtn = (ImageButton) tempDialog.findViewById(R.id.welcome_maintainBtn);
				maintainBtn.setOnClickListener(maintainListener);

				TableRow tableRowSelect = (TableRow) tempDialog.findViewById(R.id.welcome_row_select);
				tableRowSelect.setOnClickListener(selectListener);

				ImageButton selectBtn = (ImageButton) tempDialog.findViewById(R.id.welcome_selectBtn);
				selectBtn.setOnClickListener(selectListener);
				
				dialog=tempDialog;
				break;
			}
			case DIALOG_LOG_ID: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						R.layout.log_item, Logger.getLogListAsArray());
				
				ListView listView = new ListView(this);
				listView.setAdapter(adapter);
				
				builder
					.setView(listView)				       
				     .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   dialog.cancel();
				           }
				     });
				dialog = builder.create();
			}
		}
		
		return dialog;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle out) {
	  super.onSaveInstanceState(out);
	  Logger.log(this, "onSaveInstanceState");
	  
	  if (timePicker != null) {
		  instantHours = timePicker.getCurrentHour();
		  instantMinutes = timePicker.getCurrentMinute();
	  }
	}
//
//	@Override
//	protected void onRestoreInstanceState(Bundle in) {
//	  super.onRestoreInstanceState(in);
//	  if (timePicker != null) {
//		  timePicker.setCurrentHour(in.getInt("hour"));
//		  timePicker.setCurrentMinute(in.getInt("minute"));
//	  }
//	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.log(this, "onResume");

		for (TimerBean timerBean : userTimers.getTimerMap().values()) {
			if (timerBean.getState() == TimerView.STATE_RUN) {
				int mySecondsPassed = timerBean.getTimepassed();
				int mySecondsTotal = timerBean.getTotalTime() * 60;
				int secondsLeft = mySecondsTotal - mySecondsPassed;
				if (secondsLeft <= 0) {
					timerBean.setState(TimerView.STATE_COMPLETE);
				}
				
				ActivityUtil.cancelAlarm(this, timerBean);
				updateCookingLabel(timerBean);

			}
		}
		if (timePicker != null) {
			timePicker.setCurrentHour(instantHours); // This will also set on rotate
		}
		
		if (!userTimers.hasTimers()) {
			showDialog(DIALOG_WELCOME_ID);
		}

	}	
	
	public void  updateCookingLabel(TimerBean timerBean) {
		ExpandableListView timerListView = (ExpandableListView) this.findViewById(R.id.TimerListView);
		TextView cookTypeView = (TextView) timerListView.findViewWithTag("TimerView" + timerBean.getId() + "CookType");
		if (cookTypeView != null) {
			if (timerBean.isShowInterval() && !timerBean.getStartIntervalList().isEmpty()) {
				String intervalTxt = timerBean.getStartIntervalList().get(0).getName();
				cookTypeView.setText(intervalTxt);
			}
			else {
				cookTypeView.setText(timerBean.getTimerVO().getCookType());	
			}
		}

	}
	
	@Override
	public void onPause() {
		super.onPause();
		Logger.log(this, "onPause");

	}
	
	@Override
	public void onStop() {
		super.onStop();
		for (TimerBean timerBean : userTimers.getTimerMap().values()) {
			if (timerBean.getState() == TimerView.STATE_RUN) {
				ActivityUtil.setAlarm(this, timerBean);
			}
		}
		Log.e("BBQ", "onStop");
		Logger.log(this, "onStop");
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Logger.log(this, "onDestroy");
		Log.e("BBQ", "onDestroy");
		try {
			doUnbindService();
			
//			Iterator <TimerBean> beans = userTimers.getTimerMap().values().iterator();
//			while (beans.hasNext()) {
//				ActivityUtil.setAlarm(this, beans.next());
//			}
			ActivityUtil.serializeTimers(this);
			
		} catch (Exception e) {
			Log.e("ERRROR", "Could not run onPause.",e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		Logger.log(this, "onRestart");
		try {
			ActivityUtil.deserializeTimers(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Logger.log(this, "onConfigurationChanged");
	}
	
}