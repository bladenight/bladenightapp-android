package de.greencity.bladenightapp.android.actionbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;

public class ActionTrackerControl implements Action {
	ActionTrackerControl(Context context) {
		this.context = context;
	}
	
    @Override
    public int getDrawable() {
    	boolean isTrackerRunning = isTrackerRunning();
    	startOnClick = ! isTrackerRunning;
    	return getDrawableForState(isTrackerRunning);
    }
    
	@Override
	public void performAction(View view) {
		if ( startOnClick ) {
			if ( isTrackerRunning() ) {
				Log.i(TAG, "Service has been started in the meantime");
			}
			else {
				checkSDCard();
				ServiceUtils.startService(context, trackerServiceClass);
				updateImage(view, true);
			}
		}
		else {
			if ( ! isTrackerRunning() ) {
				Log.i(TAG, "Service has been stopped in the meantime");
			}
			else {
				askForConfirmation(view);
			}
		}
	}
	
	protected void askForConfirmation(final View view) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
					ServiceUtils.stopService(context, trackerServiceClass);
					updateImage(view, false);
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setMessage(R.string.msg_tracker_confirm_stop)
			.setPositiveButton(R.string.msg_yes, dialogClickListener)
		    .setNegativeButton(R.string.msg_no, dialogClickListener)
		    .show();
	}

    public int getDrawableForState(boolean isTrackerRunning) {
    	if ( ! isTrackerRunning ) {
    		return R.drawable.ic_action_playback_play;
    	}
    	else {
    		return R.drawable.ic_action_playback_stop;
    	}
    }

	protected void updateImage(View view) {
		updateImage(view, isTrackerRunning());
	}

	protected void updateImage(View view, boolean isTrackerRunning) {
    	startOnClick = ! isTrackerRunning;
        ((ImageButton) view).setImageResource(getDrawableForState(isTrackerRunning));
	}
	

	protected boolean isTrackerRunning() {
		return ServiceUtils.isServiceRunning(context, GpsTrackerService.class);
	}
	
	private void checkSDCard(){
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(!isSDPresent)
			Toast.makeText(context, R.string.msg_sd_card, Toast.LENGTH_LONG).show();
	}

	private Context context;
	private boolean startOnClick;
	
	private static final String TAG = "ActionTrackerControl";
	private static final Class<GpsTrackerService> trackerServiceClass = GpsTrackerService.class;
}
