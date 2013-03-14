package de.greencity.bladenightapp.android.network;


public interface Actions {
	static final String prefix =  "de.greencity.bladenightapp.android.";

	// Actions sent to the NetworkService to request an action
	static public final String CONNECT 				= prefix+"CONNECT"; 
	static public final String DISCONNECT 			= prefix+"DISCONNECT"; 
	static public final String GET_ACTIVE_EVENT 	= prefix+"GET_ACTIVE_EVENT";
	static public final String GET_ALL_EVENTS 		= prefix+"GET_ALL_EVENTS";
	static public final String GET_ACTIVE_ROUTE 	= prefix+"GET_ACTIVE_ROUTE";

	// Broadcast sent by the NetworkService on updates from the server 
	static public final String CONNECTED 			= prefix+"CONNECTED"; 
	static public final String DISCONNECTED 		= prefix+"DISCONNECTED"; 
	static public final String GOT_ACTIVE_EVENT 	= prefix+"GOT_ACTIVE_EVENT";
	static public final String GOT_ALL_EVENTS 		= prefix+"GOT_ALL_EVENTS";
	static public final String GOT_ACTIVE_ROUTE 	= prefix+"GOT_ACTIVE_ROUTE";
}
