package com.example.securemessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver
{
	static final String ACTION = "android.intent.action.DATA_SMS_RECEIVED"; 
	//static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";(tried this too, but failed) 

	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String msg = "";
		String number = "";
		
		if (bundle != null)
		{
			//---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			
			for (int i=0; i<msgs.length; i++){
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
								
				msg += msgs[i].getMessageBody().toString();
			}
			number =  msgs[0].getOriginatingAddress();
			Log.i("message", "message received");
			Toast.makeText(context, "Message recieved", Toast.LENGTH_LONG).show();
			if (number.contains("+971"))
			{
				number = number.replaceFirst("\\+971", "0");
				Log.i("number", number);
			}
			else if (number.contains("+1"))
			{
				number = number.replaceFirst("\\+1", "");
				Log.i("number", number);
			}
			//---display the new SMS message---
			//Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			Message mesage = new Message();
			mesage.number = number;
			//mesage.message = StringCompressor.decompress(msg);
			mesage.message = msg;
			
			try {
				
				Log.i("sending msg to addMsg", mesage.number);
				MainActivity.addMsg(mesage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//SMSActivity.addMsg(mesage);
			
			abortBroadcast();
		}
		
		
		
	}
}

