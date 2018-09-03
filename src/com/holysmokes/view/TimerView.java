package com.holysmokes.view;

import java.text.DecimalFormat;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.holysmokes.R;
import com.holysmokes.bean.TimerBean;
import com.holysmokes.data.value.TimerIntervalVO;
import com.holysmokes.util.ActivityUtil;
import com.holysmokes.util.UserTimers;

public class TimerView extends View {
	public final static int STATE_BEGIN=0;
	public final static int STATE_RUN=1;
	public final static int STATE_PAUSE=2;
	public final static int STATE_COMPLETE=3;
	private final static double TEXT_MINUTE_MULTIPLIER=6.5;
	private final static double TEXT_HOUR_MULTIPLIER=4.4;

	private int state = STATE_BEGIN;

	private final static String mPattern = "00";
	private static DecimalFormat mFormatter = new DecimalFormat(mPattern);

	private final OnClickListener listenerAdapter = new OnClickListener();


	//timer info
	UserTimers userTimers = UserTimers.getInstance();
	private TextView cookTypeView = null;

	protected final int ARC_WIDTH = 20;

	protected final Paint arcPaint = new Paint();
	protected final Paint invertArcPaint = new Paint();

	protected final Paint countDownTextPaint = new Paint();

	protected final Paint finishedTextPaint = new Paint();
	protected final Paint intervalTextPaint = new Paint();

	protected final Paint playTriangle = new Paint();
	protected final Paint pauseSquare1 = new Paint();
	protected final Paint pauseSquare2 = new Paint();

	protected Double textSize =null;


	boolean started = false;

	public TimerView(Context context) {
		super(context);
		init();

	}

	public TimerView(Context context, AttributeSet attrs) {
		super( context, attrs);
		init();
	}

	private void init() {
		this.
		textSize = new Double(this.getWidth() / 3.2);
		this.setBackgroundColor(Color.WHITE);
		this.setBackgroundResource(R.drawable.timer_edge);
		this.countDownTextPaint.setARGB(0, 255, 255, 255);

		this.countDownTextPaint.setAntiAlias(true);
		this.countDownTextPaint.setFakeBoldText(true);
		this.countDownTextPaint.setColor(Color.WHITE);

		this.finishedTextPaint.setARGB(255, 0, 0, 0);
		this.finishedTextPaint.setAntiAlias(true);
		this.finishedTextPaint.setFakeBoldText(true);

		this.intervalTextPaint.setARGB(255, 0, 0, 0);
		this.intervalTextPaint.setAntiAlias(true);
		this.intervalTextPaint.setFakeBoldText(true);
		this.intervalTextPaint.setColor(Color.WHITE);

//		this.arcPaint.setColor(Color.rgb(255, 83, 13));
		this.arcPaint.setColor(Color.rgb(255, 0, 10));
		this.arcPaint.setAntiAlias(true);
		this.arcPaint.setStyle(Style.FILL_AND_STROKE);
		this.arcPaint.setStrokeWidth(1);	

		//		this.invertArcPaint.setARGB(200, 255, 0, 20);
		invertArcPaint.setColor(Color.rgb(9, 113, 178));
		this.invertArcPaint.setAntiAlias(true);
		this.invertArcPaint.setStyle(Style.FILL_AND_STROKE);
		this.invertArcPaint.setStrokeWidth(1);	

		this.playTriangle.setColor(Color.WHITE);
		this.playTriangle.setAntiAlias(true);
		this.playTriangle.setStyle(Style.FILL_AND_STROKE);

		this.pauseSquare1.setColor(Color.WHITE);
		this.pauseSquare1.setAntiAlias(true);
		this.pauseSquare1.setStyle(Style.FILL);

		this.pauseSquare2.setColor(Color.WHITE);
		this.pauseSquare2.setAntiAlias(true);
		this.pauseSquare2.setStyle(Style.FILL);
	}

