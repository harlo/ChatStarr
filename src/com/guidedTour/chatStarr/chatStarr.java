package com.guidedTour.chatStarr;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;

public class chatStarr extends Activity {
private ArduinoReceiver aR = new ArduinoReceiver();
	
	String UID = /*[YOUR GMAIL ADDRESS]*/;
	String targetID = /*[TARGET'S GMAIL ADDRESS]*/;	
	String pssst = /*[YOUR PASSWORD]*/;
	String host = "talk.google.com";
	String service = "gmail.com";
	int port = 5222;
	
	ConnectionConfiguration conFig;
	XMPPConnection theCon;

	ImageView cSLogo;
	Button onOff;
	TextView displayStatus;
	
	String statString;
	String firstTxt = "ChatStar initialized...  Stay tuned for messages from Android.";
	
	public static final String TAG = "chatStar";
	
	boolean isOn = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        statString = "Welcome to Chat Star.  Click \"O N\" to get started.";
        
        cSLogo = (ImageView) findViewById(R.id.cSLogo);
        int logoID = getResources().getIdentifier("cslogo", "drawable", "com.guidedTour.chatStar");
       	cSLogo.setImageResource(logoID);
       	onOff = (Button) findViewById(R.id.onOff);
       	onOff.setText("O N");
       	onOff.setOnClickListener(new buttonClick());
       	displayStatus = (TextView) findViewById(R.id.displayStatus);
       	displayStatus.setText(statString);	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	registerReceiver(aR, new IntentFilter("amarino.RESPONSE"));
    	
    	// are the setCollection commands necessary?
    	Intent setCollection = new Intent("amarino.SET_COLLECTION");
    	setCollection.putExtra("COLLECTION_NAME", "chatStar");
    	sendBroadcast(setCollection);
    	sendBroadcast(new Intent("amarino.CONNECT"));
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	sendBroadcast(new Intent("amarino.DISCONNECT"));
    	unregisterReceiver(aR);
    }
    
    public void makeConnection() {
    	conFig  = new ConnectionConfiguration(host,port,service);
    	theCon = new XMPPConnection(conFig);
    	
		try {
			theCon.connect();
			Log.v(TAG, " ********** CONNECTED **********");
		} catch (XMPPException e) {}
		try {
			theCon.login(UID, pssst);
			Log.v(TAG," ********** LOGGED IN **********");
		} catch (XMPPException e) {}
		sendTxt(firstTxt);
    }
    
    public void sendTxt(String msg) {
    	Log.i(TAG,"SENDING TEXT [" + msg + "] to " + targetID);
    	Message theMessage = new Message(targetID, Message.Type.chat);
    	theMessage.setBody(msg);
    	theCon.sendPacket(theMessage);
    }
    
    public class buttonClick implements Button.OnClickListener {
    	public void onClick(View bClick) {
    		if ((bClick == onOff) && (isOn == false)) {
    			isOn = true;
    			onOff.setText("O F F");
    			statString = "Chat Star starting...";
    			makeConnection();
    			displayStatus.setText(statString);
    		} else if ((bClick == onOff) && (isOn == true)) {
    			isOn = false;
    			onOff.setText("O N");
    			statString = "Chat Star turning off...";
    			displayStatus.setText(statString);
    			theCon.disconnect();
    		}
    	}
    }
    
    public class ArduinoReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent != null) {
    			String newData = intent.getStringExtra("data");
    			if(newData != null) {
    				if(isOn) {
    					sendTxt(newData);
    				}
    			} else {
    				Log.v(TAG,"************** no data? **************");
    			}
    		} else {
    			Log.v(TAG,"************** no intent? **************");
    		}
    	}
    }
}