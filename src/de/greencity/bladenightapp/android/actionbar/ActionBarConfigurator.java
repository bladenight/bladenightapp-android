package de.greencity.bladenightapp.android.actionbar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.utils.ServiceUtils;

public class ActionBarConfigurator {

	public enum ActionItemType {
		HOME,
		EVENT_SELECTION,
		TRACKER_CONTROL,
		MAP,
		FRIENDS,
		RELOAD,
		OPTIONS,
		ADD_FRIEND
	}

	public ActionBarConfigurator(ActionBar actionBar) {
		this.actionBar = actionBar;
		typeToActionAll.put(ActionItemType.HOME, new ActionEventSelection());
		typeToActionAll.put(ActionItemType.EVENT_SELECTION, new ActionEventSelection());
		typeToActionAll.put(ActionItemType.TRACKER_CONTROL, new ActionTrackerControl(actionBar.getContext()));
		typeToActionAll.put(ActionItemType.MAP, new ActionMap());
		typeToActionAll.put(ActionItemType.FRIENDS, new ActionFriends());
		typeToActionAll.put(ActionItemType.ADD_FRIEND, new ActionAddFriend());
		typeToActionAll.put(ActionItemType.RELOAD, new ActionReload());
		typeToActionAll.put(ActionItemType.OPTIONS, new ActionOptions());
		show(ActionItemType.HOME);
	}

	public ActionBarConfigurator show(ActionItemType type) {
		typeToActionSelected.put(type, typeToActionAll.get(type));
		return this;
	}

	public ActionBarConfigurator hide(ActionItemType type) {
		typeToActionSelected.remove(type);
		return this;
	}


	public ActionBarConfigurator setTitle(int title) {
		this.title = title;
		return this;
	}

	public ActionBarConfigurator setAction(ActionItemType type, Action action) {
		typeToActionSelected.put(type, action);
		return this;
	}

	public void configure() {
		if ( actionBar == null ) {
			Log.e(TAG, "actionBar == null");
			return;
		}
		actionBar.removeAllActions();
		for ( ActionItemType type: typesToShow) {
			Action action = typeToActionSelected.get(type);
			if ( action != null ) {
				if ( type != ActionItemType.HOME ) 
					actionBar.addAction(action);
				else
					actionBar.setHomeAction(action);
			}
		}
		actionBar.setTitle(title);
	}

	private ActionBar actionBar;
	private List<ActionItemType> typesToShow = new LinkedList<ActionItemType>(Arrays.asList(ActionItemType.values()));
	private Map<ActionItemType, Action> typeToActionAll = new HashMap<ActionItemType, Action>();
	private Map<ActionItemType, Action> typeToActionSelected = new HashMap<ActionItemType, Action>();
	private int title = -1;
	private static final String TAG = "ActionBarConfigurator";
}
