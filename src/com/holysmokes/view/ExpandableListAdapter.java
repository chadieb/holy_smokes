package com.holysmokes.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.holysmokes.R;
import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.value.TimerVO;
import com.holysmokes.util.CountDownRunner;
import com.holysmokes.util.Logger;
import com.holysmokes.util.UserTimers;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;

	private ArrayList<String> groups;

	int groupLayout;

	private ArrayList<ArrayList<TimerBean>> children;

	int childLayout;

	public ExpandableListAdapter(Context context, ArrayList<String> groups,
			int groupLayout,
			ArrayList<ArrayList<TimerBean>> children,
			int childLayout) {
		this.context = context;
		this.groups = groups;
		this.children = children;
		this.groupLayout = groupLayout;
		this.childLayout = childLayout;
	}

    /**
     * A general add method, that allows you to add a TimerVO to this list
     * 
     * Depending on if the category opf the TimerVO is present or not,
     * the corresponding item will either be added to an existing group if it 
     * exists, else the group will be created and then the item will be added
     * @param TimerVO
     */
    public void addItem(TimerBean timerBean) {
        if (!groups.contains(timerBean.getTimerVO().getCategory())) {
            groups.add(timerBean.getTimerVO().getCategory());
        }
        int index = groups.indexOf(timerBean.getTimerVO().getCategory());
        if (children.size() < index + 1) {
            children.add(new ArrayList<TimerBean>());
        }
        children.get(index).add(timerBean);
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    
    public Object getChild(int groupPosition, int childPosition) {
        return children.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    // Return a child view. You can load your custom layout here.
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
		TimerBean timerBean = (TimerBean) getChild(groupPosition, childPosition);
		TimerVO timerVO = timerBean.getTimerVO();
		
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(childLayout, null);
		}

		TimerView timerView = (TimerView) convertView.findViewById(R.id.timerView);
		boolean showInterval = false;
		if (timerView != null) {
			timerView.setTag("TimerView" + timerBean.getId());
			showInterval = timerView.getShowInterval();
		}

		int cookTimeVal = timerVO.getCookTime();
		if (showInterval && !timerVO.getIntervalList().isEmpty()) {
			cookTimeVal = timerVO.getIntervalList().get(0).getCookTime();
		}

		int hours = cookTimeVal / 60;
		int minutes = cookTimeVal - (hours * 60);
		String cookTime = hours + " hr " + minutes + " min ";
		if (hours == 0) {
			cookTime = minutes + " min ";
		}

		TextView nameView = (TextView) convertView.findViewById(R.id.timeItemName);
		nameView.setText(timerVO.getName());
		nameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

		final TextView cookTypeView = (TextView) convertView.findViewById(R.id.timeItemCookType);
		if (timerView != null) {
			if (timerView.getShowInterval() && !timerVO.getIntervalList().isEmpty()) {
				cookTypeView.setText(timerVO.getIntervalList().get(0).getName());
			}
			else {
				cookTypeView.setText(timerVO.getCookType());
			}

			timerView.setCookTypeView(cookTypeView);
		}
		cookTypeView.setTag("TimerView" + timerBean.getId() + "CookType");



		final TextView cookTimeView = (TextView) convertView.findViewById(R.id.timeItemCookTime);
		cookTimeView.setText(cookTime);
		cookTimeView.setTag("CookTime" + timerBean.getId());
		
		//if there isn't a cooktype (like in instant timers) move the cookTimeViewOver
		if (timerVO.getCookType() == null) {
			cookTypeView.setVisibility(View.GONE);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.BELOW, nameView.getId());
			cookTimeView.setLayoutParams(lp);
			cookTimeView.setPadding(0, 0, 0, 0);
		}
		
		TextView subTypeView = (TextView) convertView.findViewById(R.id.timeItemSubType);
		if (timerVO.getMeetCut() == null || timerVO.getMeetCut().trim().length() == 0) {
			subTypeView.setVisibility(View.GONE);
		}
		else {
			subTypeView.setText(timerVO.getMeetCut());
		}


		if (timerView != null) {
			timerView.setOnClickListener( new View.OnClickListener() {
				public void onClick(View arg0) {
					TimerView timerView = (TimerView) arg0;
					String viewTag = (String) timerView.getTag();
					TimerBean bean = UserTimers.getInstance().getTimer(viewTag);
					int state = bean.getState();
					
					switch (state) {
					case TimerView.STATE_BEGIN :
						bean.setStartTime(new Date());
						break;
					case TimerView.STATE_RUN :
						bean.setPauseTimeLeft(bean.getTimepassed());
						break;
					case TimerView.STATE_PAUSE :
				        Calendar calendar = Calendar.getInstance();
				        calendar.setTimeInMillis(System.currentTimeMillis());
						
						calendar.add(Calendar.SECOND,-bean.getPauseTimeLeft());
						bean.setStartTime(calendar.getTime());
						break;
					}					

					Logger.log(this, "Timer view pressed for timer " + bean);
					
					if (bean.getState() != TimerView.STATE_COMPLETE) {
						bean.toggleState();
						timerView.invalidate();
					}
				}

			});


			if (!timerVO.getIntervalList().isEmpty() && timerView != null) {
				timerView.setOnLongClickListener( new OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						TimerView timerView = (TimerView) arg0;
						timerView.longClick();
						timerView.invalidate();

						UserTimers userTimers = UserTimers.getInstance();
						TimerBean timerBean = userTimers.getTimer((String) timerView.getTag());
						TimerVO timerVO = timerBean.getTimerVO();

						int cookTimeVal = timerVO.getCookTime(); //in minutes
						if (timerView.getShowInterval() && !timerVO.getIntervalList().isEmpty()) {
							String intervalTxt = timerVO.getIntervalList().get(0).getName();
							cookTypeView.setText(intervalTxt);
							cookTimeVal = timerVO.getIntervalList().get(0).getCookTime();
						}
						else {
							cookTypeView.setText(timerVO.getCookType());	
						}

						int hours = cookTimeVal / 60;
						int minutes = cookTimeVal - (hours * 60);
						String cookTime = hours + " hr " + minutes + " min ";
						if (hours == 0) {
							cookTime = minutes + " min ";
						}

						cookTimeView.setText(cookTime);
						cookTimeView.invalidate();
						return true;
					}
				});
			}
		}

		return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        return children.get(groupPosition).size();
    }

    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    public int getGroupCount() {
        return groups.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		String group = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(groupLayout, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		tv.setText(group);
		return convertView;
	}


    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }
    
	public TimerVO getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove(int groupId, int itemId) {

		if (groupId > -1) {
			children.get(groupId).remove(itemId);

			if (children.get(groupId).isEmpty()) {
				children.remove(groupId);
				groups.remove(itemId);
			}
		}
	}

	public int getGroupId(String group) {
		return groups.indexOf(group);
	}

	public void clear() {
		children.clear();
		groups.clear();
	}

}
