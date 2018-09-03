package com.holysmokes.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.holysmokes.R;
import com.holysmokes.TimerCreateActivity;
import com.holysmokes.data.value.TimerIntervalVO;

public class TimerIntervalAdapter  extends ArrayAdapter<TimerIntervalVO> {
	
	int resource;
	private Context context = null;
	private LayoutInflater mInflater;


	public TimerIntervalAdapter(Context context,
			int resource,
			List<TimerIntervalVO> timerList) {
		super(context, resource, timerList);
		this.resource = resource;
		this.context = context;
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position,
			View convertView,
			ViewGroup parent) {

		TimerIntervalVO item = getItem(position);
		
		if (convertView == null) {
			convertView = mInflater.inflate(resource, null);		
		}
		
		TextView nameView = (TextView) convertView.findViewById(R.id.create_intervalName);
		nameView.setText(item.getName());
		
		TextView timeView = (TextView) convertView.findViewById(R.id.create_intervalTime);
		int cookTimeVal = item.getCookTime();
		
		ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.create__interval_deleteBtn);
		deleteButton.setTag("" + position);
		
		final TimerIntervalAdapter intervalAdapter = this;
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
		return convertView;
	}
	
	public void removeIntervalItem(int pos) {
		TimerIntervalVO intervalVO = getItem(pos);
		remove(intervalVO);
//		notifyDataSetChanged();
	}
}
