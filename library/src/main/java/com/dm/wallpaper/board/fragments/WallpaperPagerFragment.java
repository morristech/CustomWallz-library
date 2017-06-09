package com.dm.wallpaper.board.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.wallpaper.board.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallpaperPagerFragment extends Fragment {

    ViewPager viewPager;
    String pagerPosition;
    public WallpaperPagerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_wallpaper_pager, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

        pagerPosition = "android:switcher:" + R.id.viewPager + ":1";
        viewPager.setAdapter(new MyAdapter(getFragmentManager()));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                pagerPosition = "android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem();
            }
        });


        return v;
    }

        public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            return new WallpapersFragment();
        }
    }

    public String getCurrentPagerFragmentTag() {
        return pagerPosition;
    }
}
