package de.greencity.bladenightapp.android.social;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.social.ChangeFriendDialog.ChangeFriendDialogListener;
import de.greencity.bladenightapp.android.social.ConfirmFriendDialog.ConfirmFriendDialogListener;
import de.greencity.bladenightapp.android.social.Friend.FriendColor;
import de.greencity.bladenightapp.android.social.InviteFriendDialog.InviteFriendDialogListener;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
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
		//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		//		ImageView titlebar = (ImageView)(findViewById(R.id.icon));
		//		titlebar.setImageResource(R.drawable.ic_menu_settings);
		//		TextView titletext = (TextView)findViewById(R.id.title);
		//		titletext.setText(R.string.title_social);

		friends = new Friends(this);
		friends.load();

		list = (ListView)findViewById(R.id.listview);
		createListView();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");
		configureActionBar();

	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.show(ActionItemType.ADD_FRIEND)
		.setTitle(R.string.title_social)
		.configure();

	}


	private void createListView() {

		updateList();

		//Create an adapter for the listView and add the ArrayList to the adapter.
		list.setAdapter(new FriendListAdapter(SocialActivity.this));
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int selectedIndex, long arg3) {
				FragmentManager fm = getSupportFragmentManager();
				LinearLayout row = (LinearLayout)view.findViewById(R.id.row_friend);
				int id = (Integer) row.getTag();
				ChangeFriendDialog changeFriendDialog = new ChangeFriendDialog(friends.get(id), id);
				changeFriendDialog.show(fm, "fragment_change_friend");
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int selectedIndex, long arg3) {
				LinearLayout row = (LinearLayout)view.findViewById(R.id.row_friend);
				int id = (Integer) row.getTag();
				friends.remove(id);
				updateList();
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
				newFriend.setActionData(138, 240, 2346, 5452);
				socialActivity.friends.put((int)relMsg.fid,newFriend);
				socialActivity.updateList();
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
		networkClient.createRelationship(friends.generateId(), handler, null);
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
				newFriend.setActionData(138, 240, 2346, 5452);
				socialActivity.friends.put((int)relMsg.fid, newFriend);
				socialActivity.updateList();
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
		networkClient.finalizeRelationship(Long.parseLong(code), friends.generateId(), new ConfirmRequestHandler(this, friendName, dialog), new ConfirmRequestErrorHandler(this, dialog));
	}

	@Override
	public void onFinishChangeFriendDialog(Friend friend, int friendId) { 
		friends.put(friendId, friend);
		updateList();
	}

	private void getFriendsFromStorage(){
		//dummy data to test
		//		Friend friend1 = new Friend("Hans",FriendColor.ORANGE,true);
		//		friend1.setActionData(125, 211, 2333, 5423);
		//		friends.put(id_counter++,friend1);
		//		Friend friend2 = new Friend("Heinz",FriendColor.RED,true);
		//		friend2.setActionData(120, 205, 2328, 5417);
		//		friends.put(id_counter++,friend2);
		//		Friend friend3 = new Friend("Hubert",FriendColor.GREEN,true);
		//		friend3.setActionData(138, 240, 2346, 5452);
		//		friends.put(id_counter++,friend3);
		//
		//		friends.get(ID_HEAD).setActionData(0, 0, 2208, 5212);
		//		friends.get(ID_TAIL).setActionData(213, 450, 2415, 5662);
		//		friends.get(ID_ME).setActionData(130, 215, 2338, 5427);
		friends.load();
		Friend head = new Friend("Head",FriendColor.BLACK,true);
		Friend tail = new Friend("Tail",FriendColor.BLACK,true);
		Friend myself = new Friend("Me",FriendColor.BLACK,true);
		friends.put(ID_HEAD, head);
		friends.put(ID_TAIL, tail);
		friends.put(ID_ME, myself);
	}

	public void updateList(){
		is_in_action = ServiceUtils.isServiceRunning(SocialActivity.this, GpsTrackerService.class);
		list.invalidateViews();

		if(is_in_action){
			id_order = new ArrayList<Integer>();
			for(int id : friends.keySet()){
				if(friends.get(id).getActive()) id_order.add(id);
			}
			Collections.sort(id_order, new Comparator<Integer>() {
				public int compare(Integer id1, Integer id2) {
					return ((Integer)friends.get(id1).getDistanceRel()).compareTo(
							(Integer)friends.get(id2).getDistanceRel());
				}
			});
		}
		else{
			id_order = new ArrayList<Integer>(friends.keySet());
			id_order.remove(ID_HEAD);
			id_order.remove(ID_TAIL);
			id_order.remove(ID_ME);
			Collections.sort(id_order, new Comparator<Integer>() {
				public int compare(Integer id1, Integer id2) {
					return friends.get(id1).compareTo(friends.get(id2));
				}
			});
		}
	}




	/** Inner class for implementing progress bar before fetching data **/
	private class ChangeFriendTask extends AsyncTask<Void, Void, Integer> 
	{
		private ProgressDialog Dialog = new ProgressDialog(SocialActivity.this);

		private Friend friend_update;
		private int id;

		public ChangeFriendTask(Friend friend_update, int id){
			this.friend_update = friend_update;
			this.id = id;
		}
		@Override
		protected void onPreExecute()
		{
			Dialog.setMessage("Change friend list");
			Dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) 
		{
			//load code from server
			try {
				Thread.sleep(1000); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer result)
		{

			if(result==0)
			{
				SocialActivity.this.friends.put(id, friend_update);
			}
			// after completed finished the progressbar
			Dialog.dismiss();
			SocialActivity.this.updateList();
		}
	}

	private ListView list;
	Friends friends;
	List<Integer> id_order;
	private final String TAG = "SocialActivity"; 
	public boolean is_in_action = false;
	private NetworkClient networkClient = new NetworkClient(this);

	final static Integer ID_HEAD = -1;
	final static Integer ID_TAIL = -2;
	final static Integer ID_ME = -3;

} 
