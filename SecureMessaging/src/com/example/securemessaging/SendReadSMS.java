package com.example.securemessaging;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.TabListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;

@SuppressLint("NewApi") 
public class SendReadSMS extends FragmentActivity implements ActionBar.TabListener, TabListener {

	private ViewPager viewPager;
	private SendReadSMSAdapter mAdapter;
	private android.app.ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "Send", "Read" };

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_read_sms);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager_page);
		actionBar = getActionBar();
		mAdapter = new SendReadSMSAdapter(getSupportFragmentManager());

		if (mAdapter == null)
		{
			Log.i("fragment", "adapter is null");
		}
		if (viewPager == null)
		{
			Log.i("fragment", "viewPager is null");
		}



		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener( this));
		}


		/*
		 * on swiping the viewpager make respective tab selected
		 **/
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(android.app.ActionBar.Tab tab,
			android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(android.app.ActionBar.Tab tab,
			android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(android.app.ActionBar.Tab tab,
			android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}