package com.dm.wallpaper.board.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.wallpaper.board.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallpaperPagerFragment extends Fragment {

    ViewPager viewPager;
    String pagerTag;

    public WallpaperPagerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_wallpaper_pager, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

        pagerPosition = "android:switcher:" + R.id.viewPager + ":1";
        viewPager.setAdapter(new MyAdapter(getFragmentManager()));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                pagerTag = "android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem();
            }
        });

        return v;
    }

    public class WallpaperPagerAdaptor extends FragmentPagerAdapter {

        String[] pagerTitles;

        WallpaperPagerAdaptor(FragmentManager fm) {
            super(fm);
            pagerTitles = getContext().getResources().getStringArray(R.array.wallpaper_fragment_pager_tab_titles);
        }

        @Override
        public int getCount() {
            return pagerTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            return new WallpapersFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pagerTitles[position];
        }
    }

    public String getCurrentPagerFragmentTag() {
        return pagerTag;
    }
}
