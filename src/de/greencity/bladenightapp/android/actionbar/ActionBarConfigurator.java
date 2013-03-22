package de.greencity.bladenightapp.android.actionbar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class ActionBarConfigurator {

	public enum ActionItemType {
		EVENT_SELECTION,
		MAP,
		FRIENDS,
		RELOAD,
		OPTIONS
	}
	
	public ActionBarConfigurator(ActionBar actionBar) {
		this.actionBar = actionBar;
		typeToAction = new HashMap<ActionItemType, Action>();
		typeToAction.put(ActionItemType.EVENT_SELECTION, new ActionEventSelection());
		typeToAction.put(ActionItemType.MAP, new ActionMap());
		typeToAction.put(ActionItemType.FRIENDS, new ActionFriends());
		typeToAction.put(ActionItemType.RELOAD, new ActionReload());
		typeToAction.put(ActionItemType.OPTIONS, new ActionOptions());
	}
	
	public ActionBarConfigurator hide(ActionItemType type) {
		typesToShow.remove(type);
		return this;
	}

	public ActionBarConfigurator setTitle(int title) {
		this.title = title;
		return this;
	}
	
	public void configure() {
		if ( actionBar == null ) {
			Log.e(TAG, "actionBar == null");
			return;
		}
		for ( ActionItemType type: typesToShow) {
			actionBar.addAction(typeToAction.get(type));
		}
		actionBar.setTitle(title);
	}
	
	private ActionBar actionBar;
	private List<ActionItemType> typesToShow = new LinkedList<ActionItemType>(Arrays.asList(ActionItemType.values()));
	private Map<ActionItemType, Action> typeToAction;
	private int title;
	private static final String TAG = "ActionBarConfigurator";
}
