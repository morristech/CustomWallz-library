package com.dm.wallpaper.board.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.utils.Extras;

public class WallpaperPagerFragment extends Fragment {

    private ViewPager viewPager;
    private WallpaperPagerAdaptor adapter;
    private String[] pagerTitles;

    public WallpaperPagerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_wallpaper_pager, container, false);
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab_layout);
        FrameLayout toolbarBlankLayout = (FrameLayout) v.findViewById(R.id.toolbar_blank_layout);
        pagerTitles = getResources().getStringArray(R.array.wallpaper_fragment_pager_tab_titles);
        FragmentManager fragmentManager = getChildFragmentManager();
        adapter = new WallpaperPagerAdaptor(fragmentManager);

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);


        ViewGroup.LayoutParams params = toolbarBlankLayout.getLayoutParams();
        params.height = params.height + WindowHelper.getStatusBarHeight(getContext());


        tabLayout.setTabGravity(TabLayout.MODE_FIXED);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = fragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + position);
                if (fragment instanceof FavoritesFragment)
                    ((FavoritesFragment) fragment).getFavouriteWallpapers();
                if (fragment instanceof PlaylistsHolderFragment)
                    ((PlaylistsHolderFragment) fragment).startPlaylists();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    public Fragment getCurrentPagerFragment() {
        return adapter.getFragment(viewPager.getCurrentItem());
    }

    private class WallpaperPagerAdaptor extends FragmentPagerAdapter {

        SparseArray<Fragment> fragments = new SparseArray<>();

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
            Bundle bundle = new Bundle();
            Fragment fragment = null;
            switch (position) {
                case 0:
                    bundle.putString(Extras.EXCLUDE_FILTER_TAGS, getResources().getString(R.string.wallpaper_filter_type_tag));
                    fragment = new WallpapersFragment();
                    fragment.setArguments(bundle);
                    break;
                case 1:
                    bundle.putString(Extras.INCLUDE_FILTER_TAGS, getResources().getString(R.string.wallpaper_filter_type_tag));
                    fragment = new WallpapersFragment();
                    fragment.setArguments(bundle);
                    break;
                case 2:
                    fragment = new FavoritesFragment();
                    break;

                case 3:
                    fragment = new PlaylistsHolderFragment();
            }

            return fragment;
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
