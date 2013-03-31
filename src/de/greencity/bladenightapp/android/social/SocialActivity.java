package de.greencity.bladenightapp.android.social;

import java.util.ArrayList;
import java.util.List;

import com.markupartist.android.widget.ActionBar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.social.Friend.FriendColor;
import de.greencity.bladenightapp.android.social.InviteFriendDialog.InviteFriendDialogListener;
import de.greencity.bladenightapp.android.social.ChangeFriendDialog.ChangeFriendDialogListener;
import de.greencity.bladenightapp.android.social.ConfirmFriendDialog.ConfirmFriendDialogListener;

public class SocialActivity extends FragmentActivity implements InviteFriendDialogListener, 
ConfirmFriendDialogListener, ChangeFriendDialogListener {


	ListView list;
	private List<Friend> friends;
	private final String TAG = "SocialActivity"; 

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

		friends =new ArrayList<Friend>();
		list = (ListView)findViewById(R.id.listview);
		CreateListView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");

		configureActionBar();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
//		Action mapActionWithParameters = new ActionMap() {
//			@Override
//			public void performAction(View view) {
//				Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
//				Event event = getEventShown();
//				if ( event == null ) {
//					Log.e(TAG, "No event currently shown");
//					return;
//				}
//				intent.putExtra("routeName", event.getRouteName());
//				intent.putExtra("isRealTime", posEventCurrent == posEventShown);
//				view.getContext().startActivity(intent);
//			}
//		};
		new ActionBarConfigurator(actionBar)
		.hide(ActionItemType.EVENT_SELECTION)
		.hide(ActionItemType.FRIENDS)
//		.replaceAction(ActionItemType.MAP, mapActionWithParameters)
		.setTitle(R.string.title_social)
		.configure();

	}


	private void CreateListView()
	{
		//dummy data to test
		friends.add(new Friend("Hans",FriendColor.ORANGE,true));
		friends.add(new Friend("Heinz",FriendColor.RED,true));
		friends.add(new Friend("Hubert",FriendColor.GREEN,true));

		//Create an adapter for the listView and add the ArrayList to the adapter.
		list.setAdapter(new FriendListAdapter(SocialActivity.this, friends));
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,long arg3)
			{
				//args2 is the listViews Selected index
				FragmentManager fm = getSupportFragmentManager();
				ChangeFriendDialog changeFriendDialog = new ChangeFriendDialog(friends.get(arg2), arg2);
				changeFriendDialog.show(fm, "fragment_change_friend");
				return true;
			}
		});
	}


	@Override
	public void onFinishInviteFriendDialog(String friendName) { 
		new InviteFriendTask(friendName).execute();
	}

	@Override
	public void onFinishConfirmFriendDialog(String friendName, String code) { 
		new ConfirmFriendTask(friendName,code).execute();
	}

	@Override
	public void onFinishChangeFriendDialog(Friend friend_update, int index) { 
		new ChangeFriendTask(friend_update, index).execute();
	}


	/** Inner class for implementing progress bar before fetching data **/
	private class InviteFriendTask extends AsyncTask<Void, Void, Integer> 
	{
		private ProgressDialog Dialog = new ProgressDialog(SocialActivity.this);

		private String friendName;
		private String code;

		public InviteFriendTask(String friendName){
			this.friendName = friendName;
		}
		@Override
		protected void onPreExecute()
		{
			Dialog.setMessage("Loading add-friend code...");
			Dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) 
		{
			//load code from server
			try {
				Thread.sleep(4000); 
				code = "FGH46H"; //dummy example to test layout
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

				FragmentManager fm = getSupportFragmentManager();
				ShowCodeDialog showCodeDialog = new ShowCodeDialog(friendName, code);
				showCodeDialog.show(fm, "fragment_show_code");
				SocialActivity.this.friends.add(new Friend(friendName + " ... pending", FriendColor.GREEN,true));
			}
			// after completed finished the progressbar
			Dialog.dismiss();
			SocialActivity.this.list.invalidateViews();
		}
	}

	/** Inner class for implementing progress bar before fetching data **/
	private class ConfirmFriendTask extends AsyncTask<Void, Void, Integer> 
	{
		private ProgressDialog Dialog = new ProgressDialog(SocialActivity.this);

		private String friendName;
		private String code;

		public ConfirmFriendTask(String friendName, String code){
			this.friendName = friendName;
			this.code = code;
		}
		@Override
		protected void onPreExecute()
		{
			Dialog.setMessage("Validating friend code...");
			Dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) 
		{
			int exit = 0;
			//send code to server and get friend data
			try {
				Thread.sleep(4000); 
				if(code.equals("aaa")){ 
					exit = 1;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return exit;
		}

		@Override
		protected void onPostExecute(Integer result)
		{

			if(result==1)
			{
				SocialActivity.this.friends.add(new Friend(friendName, FriendColor.GREEN,true));
				Toast.makeText(getApplicationContext(), friendName + " was added", Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(getApplicationContext(), "Code is not valid", Toast.LENGTH_LONG).show();
			}
			// after completed finished the progressbar
			Dialog.dismiss();
			SocialActivity.this.list.invalidateViews();
		}
	}

	/** Inner class for implementing progress bar before fetching data **/
	private class ChangeFriendTask extends AsyncTask<Void, Void, Integer> 
	{
		private ProgressDialog Dialog = new ProgressDialog(SocialActivity.this);

		private Friend friend_update;
		private int index;

		public ChangeFriendTask(Friend friend_update, int index){
			this.friend_update = friend_update;
			this.index = index;
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
				SocialActivity.this.friends.set(index, friend_update);
			}
			// after completed finished the progressbar
			Dialog.dismiss();
			SocialActivity.this.list.invalidateViews();
		}
	}
} 
