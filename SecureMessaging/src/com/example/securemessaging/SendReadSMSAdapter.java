package com.example.securemessaging;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SendReadSMSAdapter extends FragmentPagerAdapter {
 
    public SendReadSMSAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
            return new SendSMSFragment();
        case 1:
            // Games fragment activity
            return new ReadSMSFragment();
        
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
 
}