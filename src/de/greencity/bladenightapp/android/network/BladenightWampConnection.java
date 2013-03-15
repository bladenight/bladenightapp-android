package de.greencity.bladenightapp.android.network;

import de.tavendo.autobahn.WampConnection;

public class BladenightWampConnection extends WampConnection {

	public boolean isUsable() {
		return isConnected() && isUsable;
	}

	public void isUsable(boolean isUsable) {
		this.isUsable = isUsable;
	}
	

	private boolean isUsable;
}
