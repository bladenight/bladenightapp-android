package de.greencity.bladenightapp.android.social;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import de.greencity.bladenightapp.android.social.InviteFriendDialog.InviteFriendDialogListener;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.network.messages.FriendMessage;
import de.greencity.bladenightapp.network.messages.FriendsMessage;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
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
	protected void onResume() {
		super.onStart();

		Log.i(TAG, "onResume");
		configureActionBar();

		getFriendsFromStorage();
		getFriendsListFromServer();
		createListView();

		if ( ServiceUtils.isServiceRunning(SocialActivity.this, GpsTrackerService.class) ) {
			getRealTimeDataFromServer();
			schedulePeriodicTask();
		}
	}

	@Override
	protected void onPause() {
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
					getRealTimeDataFromServer();
					getFriendsListFromServer();
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
				ChangeFriendDialog changeFriendDialog = new ChangeFriendDialog();
				Bundle arguments = new Bundle();
				arguments.putSerializable(ChangeFriendDialog.KEY_FRIENDOBJ, friends.get(friendId));
				arguments.putInt(ChangeFriendDialog.KEY_FRIENDID, friendId);
				changeFriendDialog.setArguments(arguments);
				changeFriendDialog.show(fm, "fragment_change_friend");
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int selectedIndex, long rowId) {
				LinearLayout row = (LinearLayout)view.findViewById(R.id.row_friend);
				int friendId = (Integer) row.getTag();
				removeFriendOnServer(friendId);
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
				arguments.putString(ShowCodeDialog.ARG_CODE, formatRequestId(relMsg.getRequestId()));
				ShowCodeDialog showCodeDialog = new ShowCodeDialog();
				showCodeDialog.setArguments(arguments);
				showCodeDialog.show(fm, "fragment_show_code");
				Friend newFriend = socialActivity.newFriend(friendName);
				newFriend.setRequestId(relMsg.getRequestId());
				socialActivity.friends.put((int)relMsg.fid,newFriend);
				socialActivity.updateGui();
				progressDialog.dismiss();
				socialActivity.getFriendsListFromServer();
			}
		}
		private WeakReference<SocialActivity> reference;
		private String friendName;
		private ProgressDialog progressDialog;
	}
	
	public Friend newFriend(String name) {
		Friend newFriend = new Friend(name);
		newFriend.setColor(findViewById(android.R.id.content).getResources().getColor(R.color.default_friend_color));
		return newFriend;
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
				Friend newFriend = socialActivity.newFriend(friendName);
				socialActivity.friends.put((int)relMsg.fid, newFriend);
				socialActivity.updateGui();
				progressDialog.dismiss();
				Toast.makeText(socialActivity, friendName + " was added", Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(socialActivity, "Code is not valid", Toast.LENGTH_LONG).show();
			}
			socialActivity.getFriendsListFromServer();
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
		getFriendsListFromServer();
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

	private void getRealTimeDataFromServer(){
		networkClient.getRealTimeData(new GetRealTimeDataFromServerHandler(this), null);
	}

	static class DeleteFriendOnServerHandler extends Handler {
		private WeakReference<SocialActivity> reference;
		private int friendId;
		DeleteFriendOnServerHandler(SocialActivity activity, int friendId) {
			this.reference = new WeakReference<SocialActivity>(activity);
			this.friendId = friendId;
		}
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(reference.get(), "Friend has been removed", Toast.LENGTH_SHORT).show();
			reference.get().friends.remove(friendId);
			reference.get().friends.save();
			reference.get().updateGui();
			reference.get().getFriendsListFromServer();
		}
	}

	private void removeFriendOnServer(int friendId){
		networkClient.deleteRelationship(friendId, new DeleteFriendOnServerHandler(this, friendId), null);
	}


	static class GetFriendsListFromServerHandler extends Handler {
		private WeakReference<SocialActivity> reference;
		GetFriendsListFromServerHandler(SocialActivity activity) {
			this.reference = new WeakReference<SocialActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			FriendsMessage friendsMessage = (FriendsMessage)msg.obj;
			Log.i(TAG, "friendsMessage="+friendsMessage);
			reference.get().updateGuiFromFriendsMessage(friendsMessage);
		}
	}

	public void getFriendsListFromServer(){
		networkClient.getFriendsList(new GetFriendsListFromServerHandler(this), null);
	}


	private void updateGuiFromFriendsMessage(FriendsMessage friendsMessage) {
		Set<Integer> combinedFriendIds = new HashSet<Integer>();
		combinedFriendIds.addAll(friendsMessage.keySet());
		combinedFriendIds.addAll(friends.keySet());

		for ( int friendId : combinedFriendIds) {
			if ( friendId < 0)
				continue;

			Friend friend = friends.get(friendId);
			if ( friend == null ) {
				// for some reason the server knows about this friend but we don't
				friend = newFriend("?");
				friends.put(friendId, friend);
			}

			FriendMessage friendMessage = friendsMessage.get(friendId);
			
			Log.i(TAG, "friendMessage="+friendMessage);
			if ( friendMessage == null ) {
				friend.isValid(false);
			}
			else {
				friend.isValid(true);
				friend.setRequestId(friendMessage.getRequestId());
			}
		}
		friends.save();
		updateGui();
	}

	private void getFriendsFromStorage(){
		friends = new Friends(this);
		friends.load();

		friends.put(ID_HEAD, new Friend("Head"));
		friends.put(ID_TAIL, new Friend("Tail"));
		if ( friends.get(ID_ME) == null )
			friends.put(ID_ME, new Friend("Me"));
	}

	private void updateFriendDynamicData(RealTimeUpdateData realTimeUpdateData, MovingPointMessage nmp, Friend friend) {
		friend.resetPositionData();
		// RealTimeUpdateData contains only online friends by convention
		friend.isOnline(true);
		if ( nmp.isOnRoute() ) {
			friend.setAbsolutePosition(nmp.getPosition());
			MovingPointMessage me = realTimeUpdateData.getUser();
			if ( me.isOnRoute() ) {
				friend.setRelativeDistance(nmp.getPosition()-me.getPosition());
				friend.setRelativeTime(me.getEstimatedTimeToArrival()-nmp.getEstimatedTimeToArrival());
			}
		}
	}
	private void updateGuiFromRealTimeUpdateData(RealTimeUpdateData realTimeUpdateData) {

		FriendsMessage friendsMessage = realTimeUpdateData.getFriends();

		Set<Integer> combinedFriendIds = new HashSet<Integer>();
		combinedFriendIds.addAll(friendsMessage.keySet());
		combinedFriendIds.addAll(friends.keySet());

		for ( int friendId : combinedFriendIds) {
			// Log.i(TAG, "updateGuiFromRealTimeUpdateData: FriendId="+friendId);
			Friend friend = friends.get(friendId);
			MovingPointMessage friendLocation;
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
				friendLocation =  friendsMessage.get(friendId);
				if ( friends.get(friendId) == null ) {
					// for one reason the server knows about this friend but we don't
					friend = newFriend("?");
					friends.put(friendId, friend);
				}
			}
			if ( friendLocation != null ) {
				updateFriendDynamicData(realTimeUpdateData, friendLocation, friend);
			}
			else {
				friend.isOnline(false);
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
		sortedFriendIdsToDisplay = new ArrayList<Integer>(friends.keySet());
		sortedFriendIdsToDisplay.remove(ID_HEAD);
		sortedFriendIdsToDisplay.remove(ID_TAIL);
		sortedFriendIdsToDisplay.remove(ID_ME);
		Collections.sort(sortedFriendIdsToDisplay, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				return friends.get(id1).compareTo(friends.get(id2));
			}
		});
	}

	private void sortListViewWhileInAction() {
		sortedFriendIdsToDisplay = new ArrayList<Integer>();
		for(int friendId : friends.keySet()){
			Friend friend = friends.get(friendId);
			if( friend.isActive() && friend.isOnline() )
				sortedFriendIdsToDisplay.add(friendId);
		}
		Collections.sort(sortedFriendIdsToDisplay, new Comparator<Integer>() {
			public int compare(Integer id1, Integer id2) {
				Long d1 = friends.get(id1).getAbsolutePosition();
				Long d2 = friends.get(id2).getAbsolutePosition();
				if ( d1 == null && d2 == null)
					return 0;
				if ( d1 == null )
					return 1;
				if ( d2 == null )
					return -1;
				if ( d2.compareTo(d1) != 0)
					return d2.compareTo(d1);
				return id2.compareTo(id1);
			}
		});
	}

	private void schedulePeriodicTask() {
		periodicTask = new Runnable() {
			@Override
			public void run() {
				getRealTimeDataFromServer();
				periodicHandler.postDelayed(this, updatePeriod);
			}
		};
		periodicHandler.postDelayed(periodicTask, updatePeriod);
	}

	private void cancelPeriodicTask() {
		if ( periodicTask != null )
			periodicHandler.removeCallbacks(periodicTask);
	}
	
	public static String formatRequestId(long requestId) {
		return MessageFormat.format("{0,number,#,00}", requestId).replace(",", " ");
	}

	private ListView listView;
	Friends friends;
	List<Integer> sortedFriendIdsToDisplay;
	private final static String TAG = "SocialActivity"; 
	private NetworkClient networkClient = new NetworkClient(this);
	private final Handler periodicHandler = new Handler();
	private Runnable periodicTask;
	private long updatePeriod = 2000;

	public final static Integer ID_HEAD 	= -1;
	public final static Integer ID_ME 		= -2;
	public final static Integer ID_TAIL 	= -3;

} 
