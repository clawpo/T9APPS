package com.runpkg.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MyToggle extends View implements OnTouchListener {

	private Bitmap bkgSwitchOn;
	private Bitmap bkgSwitchOff;
	private Bitmap btnSlip;
	private boolean toggleStateOn;// 当前开关是否为开启状态
	private OnToggleStateListener toggleStateListener;
	private boolean isToggleStateListenerOn;//记录开关·当前的状态
	private float proX;
	private float currentX;
	private boolean isSlipping;//是否处于滑动状态
	private boolean proToggleState = false;//记录上一次开关的状态
	private Rect rect_on;
	private Rect rect_off;

	public MyToggle(Context context) {
		super(context);
		init(context);
	}
	
	public MyToggle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setOnTouchListener(this);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(event.getX() > bkgSwitchOn.getWidth() || event.getY() > bkgSwitchOn.getHeight()){
				return false;
			}
			proX = event.getX(); 
			currentX = proX;
			
			isSlipping = true;
			break;

		case MotionEvent.ACTION_MOVE:
			currentX = event.getX();
			break;

		case MotionEvent.ACTION_UP:
			isSlipping = false;
			
			
			if(currentX < bkgSwitchOn.getWidth() / 2 ){//处于关闭状态
				toggleStateOn = false;
			} else { // 处于开启s状态
				toggleStateOn = true;
			}
			
			// 如果使用了开关监听器，同时开关的状态发生了改变，这时使用该代码
			if(isToggleStateListenerOn && toggleStateOn != proToggleState){
				proToggleState = toggleStateOn;
				toggleStateListener.onToggleState(toggleStateOn);
			}

			break;
		}
		
		invalidate();//重绘

		return true;
	}
	
	

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int left_slip = 0; //用来记录我们滑动块的位置
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		
		if(currentX < bkgSwitchOn.getWidth() / 2){//在画布上绘制出开关状态为关闭时的  背景图片
			canvas.drawBitmap(bkgSwitchOff, matrix, paint);
		} else {//在画布上绘制出开关状态为开启时的  背景图片
			canvas.drawBitmap(bkgSwitchOn, matrix, paint);
		}
		
		if(isSlipping){//开关是否处于滑动状态
			if(currentX > bkgSwitchOn.getWidth()){// 滑动块 是否超过了整个滑动按钮的宽度 
				left_slip = bkgSwitchOn.getWidth() - btnSlip.getWidth();//指定下滑动块的位置
			} else {//设置当前滑动块的位置
				left_slip = (int) (currentX - btnSlip.getWidth() /2);
			}
		} else {//开关是否处于   不滑动状态 
			if(toggleStateOn){
				left_slip = rect_on.left;
			} else {
				left_slip = rect_off.left;
			}
		}
		
		if(left_slip < 0){
			left_slip = 0;
		} else if( left_slip > bkgSwitchOn.getWidth() - btnSlip.getWidth()){
			left_slip = bkgSwitchOn.getWidth() - btnSlip.getWidth();
		}
		
		canvas.drawBitmap(btnSlip, left_slip, 0, paint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(bkgSwitchOn.getWidth(), bkgSwitchOn.getHeight());
	}
	
	

	public void setImageRes(int bkgSwitch_on, int bkgSwitch_off, int btn_Slip) {
		bkgSwitchOn = BitmapFactory
				.decodeResource(getResources(), bkgSwitch_on);
		bkgSwitchOff = BitmapFactory.decodeResource(getResources(),
				bkgSwitch_off);
		btnSlip = BitmapFactory.decodeResource(getResources(), btn_Slip);

		rect_on = new Rect(bkgSwitchOn.getWidth() - btnSlip.getWidth(), 0,
				bkgSwitchOn.getWidth(), btnSlip.getHeight());

		rect_off = new Rect(0, 0, btnSlip.getWidth(), btnSlip.getHeight());
	}

	public void setToggleState(boolean state) {
		toggleStateOn = state;
	}

	public interface OnToggleStateListener {
		abstract void onToggleState(boolean state);
	}

	public void setOnToggleStateListener(OnToggleStateListener listener) {
		toggleStateListener = listener;
		isToggleStateListenerOn = true;
	}

}
