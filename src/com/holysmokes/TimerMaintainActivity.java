package com.holysmokes;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.DAO;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.view.ExpandableListAdapter;

public class TimerMaintainActivity extends Activity  {
	private static final int DIALOG_DELETE_ID = 0;
	private ExpandableListAdapter adapter = null;
	private static List<TimerVO> timerList = new ArrayList<TimerVO>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer_maintain_layout);

		adapter = new  ExpandableListAdapter(this, new ArrayList<String>(),
				R.layout.timer_category_layout,
				new ArrayList<ArrayList<TimerBean>>(),
				R.layout.timer_child_layout);

		DAO dao = new DAO(this);
		List<TimerVO> timerList = dao.getAllTimersCategory();
		for (TimerVO timerVO : timerList) {
			TimerBean bean = new TimerBean();
			bean.setTimerVO(timerVO);
			adapter.addItem(bean);
		}

		ExpandableListView listView = (ExpandableListView) findViewById(R.id.timerMaintainList);
		listView.setAdapter(adapter);
		listView.setOnCreateContextMenuListener(this);

		listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id){
				TimerBean timerBean = (TimerBean) adapter.getChild(groupPosition, childPosition);
				Intent intent = new Intent(view.getContext(), TimerCreateActivity.class);
				intent.putExtra("timer", timerBean.getTimerVO());
				intent.putExtra("previousIntent", TimerMaintainActivity.class);
				startActivity(intent);
				
				return true;
			}
		});
	}

	public static void addTimerToList(TimerVO timer) {
		timerList.add(timer);
	}
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = this.getMenuInflater();    
		inflater.inflate(R.menu.timer_maintain_context_menu, menu);  
		menu.setHeaderTitle("Timers Menu");
	}

	public boolean onContextItemSelected(MenuItem item)
	{
		ExpandableListView.ExpandableListContextMenuInfo exandableMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		//		final TimerVO timer = adapter.getItem(adapterMenuInfo.position);
		TimerBean timerBean = null;
		
		int groupPosition = -1;
		int childPosition = -1;
		if (ExpandableListView.getPackedPositionType (exandableMenuInfo.packedPosition) ==  ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPosition = ExpandableListView.getPackedPositionGroup(exandableMenuInfo.packedPosition);
			childPosition = ExpandableListView.getPackedPositionChild(exandableMenuInfo.packedPosition);
			
			timerBean = (TimerBean) adapter.getChild(groupPosition, childPosition);
		}
		
		
		if (timerBean == null) {
			return true;
		}
		
		final TimerVO deleteTimer = timerBean.getTimerVO();
		final int deleteGroupPosition = groupPosition;
		final int deleteChildPosition = childPosition;
		
		final DAO dao = new DAO(this);

		switch (item.getItemId()) {
		case R.id.timer_maintain_delete: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to delete " + timerBean.getTimerVO().getName() + "?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dao.deleteTimer(deleteTimer.getId());
					adapter.remove(deleteGroupPosition,deleteChildPosition);
					adapter.notifyDataSetChanged();
					//						Toast.makeText(this, "Timer " + timer.getName() + " deleted.\n", Toast.LENGTH_LONG).show();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();

			break;
		}
		case R.id.timer_maintain_add: {
			Intent intent = new Intent(this, TimerCreateActivity.class);
			intent.putExtra("previousIntent", TimerMaintainActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.timer_maintain_edit: {
			Intent intent = new Intent(this, TimerCreateActivity.class);
			intent.putExtra("timer",timerBean.getTimerVO());
			intent.putExtra("previousIntent", TimerMaintainActivity.class);
			startActivity(intent);
		}
		}
		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();    
		inflater.inflate(R.menu.timer_maintain_menu, menu); 

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.timer_maintain_timerList: {
			Intent intent = new Intent(this, BbqListActivity.class);
			intent.putParcelableArrayListExtra("timerList", (ArrayList<? extends Parcelable>)timerList);
			startActivity(intent);
			break;
		}
		case R.id.timer_maintain_add: {
			Intent intent = new Intent(this, TimerCreateActivity.class);
			startActivity(intent);
			break;
		}
		}
		return true;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id) {
		case DIALOG_DELETE_ID:
			// do the work to define the pause Dialog
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

}
