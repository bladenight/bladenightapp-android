package de.greencity.bladenightapp.android.network;


public class BroadcastWampBridgeBuilder<Input, Output> {
	
	BroadcastWampBridgeBuilder(Class<Input> inputClass, Class<Output> outputClass) {
		this.broadcastWampBridge = new BroadcastWampBridge<Input, Output>(inputClass, outputClass);
	}
			
	private BroadcastWampBridge<Input, Output> broadcastWampBridge;


	public BroadcastWampBridgeBuilder<Input, Output> setLogPrefix(String logPrefix) {
		broadcastWampBridge.setLogPrefix(logPrefix);
		return this;
	}


	public BroadcastWampBridgeBuilder<Input, Output> setWampConnection(BladenightWampConnection wampConnection) {
		broadcastWampBridge.setWampConnection(wampConnection);
		return this;
	}


	public BroadcastWampBridgeBuilder<Input, Output> setUrl(String url) {
		broadcastWampBridge.setUrl(url);
		return this;
	}

	public BroadcastWampBridgeBuilder<Input, Output> setOutputIntentName(String name) {
		broadcastWampBridge.setOutputIntentName(name);
		return this;
	}
	
	public BroadcastWampBridge<Input, Output> build() {
		return broadcastWampBridge;
	}

}