package de.greencity.bladenightapp.android.social;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.greencity.bladenightapp.dev.android.R;


public class ShowCodeDialog extends DialogFragment  {

	public ShowCodeDialog() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Bundle arguments = getArguments();
		if ( arguments != null ) {
			this.friendName = arguments.getString(ARG_NICKNAME);
			this.code = arguments.getString(ARG_CODE);
		}
		else {
			Log.e(TAG, "arguments="+arguments);
		}

		View view = inflater.inflate(R.layout.show_code_dialog, container);

		getDialog().setTitle(getResources().getString(R.string.msg_new_code)+ " " + friendName);

		TextView codetext = (TextView) view.findViewById(R.id.code_text);
		codetext.setText(code);

		Button confirmButton = (Button) view.findViewById(R.id.show_code_confirm);
		confirmButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;
	}


	final private static String TAG = "ShowCodeDialog"; 
	final public static String ARG_CODE = "code";
	final public static String ARG_NICKNAME = "nickname";
	private String friendName;
	private String code;
}