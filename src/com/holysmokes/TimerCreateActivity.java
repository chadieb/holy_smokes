package com.holysmokes;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.holysmokes.data.DAO;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.util.ActivityUtil;
import com.holysmokes.view.TimerIntervalAdapter;

public class TimerCreateActivity extends Activity {
	
	private static final int DIALOG_ADD_CATEGORY_ID = 0;
	private static final int DIALOG_DELETE_CATEGORY_ID = 1;
	private static final int DIALOG_INTERVAL_ID = 2;
	
	DAO dao = new DAO(this);
	TimerVO timer =  null;
	boolean doUpdate = false;
	boolean enableAddInterval = false;
	
	static TimerIntervalAdapter intervalAdapter = null;
	static TimePicker timePicker = null;

	
	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.timer_create);
		
		
		Class previousIntent = BbqListActivity.class;
		Intent activtyIntent = getIntent();
		if (activtyIntent.getExtras() != null) {
			timer = (TimerVO) activtyIntent.getExtras().get("timer");
			previousIntent = (Class) activtyIntent.getExtras().get("previousIntent");
			
			if (timer != null)
				doUpdate = true;
		}
		
		if (timer == null) {
			timer = (TimerVO) getLastNonConfigurationInstance();
		}
		
		if (timer == null) {
			timer = new TimerVO();
		}
		
		intervalAdapter = new TimerIntervalAdapter(this, R.layout.timer_create_interval, timer.getIntervalList());
		
		final Class previousIntentClass = previousIntent;
		timePicker = (TimePicker) findViewById(R.id.create_TimePicker);
		timePicker.setIs24HourView(true);
		

		intervalAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				createIntervalTable();
			}
		});
		
		//disable interval button till user adds time
		ImageButton addBtn = (ImageButton)findViewById(R.id.create_addSubCategoryBtn);
		addBtn.setEnabled(false);
		
		timePicker.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				if (!enableAddInterval) {
					ImageButton addBtn = (ImageButton)findViewById(R.id.create_addSubCategoryBtn);
					addBtn.setEnabled(true);
					enableAddInterval = true;
				}
				
			}
			
		});
		
		timePicker.setOnTimeChangedListener (new TimePicker.OnTimeChangedListener() {

			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				if (!enableAddInterval) {
					ImageButton addBtn = (ImageButton)findViewById(R.id.create_addSubCategoryBtn);
					addBtn.setEnabled(true);
					enableAddInterval = true;
				}
			}
			
		});
		
		
		List<String> categoryList = dao.getAllCategories();
		
		Spinner categoryListView = (Spinner) findViewById(R.id.create_categorySpinner);
		
		ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryList);
		categoryListView.setAdapter(catAdapter);
		
		
		ImageButton addCategoryBtn = (ImageButton) findViewById(R.id.create_addCategoryBtn);
		addCategoryBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDialog(DIALOG_ADD_CATEGORY_ID);
			}	
		});
		
		ImageButton deleteCategoryBtn = (ImageButton) findViewById(R.id.create_deleteCategoryBtn);
		deleteCategoryBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDialog(DIALOG_DELETE_CATEGORY_ID);
			}	
		});
		
		Button updateBtn = (Button) findViewById(R.id.create_updateBtn);
		updateBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				handleClick(previousIntentClass);
			}
		});
		Button okBtn = (Button) findViewById(R.id.create_okBtn);
		okBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleClick(previousIntentClass);
			}
			
		});
		
		Button cancelBtn = (Button) findViewById(R.id.create_cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(getBaseContext(), previousIntentClass);
				startActivity(intent);
			}
		});
		
		ImageButton addInterval = (ImageButton) findViewById(R.id.create_addSubCategoryBtn);
		addInterval.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				showDialog(DIALOG_INTERVAL_ID);
			}
		});
		
		
		
		if (!timer.getIntervalList().isEmpty()) {
			createIntervalTable();
			int availableIntervalTime = timer.getCookTime();
			List<TimerIntervalVO> intervalTime = timer.getIntervalList();
			for (TimerIntervalVO intervalVO : intervalTime) {
				availableIntervalTime -= intervalVO.getCookTime();
			}
			
			if (availableIntervalTime > 0) {
				addBtn.setEnabled(true);
			}
		}
		
		if (doUpdate) {
			initUpdate(timer);
		}
		else {
			((Button) findViewById(R.id.create_updateBtn)).setVisibility(View.GONE);
		}
		
		int hours = timer.getCookTime() / 60;
		int minutes = timer.getCookTime() - (hours * 60);

		timePicker.setCurrentHour(Integer.valueOf(hours));
		timePicker.setCurrentMinute(Integer.valueOf(minutes));
		timePicker.invalidate();
		
	};
	
	private void handleClick(Class previousIntentClass) {
		Spinner categorySpinner = (Spinner) findViewById(R.id.create_categorySpinner);
		EditText nameText = (EditText) findViewById(R.id.create_nameEditText);
		EditText subCategoryText = (EditText) findViewById(R.id.create_cutEditText);
		RadioButton directRadioButton = (RadioButton) findViewById(R.id.create_direct);
		
		String category = (String) categorySpinner.getSelectedItem();
		String name = nameText.getText().toString();
		String subCategory = subCategoryText != null ? subCategoryText.getText().toString() : null;
		String cookType = directRadioButton.isChecked() ? "DIRECT" : "INDIRECT";
		
		int cookTime = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
		
		if (cookTime == 0) {
			AlertDialog.Builder errorDialog = new AlertDialog.Builder(TimerCreateActivity.this);
			errorDialog.setTitle("Error");
			errorDialog.setMessage("Please select a value");
			
			errorDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
				}
			});
			
			errorDialog.show();							
		}
		else {
			timer.setCategory(category);
			timer.setName(name);
			timer.setMeetCut(subCategory);

			timer.setCookTime(cookTime);
			timer.setCookType(cookType);

			if (doUpdate) {
				dao.updateTimer(timer);
			}
			else {
				dao.insertTimer(timer);
			}

			Intent intent = new Intent(getBaseContext(), previousIntentClass);
			startActivity(intent);
		}
	}
	private void initUpdate(TimerVO timer) {
		((Button) findViewById(R.id.create_okBtn)).setVisibility(View.GONE);
		
		Spinner categorySpinner = (Spinner) findViewById(R.id.create_categorySpinner);
		EditText nameText = (EditText) findViewById(R.id.create_nameEditText);
		EditText subCategoryText = (EditText) findViewById(R.id.create_cutEditText);
		RadioButton directRadioButton = (RadioButton) findViewById(R.id.create_direct);
		RadioButton indirectRadioButton = (RadioButton) findViewById(R.id.create_indirect);
		
		ActivityUtil.setSpinnerSelection(categorySpinner,timer.getCategory());

		nameText.setText(timer.getName());
		if (timer.getMeetCut() != null)
			subCategoryText.setText(timer.getMeetCut());
		
		if (timer.getCookType().equalsIgnoreCase("DIRECT")) {
			directRadioButton.setChecked(true);
		}
		else {
			indirectRadioButton.setChecked(true);
		}
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		final Spinner categorySpiner = (Spinner) findViewById(R.id.create_categorySpinner);
		switch(id) {
			case DIALOG_ADD_CATEGORY_ID: {
				final Dialog tempDialog = new Dialog(this);
				tempDialog.setContentView(R.layout.timer_create_category);
				tempDialog.setTitle("Add Category");
		
				
				Button okBtn = (Button) tempDialog.findViewById(R.id.dialog_catagory_okBtn);
				okBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						EditText categoryExitTest = (EditText) tempDialog.findViewById(R.id.dialog_catagory_edittext);
						String newCategory = categoryExitTest.getText().toString();
						DAO dao = new DAO(v.getContext());
						dao.insertCategory(newCategory);
						
						ArrayAdapter<String> catAdapter = (ArrayAdapter<String>) categorySpiner.getAdapter();
						catAdapter.add(newCategory);
						catAdapter.notifyDataSetChanged();
						
						ActivityUtil.setSpinnerSelection(categorySpiner,newCategory);
						
						removeDialog(DIALOG_ADD_CATEGORY_ID);
						
					}				
				});
				Button cancelBtn = (Button) tempDialog.findViewById(R.id.dialog_catagory_cancelBtn);
				cancelBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						removeDialog(DIALOG_ADD_CATEGORY_ID);
					}				
				});
				
				dialog = tempDialog;
				
				break;
			}
			case DIALOG_DELETE_CATEGORY_ID: {
				final AlertDialog.Builder ad = new AlertDialog.Builder(this);
				
				
				ad.setTitle("Delete Category");
				ad.setMessage("Are you sure you want to delete the category " + (String) categorySpiner.getSelectedItem() + "?");
				ad.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,
							int arg1) {
						System.out.println("Cancel negative");
					}
				});
				ad.setCancelable(true);
				
				ad.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						
						String category = (String) categorySpiner.getSelectedItem();
						DAO dao = new DAO(TimerCreateActivity.this);
						try {
							dao.deleteCategory(category);
						} catch (Exception e) {
							e.printStackTrace();
							AlertDialog.Builder errorDialog = new AlertDialog.Builder(TimerCreateActivity.this);
							errorDialog.setTitle("Error");
							errorDialog.setMessage(e.getMessage());
							
							errorDialog.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
								}
							});
							
							errorDialog.show();
							
						}
						
						List<String> categoryList = dao.getAllCategories();
						ArrayAdapter<String> catAdapter = (ArrayAdapter<String>) categorySpiner.getAdapter();
						catAdapter.remove(category);
						catAdapter.notifyDataSetChanged();
					}
				});
				dialog = ad.create();
				break;
			}
			
			case DIALOG_INTERVAL_ID: {
				final Dialog tempDialog = new Dialog(this);
				tempDialog.setContentView(R.layout.timer_instant);
				tempDialog.setTitle("Create Interval");
				
				TimePicker instantTimePicker = (TimePicker) tempDialog.findViewById(R.id.instant_timePicker);
				
				int maxIntervalTime = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
				

				List<TimerIntervalVO> intervalTime = timer.getIntervalList();
				for (TimerIntervalVO intervalVO : intervalTime) {
					maxIntervalTime -= intervalVO.getCookTime();
				}
				
				final int maxHours = maxIntervalTime / 60;
				final int maxMinutes = maxIntervalTime - (maxHours * 60);
				
				instantTimePicker.setIs24HourView(true);
				instantTimePicker.setCurrentHour(maxHours);
				instantTimePicker.setCurrentMinute(maxMinutes);
				
				instantTimePicker.setOnTimeChangedListener( new OnTimeChangedListener() {

					public void onTimeChanged(TimePicker view, int hourOfDay,int minute) {
						if (hourOfDay > maxHours) {
							view.setCurrentHour(maxHours);
						}
						
						int maxIntervalTime = maxHours * 60 + maxMinutes;
						int currentIntervalTime = hourOfDay * 60 + minute;
						
						if (currentIntervalTime > maxIntervalTime) {
							view.setCurrentMinute(maxMinutes);
							view.setCurrentHour(maxHours);
						}
					}
				});
				
				Button okBtn = (Button) tempDialog.findViewById(R.id.instant_okBtn);
				okBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						TimePicker timePicker = (TimePicker) tempDialog.findViewById(R.id.instant_timePicker);
						EditText textEdit = (EditText) tempDialog.findViewById(R.id.instant_nameEditText);
						int cookTime = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
						
						if (cookTime == 0) {
							AlertDialog.Builder errorDialog = new AlertDialog.Builder(TimerCreateActivity.this);
							errorDialog.setTitle("Error");
							errorDialog.setMessage("Please select a value");
							
							errorDialog.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
								}
							});
							
							errorDialog.show();							
						}
						else {
							TimerIntervalVO timerInterval = new TimerIntervalVO();
							timerInterval.setCookTime(cookTime);
							timerInterval.setName(textEdit.getText().toString());
							
							timer.getIntervalList().add(timerInterval);
							
//							findViewById(R.id.create_intervalList).setVisibility(View.VISIBLE);
							intervalAdapter.notifyDataSetChanged();
//							createIntervalTable();
							
							
							int availableIntervalTime = (maxHours * 60 + maxMinutes) - cookTime;
							if (availableIntervalTime <= 0) {
								ImageButton addBtn = (ImageButton)findViewById(R.id.create_addSubCategoryBtn);
								addBtn.setEnabled(false);
							}
	
							removeDialog(DIALOG_INTERVAL_ID);
						}
					}
				});
				
				Button cancelBtn = (Button) tempDialog.findViewById(R.id.instant_cancelBtn);
				cancelBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						removeDialog(DIALOG_INTERVAL_ID);
					}
				});
				
				dialog = tempDialog;
			}
		
		}
		
		return dialog;
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		final TimerVO myTimer = new TimerVO();
		
		Spinner categorySpinner = (Spinner) findViewById(R.id.create_categorySpinner);
		EditText nameText = (EditText) findViewById(R.id.create_nameEditText);
		EditText subCategoryText = (EditText) findViewById(R.id.create_cutEditText);
		RadioButton directRadioButton = (RadioButton) findViewById(R.id.create_direct);
		TimePicker timePicker = (TimePicker) findViewById(R.id.create_TimePicker);
		
		String category = (String) categorySpinner.getSelectedItem();
		String name = nameText.getText().toString();
		String subCategory = subCategoryText != null ? subCategoryText.getText().toString() : null;
		String cookType = directRadioButton.isChecked() ? "DIRECT" : "INDIRECT";
		
		int cookTime = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();

		myTimer.setCategory(category);
		myTimer.setName(name);
		myTimer.setMeetCut(subCategory);
		myTimer.setIntervalList(timer.getIntervalList());
		myTimer.setCookTime(cookTime);
		myTimer.setCookType(cookType);

		timePicker.setCurrentHour(0);
		timePicker.setCurrentMinute(0);
		timePicker.invalidate();
		return myTimer;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle out) {
	  super.onSaveInstanceState(out);
	  out.putInt("hour", timePicker.getCurrentHour());
	  out.putInt("minute", timePicker.getCurrentMinute());
	}

	@Override
	protected void onRestoreInstanceState(Bundle in) {
	  super.onRestoreInstanceState(in);
	  timePicker.setCurrentHour(in.getInt("hour"));
	  timePicker.setCurrentMinute(in.getInt("minute"));
	}
	
	private void createIntervalTable() {
		TableLayout tableView = (TableLayout) findViewById(R.id.create_intervalTableLayout);
			LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			

		
		//remove all the children but the header
		int tableSize = tableView.getChildCount();
		if (tableSize > 1) {
			List<View> childViews = new ArrayList<View>();
			for (int i=1;i<tableSize;i++) {
				childViews.add(tableView.getChildAt(i));
			}
			for (View childView : childViews) {
				tableView.removeView(childView);
			}
		}
		tableView.invalidate();
		for (int i=0;i<intervalAdapter.getCount();i++) {
			View convertView = mInflater.inflate(R.layout.timer_create_interval, null);
			
			TimerIntervalVO item = intervalAdapter.getItem(i);
			
			TextView nameView = (TextView) convertView.findViewById(R.id.create_intervalName);
			nameView.setText(item.getName());
			
			TextView timeView = (TextView) convertView.findViewById(R.id.create_intervalTime);
			int cookTimeVal = item.getCookTime();
			
			ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.create__interval_deleteBtn);
			deleteButton.setTag("" + i);

			deleteButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String posStr = (String) v.getTag();
					intervalAdapter.removeIntervalItem(Integer.parseInt(posStr));
				}
			});

			int hours = cookTimeVal / 60;
			int minutes = cookTimeVal - (hours * 60);
			String cookTime = hours + " hr " + minutes + " min ";
			if (hours == 0) {
				cookTime = minutes + " min ";
			}

			timeView.setText(cookTime);
			
			tableView.addView(convertView);

		}
		tableView.invalidate();
	}
	
}
