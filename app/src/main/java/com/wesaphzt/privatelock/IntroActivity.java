package com.wesaphzt.privatelock;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import static com.wesaphzt.privatelock.service.LockService.mInitialized;

import com.github.appintro.*;
import com.github.appintro.model.*;
import com.google.android.material.color.DynamicColors;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //slider pages
        SliderPage sliderPageOne = new SliderPage();
        sliderPageOne.setTitle(getResources().getString(R.string.slider_page_one_title));
        sliderPageOne.setDescription(getString(R.string.slider_page_one_desc));
        sliderPageOne.setImageDrawable(R.drawable.ic_intro_lock);
        //sliderPageOne.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(sliderPageOne));

        SliderPage sliderPageTwo = new SliderPage();
        sliderPageTwo.setTitle(getResources().getString(R.string.slider_page_two_title));
        sliderPageTwo.setDescription(getResources().getString(R.string.slider_page_two_desc));
        sliderPageTwo.setImageDrawable(R.drawable.ic_intro_iris);
        //sliderPageTwo.setBgColor(getResources().getColor(R.color.colorIntroGrey));
        addSlide(AppIntroFragment.newInstance(sliderPageTwo));

        SliderPage sliderPageThree = new SliderPage();
        sliderPageThree.setTitle(getResources().getString(R.string.slider_page_three_title));
        sliderPageThree.setDescription(getResources().getString(R.string.slider_page_three_desc));
        sliderPageThree.setImageDrawable(R.drawable.ic_intro_shield);
        //sliderPageThree.setBgColor(getResources().getColor(R.color.colorIntroGreen));
        addSlide(AppIntroFragment.newInstance(sliderPageThree));

        //options
        //setTransformer();
        setSkipButtonEnabled(false);
        setProgressBarVisibility(true);

        //setBarColor(getResources().getColor(R.color.colorPrimary));
        //setSeparatorColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        mInitialized = false;
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}