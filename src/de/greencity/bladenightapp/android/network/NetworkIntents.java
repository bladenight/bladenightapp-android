package de.greencity.bladenightapp.android.network;





public interface NetworkIntents {
	static final String prefix =  "de.greencity.bladenightapp.android.network.";

	// Actions sent to the NetworkService to request an action
	static public final String CONNECT 				= prefix+"CONNECT"; 
	static public final String DISCONNECT 			= prefix+"DISCONNECT"; 
	static public final String GET_ACTIVE_EVENT 	= prefix+"GET_ACTIVE_EVENT";
	static public final String GET_ALL_EVENTS 		= prefix+"GET_ALL_EVENTS";
	static public final String GET_ACTIVE_ROUTE 	= prefix+"GET_ACTIVE_ROUTE";
	static public final String GET_REAL_TIME_DATA 	= prefix+"GET_REAL_TIME_DATA";
	static public final String DOWNLOAD_REQUEST 	= prefix+"DOWNLOAD_REQUEST";

	// Broadcast sent by the NetworkService on updates from the server 
	static public final String CONNECTED 			= prefix+"CONNECTED"; 
	static public final String DISCONNECTED 		= prefix+"DISCONNECTED"; 
	static public final String GOT_ACTIVE_EVENT 	= prefix+"GOT_ACTIVE_EVENT";
	static public final String GOT_ALL_EVENTS 		= prefix+"GOT_ALL_EVENTS";
	static public final String GOT_ACTIVE_ROUTE 	= prefix+"GOT_ACTIVE_ROUTE";
	static public final String GOT_REAL_TIME_DATA 	= prefix+"GOT_REAL_TIME_DATA";
	static public final String DOWNLOAD_PROGRESS 	= prefix+"DOWNLOAD_PROGRESS";
	static public final String DOWNLOAD_SUCCESS 	= prefix+"DOWNLOAD_SUCCESS";
	static public final String DOWNLOAD_FAILURE 	= prefix+"DOWNLOAD_FAILURE";
	
	// Actions to update the persistent state of the network service
	static public final String LOCATION_UPDATE		= prefix+"LOCATION_UPDATE";
}
