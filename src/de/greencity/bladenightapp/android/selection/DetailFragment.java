package de.greencity.bladenightapp.android.selection;


 
import de.greencity.bladenightapp.android.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class DetailFragment extends Fragment {
	public SelectionActivity activity;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public DetailFragment(SelectionActivity activity){
    	super();
    	this.activity = activity;
    }
 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
 
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_view, container, false);
        //activity.updateEvent();
        // in die klasse muss update funktion rein, man kann über view auf die 
        // elemente zugreifen, einzeln durchhangeln, dann kommt man auch hin.
        // vgl: http://stackoverflow.com/questions/7968573/android-viewpager-findviewbyid-not-working-always-returning-null
        return view;
    }
}
