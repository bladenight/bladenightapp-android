package de.greencity.bladenightapp.android.selection;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.greencity.bladenightapp.announcements.*;
import de.greencity.bladenightapp.dev.android.R;


public class AnnouncementDialog extends DialogFragment  {
    public AnnouncementDialog() {
        // Empty constructor required for DialogFragment
    }
    
    public void setAnnouncement(Announcement announcement){
    	this.annoucement = announcement;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	setValues();
        rootView = inflater.inflate(R.layout.help_dialog, container);
        getDialog().setTitle(headline);
        setButtonListeners(rootView);
        TextView text = (TextView) rootView.findViewById(R.id.help_text);
        text.setText(message);
        text.setMovementMethod(new ScrollingMovementMethod());
        
        return rootView;
    }
    
    private void setButtonListeners(View view){
    	Button confirmButton = (Button) view.findViewById(R.id.help_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        });
    }
    
    private void setValues(){
    	if(Locale.getDefault().getLanguage()=="de"){
    		message = annoucement.getMessageD();
    		headline = annoucement.getHeadlineD();
    	}
    	else{
    		message = annoucement.getMessageE();
    		headline = annoucement.getHeadlineE();
    	}
    	//TODO
//    	switch (annoucement.getType()){
//    	case Announcement.Type.NEW_FEATURE:
//    		headline = ""
//    	
//    	}
    	
    }
    
	private View rootView;
	private Announcement annoucement;
	private String message;
	private String headline;
}