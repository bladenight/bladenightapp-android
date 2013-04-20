package de.greencity.bladenightapp.android.about;


import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.admin.AdminUtilities;
import de.greencity.bladenightapp.android.network.NetworkClient;

public class AboutActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
	}

	@Override
	public void onStart() {
		super.onStart();

		configureActionBar();

	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.setTitle(R.string.title_about)
		.configure();
	}


	@Override
	public void onStop() {
		super.onStop();
	}

	public void onClick(View view) {

		switch (view.getId()) {
		default:
			clickCounter++;
			if(clickCounter == 5) {
				clickCounter = 0;
				requestAdminPassword();
			}
		}
	}


	private void requestAdminPassword() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//@Override
			public void onClick(DialogInterface dialog, int which) {
				Editable value = input.getText();
				String password = value.toString();
				Log.i(TAG, "Entered value: " + password);
				handleEnteredPassword(password);
			}
		});
		alert.show();
	}

	private static class VerificationHandler extends Handler {
		protected WeakReference<AboutActivity> reference;
		protected ProgressDialog progressDialog;
		protected String password;
		VerificationHandler(AboutActivity activity, ProgressDialog progressDialog, String password) {
			this.reference = new WeakReference<AboutActivity>(activity);
			this.progressDialog = progressDialog;
			this.password = password;
		}
	}

	private static class VerificationSuccessHandler extends VerificationHandler {
		VerificationSuccessHandler(AboutActivity activity, ProgressDialog progressDialog, String password) {
			super(activity, progressDialog, password);
		}
		@Override
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			Toast.makeText(reference.get(), "OK", Toast.LENGTH_LONG).show();
			AdminUtilities.saveAdminPassword(reference.get(), password);
		}
	}

	private static class VerificationFailureHandler extends VerificationHandler {
		VerificationFailureHandler(AboutActivity activity, ProgressDialog progressDialog, String password) {
			super(activity, progressDialog, password);
		}
		@Override
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			Toast.makeText(reference.get(), "Failed", Toast.LENGTH_LONG).show();
			AdminUtilities.deleteAdminPassword(reference.get());
		}
	}

	protected void handleEnteredPassword(String password) {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Checking password...");
		progressDialog.show();

		NetworkClient networkClient = new NetworkClient(this);
		networkClient.verifyAdminPassword(password,
				new VerificationSuccessHandler(this, progressDialog, password),
				new VerificationFailureHandler(this, progressDialog, password));
	}

	final static String TAG = "AboutActivity";
	private int clickCounter; 
} 