package com.dm.wallpaper.board.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.utils.Extras;

public class WallpaperPagerFragment extends Fragment {

    private ViewPager viewPager;
    private WallpaperPagerAdaptor adapter;

    public WallpaperPagerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_wallpaper_pager, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

        adapter = new WallpaperPagerAdaptor(getChildFragmentManager());
        viewPager.setAdapter(adapter);

        return v;
    }

    public Fragment getCurrentPagerFragment() {
        return adapter.getFragment(viewPager.getCurrentItem());
    }

    private class WallpaperPagerAdaptor extends FragmentPagerAdapter {

        SparseArray<Fragment> fragments = new SparseArray<>();
        String [] pagerTitles;

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

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragments.remove(position);
            super.destroyItem(container, position, object);
        }

        private Fragment getFragment(int positon) {
            return fragments.get(positon);
        }
    }
}
