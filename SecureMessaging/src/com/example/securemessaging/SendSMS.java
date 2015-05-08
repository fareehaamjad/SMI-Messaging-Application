package com.example.securemessaging;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

public class SendSMS 
{

	static String className = "SendSMS";


	/*public static void sendSMSMessage(String message) 
	{
		message = "APP:" + message;
		Log.i(className , "sendSMSMessage: Message: "+ message);
		String phoneNo = Constants.SERVER_NUMBER;
		try 
		{
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNo, null, message, null, null);
			Log.i(className, "SMS sent");
		} catch (Exception e) 
		{
			Log.e(className, "SMS failed");
			e.printStackTrace();
		}
	}*/


	public static void sendSMSMessage(String msg, Context context, final String phoneNo1) 
	{
		msg = "APP:" + msg;
		//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		//final String phoneNo = preferences.getString("SERVER_NUMBER", Constants.SERVER_NUMBER);
		
		//final String tempPhoneNo = preferences.getString("SERVER_NUMBER", "abc");
		String pi[] = phoneNo1.split("0", 2);
		final String phoneNo = "00971" + pi[1];
		//Log.i(className , p +"end");
		Log.i(className , "sendSMSMessage: Message: "+ msg + ": "+phoneNo);
		//Log.i(className , tempPhoneNo);

		try 
		{
			//final Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

			//String phoneNo = MainActivity.prefs.getString("pref_key_number", Constants.SERVER_NUMBER);

			String SENT = "sent";
			String DELIVERED = "delivered";

			Intent sentIntent = new Intent(SENT+phoneNo+msg );
			sentIntent.putExtra("messageId", phoneNo + "."+msg);

			PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
					sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);




			//PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent,PendingIntent.FLAG_UPDATE_CURRENT);

			Intent deliveryIntent = new Intent(DELIVERED+phoneNo+msg);
			deliveryIntent.putExtra("messageId", phoneNo + "."+msg);

			PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0,
					deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);


			//PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, deliveryIntent,PendingIntent.FLAG_UPDATE_CURRENT);

			/* Register for SMS send action */
			context.registerReceiver(new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) 
				{
					String messageID = intent.getStringExtra("messageId");

					String result = messageID+"-";

					switch (getResultCode()) {

					case Activity.RESULT_OK:
						result = result + "Transmission successful";

						Log.i("transmission", "successful" + messageID);
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						//this is when the message is not sent
						result = result + "Transmission failed";
						Log.i("transmission", "failed" + messageID);

						SMSfail(context);

						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						result = result + "Radio off";
						Log.i("transmission", "radio off"+ messageID);

						SMSfail(context);
						
						
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						result = result + "No PDU defined";
						Log.i("transmission", "no PDU defined"+ messageID);
						
						SMSfail(context);

						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						result = result + "No service";
						Log.i("transmission", "no service"+ messageID);
						
						SMSfail(context);

						break;
					}

					//Logger.add(phoneNo + " ---" + result);
					Log.i("test",phoneNo+"--"+ result);
					//Toast.makeText(context, result, Toast.LENGTH_SHORT).show();


				}

			}, new IntentFilter(SENT+phoneNo+msg));
			/* Register for Delivery event */
			context.registerReceiver(new BroadcastReceiver() 
			{

				@Override
				public void onReceive(Context context, Intent intent) 
				{
					//Log.i(phoneNo, "DELIVERED");

					//Toast.makeText(context, "Deliverd",
					//		Toast.LENGTH_LONG).show();


					String messageID = intent.getStringExtra("messageId");

					String result = messageID+"-";

					switch (getResultCode()) {
					case Activity.RESULT_OK:
						result = result+"dElIvErEd";
						break;
					case Activity.RESULT_CANCELED:
						result = result+"Not Delivered";
						break;
					}

					//Logger.add(phoneNo + " ---" + result);
					Log.i("test", phoneNo +"--"+ result);
				}

			}, new IntentFilter(DELIVERED+phoneNo+msg));

			/*Send SMS*/

			ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
			ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();

			try 
			{
				SmsManager sms = SmsManager.getDefault();
				ArrayList<String> mSMSMessage = sms.divideMessage(msg);
				for (int i = 0; i < mSMSMessage.size(); i++) 
				{
					sentPendingIntents.add(i, sentPI);

					deliveredPendingIntents.add(i, deliverPI);
				}
				sms.sendMultipartTextMessage(phoneNo, null, mSMSMessage,
						sentPendingIntents, deliveredPendingIntents);

			} catch (Exception e) 
			{

				e.printStackTrace();

				Log.i("test", "EXCEPTION");
				/*Toast.makeText(mContext, "SMS sending failed...",
		                Toast.LENGTH_SHORT).show();*/
			}

		} 
		catch (Exception ex) 
		{
			//Logger.add("EXCEPTION EXCEPTION EXCEPTION EXCEPTION");
			Log.i("test", "EXCEPTION EXCEPTION EXCEPTION EXCEPTION");
			//Toast.makeText(context,ex.getMessage().toString(), Toast.LENGTH_LONG).show();
			ex.printStackTrace();
		}
	}


	protected static void SMSfail(Context context) 
	{
		Intent i = new Intent(context, SMSFail.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(i);
		((Activity) context).finish();

		// TODO Auto-generated method stub
		
	}

}