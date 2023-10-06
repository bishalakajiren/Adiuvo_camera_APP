package Adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.adiuvo.CaptureFragment;
import com.example.adiuvo.GalleryFragment;

public class TabAdapter extends FragmentPagerAdapter {

    private static final int NUM_TABS = 2;

    public TabAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new GalleryFragment();
            case 1:
                return new CaptureFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_TABS;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Gallery";
            case 1:
                return "Capture";
            default:
                return null;
        }
    }
}

