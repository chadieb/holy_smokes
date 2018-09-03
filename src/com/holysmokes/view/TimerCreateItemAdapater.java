package com.holysmokes.view;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holysmokes.R;
import com.holysmokes.data.value.TimerVO;

public class TimerCreateItemAdapater extends ArrayAdapter<TimerVO> {
	public static final String INSTANT_TYPE="instant";
	int resource;
	private Context context = null;
	private LayoutInflater mInflater;


	public TimerCreateItemAdapater(Context context,
			int resource,
			List<TimerVO> timerList) {
		super(context, resource, timerList);
		this.resource = resource;
		this.context = context;
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position,
			View convertView,
			ViewGroup parent) {

		TimerVO item = getItem(position);
		
		if (convertView == null) {
			convertView = mInflater.inflate(resource, null);		
		}


		TextView nameView = (TextView) convertView.findViewById(R.id.timer_createItem_nameTextView);
		TextView cutView= (TextView) convertView.findViewById(R.id.timer_createItem_cutTextView);
		TextView timerView = (TextView) convertView.findViewById(R.id.timer_createItem_timeTextView);
		
		if (item.isCategory()) {
			convertView.setBackgroundColor(Color.BLACK);
			convertView.setPadding(0, 0, 0, 1);
			
			nameView.setText(item.getCategory());
			nameView.setTextSize(25f);
			nameView.setBackgroundColor(Color.rgb(210, 0, 0));
			nameView.setTextColor(Color.WHITE);
			nameView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);

			lp.setMargins(0, 2, 2, 0);
			nameView.setLayoutParams(lp);
			
			cutView.setVisibility(View.GONE);
			timerView.setVisibility(View.GONE);
		}
		else {
			nameView.setText(item.getName());
			
			if (item.getMeetCut() == null || item.getMeetCut().equals(INSTANT_TYPE)) {
				cutView.setVisibility(View.GONE);
			}
			else {
				cutView.setText(item.getMeetCut());
			}
			

			int cookTimeVal = item.getCookTime();

			int hours = cookTimeVal / 60;
			int minutes = cookTimeVal - (hours * 60);
			String cookTime = hours + " hr " + minutes + " min ";
			if (hours == 0) {
				cookTime = minutes + " min ";
			}
			timerView.setText(cookTime);
		}
			
		return convertView;
	}
}
