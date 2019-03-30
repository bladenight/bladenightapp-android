/*
 * Based on original work from:
 *
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greencity.bladenightapp.android.selection;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import de.greencity.bladenightapp.android.R;

/**
 * Draws circles (one for each view). The current view position is filled and
 * others are only stroked.
 */
public class CirclePageIndicator extends View implements PageIndicator {
    interface ColorResolver {
        int resolve(int index);
    }

    public CirclePageIndicator(Context context) {
        this(context, null);
    }

    public CirclePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;

        final Resources res = getResources();

        defaultFillPaint.setStyle(Style.FILL);
        defaultFillPaint.setColor(res.getColor(R.color.black));

        strokePaint.setStyle(Style.STROKE);
        strokePaint.setColor(res.getColor(R.color.bn_white));

        double scale = getResources().getDisplayMetrics().density;
        int sizeInDp = (int) (2.0*scale);
        strokePaint.setStrokeWidth(sizeInDp);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

        referenceRadius = res.getDimensionPixelSize(R.dimen.cpi_reference_radius);
    }

    public void setColorResolver(ColorResolver colorResolver) {
        this.colorResolver = colorResolver;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewPager == null) {
            return;
        }
        final int count = getNumberOfItems();
        if (count == 0) {
            return;
        }

        if (pageIndex >= count) {
            setCurrentItem(count - 1);
            return;
        }

        int longPaddingBefore;
        int longPaddingAfter;
        int shortPaddingBefore;

        longPaddingBefore = getPaddingLeft();
        longPaddingAfter = getPaddingRight();
        shortPaddingBefore = getPaddingTop();

        float longPaddingBetween = referenceRadius * paddingFactor;

        float drawLong = longPaddingBefore;

        final float shortOffset = shortPaddingBefore + referenceRadius;

        final float availableLongSize = ( getWidth() - longPaddingAfter - longPaddingBefore);
        final float requiredLongSize = getRequiredLongSizeWithoutPadding();
        drawLong += (availableLongSize-requiredLongSize) / 2.0f;

        for (int iLoop = 0; iLoop < count; iLoop++) {
            Paint paint = resolveFillPaintForIndex(iLoop);
            final float growthFactor = getGrowthFactor(iLoop);
            final float pitch = (referenceRadius + longPaddingBetween / 2.0f) * growthFactor;

            drawLong += pitch;

            float dX = drawLong;
            float dY = shortOffset;

            final float dynamicRadius = referenceRadius * growthFactor;
            // Only paint fill if not completely transparent
            if (paint.getAlpha() > 0) {
                canvas.drawCircle(dX, dY, dynamicRadius, paint);
            }

            canvas.drawCircle(dX, dY, dynamicRadius, strokePaint);
            drawLong += pitch;
        }
    }

    private int getRequiredLongSizeWithoutPadding() {
        final float longPaddingBetween = referenceRadius * 0.4f;
        final float referencePitch = 2.0f * referenceRadius + longPaddingBetween;
        final int count = getNumberOfItems();
        return (int) (( count - 1 ) * referencePitch + referencePitch * ( 1.0f + radiusGrowthPotential));
    }

    private int getNumberOfItems() {
        return viewPager.getAdapter().getCount();
    }

    private float getGrowthFactor(int circleIndex) {
        float subFactor = (float)0.0;
        if ( circleIndex == pageIndex )
            subFactor = 1 - pagePositionOffset;
        else if ( circleIndex == pageIndex + 1)
            subFactor = pagePositionOffset;
        return 1.0f + radiusGrowthPotential * subFactor;
    }

    private Paint resolveFillPaintForIndex(int iLoop) {
        if (colorResolver == null)
            return defaultFillPaint;
        int color = colorResolver.resolve(iLoop);
        if (color < 0 )
            return defaultFillPaint;
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setColor(getResources().getColor(color));
        return paint;
    }

    public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if ((viewPager == null) || (getNumberOfItems() == 0)) {
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            activePointerId = MotionEventCompat.getPointerId(ev, 0);
            lastMotionX = ev.getX();
            break;

        case MotionEvent.ACTION_MOVE: {
            final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
            final float x = MotionEventCompat.getX(ev, activePointerIndex);
            final float deltaX = x - lastMotionX;

            if (!isDragging) {
                if (Math.abs(deltaX) > touchSlop) {
                    isDragging = true;
                }
            }

            if (isDragging) {
                lastMotionX = x;
                if (viewPager.isFakeDragging() || viewPager.beginFakeDrag()) {
                    viewPager.fakeDragBy(deltaX);
                }
            }

            break;
        }

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if (!isDragging) {
                final int count = getNumberOfItems();
                final int width = getWidth();
                final float halfWidth = width / 2f;
                final float sixthWidth = width / 6f;

                if ((pageIndex > 0) && (ev.getX() < halfWidth - sixthWidth)) {
                    if (action != MotionEvent.ACTION_CANCEL) {
                        viewPager.setCurrentItem(pageIndex - 1);
                    }
                    return true;
                } else if ((pageIndex < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
                    if (action != MotionEvent.ACTION_CANCEL) {
                        viewPager.setCurrentItem(pageIndex + 1);
                    }
                    return true;
                }
            }

            isDragging = false;
            activePointerId = INVALID_POINTER;
            if (viewPager.isFakeDragging()) viewPager.endFakeDrag();
            break;

        case MotionEventCompat.ACTION_POINTER_DOWN: {
            final int index = MotionEventCompat.getActionIndex(ev);
            lastMotionX = MotionEventCompat.getX(ev, index);
            activePointerId = MotionEventCompat.getPointerId(ev, index);
            break;
        }

        case MotionEventCompat.ACTION_POINTER_UP:
            final int pointerIndex = MotionEventCompat.getActionIndex(ev);
            final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
            if (pointerId == activePointerId) {
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            }
            lastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, activePointerId));
            break;
        }

        return true;
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (viewPager == view) {
            return;
        }
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(null);
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        viewPager = view;
        viewPager.setOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (viewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        viewPager.setCurrentItem(item);
        pageIndex = item;
        invalidate();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        scrollState = state;

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Log.i("OnPageScrolled", "position="+position+" positionOffset="+positionOffset+" positionOffsetPixels="+positionOffsetPixels);
        pageIndex = position;
        pagePositionOffset = positionOffset;
        invalidate();

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if ( scrollState == ViewPager.SCROLL_STATE_IDLE) {
            pageIndex = position;
            invalidate();
        }

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        onPageChangeListener = listener;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureLong(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (viewPager == null)) {
            //We were told how big to be
            result = specSize;
        } else {
            result = (int) (getPaddingLeft() + getPaddingRight() + getRequiredLongSizeWithoutPadding());
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureShort(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = (int)(2 * referenceRadius + getPaddingTop() + getPaddingBottom() + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        pageIndex = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = pageIndex;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private static final int INVALID_POINTER = -1;

    private float referenceRadius = 8.0f;
    private float radiusGrowthPotential = 0.5f;
    private float paddingFactor = 0.4f;

    private final Paint defaultFillPaint = new Paint(ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(ANTI_ALIAS_FLAG);
    private ViewPager viewPager;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private int pageIndex;
    private float pagePositionOffset;
    private int scrollState;

    private int touchSlop;
    private float lastMotionX = -1;
    private int activePointerId = INVALID_POINTER;
    private boolean isDragging;
    private ColorResolver colorResolver;
}
