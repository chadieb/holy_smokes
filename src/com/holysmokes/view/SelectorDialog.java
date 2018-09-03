package com.holysmokes.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.holysmokes.BbqListActivity;
import com.holysmokes.R;
import com.holysmokes.data.DAO;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.util.UserTimers;

public class SelectorDialog {
	Dialog dialog = null;
	BbqListActivity bbqList = null;

	Map<String, TimerVO> timerVOMap = new HashMap<String, TimerVO>();
	Map<String, List<String>> timerCategoryMap = new HashMap<String, List<String>>();
	Map<String, List<String>> timerNameMap = new HashMap<String, List<String>>();

	public SelectorDialog (BbqListActivity bbqList) {
		this.bbqList = bbqList;
		dialog = new Dialog(bbqList);

		loadData();
	}

	public void loadData() {
		DAO dao = new DAO(bbqList);

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
				nameList = new ArrayList<String>();
				timerCategoryMap.put(timerVO.getCategory(), nameList);
			}
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
	public Dialog createDialog() {

		dialog.setContentView(R.layout.timer_selector);
		dialog.setTitle("Select Timer");
		Spinner categorySpinner = (Spinner) dialog.findViewById(R.id.timer_categorySpinner);

		updateCategorySelectBox();
		updateNameSelectBox();
		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapter, View view,int position, long id) {
				TextView categoryTextView = (TextView) view;
				if (categoryTextView != null) {
					updateNameSelectBox();		
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		Spinner nameSpinner = (Spinner) dialog.findViewById(R.id.timer_nameSpinner);
		nameSpinner.refreshDrawableState();

		nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0,
					View view, int position, long id) {
				TextView nameTextView = (TextView) view;
				if (nameTextView != null) {
					updateCutSelectBox();

				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});


		Spinner cutSpinner = (Spinner) dialog.findViewById(R.id.timer_cutSpinner);
		cutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0,
					View view, int position, long id) {
				TextView cutTextView = (TextView) view;
				if (cutTextView != null) {
					updateTextInfo();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});


		Button okButton = (Button) dialog.findViewById(R.id.timer_okBtn);  
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				TimerVO timer = getSelectedItem();
				ExpandableListAdapter timerItemAdapter = bbqList.getTimerItemAdapter();
				UserTimers.getInstance().createTimer(getSelectedItem(), 
						bbqList,
						new Date());
				
				ExpandableListView timerListView = (ExpandableListView) bbqList.findViewById(R.id.TimerListView);
				timerListView.invalidate();
				
				int groupId = timerItemAdapter.getGroupId(timer.getCategory());
				timerListView.expandGroup(groupId);
				dialog.dismiss();
			}
		}); 

		updateTextInfo();

		return dialog;
	}

	private void updateCategorySelectBox() {
		Spinner categorySpinner = (Spinner) dialog.findViewById(R.id.timer_categorySpinner);

		List<String> categoryList = new ArrayList<String>();
		categoryList.addAll(timerCategoryMap.keySet());				

		ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(bbqList, android.R.layout.simple_spinner_item, categoryList);
		categorySpinner.setAdapter(catAdapter);
		categorySpinner.refreshDrawableState();
	}

	private void updateNameSelectBox() {
		Spinner categorySpinner = (Spinner) dialog.findViewById(R.id.timer_categorySpinner);
		Spinner nameSpinner = (Spinner) dialog.findViewById(R.id.timer_nameSpinner);

		String selectedView = (String) categorySpinner.getSelectedItem();
		List<String> nameList = timerCategoryMap.get(selectedView);
		ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(bbqList, android.R.layout.simple_spinner_item, nameList);
		nameSpinner.setAdapter(nameAdapter);
		nameSpinner.refreshDrawableState();
	}

	private void updateCutSelectBox() {		
		Spinner nameSpinner = (Spinner) dialog.findViewById(R.id.timer_nameSpinner);
		String name = (String) nameSpinner.getSelectedItem();

		Spinner cutSpinner = (Spinner) dialog.findViewById(R.id.timer_cutSpinner);
		TextView textView = (TextView) dialog.findViewById(R.id.timer_cutTextView);

		List<String> cutList = timerNameMap.get(name);
		if (cutList == null || cutList.isEmpty()) {
			textView.setVisibility(View.GONE);
			cutSpinner.setVisibility(View.GONE);
			updateTextInfo();
		}
		else {
			textView.setVisibility(View.VISIBLE);
			cutSpinner.setVisibility(View.VISIBLE);

			ArrayAdapter<String> cutAdapter = new ArrayAdapter<String>(bbqList, android.R.layout.simple_spinner_item, cutList);
			cutSpinner.setAdapter(cutAdapter);
			cutSpinner.refreshDrawableState();
		}
	}

	private void updateTextInfo() {
		TimerVO timerVO = getSelectedItem();
		TextView methodView = (TextView) dialog.findViewById(R.id.timer_MethodTextView);
		TextView cookTimeView = (TextView) dialog.findViewById(R.id.timer_CookTimeTextView);

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

		TableLayout intervalTable = (TableLayout) dialog.findViewById(R.id.timer_intervalTableLayout);
//		ScrollView scrollView = (ScrollView) dialog.findViewById(R.id.timer_intervalScroll);
		if (timerVO.getIntervalList() == null || timerVO.getIntervalList().isEmpty()) {
			intervalTable.setVisibility(View.GONE);
		}
		else {
			intervalTable.setVisibility(View.VISIBLE);

			for (TimerIntervalVO intervalVO : timerVO.getIntervalList()) {
				if (intervalTable.findViewWithTag("text" + intervalVO.getId()) == null) {
					TableRow tableRow = new TableRow(bbqList);
					TextView intervalNameTextView = new TextView(bbqList);
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

					TextView intervalTimeTextView = new TextView(bbqList);
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
		Spinner categorySpinner = (Spinner) dialog.findViewById(R.id.timer_categorySpinner);
		String catTextView = (String) categorySpinner.getSelectedItem();

		Spinner nameSpinner = (Spinner) dialog.findViewById(R.id.timer_nameSpinner);
		String nameTextView = (String) nameSpinner.getSelectedItem();

		Spinner cutSpinner = (Spinner) dialog.findViewById(R.id.timer_cutSpinner);
		String selectedView  = null;
		List<String> cutList = timerNameMap.get(nameTextView);
		if (cutList != null && !cutList.isEmpty())
			selectedView = (String) cutSpinner.getSelectedItem();

		String  key = catTextView + ":" + 
		nameTextView;

		if (selectedView != null) {
			key += ":" + selectedView;
		}

		return timerVOMap.get(key);
	}
}
