package com.thecoder.awesomeads

import android.app.Activity
import android.app.Application
import devawesome.ads.AwesomeAds


class App:AwesomeAds() , AwesomeAds.ActivityCallBackCustom{
    override fun onCreate() {
        super.onCreate()

        setBannerID(getString(R.string.banner_id))
        setBannerCollapseID(getString(R.string.banner_collapse_id))
        setInterID(getString(R.string.inter_id))
        setNativeID(getString(R.string.native_id))
        setAppOpenID(getString(R.string.app_open_id))


        setCustomCallback(this)
    }

    override fun onCreated(activity: Activity) {

        // you can store this activity
        // for showing or check activity instance
    }

    override fun onAppOpen() {

        //this callback will be called when app is resumed from background
        // you can use it to show app open and whitelist activities showing ad
    }
}