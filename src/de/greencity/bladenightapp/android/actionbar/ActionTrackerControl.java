package de.greencity.bladenightapp.android.actionbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.gps.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;

public class ActionTrackerControl implements Action {
	ActionTrackerControl(Context context) {
		this.context = context;
		wasRunningAtSetupTime = isTrackerRunning();
	}
	
    @Override
    public int getDrawable() {
    	if ( ! wasRunningAtSetupTime ) {
    		return R.drawable.ic_action_playback_play;
    	}
    	else {
    		return R.drawable.ic_action_playback_stop;
    	}
    }

	@Override
	public void performAction(View view) {
		boolean shallStartNow = ! wasRunningAtSetupTime; 
		if ( shallStartNow ) {
			if ( isTrackerRunning() ) {
				Log.i(TAG, "Service has been started in the meantime");
			}
			else {
				ServiceUtils.startService(context, trackerServiceClass);
			}
		}
		else {
			if ( ! isTrackerRunning() ) {
				Log.i(TAG, "Service has been stopped in the meantime");
			}
			else {
				askForConfirmation();
			}
		}
	}
	
	protected void askForConfirmation() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
					ServiceUtils.stopService(context, trackerServiceClass);
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
			.setMessage("Are you sure?")
			.setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener)
		    .show();
	}
	
	protected boolean isTrackerRunning() {
		return ServiceUtils.isServiceRunning(context, GpsTrackerService.class);
	}

	private boolean wasRunningAtSetupTime;
	private Context context;
	private static final String TAG = "ActionTrackerControl";
	private static final Class<GpsTrackerService> trackerServiceClass = GpsTrackerService.class;
}
