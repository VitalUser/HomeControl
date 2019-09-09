package com.vital.homecontrol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends FragmentPagerAdapter {
    private static final String TAG = "MyclassRoomAdapter";
    private List<String> names;
    private long baseId = 0;


    public RoomAdapter(FragmentManager fm, List<String> pageNames) {
        super(fm);
//        roomCount = pageNames.size();
        names = pageNames;
//        Log.i(TAG, " RoomAdapter: roomCount =" + roomCount);
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position + 1);
    }


    @Override
    public int getCount() {
        return names.size();
    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        /*
        String ss =  ((Fragment)object).getTag();
        int position = Integer.parseInt(ss.substring(ss.lastIndexOf(":")+1));
        if (position==posToDelete){
            posToDelete=-1;
            return  POSITION_NONE;
        }else{
            return super.getItemPosition(object);
        }
        */
        return  POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return names.get(position);
    }


    public void addRoom(String roomName) {
        names.add(roomName);
//        roomCount++;
        notifyDataSetChanged();
    }

    public void delPage(int index){
        names.remove(index);
//        roomCount--;
        notifyDataSetChanged();
    }

    public void setPageName(String name, int index){
        names.set(index, name);
    }

}
