package com.holysmokes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.holysmokes.data.DAO;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;

public class TimerSelectActivity extends Activity {
	Map<String, TimerVO> timerVOMap = new HashMap<String, TimerVO>();
	Map<String, List<String>> timerCategoryMap = new HashMap<String, List<String>>();
	List<String> categoryList = new ArrayList<String>();
	Map<String, List<String>> timerNameMap = new HashMap<String, List<String>>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.timer_selector);  

		loadData();
		init();
	}  

	
	public void loadData() {
		DAO dao = new DAO(this);

		List<TimerVO> timerList = dao.getTimerList();

		for (TimerVO timerVO : timerList) {
			String key = timerVO.getCategory() + ":" + timerVO.getName();
			if (timerVO.getMeetCut() != null) {
				key += ":" + timerVO.getMeetCut();
			}
			timerVOMap.put(key, timerVO);
			

			List<String> nameList = null;
			if (timerCategoryMap.containsKey(timerVO.getCategory())) {
				nameList = timerCategoryMap.get(timerVO.getCategory());
			}
			else {
				categoryList.add(timerVO.getCategory());
				nameList = new ArrayList<String>();
				timerCategoryMap.put(timerVO.getCategory(), nameList);
			}
			
			if (!nameList.contains(timerVO.getName()))
				nameList.add(timerVO.getName());

			List<String> valueList = null;
			if (timerNameMap.containsKey(timerVO.getName())) {
				valueList = timerNameMap.get(timerVO.getName());
			}
			else {
				valueList = new ArrayList<String>();
				timerNameMap.put(timerVO.getName(), valueList);
			}
			if (timerVO.getMeetCut() != null) {
				valueList.add(timerVO.getMeetCut());
			}
		}
	}
	
	public void init() {
		setContentView(R.layout.timer_selector);
		setTitle("Select Timer");
		Spinner categorySpinner = (Spinner) findViewById(R.id.timer_categorySpinner);

		updateCategorySelectBox();
		updateNameSelectBox();
		updateCutSelectBox();
		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapter, View view,int position, long id) {
				LinearLayout layout = (LinearLayout) view;
				
				if (layout == null) {
					return;
				}
				
				TextView categoryTextView = (TextView) view.findViewById(R.id.SpinnerTextView);
				if (categoryTextView != null) {
					updateNameSelectBox();		
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		Spinner nameSpinner = (Spinner) findViewById(R.id.timer_nameSpinner);
		nameSpinner.refreshDrawableState();

		nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0,
					View view, int position, long id) {
				LinearLayout layout = (LinearLayout) view;
				
				if (layout == null) {
					return;
				}
				TextView nameTextView =  (TextView) view.findViewById(R.id.SpinnerTextView);
				if (nameTextView != null) {
					updateCutSelectBox();

				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});


		Spinner cutSpinner = (Spinner) findViewById(R.id.timer_cutSpinner);
		cutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0,
					View view, int position, long id) {
				LinearLayout layout = (LinearLayout) view;
				
				if (layout == null) {
					return;
				}
				TextView cutTextView =  (TextView) view.findViewById(R.id.SpinnerTextView);
				if (cutTextView != null) {
					updateTextInfo();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});


		Button okButton = (Button) findViewById(R.id.timer_okBtn);  
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				TimerVO timer = getSelectedItem();
				Intent intent = new Intent(v.getContext(), BbqListActivity.class);
				intent.putExtra("timer", timer);
				startActivity(intent);
			}
		}); 

		updateTextInfo();

	}

	private void updateCategorySelectBox() {
		Spinner categorySpinner = (Spinner) findViewById(R.id.timer_categorySpinner);			

		ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, R.id.SpinnerTextView, categoryList);
		categorySpinner.setAdapter(catAdapter);
		categorySpinner.refreshDrawableState();
	}

	private void updateNameSelectBox() {
		Spinner categorySpinner = (Spinner) findViewById(R.id.timer_categorySpinner);
		Spinner nameSpinner = (Spinner) findViewById(R.id.timer_nameSpinner);

		String selectedView = (String) categorySpinner.getSelectedItem();
		List<String> nameList = timerCategoryMap.get(selectedView);
		ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, R.id.SpinnerTextView, nameList);
		nameSpinner.setAdapter(nameAdapter);
		nameSpinner.refreshDrawableState();
	}

	private void updateCutSelectBox() {		
		Spinner nameSpinner = (Spinner) findViewById(R.id.timer_nameSpinner);
		String name = (String) nameSpinner.getSelectedItem();

		Spinner cutSpinner = (Spinner) findViewById(R.id.timer_cutSpinner);
		TableRow tableRow = (TableRow) findViewById(R.id.select_subtype_row);

		List<String> cutList = timerNameMap.get(name);
		if (cutList == null || cutList.isEmpty() || (cutList.size() == 1 && cutList.get(0).trim().length() ==0)) {
			tableRow.setVisibility(View.INVISIBLE);
			updateTextInfo();
		}
		else {
			tableRow.setVisibility(View.VISIBLE);

			ArrayAdapter<String> cutAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, R.id.SpinnerTextView, cutList);
			cutSpinner.setAdapter(cutAdapter);
			cutSpinner.refreshDrawableState();
		}
	}

	private void updateTextInfo() {
		TimerVO timerVO = getSelectedItem();
		
		if (timerVO == null) {
			return; 
		}
		TextView methodView = (TextView) findViewById(R.id.timer_MethodTextView);
		TextView cookTimeView = (TextView) findViewById(R.id.timer_CookTimeTextView);

		methodView.setText("Cooking Method:" + timerVO.getCookType());
		methodView.invalidate();

		int hours = timerVO.getCookTime() / 60;
		int minutes = timerVO.getCookTime() - (hours * 60);
		String cookTime = hours + " hr " + minutes + " min ";
		if (hours == 0) {
			cookTime = minutes + " min ";
		}

		cookTimeView.setText("Cooking Time:" + cookTime);
		cookTimeView.invalidate();

		TableLayout intervalTable = (TableLayout) findViewById(R.id.timer_intervalTableLayout);
		
		int tableCount = intervalTable.getChildCount();
		for (int i=1;i<tableCount;i++) {
			if (intervalTable.getChildAt(i) != null)
				intervalTable.removeViewAt(i);
		}
		
//		ScrollView scrollView = (ScrollView) findViewById(R.id.timer_intervalScroll);
		if (timerVO.getIntervalList() == null || timerVO.getIntervalList().isEmpty()) {
			intervalTable.setVisibility(View.INVISIBLE);
		}
		else {
			intervalTable.setVisibility(View.VISIBLE);

			for (TimerIntervalVO intervalVO : timerVO.getIntervalList()) {
				if (intervalTable.findViewWithTag("text" + intervalVO.getId()) == null) {
					TableRow tableRow = new TableRow(this);
					TextView intervalNameTextView = new TextView(this);
					intervalNameTextView.setText(intervalVO.getName());
					intervalNameTextView.setLayoutParams(new LayoutParams(
							LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT
							,1));
					intervalNameTextView.setTextColor(Color.BLACK);
					intervalNameTextView.setTag("text" + intervalVO.getId());

					tableRow.addView(intervalNameTextView);

					int intervalHours = intervalVO.getCookTime() / 60;
					int intervalMinutes = intervalVO.getCookTime() - (intervalHours * 60);
					String intervalCookTime = intervalHours + " hr " + intervalMinutes + " min ";
					if (hours == 0) {
						cookTime = intervalMinutes + " min ";
					}

					TextView intervalTimeTextView = new TextView(this);
					intervalTimeTextView.setText(intervalCookTime);
					intervalTimeTextView.setLayoutParams(new LayoutParams(
							LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT
							,1));
					intervalTimeTextView.setTextColor(Color.BLACK);

					tableRow.addView(intervalTimeTextView);
					intervalTable.addView(tableRow);
				}
			}
		}
	}

	private TimerVO getSelectedItem() {
		Spinner categorySpinner = (Spinner) findViewById(R.id.timer_categorySpinner);
		String catTextView = (String) categorySpinner.getSelectedItem();

		Spinner nameSpinner = (Spinner) findViewById(R.id.timer_nameSpinner);
		String nameTextView = (String) nameSpinner.getSelectedItem();

		Spinner cutSpinner = (Spinner) findViewById(R.id.timer_cutSpinner);
		TableRow tableRow = (TableRow) findViewById(R.id.select_subtype_row);
		String selectedView  = null;
		
		List<String> cutList = timerNameMap.get(nameTextView);
		if (cutList != null && !cutList.isEmpty() && tableRow.getVisibility() == View.VISIBLE) {
			selectedView = (String) cutSpinner.getSelectedItem();
		}
			

		String  key = catTextView + ":" + 
		nameTextView;

		if (selectedView != null) {
			key += ":" + selectedView;
		}

		return timerVOMap.get(key);
	}
}
