package de.greencity.bladenightapp.android.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import de.greencity.bladenightapp.android.utils.DistanceFormatting;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class ProcessionProgressBar extends ProgressBar {
	public ProcessionProgressBar(Context context) {
		super(context);
		init();
	}

	public ProcessionProgressBar(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		init();
	}  

	public ProcessionProgressBar(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);
		init();
	}

	private void init() {

		textPaint = new Paint();  
		textPaint.setColor(Color.WHITE);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(20);

		processionOutlinePaint = new Paint();
		processionOutlinePaint.setStyle(Paint.Style.STROKE);
		processionOutlinePaint.setColor(Color.rgb(0, 0, 0));

		processionInfillPaint = new Paint();
		processionInfillPaint.setColor(Color.rgb(0, 140, 40));


		userRectPaint = new Paint();
		userRectPaint.setColor(Color.rgb(255, 0, 0));

		routeLength  = -1;
		tailPosition = -1;
		headPosition = -1;
		userPosition = -1;

		// setDemoData();
	}

	@SuppressWarnings("unused")
	private void setDemoData() {
		routeLength = 20000;
		tailPosition = 5000;
		headPosition = 14000;
		userPosition = 7000;
	}

	@Override  
	protected synchronized void onDraw(Canvas canvas) {
		getBackgroundDrawable().draw(canvas);
		userPosition = 7000;
		drawProcession(canvas);
		drawUser(canvas);
		drawTexts(canvas);
	} 

	protected void drawProcession(Canvas canvas) {
		int margin = 2;
		if ( tailPosition < 0.0 || headPosition < 0.0 )
			return;
		int x1 = convertDistanceToPixels(tailPosition);
		int x2 = convertDistanceToPixels(headPosition);
		if ( x2 - x1 < 2 ) {
			x1 -= 2;
			x2 += 2;
		}

		getProcessionDrawable().setBounds(x1, margin, x2, getHeight()-margin);
		getProcessionDrawable().draw(canvas);
	}


	protected void drawUser(Canvas canvas) {
		int width = 6;
		int margin = 4;
		if ( userPosition < 0.0)
			return;
		getUserDrawable().setBounds(convertDistanceToPixels(userPosition), margin, convertDistanceToPixels(userPosition)+width, getHeight()-margin);
		getUserDrawable().draw(canvas);
	}


	protected void drawTexts(Canvas canvas) {
		if ( routeLength < 0.0 )
			return;

		String routeLengthString = DistanceFormatting.getDistanceAsString(routeLength, true);
		Rect routeLengthBounds = new Rect();  
		textPaint.getTextBounds(routeLengthString, 0, routeLengthString.length(), routeLengthBounds);
		int routeLenghtLeftPosition = getWidth() - routeLengthBounds.width(); 
		canvas.drawText(
				routeLengthString,
				routeLenghtLeftPosition,
				getHeight() - routeLengthBounds.height() / 2,
				textPaint);  

		int headPositionLeftPosition = routeLenghtLeftPosition;
		if ( headPosition >= 0.0 ) {
			String headPositionString = DistanceFormatting.getDistanceAsString(headPosition, true);
			Rect headPositionBounds = new Rect();  
			textPaint.getTextBounds(headPositionString, 0, headPositionString.length(), headPositionBounds);
			headPositionLeftPosition = convertDistanceToPixels(headPosition) - headPositionBounds.width() / 2;
			if ( headPositionLeftPosition <= 0 )
				headPositionLeftPosition = 1;
			if ( headPositionLeftPosition + headPositionBounds.width() < routeLenghtLeftPosition ) {
				canvas.drawText(
						headPositionString,
						headPositionLeftPosition,
						getHeight() - headPositionBounds.height() / 2,
						textPaint);  
			}
		}


		if ( tailPosition >= 0.0 ) {
			String tailPositionString = DistanceFormatting.getDistanceAsString(tailPosition, true);
			Rect tailPositionBounds = new Rect();  
			textPaint.getTextBounds(tailPositionString, 0, tailPositionString.length(), tailPositionBounds);
			int tailPositionLeftPosition = convertDistanceToPixels(tailPosition) - tailPositionBounds.width() / 2;
			if ( tailPositionLeftPosition > 0 && tailPositionLeftPosition + tailPositionBounds.width() < headPositionLeftPosition ) {
				canvas.drawText(
						tailPositionString,
						tailPositionLeftPosition,
						getHeight() - tailPositionBounds.height() / 2,
						textPaint);  
			}
		}
	}

	protected int convertDistanceToPixels(double distance) {
		return (int)(getWidth() * distance / routeLength);
	}

	public void setTextColor(int color) {  
		textPaint.setColor(color);  
		drawableStateChanged();  
	}  

	public double getTailPosition() {
		return tailPosition;
	}

	public void setTailPosition(double tailPosition) {
		this.tailPosition = tailPosition;
	}

	public double getHeadPosition() {
		return headPosition;
	}

	public void setHeadPosition(double headPosition) {
		this.headPosition = headPosition;
	}

	public double getUserPosition() {
		return userPosition;
	}

	public void setUserPosition(double userPosition) {
		this.userPosition = userPosition;
	}

	public double getRouteLength() {
		return routeLength;
	}

	public void setRouteLength(double routeLength) {
		this.routeLength = routeLength;
	}

	public void update(RealTimeUpdateData realTimeUpdateData) {
		setHeadPosition(-1);
		setTailPosition(-1);
		setUserPosition(-1);
		invalidate();

		if ( realTimeUpdateData == null )
			return;

		setRouteLength(realTimeUpdateData.getRouteLength());
		setHeadPosition(realTimeUpdateData.getHeadPosition());
		setTailPosition(realTimeUpdateData.getTailPosition());
		setUserPosition(realTimeUpdateData.getUserPosition());
	}

	private Drawable getBackgroundDrawable() {
		if ( backgroundDrawable != null)
			return backgroundDrawable;
		LayerDrawable layerDrawable = (LayerDrawable)getProgressDrawable();
		backgroundDrawable = layerDrawable.getDrawable(0);
		return backgroundDrawable;
	}

	private Drawable getProcessionDrawable() {
		if ( processionDrawable != null)
			return processionDrawable;
		LayerDrawable layerDrawable = (LayerDrawable)getProgressDrawable();
		processionDrawable = layerDrawable.getDrawable(1);
		return processionDrawable;
	}

	private Drawable getUserDrawable() {
		if ( userDrawable != null)
			return userDrawable;
		LayerDrawable layerDrawable = (LayerDrawable)getProgressDrawable();
		userDrawable = layerDrawable.getDrawable(2);
		return userDrawable;
	}

	private Paint textPaint;
	protected double tailPosition;
	protected double headPosition;
	protected double userPosition;
	protected double routeLength;
	protected Paint processionInfillPaint;
	protected Paint processionOutlinePaint;
	protected Paint userRectPaint;
	final static String TAG = "ProcessionProgressBar";
	private Drawable processionDrawable;
	private Drawable backgroundDrawable;
	private Drawable userDrawable;
}