	protected void onDraw(Canvas canvas) {
		TimerBean bean = userTimers.getTimer((String)getTag());

		if (bean == null) {
			return;
		}

		state = bean.getState();
		
		int mySecondsPassed = bean.getTimepassed();
		int mySecondsTotal = bean.getTotalTime() * 60;
		int secondsLeft = mySecondsTotal - mySecondsPassed;

		if(state == STATE_RUN && secondsLeft <= 0){
			state = STATE_COMPLETE;
			bean.setState(STATE_COMPLETE);
			NotificationManager notifier = (NotificationManager)this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            ActivityUtil.sendNotification(notifier, this.getContext(), bean.getTimerVO().getName() + " has completed.");
		}

		//handle the interval if we are showing it
		String intervalName = null;
		if (bean.isShowInterval()) {
			TimerIntervalVO intervalVO = bean.getInterval(this.getContext(), cookTypeView);
			if (intervalVO != null) {
				secondsLeft = bean.getIntervalTime();
				intervalName = intervalVO.getName();				
			}
		}



		double textSizeMult = 3.5;
		int hours = secondsLeft / 3600;
		int minutes = secondsLeft / 60;
		if (hours > 0) {
			minutes = (secondsLeft - (hours * 3600)) / 60;
			textSizeMult = 5;
		}
		int seconds = secondsLeft - ((minutes * 60) + (hours * 3600));

		textSize = new Double(this.getWidth() / textSizeMult);
		this.countDownTextPaint.setTextSize(textSize.intValue());
		this.finishedTextPaint.setTextSize(textSize.intValue());


		RectF arcRect = new RectF(ARC_WIDTH / 2, 
				ARC_WIDTH / 2,
				this.getWidth() - ARC_WIDTH / 2,
				this.getHeight() - ARC_WIDTH / 2);


		String timeDisplayString;

		Double yOffSetTxt = new Double(this.getHeight() / 2 + (textSize.intValue() / 3));

		switch (state) {
		case STATE_BEGIN :
			invertArcPaint.setColor(Color.rgb(9, 113, 178));

			canvas.drawArc(arcRect, -90, 360, true, this.invertArcPaint);
			Double xOffSetDbl = new Double(this.getWidth() / 2.5);
			float xOffSet = xOffSetDbl.floatValue();

			Double yOffSetDble = new Double(this.getHeight() / 2.85);
			float yOffSet = yOffSetDble.floatValue();

			//show the play symbole (triangle)
			Path path = new Path();
			path.moveTo(0, yOffSet);
			path.lineTo(xOffSet, 0);
			path.lineTo(0, -yOffSet);
			path.close();

			xOffSetDbl = new Double(this.getWidth() / 2.6);
			xOffSet = xOffSetDbl.floatValue();

			path.offset(xOffSet, this.getHeight()/ 2);
			canvas.drawPath(path, this.playTriangle);
			break;
		case STATE_RUN :
			
			double textMultiplier = TEXT_MINUTE_MULTIPLIER;
			if (hours > 0) {
				textMultiplier = TEXT_HOUR_MULTIPLIER;
			}


			countDownTextPaint.setColor(Color.WHITE);
			if (hours > 0) {
				timeDisplayString = "" + mFormatter.format(hours) + ":" + mFormatter.format(minutes) + ":" + mFormatter.format(seconds);
			}
			else {
				timeDisplayString = "" + mFormatter.format(minutes) + ":" + mFormatter.format(seconds);
			}


			canvas.drawArc(arcRect, -90, 360, true, this.invertArcPaint);

			float arcAngle = ((mySecondsPassed * 1.0f) / mySecondsTotal) * 360;
			canvas.drawArc(arcRect, -90, arcAngle, true, this.arcPaint);

			Double xOffSetTxt = new Double(this.getWidth() / 2 - (timeDisplayString.length() * textMultiplier));
			canvas.drawText(timeDisplayString,
					xOffSetTxt.floatValue(),
					yOffSetTxt.floatValue(), //minus the text heigh,
					this.countDownTextPaint);	
			if (intervalName != null) {
				xOffSetTxt = new Double(this.getWidth() / 2 - (intervalName.length() * 3.9));
				this.intervalTextPaint.setTextSize(14);
//				canvas.drawText(intervalName,
//						xOffSetTxt.floatValue(),
//						this.getHeight()/ 2 + 20, //minus the text heigh,
//						this.intervalTextPaint);
			}
			this.postInvalidate();
			break;
		case STATE_PAUSE :
			if (hours > 0) {
				timeDisplayString = "" + mFormatter.format(hours) + ":" + mFormatter.format(minutes) + ":" + mFormatter.format(seconds);
			}
			else {
				timeDisplayString = "" + mFormatter.format(minutes) + ":" + mFormatter.format(seconds);
			}

			double textSpacerMultiplier = TEXT_MINUTE_MULTIPLIER;
			if (hours > 0) {
				textSpacerMultiplier = TEXT_HOUR_MULTIPLIER;
			}
			xOffSetTxt = new Double(this.getWidth() / 2 - (timeDisplayString.length() * textSpacerMultiplier));

			canvas.drawArc(arcRect, -90, 360, true, this.invertArcPaint);

			arcAngle = ((mySecondsPassed * 1.0f) / mySecondsTotal) * 360;
			canvas.drawArc(arcRect, -90, arcAngle, true, this.arcPaint);

			countDownTextPaint.setColor(Color.GRAY);

			canvas.drawText(timeDisplayString,
					xOffSetTxt.floatValue(),
					yOffSetTxt.floatValue(),  //minus the text heigh,
					this.countDownTextPaint);	

			//draw the first pause bar
			int pauseBarWidth = 7;
			int leftBarSpace = 3;
			int rightBarSpace = 3;

			int rectLeft = this.getWidth() / 2 - pauseBarWidth - leftBarSpace;
			int rectRight = this.getWidth() / 2 - leftBarSpace;
			int recTop = this.getHeight() /2 + 20;
			int recBottom = this.getHeight() /2 - 20;
			canvas.drawRect(rectLeft, recTop, rectRight, recBottom, pauseSquare1);

			rectLeft = this.getWidth() / 2 + rightBarSpace;
			rectRight = this.getWidth() / 2 + pauseBarWidth + rightBarSpace;
			canvas.drawRect(rectLeft, recTop, rectRight, recBottom, pauseSquare2);

			break;
		case STATE_COMPLETE :
			this.invertArcPaint.setColor(Color.rgb(9, 113, 178));
			canvas.drawArc(arcRect, -90, 360, true, this.invertArcPaint);

			timeDisplayString = "DONE";
			this.countDownTextPaint.setTextSize(22);
			xOffSetTxt = new Double(this.getWidth() / 2 - (timeDisplayString.length() * 7.5));
			canvas.drawText(timeDisplayString,
					xOffSetTxt.floatValue(),
					this.getHeight()/ 2 + 8, //minus the text heigh,
					this.countDownTextPaint);
		}
	}

	public void setOnClickListener(OnClickListener newListener) {
		listenerAdapter.setListener(newListener);
	}

	public void longClick() {
		if (state == STATE_BEGIN || state == STATE_COMPLETE) {
			return;
		}

		TimerBean timerBean = userTimers.getTimer((String)getTag());
		timerBean.setShowInterval(!timerBean.isShowInterval());
	}

	public boolean getShowInterval() {
		TimerBean timerBean = userTimers.getTimer((String)getTag());
		if (timerBean != null)
			return timerBean.isShowInterval();
		else
			return false;
	}

	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect)
	{
		if (gainFocus == true)
		{
			invertArcPaint.setColor(Color.RED);
		}
		else
		{
			this.setBackgroundColor(Color.BLUE);
		}
	}


	public static class OnClickListener implements View.OnClickListener {	
		@SuppressWarnings("unused")
		private View.OnClickListener listener = null;

		public void setListener(View.OnClickListener newListener) {
			listener = newListener;
		}

		public void onClick(View v) {
		}
	}


	public TextView getCookTypeView() {
		return cookTypeView;
	}

	public void setCookTypeView(TextView cookTypeView) {
		this.cookTypeView = cookTypeView;
	}

}
