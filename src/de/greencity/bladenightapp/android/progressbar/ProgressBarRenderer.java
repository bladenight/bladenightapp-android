package de.greencity.bladenightapp.android.progressbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.social.Friend;
import de.greencity.bladenightapp.android.social.Friends;
import de.greencity.bladenightapp.android.utils.DistanceFormatting;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class ProgressBarRenderer {

    private RealTimeUpdateData realTimeUpdateData;
    private Context context;
    private int bitmapWidth;
    private int bitmapHeight;
    private int fontSize = 18;
    private Canvas canvas;
    private DisplayMetrics displayMetrics;

    public ProgressBarRenderer(Context context) {
        this.context = context;
    }

    public void updateRealTimeUpdateData(RealTimeUpdateData realTimeUpdateData) {
        this.realTimeUpdateData = realTimeUpdateData;
    }

    public Bitmap renderToBitmap(int bitmapWidth, int bitmapHeight, DisplayMetrics displayMetrics) {
        if (bitmapWidth == 0) {
            Log.e("ProgressBarRenderer", "bitmapWidth==0");
            bitmapWidth = 1;
        }
        if (bitmapHeight == 0) {
            Log.e("ProgressBarRenderer", "bitmapHeight==0");
            bitmapHeight = 1;
        }

        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        this.displayMetrics = displayMetrics;

        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        drawBackground();

        if (realTimeUpdateData == null)
            return bitmap;

        drawProcession();

        drawFriends(canvas);

        drawTexts(canvas);

        return bitmap;
    }

    private void drawBackground() {
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(context.getResources().getColor(R.color.new_background));

        canvas.drawRect(0, 0, bitmapWidth, bitmapHeight, backgroundPaint);
    }

    private void drawProcession() {
        Paint processionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        processionPaint.setColor(context.getResources().getColor(R.color.new_bar));

        int left = (int) (1.0 * bitmapWidth * realTimeUpdateData.tai.getPosition() / realTimeUpdateData.rle);
        int right = (int) (1.0 * bitmapWidth * realTimeUpdateData.hea.getPosition() / realTimeUpdateData.rle);

        canvas.drawRect(left, 0, right, bitmapHeight, processionPaint);

        int strokeWidth = 3;
        int halfstrokeWidth = strokeWidth / 2;
        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(strokeWidth);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(
                    left + halfstrokeWidth, halfstrokeWidth,
                    right - halfstrokeWidth, bitmapHeight - halfstrokeWidth,
                    strokeWidth * 2,
                    strokeWidth * 2,
                    outlinePaint
            );
        }
        else {
            canvas.drawRect(
                    left + halfstrokeWidth, halfstrokeWidth,
                    right - halfstrokeWidth, bitmapHeight - halfstrokeWidth,
                    outlinePaint
            );
        }
    }

    private void drawFriends(Canvas canvas) {
        Friends friends = new Friends(context);
        friends.load();
        for (Integer friendId : realTimeUpdateData.fri.keySet()) {
            Friend friend = friends.get(friendId);
            int color;
            if (friend == null)
                color = context.getResources().getColor(R.color.black);
            else
                color = friend.getColor();
            drawMovingPoint(canvas, color, realTimeUpdateData.fri.get(friendId));
        }
        drawMovingPoint(canvas, Friends.getOwnColor(context), realTimeUpdateData.up);
    }

    protected void drawMovingPoint(Canvas canvas, int color, MovingPointMessage mp) {
        int lineWidth = 6;
        int margin = 4;
        if (!isPointOnRoute(mp))
            return;
        double position = mp.getPosition();
        int positionPx = convertDistanceToPixels(position);
        int halfWidth = Math.max(lineWidth / 2, 1);

        int left = positionPx - halfWidth;
        int right = positionPx + halfWidth;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        canvas.drawRect(left, 0, right, bitmapHeight, paint);

    }

    protected boolean isPointOnRoute(MovingPointMessage point) {
        return point.isOnRoute() && point.getPosition() >= 0;
    }

    protected int convertDistanceToPixels(double distance) {
        return (int) (bitmapWidth * distance / realTimeUpdateData.getRouteLength());
    }

    private int getResolutionIndependantSize(int pixelSize) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixelSize, displayMetrics);
    }

    private int getResolutionIndependantFontSize() {
        return getResolutionIndependantSize(fontSize);
    }


    protected void drawTexts(Canvas canvas) {
        if (realTimeUpdateData.getRouteLength() <= 0.0)
            return;

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(getResolutionIndependantFontSize());

        String routeLengthString = DistanceFormatting.getDistanceAsString(realTimeUpdateData.getRouteLength(), true);
        Rect routeLengthBounds = new Rect();
        textPaint.getTextBounds(routeLengthString, 0, routeLengthString.length(), routeLengthBounds);
        int margin = getResolutionIndependantFontSize() / 4;
        int routeLenghtLeftPosition = bitmapWidth - routeLengthBounds.width() - margin;
        canvas.drawText(
                routeLengthString,
                routeLenghtLeftPosition,
                bitmapHeight / 2 + routeLengthBounds.height() / 2,
                textPaint);

        int headPositionLeftPosition = routeLenghtLeftPosition;
        if (isPointOnRoute(realTimeUpdateData.getHead())) {
            double headPosition = realTimeUpdateData.getHeadPosition();
            String headPositionString = DistanceFormatting.getDistanceAsString(headPosition, true);
            Rect headPositionBounds = new Rect();
            textPaint.getTextBounds(headPositionString, 0, headPositionString.length(), headPositionBounds);
            headPositionLeftPosition = convertDistanceToPixels(headPosition) - headPositionBounds.width() / 2;
            if (headPositionLeftPosition <= 0)
                headPositionLeftPosition = 1;
            if (headPositionLeftPosition + headPositionBounds.width() < routeLenghtLeftPosition) {
                canvas.drawText(
                        headPositionString,
                        headPositionLeftPosition,
                        bitmapHeight / 2 + headPositionBounds.height() / 2,
                        textPaint);
            }
        }


        if (isPointOnRoute(realTimeUpdateData.getTail())) {
            double tailPosition = realTimeUpdateData.getTailPosition();
            String tailPositionString = DistanceFormatting.getDistanceAsString(tailPosition, true);
            Rect tailPositionBounds = new Rect();
            textPaint.getTextBounds(tailPositionString, 0, tailPositionString.length(), tailPositionBounds);
            int tailPositionLeftPosition = convertDistanceToPixels(tailPosition) - tailPositionBounds.width() / 2;
            if (tailPositionLeftPosition > 0 && tailPositionLeftPosition + tailPositionBounds.width() < headPositionLeftPosition) {
                canvas.drawText(
                        tailPositionString,
                        tailPositionLeftPosition,
                        bitmapHeight / 2 + tailPositionBounds.height() / 2,
                        textPaint);
            }
        }
    }


    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
