package de.greencity.bladenightapp.android.social;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.social.AddFriendDialog.AddFriendDialogListener;
import de.greencity.bladenightapp.android.social.ChangeFriendDialog.ChangeFriendDialogListener;
import de.greencity.bladenightapp.android.social.ConfirmFriendDialog.ConfirmFriendDialogListener;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;

public class SocialActivity extends FragmentActivity implements AddFriendDialogListener, 
ConfirmFriendDialogListener, ChangeFriendDialogListener {


	ListView list;
	private List<String> List_file;
	private final String TAG = "SocialActivity"; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_social);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		ImageView titlebar = (ImageView)(findViewById(R.id.icon));
		titlebar.setImageResource(R.drawable.ic_menu_settings);
		TextView titletext = (TextView)findViewById(R.id.title);
		titletext.setText(R.string.title_social);

		List_file =new ArrayList<String>();
		list = (ListView)findViewById(R.id.listview);
		CreateListView();
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {	  
		FragmentManager fm = getSupportFragmentManager();
		switch (view.getId()) {
		case R.id.addFriend: 
			AddFriendDialog addFriendDialog = new AddFriendDialog();
			addFriendDialog.show(fm, "fragment_add_friend");

			break;
		case R.id.confirmFriend: 
			ConfirmFriendDialog confirmFriendDialog = new ConfirmFriendDialog();
			confirmFriendDialog.show(fm, "fragment_confirm_friend");
			break;

		}
	}


	private void CreateListView()
	{
		//dummy data to test
		List_file.add("Hans");
		List_file.add("Heinz");
		List_file.add("Hubert");

		//Create an adapter for the listView and add the ArrayList to the adapter.
		list.setAdapter(new ArrayAdapter<String>(SocialActivity.this, android.R.layout.simple_list_item_1,List_file));
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3)
			{
				//args2 is the listViews Selected index
				FragmentManager fm = getSupportFragmentManager();
				ChangeFriendDialog changeFriendDialog = new ChangeFriendDialog(List_file.get(arg2), arg2);
				changeFriendDialog.show(fm, "fragment_change_friend");
			}
		});
	}


	@Override
	public void onFinishAddFriendDialog(String friendName) { 
		new AddFriendTask(friendName).execute();
	}

	@Override
	public void onFinishConfirmFriendDialog(String friendName, String code) { 
		new ConfirmFriendTask(friendName,code).execute();
	}

	@Override
	public void onFinishChangeFriendDialog(String friendName, int index) { 
		new ChangeFriendTask(friendName, index).execute();
	}


	/** Inner class for implementing progress bar before fetching data **/
	private class AddFriendTask extends AsyncTask<Void, Void, Integer> 
	{
		private ProgressDialog Dialog = new ProgressDialog(SocialActivity.this);

		private String friendName;
		private String code;

		public AddFriendTask(String friendName){
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
				SocialActivity.this.List_file.add(friendName + " ... pending");
			}
			// after completed finished the progressbar
			Dialog.dismiss();
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
				SocialActivity.this.List_file.add(friendName);
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

		private String friendName;
		private int index;

		public ChangeFriendTask(String friendName, int index){
			this.friendName = friendName;
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
				SocialActivity.this.List_file.set(index, friendName);
			}
			// after completed finished the progressbar
			Dialog.dismiss();
		}
	}
} 
