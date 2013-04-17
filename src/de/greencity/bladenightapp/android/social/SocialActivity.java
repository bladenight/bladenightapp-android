package de.greencity.bladenightapp.android.social;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionReload;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.social.ChangeFriendDialog.ChangeFriendDialogListener;
import de.greencity.bladenightapp.android.social.ConfirmFriendDialog.ConfirmFriendDialogListener;
import de.greencity.bladenightapp.android.social.Friend.FriendColor;
import de.greencity.bladenightapp.android.social.InviteFriendDialog.InviteFriendDialogListener;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.network.messages.NetMovingPoint;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;

public class SocialActivity extends FragmentActivity implements InviteFriendDialogListener, 
ConfirmFriendDialogListener, ChangeFriendDialogListener {

	@SuppressLint("UseSparseArrays")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_social);

		listView = (ListView)findViewById(R.id.listview);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");
		configureActionBar();

		getFriendsFromStorage();
		getFriendsFromServer();
		createListView();

		if ( ServiceUtils.isServiceRunning(SocialActivity.this, GpsTrackerService.class) )
			schedulePeriodicTask();
	}

	@Override
	protected void onStop() {
		super.onStop();
		cancelPeriodicTask();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);

		ActionBarConfigurator actionBarConfigurator = new ActionBarConfigurator(actionBar)
		.show(ActionItemType.ADD_FRIEND)
		.setTitle(R.string.title_social);

		if ( ServiceUtils.isServiceRunning(this, GpsTrackerService.class)) {
			actionBarConfigurator
			.show(ActionItemType.MAP);
		}
		else {
			Action reloadAction = new ActionReload() {
				@Override
				public void performAction(View view) {
					getFriendsFromStorage();
					getFriendsFromServer();
				}
			};

			actionBarConfigurator
			.setAction(ActionItemType.RELOAD, reloadAction);
		}

		actionBarConfigurator.configure();

	}


	private void createListView() {

		updateGui();

		//Create an adapter for the listView and add the ArrayList to the adapter.
		listView.setAdapter(new FriendListAdapter(SocialActivity.this));
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int selectedIndex, long rowId) {
				FragmentManager fm = getSupportFragmentManager();
				LinearLayout row = (LinearLayout)view.findViewById(R.id.row_friend);
				int friendId = (Integer) row.getTag();
				ChangeFriendDialog changeFriendDialog = new ChangeFriendDialog(friends.get(friendId), friendId);
				changeFriendDialog.show(fm, "fragment_change_friend");
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int selectedIndex, long rowId) {
				LinearLayout row = (LinearLayout)view.findViewById(R.id.row_friend);
				int friendId = (Integer) row.getTag();
				friends.remove(friendId);
				updateGui();
				return true;
			}
		});
	}

	static class CreateNewRequestHandler extends Handler {
		CreateNewRequestHandler(SocialActivity activity, String friendName, ProgressDialog progressDialog) {
			this.reference = new WeakReference<SocialActivity>(activity);
			this.friendName = friendName;
			this.progressDialog = progressDialog;
		}
		@Override
		public void handleMessage(Message msg) {
			RelationshipOutputMessage relMsg = (RelationshipOutputMessage)msg.obj;
			Log.i("CreateNewRequestHandler", "Got answer from server:" + relMsg);
			if (relMsg != null ) {
				final SocialActivity socialActivity = reference.get();
				FragmentManager fm = socialActivity.getSupportFragmentManager();
				Bundle arguments = new Bundle();
				Log.i("CreateNewRequestHandler", "Got: " + relMsg);
				arguments.putString(ShowCodeDialog.ARG_NICKNAME, friendName);
				arguments.putString(ShowCodeDialog.ARG_CODE, Long.toString(relMsg.getRequestId()));
				ShowCodeDialog showCodeDialog = new ShowCodeDialog();
				showCodeDialog.setArguments(arguments);
				showCodeDialog.show(fm, "fragment_show_code");
				Friend newFriend = new Friend(friendName + " ...pending", FriendColor.GREEN,true);
				socialActivity.friends.put((int)relMsg.fid,newFriend);
				socialActivity.updateGui();
				progressDialog.dismiss();
			}
		}
		private WeakReference<SocialActivity> reference;
		private String friendName;
		private ProgressDialog progressDialog;
	}

	@Override
	public void onFinishInviteFriendDialog(String friendName) { 
		ProgressDialog dialog = new ProgressDialog(SocialActivity.this);

		dialog.setMessage("Loading add-friend code...");
		dialog.show();

		CreateNewRequestHandler handler = new CreateNewRequestHandler(this, friendName, dialog);
		networkClient.createRelationship(Friends.generateId(this), handler, null);
	}

	static class ConfirmRequestHandler extends Handler {
		ConfirmRequestHandler(SocialActivity activity, String friendName, ProgressDialog progressDialog) {
			this.reference = new WeakReference<SocialActivity>(activity);
			this.friendName = friendName;
			this.progressDialog = progressDialog;
		}
		@Override
		public void handleMessage(Message msg) {
			SocialActivity socialActivity = reference.get();
			RelationshipOutputMessage relMsg = (RelationshipOutputMessage)msg.obj;
			Log.i("CreateNewRequestHandler", "Got answer from server:" + relMsg);
			if (relMsg != null ) {
				Friend newFriend = new Friend(friendName, FriendColor.GREEN,true);
				socialActivity.friends.put((int)relMsg.fid, newFriend);
				socialActivity.updateGui();
				progressDialog.dismiss();
				Toast.makeText(socialActivity, friendName + " was added", Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(socialActivity, "Code is not valid", Toast.LENGTH_LONG).show();
			}
		}
		private WeakReference<SocialActivity> reference;
		private String friendName;
		private ProgressDialog progressDialog;
	}

	static class ConfirmRequestErrorHandler extends Handler {
		ConfirmRequestErrorHandler(SocialActivity activity, ProgressDialog progressDialog) {
			this.reference = new WeakReference<SocialActivity>(activity);
			this.progressDialog = progressDialog;
		}
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(reference.get(), "Code is not valid", Toast.LENGTH_LONG).show();
			progressDialog.dismiss();
		}
		private WeakReference<SocialActivity> reference;
		private ProgressDialog progressDialog;
	}


	@Override
	public void onFinishConfirmFriendDialog(String friendName, String code) { 
		ProgressDialog dialog = new ProgressDialog(SocialActivity.this);
		dialog.setMessage("Validating friend code...");
		dialog.show();
		networkClient.finalizeRelationship(Long.parseLong(code), Friends.generateId(this), new ConfirmRequestHandler(this, friendName, dialog), new ConfirmRequestErrorHandler(this, dialog));
	}

	@Override
	public void onFinishChangeFriendDialog(Friend friend, int friendId) { 
		friends.put(friendId, friend);
		updateGui();
	}

	static class GetRealTimeDataFromServerHandler extends Handler {
		private WeakReference<SocialActivity> reference;
		GetRealTimeDataFromServerHandler(SocialActivity activity) {
			this.reference = new WeakReference<SocialActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			RealTimeUpdateData realTimeUpdateData = (RealTimeUpdateData)msg.obj;
			reference.get().updateGuiFromRealTimeUpdateData(realTimeUpdateData);
		}
	}

	private void getFriendsFromServer(){
		networkClient.getRealTimeData(new GetRealTimeDataFromServerHandler(this), null);
	}

	private void getFriendsFromStorage(){
		friends = new Friends(this);
		friends.load();

		Friend head = new Friend("Head", FriendColor.BLACK, true);
		Friend tail = new Friend("Tail", FriendColor.BLACK, true);
		Friend myself = new Friend("Me", FriendColor.BLACK, true);
		friends.put(ID_HEAD, head);
		friends.put(ID_TAIL, tail);
		friends.put(ID_ME, myself);
	}

	private void updateFriendDynamicData(RealTimeUpdateData realTimeUpdateData, NetMovingPoint nmp, Friend friend) {
		friend.resetDynamicData();
		if ( nmp.isOnRoute() ) {
			friend.setAbsolutePosition(nmp.getPosition());
			NetMovingPoint me = realTimeUpdateData.getUser();
			if ( me.isOnRoute() ) {
				friend.setRelativeDistance(nmp.getPosition()-me.getPosition());
				friend.setRelativeTime(me.getEstimatedTimeToArrival()-nmp.getEstimatedTimeToArrival());
			}
		}
	}
	private void updateGuiFromRealTimeUpdateData(RealTimeUpdateData realTimeUpdateData) {

		Map<Integer, NetMovingPoint> friendsMap = realTimeUpdateData.getFriendsMap();

		Set<Integer> combinedFriendIds = new HashSet<Integer>();
		combinedFriendIds.addAll(friendsMap.keySet());
		combinedFriendIds.addAll(friends.keySet());

		for ( int friendId : combinedFriendIds) {
			Friend friend = friends.get(friendId);
			NetMovingPoint friendLocation;
			if ( friendId == ID_HEAD ) {
				friendLocation =  realTimeUpdateData.getHead();
			}
			else if ( friendId == ID_TAIL ) {
				friendLocation =  realTimeUpdateData.getTail();
			}
			else if ( friendId == ID_ME ) {
				friendLocation =  realTimeUpdateData.getUser();
			}
			else {
				friendLocation =  friendsMap.get(friendId);
				if ( friends.get(friendId) == null ) {
					// for one reason the server knows about this friend but we don't
					friend = new Friend("?", FriendColor.BLACK, true);
					friends.put(friendId, friend);
				}
			}
			if ( friendLocation != null ) {
				updateFriendDynamicData(realTimeUpdateData, friendLocation, friend);
			}
			else {
				// TODO the server didn't send us any information about this friend
			}
		}
		// This relative values are relative to me, don't display them for me (otherwise it would just display 0)
		friends.get(ID_ME).setRelativeDistance(null);
		friends.get(ID_ME).setRelativeTime(null);
		updateGui();
	}


	public void updateGui(){
		boolean isInAction = ServiceUtils.isServiceRunning(SocialActivity.this, GpsTrackerService.class);

		if( isInAction ){
			sortListViewWhileInAction();
		}
		else{
			sortListViewWhileNotInAction();
		}

		listView.invalidateViews();
	}

	private void sortListViewWhileNotInAction() {
		idOrder = new ArrayList<Integer>(friends.keySet());
		idOrder.remove(ID_HEAD);
		idOrder.remove(ID_TAIL);
		idOrder.remove(ID_ME);
		Collections.sort(idOrder, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				return friends.get(id1).compareTo(friends.get(id2));
			}
		});
	}

	private void sortListViewWhileInAction() {
		idOrder = new ArrayList<Integer>();
		for(int id : friends.keySet()){
			if(friends.get(id).getActive())
				idOrder.add(id);
		}
		Collections.sort(idOrder, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				Long d1 = friends.get(id1).getAbsolutePosition();
				Long d2 = friends.get(id2).getAbsolutePosition();
				if ( d1 == null && d2 == null)
					return 0;
				if ( d1 == null )
					return 1;
				if ( d2 == null )
					return -1;
				return (d2.compareTo(d1));
			}
		});
	}

	private void schedulePeriodicTask() {
		periodicTask = new Runnable() {
			@Override
			public void run() {
				getFriendsFromServer();
				periodicHandler.postDelayed(this, updatePeriod);
			}
		};
		periodicHandler.postDelayed(periodicTask, updatePeriod);
	}

	private void cancelPeriodicTask() {
		if ( periodicTask != null )
			periodicHandler.removeCallbacks(periodicTask);
	}

	private ListView listView;
	Friends friends;
	List<Integer> idOrder;
	private final String TAG = "SocialActivity"; 
	private NetworkClient networkClient = new NetworkClient(this);
	private final Handler periodicHandler = new Handler();
	private Runnable periodicTask;
	private long updatePeriod = 2000;

	final static Integer ID_HEAD = -1;
	final static Integer ID_TAIL = -2;
	final static Integer ID_ME = -3;

} 
