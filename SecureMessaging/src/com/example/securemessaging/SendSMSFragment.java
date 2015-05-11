package com.example.securemessaging;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendSMSFragment extends Fragment 
{
	TextView number;
	static EditText message;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{

		View rootView = inflater.inflate(R.layout.send_sms_fragment, container, false);
		
		number = (TextView) rootView.findViewById(R.id.tv_toSendSMS);
		number.setText(Constants.number);
		
		message = (EditText) rootView.findViewById(R.id.et_message);

		return rootView;
	}

	public static void sendMsg(Context context) throws Exception 
	{
		if (message.getText().toString().equals(""))
		{
			Toast.makeText(context, "Can not send empty message", Toast.LENGTH_LONG).show();
			
		}
		else
		{
			String msg = message.getText().toString();
			if (MainActivity.isDebugging)
			{
				byte [] b= (Constants.startEnc + "\n").getBytes();
				MainActivity.mCommandService.write(b);
			}
			
			msg = MainActivity.encryptionManager.encrypt(msg);
			
			if (MainActivity.isDebugging)
			{
				byte [] b= (Constants.endEnc + "\n").getBytes();
				MainActivity.mCommandService.write(b);
			}
			
			if (MainActivity.isDebugging)
			{
				byte [] b= (Constants.sentMsg + "\n").getBytes();
				MainActivity.mCommandService.write(b);
			}
			MainActivity.sendLongSMS(msg, Constants.number) ;     
		}
		
	}
	
	

}
