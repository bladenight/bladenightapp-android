package de.greencity.bladenightapp.android.map;

import java.util.HashMap;

import org.mapsforge.android.maps.overlay.Marker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.social.Friends;
import de.greencity.bladenightapp.android.utils.DistanceFormatting;
import de.greencity.bladenightapp.network.messages.FriendMessage;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class ProcessionProgressBar extends ProgressBar {
	public ProcessionProgressBar(Context context) {
		super(context);
		init(context);
	}

	public ProcessionProgressBar(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		init(context);
	}  

	public ProcessionProgressBar(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		textPaint = new Paint();  
		textPaint.setColor(Color.BLACK);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(20);
		realTimeUpdateData = new RealTimeUpdateData();
		if ( isInEditMode() ) {
			setDemoData();
		}
		friends = new Friends(context);
		friends.load();
	}

	public void onResume() {
		// reload colors:
		friends.load();
	}

	public void update(RealTimeUpdateData realTimeUpdateData) {
		invalidate();

		if ( realTimeUpdateData == null )
			return;

		this.realTimeUpdateData = realTimeUpdateData;
	}


	private void setDemoData() {
		realTimeUpdateData.setRouteLength(20000);
		realTimeUpdateData.setTail(new MovingPointMessage(5000, 0, true));
		realTimeUpdateData.setHead(new MovingPointMessage(14000, 0, true));
		realTimeUpdateData.setUser(new MovingPointMessage(9000, 0, true));
		FriendMessage friendMessage = new FriendMessage();
		friendMessage.setPosition(10000);
		friendMessage.isInProcession(true);
		realTimeUpdateData.fri.put(1, friendMessage);
	}

	@Override  
	protected synchronized void onDraw(Canvas canvas) {
		getBackgroundDrawable().draw(canvas);
		drawProcession(canvas);
		drawFriends(canvas);
		drawUser(canvas);
		drawTexts(canvas);
	} 


	protected void drawProcession(Canvas canvas) {
		int margin = 2;
		if ( ! isPointOnRoute(realTimeUpdateData.getHead()) || ! isPointOnRoute(realTimeUpdateData.getTail()) )
			return;
		int x1 = convertDistanceToPixels(realTimeUpdateData.getTailPosition());
		int x2 = convertDistanceToPixels(realTimeUpdateData.getHeadPosition());
		if ( x2 - x1 < 2 ) {
			x1 -= 2;
			x2 += 2;
		}

		getProcessionDrawable().setBounds(x1, margin, x2, getHeight()-margin);
		getProcessionDrawable().draw(canvas);
	}

	protected boolean isPointOnRoute(MovingPointMessage point) {
		return point.isOnRoute() && point.getPosition() >= 0;
	}


	protected void drawUser(Canvas canvas) {
		drawMovingPoint(canvas, getUserDrawable(), realTimeUpdateData.getUser());
	}

	protected void drawFriends(Canvas canvas) {
		for (Integer friendId: realTimeUpdateData.fri.keySet()) {
			drawMovingPoint(canvas, getFriendDrawable(friendId), realTimeUpdateData.fri.get(friendId));
		}
	}

	protected void drawMovingPoint(Canvas canvas, Drawable drawable, MovingPointMessage mp) {
		int width = 4;
		int margin = 4;
		if ( ! isPointOnRoute(mp) )
			return;
		double position = mp.getPosition();
		int positionPx = convertDistanceToPixels(position);
		int halfWidth = Math.max(width/2, 1);
		drawable.setBounds(positionPx-halfWidth, margin, positionPx+halfWidth, getHeight()-margin);
		drawable.draw(canvas);
	}


	protected void drawTexts(Canvas canvas) {
		if ( realTimeUpdateData.getRouteLength() <= 0.0 )
			return;

		String routeLengthString = DistanceFormatting.getDistanceAsString(realTimeUpdateData.getRouteLength(), true);
		Rect routeLengthBounds = new Rect();  
		textPaint.getTextBounds(routeLengthString, 0, routeLengthString.length(), routeLengthBounds);
		int routeLenghtLeftPosition = getWidth() - routeLengthBounds.width(); 
		canvas.drawText(
				routeLengthString,
				routeLenghtLeftPosition,
				getHeight() - routeLengthBounds.height() / 2,
				textPaint);  

		int headPositionLeftPosition = routeLenghtLeftPosition;
		if ( isPointOnRoute(realTimeUpdateData.getHead()) ) {
			double headPosition = realTimeUpdateData.getHeadPosition();
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


		if ( isPointOnRoute(realTimeUpdateData.getTail()) ) {
			double tailPosition = realTimeUpdateData.getTailPosition();
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
		return (int)(getWidth() * distance / realTimeUpdateData.getRouteLength());
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

		int[] colors = new int[2];
		int color = Friends.getOwnColor(context);
		colors[0] = color;
		colors[1] = color;

		userDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
		return userDrawable;
	}

	private Drawable getFriendDrawable(int friendId) {
		if ( friendDrawables.get(friendId) != null)
			return friendDrawables.get(friendId);
		
		int color = friends.get(friendId).getColor();

		int[] colors = new int[2];
		//		colors[0] = getResources().getColor();
		colors[0] = color;
		colors[1] = color;
		
		Drawable friendDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
		friendDrawables.put(friendId, friendDrawable);
		return friendDrawable;
	}

	private Context context;
	private Paint textPaint;
	final static String TAG = "ProcessionProgressBar";
	private Drawable processionDrawable;
	private Drawable backgroundDrawable;
	private Drawable userDrawable;
	private HashMap<Integer, Drawable> friendDrawables = new HashMap<Integer, Drawable>();
	private RealTimeUpdateData realTimeUpdateData;
	private Friends friends;
}
