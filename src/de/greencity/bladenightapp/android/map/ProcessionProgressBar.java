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
import de.greencity.bladenightapp.network.messages.NetMovingPoint;
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

		// setDemoData();
	}

	public void update(RealTimeUpdateData realTimeUpdateData) {
		invalidate();

		if ( realTimeUpdateData == null )
			return;

		setRouteLength(realTimeUpdateData.getRouteLength());
		setHeadPoint(realTimeUpdateData.getHead());
		setTailPoint(realTimeUpdateData.getTail());
		setUserPoint(realTimeUpdateData.getUser());
	}


	@SuppressWarnings("unused")
	private void setDemoData() {
		routeLength = 20000;
		tailPoint = new NetMovingPoint(5000, 0, true);
		headPoint = new NetMovingPoint(14000, 0, true);
		userPoint = new NetMovingPoint(7000, 0, true);
	}

	@Override  
	protected synchronized void onDraw(Canvas canvas) {
		getBackgroundDrawable().draw(canvas);
		drawProcession(canvas);
		drawUser(canvas);
		drawTexts(canvas);
	} 

	protected void drawProcession(Canvas canvas) {
		int margin = 2;
		if ( ! isPointOnRoute(headPoint) || ! isPointOnRoute(tailPoint) )
			return;
		int x1 = convertDistanceToPixels(tailPoint.getPosition());
		int x2 = convertDistanceToPixels(headPoint.getPosition());
		if ( x2 - x1 < 2 ) {
			x1 -= 2;
			x2 += 2;
		}

		getProcessionDrawable().setBounds(x1, margin, x2, getHeight()-margin);
		getProcessionDrawable().draw(canvas);
	}

	protected boolean isPointOnRoute(NetMovingPoint point) {
		return point.isOnRoute() && point.getPosition() >= 0;
	}


	protected void drawUser(Canvas canvas) {
		int width = 6;
		int margin = 4;
		if ( ! isPointOnRoute(userPoint))
			return;
		double userPosition = userPoint.getPosition();
		int userPositionPx = convertDistanceToPixels(userPosition);
		int halfWidth = Math.min(width/2, 1);
		getUserDrawable().setBounds(userPositionPx-halfWidth, margin, convertDistanceToPixels(userPositionPx)+halfWidth, getHeight()-margin);
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
		if ( isPointOnRoute(headPoint) ) {
			double headPosition = headPoint.getPosition();
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


		if ( isPointOnRoute(tailPoint) ) {
			double tailPosition = headPoint.getPosition();
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

	private void setTailPoint(NetMovingPoint tailPoint) {
		this.tailPoint = tailPoint;
	}

	private void setUserPoint(NetMovingPoint userPoint) {
		this.userPoint = userPoint;
	}

	private  void setHeadPoint(NetMovingPoint headPoint) {
		this.headPoint = headPoint;
	}

	public double getRouteLength() {
		return routeLength;
	}

	public void setRouteLength(double routeLength) {
		this.routeLength = routeLength;
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
	protected NetMovingPoint tailPoint = new NetMovingPoint();
	protected NetMovingPoint headPoint = new NetMovingPoint();
	protected NetMovingPoint userPoint = new NetMovingPoint();
	protected double routeLength;
	protected Paint processionInfillPaint;
	protected Paint processionOutlinePaint;
	protected Paint userRectPaint;
	final static String TAG = "ProcessionProgressBar";
	private Drawable processionDrawable;
	private Drawable backgroundDrawable;
	private Drawable userDrawable;
}
