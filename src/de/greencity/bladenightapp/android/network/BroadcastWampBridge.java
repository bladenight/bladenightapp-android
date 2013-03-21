package de.greencity.bladenightapp.android.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import de.tavendo.autobahn.Wamp.CallHandler;

public class BroadcastWampBridge<Input, Output> extends BroadcastReceiver {

	BroadcastWampBridge(Class<Input> inputPayloadClass, Class<Output> outputPayloadClass) {
		this.inputPayloadClass = inputPayloadClass;
		this.outputPayloadClass = outputPayloadClass;
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, logPrefix+"onReceive");

		if ( wampConnection == null ) {
			Log.w(TAG, logPrefix + ": no wamp connection provided");
			return;
		}

		if ( ! wampConnection.isUsable() ) {
			Log.w(TAG, logPrefix + ": Not connected");
			context.sendBroadcast(new Intent(Actions.CONNECT));
			return;
		}

		CallHandler callHandler = new CallHandler() {
			@Override
			public void onError(String arg0, String arg1) {
				Log.e(TAG, logPrefix + " onError: " + arg0 + " " + arg1);
			}

			@Override
			public void onResult(Object object) {
				@SuppressWarnings("unchecked")
				Output msg = (Output) object;
				if ( msg == null ) {
					Log.e(TAG, logPrefix+" Failed to cast");
					return;
				}
				Intent intent = new Intent(outputIntentName);
				intent.putExtra("json", new Gson().toJson(msg));
				context.sendBroadcast(intent);
			}
		};
		
		Input input = getInput(intent);
		
		if ( input == null )
			wampConnection.call(url, outputPayloadClass, callHandler);
		else
			wampConnection.call(url, outputPayloadClass, callHandler, input);
	}
	
	private Input getInput(Intent intent) {
		Bundle extras = intent.getExtras();
		if ( extras == null )
			return null;
		String inputJson = extras.getString("json");
		if ( inputJson == null )
			return null;
		Input input = new Gson().fromJson(inputJson, inputPayloadClass);
		if ( input == null ) {
			Log.e(TAG, "Failed to parse json: " + inputJson);
		}
		return input;
	}

	public String getLogPrefix() {
		return logPrefix;
	}

	public void setLogPrefix(String logPrefix) {
		this.logPrefix = logPrefix;
	}

	public BladenightWampConnection getWampConnection() {
		return wampConnection;
	}

	public void setWampConnection(BladenightWampConnection wampConnection) {
		this.wampConnection = wampConnection;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Class<Input> getInputPayloadClass() {
		return inputPayloadClass;
	}

	public void setInputPayloadClass(Class<Input> inputPayloadClass) {
		this.inputPayloadClass = inputPayloadClass;
	}

	public Class<Output> getOutputPayloadClass() {
		return outputPayloadClass;
	}

	public void setOutputPayloadClass(Class<Output> outputPayloadClass) {
		this.outputPayloadClass = outputPayloadClass;
	}

	public String getOutputIntentName() {
		return outputIntentName;
	}

	public void setOutputIntentName(String outputIntentName) {
		this.outputIntentName = outputIntentName;
	}

	public String logPrefix = "";
	final String TAG = "BroadcastWampBridge";
	private BladenightWampConnection wampConnection;
	private String url;
	private Class<Input> inputPayloadClass;
	private Class<Output> outputPayloadClass;
	private String outputIntentName;
}
