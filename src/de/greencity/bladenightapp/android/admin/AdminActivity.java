package de.greencity.bladenightapp.android.admin;


import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionReload;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import de.greencity.bladenightapp.network.messages.RouteNamesMessage;

public class AdminActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkClient = BladeNightApplication.networkClient;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_admin);
    }

    @Override
    public void onStart() {
        super.onStart();

        configureActionBar();
        configureRouteNameSpinner();
        configureStatusSpinner();

        configureSetMinPosButton();
        configureKillServerButton();

        getRouteListFromServer();
    }

    private void configureSetMinPosButton() {
        Button button= (Button) findViewById(R.id.button_set_min_position);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AdminActivity.this);

                alert.setTitle("Min. position");
                alert.setMessage("Enter the required value:");

                // Set an EditText view to get user input
                final EditText editText = new EditText(AdminActivity.this);
                editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(editText);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int value = Integer.parseInt(editText.getText().toString());
                        networkClient.setMinimumLinearPosition(value, null, null);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });
    }

    private void configureKillServerButton() {
        Button button= (Button) findViewById(R.id.button_kill_server);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            networkClient.killServer(null,null);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                builder.setMessage("Are you sure you want to kill the server?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }


    private void configureStatusSpinner() {
        statusSpinner = (Spinner) findViewById(R.id.spinner_current_status);
        spinnerStatusAdapter = new ArrayAdapter<CharSequence>(AdminActivity.this, android.R.layout.simple_spinner_item);
        spinnerStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerStatusAdapter);

        statusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.i(TAG,"statusSpinner.onItemSelected");
                // Silly Android might fire the listener even before we are ready, hence this check
                if ( isStatusSpinnerInitialized ) {
                    String status = (String) statusSpinner.getSelectedItem();
                    setActiveStatusOnServer(status);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    private void configureRouteNameSpinner() {
        Log.i(TAG, "configureRouteNameSpinner ");

        routeNameSpinner = (Spinner) findViewById(R.id.spinner_current_route);
        spinnerRouteNameAdapter = new ArrayAdapter<CharSequence>(AdminActivity.this, android.R.layout.simple_spinner_item);
        spinnerRouteNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeNameSpinner.setAdapter(spinnerRouteNameAdapter);

        routeNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.i(TAG, "onItemSelected arg2=" + arg2 + " arg3="+arg3);
                // Silly Android might fire the listener even before we are ready
                if (isRouteNameSpinnerInitialized) {
                    String routeName = (String) routeNameSpinner.getSelectedItem();
                    setActiveRouteOnServer(routeName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    protected void getAllInformationFromServer() {
        getNextEventFromServer();
        getRouteListFromServer();
    }


    static class GetAllRouteNamesFromServerHandler extends Handler {
        private WeakReference<AdminActivity> reference;
        GetAllRouteNamesFromServerHandler(AdminActivity activity) {
            this.reference = new WeakReference<AdminActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final AdminActivity adminActivity = reference.get();
            if ( adminActivity == null || adminActivity.isFinishing() )
                return;
            RouteNamesMessage routeNamesMessage = (RouteNamesMessage)msg.obj;
            adminActivity.updateGuiRouteListFromServerResponse(routeNamesMessage);
        }
    }

    public void updateGuiRouteListFromServerResponse(RouteNamesMessage routeNamesMessage) {
        Log.i(TAG, "updateGuiRouteListFromServerResponse routeNamesMessage=" + routeNamesMessage);

        List<String> routeNames = Arrays.asList(routeNamesMessage.rna);
        Collections.sort(routeNames);

        spinnerRouteNameAdapter.clear();
        for(String name: routeNames) {
            spinnerRouteNameAdapter.add(name);
        }
        spinnerRouteNameAdapter.notifyDataSetChanged();
        getNextEventFromServer();
    }

    protected void getRouteListFromServer() {
        networkClient.getAllRouteNames(new GetAllRouteNamesFromServerHandler(this), null);
    }

    static class GetActiveEventFromServerHandler extends Handler {
        private WeakReference<AdminActivity> reference;
        GetActiveEventFromServerHandler(AdminActivity activity) {
            this.reference = new WeakReference<AdminActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final AdminActivity adminActivity = reference.get();
            if ( adminActivity == null || adminActivity.isFinishing() )
                return;
            EventMessage eventMessage = (EventMessage)msg.obj;
            Log.i(TAG, "Got active event: " + eventMessage.toString());
            if ( eventMessage.getRouteName() == null)
                Log.e(TAG, "Server sent invalid route name:" + eventMessage.toString());
            else
                adminActivity.updateGuiRouteCurrent(eventMessage.getRouteName());
            if ( eventMessage.getStatus() == null )
                Log.e(TAG, "Server sent invalid status:" + eventMessage.toString());
            else
                adminActivity.updateGuiStatus(eventMessage.getStatus().toString());
        }
    }


    protected void getNextEventFromServer() {
        networkClient.getActiveEvent(new GetActiveEventFromServerHandler(this), null);
    }


    public void updateGuiRouteCurrent(String currentRouteName) {
        for(int i = 0 ; i < spinnerRouteNameAdapter.getCount() ; i ++) {
            if ( currentRouteName.equals(spinnerRouteNameAdapter.getItem(i)) )
                setSpinnerSelectionWithoutCallingListener(routeNameSpinner, i);
        }
        isRouteNameSpinnerInitialized = true;
        configureStatusSpinner();
    }

    public void updateGuiStatus(String status) {
        spinnerStatusAdapter.clear();
        spinnerStatusAdapter.add("CAN");
        spinnerStatusAdapter.add("CON");
        spinnerStatusAdapter.add("PEN");
        for(int i = 0 ; i < spinnerStatusAdapter.getCount() ; i ++) {
            if ( status.equals(spinnerStatusAdapter.getItem(i)) )
                setSpinnerSelectionWithoutCallingListener(statusSpinner, i);
        }
        isStatusSpinnerInitialized = true;
    }


    static private class NetworkResultHandler extends Handler {
        private WeakReference<AdminActivity> reference;
        NetworkResultHandler(AdminActivity activity) {
            this.reference = new WeakReference<AdminActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final AdminActivity adminActivity = reference.get();
            if ( adminActivity == null || adminActivity.isFinishing() )
                return;
            Toast.makeText(adminActivity, "OK", Toast.LENGTH_SHORT).show();
            adminActivity.getAllInformationFromServer();
        }
    }

    static private class NetwortErrorHandler extends Handler {
        private WeakReference<AdminActivity> reference;
        NetwortErrorHandler(AdminActivity activity) {
            this.reference = new WeakReference<AdminActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final AdminActivity adminActivity = reference.get();
            if ( adminActivity == null || adminActivity.isFinishing() )
                return;
            Toast.makeText(adminActivity, "Failed" + msg, Toast.LENGTH_SHORT).show();
            adminActivity.getAllInformationFromServer();
        }
    }


    protected void setActiveRouteOnServer(String routeName) {
        Log.i(TAG, "setActiveRouteOnServer");
        networkClient.setActiveRoute(routeName, new NetworkResultHandler(this), null);
    }

    protected void setActiveStatusOnServer(String status) {
        Log.i(TAG, "setActiveStatusOnServer");
        networkClient.setActiveStatus(EventStatus.valueOf(status), new NetworkResultHandler(this), new NetwortErrorHandler(this));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void configureActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        Action reloadAction = new ActionReload() {
            @Override
            public void performAction(View view) {
                getAllInformationFromServer();
            }
        };
        new ActionBarConfigurator(actionBar)
        .setAction(ActionItemType.RELOAD, reloadAction)
        .setTitle(R.string.title_admin)
        .configure();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    private void setSpinnerSelectionWithoutCallingListener(final Spinner spinner, final int selection) {
        final OnItemSelectedListener l = spinner.getOnItemSelectedListener();
        spinner.setOnItemSelectedListener(null);
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(selection);
                spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setOnItemSelectedListener(l);
                    }
                });
            }
        });
    }

    final static String TAG = "AdminActivity";
    private NetworkClient networkClient;
    private ArrayAdapter<CharSequence> spinnerRouteNameAdapter;
    private ArrayAdapter<CharSequence> spinnerStatusAdapter;
    private Spinner routeNameSpinner;
    private Spinner statusSpinner;
    private boolean isStatusSpinnerInitialized = false;
    private boolean isRouteNameSpinnerInitialized = false;
}
