package de.greencity.bladenightapp.android.admin;


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
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.actions.ActionReload;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.network.NetworkClient;
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
        Button button = (Button) findViewById(R.id.button_set_min_position);
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
        Button button = (Button) findViewById(R.id.button_kill_server);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                networkClient.killServer(null, null);
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
        statusSpinner = (Spinner) findViewById(R.id.spinner_change_status);
        spinnerStatusAdapter = new ArrayAdapter<CharSequence>(AdminActivity.this, android.R.layout.simple_spinner_item);
        spinnerStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerStatusAdapter);

        spinnerStatusAdapter.clear();
        spinnerStatusAdapter.add("");
        spinnerStatusAdapter.add("CAN");
        spinnerStatusAdapter.add("CON");
        spinnerStatusAdapter.add("PEN");

        statusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.i(TAG, "statusSpinner.onItemSelected");
                String status = (String) statusSpinner.getSelectedItem();
                if ( ! status.isEmpty() ) {
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

        routeSpinner = (Spinner) findViewById(R.id.spinner_change_route);
        spinnerRouteNameAdapter = new ArrayAdapter<CharSequence>(AdminActivity.this, android.R.layout.simple_spinner_item);
        spinnerRouteNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(spinnerRouteNameAdapter);

        routeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.i(TAG, "onItemSelected arg2=" + arg2 + " arg3=" + arg3);
                String routeName = (String) routeSpinner.getSelectedItem();
                setActiveRouteOnServer(routeName);
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
            if (adminActivity == null || adminActivity.isFinishing())
                return;
            RouteNamesMessage routeNamesMessage = (RouteNamesMessage) msg.obj;
            adminActivity.updateGuiRouteListFromServerResponse(routeNamesMessage);

            // Now that we have the list of all route names, we can get the active route
            // and update the current selection in the GUI accordingly
            adminActivity.getNextEventFromServer();
        }
    }

    public void updateGuiRouteListFromServerResponse(RouteNamesMessage routeNamesMessage) {
        Log.i(TAG, "updateGuiRouteListFromServerResponse routeNamesMessage=" + routeNamesMessage);

        List<String> routeNamesFromServer = Arrays.asList(routeNamesMessage.rna);
        Collections.sort(routeNamesFromServer);

        List<String> routeNamesFromGui = new ArrayList<>();
        for (int i = 0; i < spinnerRouteNameAdapter.getCount(); i++) {
            routeNamesFromGui.add(spinnerRouteNameAdapter.getItem(i).toString());
        }

        Collections.sort(routeNamesFromServer);
        Collections.sort(routeNamesFromGui);

        if (routeNamesFromServer.equals(routeNamesFromGui)) {
            Log.i(TAG, "updateGuiRouteListFromServerResponse: no need to update GUI");
        } else {
            spinnerRouteNameAdapter.clear();
            spinnerRouteNameAdapter.add("");
            for (String name : routeNamesFromServer) {
                spinnerRouteNameAdapter.add(name);
            }
        }
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
            if (adminActivity == null || adminActivity.isFinishing())
                return;
            EventMessage eventMessage = (EventMessage) msg.obj;
            Log.i(TAG, "Got active event: " + eventMessage.toString());
            if (eventMessage.getRouteName() == null)
                Log.e(TAG, "Server sent invalid route name:" + eventMessage.toString());
            else
                adminActivity.updateGuiRouteCurrent(eventMessage.getRouteName());
            if (eventMessage.getStatus() == null)
                Log.e(TAG, "Server sent invalid status:" + eventMessage.toString());
            else
                adminActivity.updateGuiStatus(eventMessage.getStatus().toString());
        }
    }


    protected void getNextEventFromServer() {
        networkClient.getActiveEvent(new GetActiveEventFromServerHandler(this), null);
    }


    public void updateGuiRouteCurrent(String currentRouteName) {
        TextView routeText = (TextView) findViewById(R.id.text_current_route);
        routeText.setText(currentRouteName);
    }

    public void updateGuiStatus(String status) {
        TextView statusText = (TextView) findViewById(R.id.text_current_status);
        statusText.setText(status);
    }


    static private class NetworkResultHandler extends Handler {
        private WeakReference<AdminActivity> reference;

        NetworkResultHandler(AdminActivity activity) {
            this.reference = new WeakReference<AdminActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final AdminActivity adminActivity = reference.get();
            if (adminActivity == null || adminActivity.isFinishing())
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
            if (adminActivity == null || adminActivity.isFinishing())
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

    final static String TAG = "AdminActivity";
    private NetworkClient networkClient;
    private ArrayAdapter<CharSequence> spinnerRouteNameAdapter;
    private ArrayAdapter<CharSequence> spinnerStatusAdapter;
    private Spinner routeSpinner;
    private Spinner statusSpinner;
}
