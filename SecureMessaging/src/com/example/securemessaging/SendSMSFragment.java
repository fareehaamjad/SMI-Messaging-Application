package com.example.securemessaging;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SendSMSFragment extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{

		View rootView = inflater.inflate(R.layout.send_sms_fragment, container, false);

		return rootView;
	}

}
