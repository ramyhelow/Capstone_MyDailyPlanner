package com.ramyhelow.captone_mydailyplanner.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<String> myFragmentTitleList;
    private List<Fragment> myFragmentList;

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
        myFragmentList = new ArrayList<>();
        myFragmentTitleList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return myFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return myFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return myFragmentTitleList.get(position);
    }

    public Fragment getCurrentFragment(int position){
        return myFragmentList.get(position);
    }

    public void addNewFragment(Fragment fragment, String title) {
        myFragmentList.add(fragment);
        myFragmentTitleList.add(title);
    }


}