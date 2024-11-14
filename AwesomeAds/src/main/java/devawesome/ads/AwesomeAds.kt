package devawesome.ads

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner



import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import devawesome.ads.databinding.LoadingAdBinding
import devawesome.ads.databinding.ShimmerBinding

abstract class AwesomeAds:Application(),ActivityLifecycleCallbacks,LifecycleEventObserver {

    private val nativeAdsMap=HashMap<String,NativePreloader>()

    lateinit var act:Activity
   private lateinit var appOpenAd: AdmobAppOpen

   private lateinit var interID:String
   private lateinit var nativeID:String
   private lateinit var bannerID:String
   private lateinit var appOpenID:String

    private lateinit var bannerCollapseID:String
    private lateinit var bannerMrecID:String


   private var activityCallBackCustom:ActivityCallBackCustom?=null


    override fun onCreate() {
        super.onCreate()
        nativeBtnBg=resources.getDrawable(R.drawable.round_btn)
        appOpenAd= AdmobAppOpen(this)
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        MobileAds.initialize(this)

    }


    fun setCustomCallback(callBackCustom: ActivityCallBackCustom){
        this.activityCallBackCustom=callBackCustom
    }


    fun setInterID(id:String){
        interID=id
    }
    fun setNativeID(id:String){
        nativeID=id
    }
    fun setBannerID(id:String){
        bannerID=id
        }

    fun setAppOpenID(id:String){
        appOpenID=id
    }

    fun setBannerCollapseID(id:String){
        bannerCollapseID=id
    }


    fun setBannerMrecID(id:String) {
        bannerMrecID = id

    }
    fun  setNativeBtnBg(bg:Drawable){
        nativeBtnBg=bg
    }

    fun setNativeBtnTextColor(color:Int){
        nativeBtnTextColor=color

    }

    fun loadAppOpen(awesomeListener: AwesomeListener?=null){
        appOpenAd.preloadAd(awesomeListener)
    }
    fun showAppOpen(runnable: Runnable?){
        appOpenAd.showAd(runnable)
    }

    fun showLoadNative(act: Activity,frameLayout: FrameLayout,showMedia: Boolean,style: NativeStyle=NativeStyle.BOTTOM_BTN_STYLE,listener: AwesomeListener?=null){

        // Checking if there is any Add in the map
        for(item in nativeAdsMap){
            val nativeAd=item.value
            if(nativeAd.isLoaded()){
                bindNative(act,frameLayout,nativeAd.getAd()!!,showMedia)
                nativeAdsMap.remove(item.key)

                return
            }else if(nativeAd.isLoading){
                nativeAd.setListener {
                    if (it) {
                        bindNative(act, frameLayout, nativeAd.getAd()!!, showMedia)
                        nativeAdsMap.remove(item.key)
                    }

                }

                return
            }
            else{
                nativeAdsMap.remove(item.key)
            }

        }


        val native= AdmobNative(act)
        val shimmer=ShimmerBinding.inflate(act.layoutInflater).root
        frameLayout.removeAllViews()
        frameLayout.addView(shimmer)
        native.loadAndShowNative(frameLayout,nativeID,showMedia,listener)

    }


    /**  @uniqueKey  is used to find native ad added in the hashmap*/
    fun preloadNative(id2: String,uniqueKey:String,loadListener: AwesomeListener?=null){

        val context=applicationContext

        val native= nativeAdsMap[uniqueKey]
        if(native!=null){

            native.setListener(loadListener)
            return
        }


        val id=if(BuildConfig.DEBUG){
            "ca-app-pub-3940256099942544/2247696110"
        }else{
            id2
        }

        val nativeloader=NativePreloader(id)
        nativeloader.setListener(loadListener)
        nativeloader.load(context)

        nativeAdsMap[uniqueKey]=nativeloader
    }



   private class NativePreloader(val id: String){
       var isLoading=false

       private  var awesomeListener: AwesomeListener?=null
        private var nativeAd:NativeAd?=null

        fun load(context: Context){
            isLoading=true
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val nativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            val adRequest = AdRequest.Builder().build()

            val listener=object :AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)

                    awesomeListener?.onComplete(false)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
//                val native2= hashMap[uniqueKey]
//                if(native2!=null){
//                    native2.destroy()
//                    hashMap.remove(uniqueKey)
//                }

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()

                    awesomeListener?.onComplete(true)


                }
            }
            val adLoader= AdLoader.Builder(context,id).forNativeAd {
                nativeAd=it
            }.withNativeAdOptions(nativeAdOptions)
                .withAdListener(listener)

                .build()


            adLoader.loadAd(adRequest)
        }

        fun setListener(listener: AwesomeListener?){
            this.awesomeListener=listener
        }

        fun isLoaded():Boolean{
            return nativeAd!=null
        }
       fun getAd():NativeAd?{
           return nativeAd
       }


    }




    /**  @uniqueKey  is used to find native ad added in the hashmap*/

    fun showPreloadedNative(ctx:Context,uniqueKey: String,frameLayout: FrameLayout,showMedia: Boolean){
        val nativeAd= nativeAdsMap[uniqueKey]

        if(nativeAd!=null){
            if(nativeAd.isLoaded()){
                bindNative(ctx,frameLayout,nativeAd.getAd()!!,showMedia)
                nativeAdsMap.remove(uniqueKey)
            }else if(nativeAd.isLoading){
                nativeAd.setListener(AwesomeListener {
                    if(it){
                        bindNative(ctx,frameLayout,nativeAd.getAd()!!,showMedia)
                        nativeAdsMap.remove(uniqueKey)
                    }

                })
            }
            else{
                nativeAdsMap.remove(uniqueKey)
            }

        }
    }

/*
  using this approach to bind preloaded native ad with proper lifecycle to destroy it
 when Activity is finished*/

    private fun bindNative(ctx: Context,frameLayout: FrameLayout,nativeAd: NativeAd,showMedia: Boolean){
        frameLayout.visibility=View.VISIBLE
        frameLayout.removeAllViews()
        frameLayout.addView(inflateNativeView(nativeAd, frameLayout.context,showMedia))
        val admobNative=AdmobNative(ctx,nativeAd)
        admobNative.inflate(frameLayout,showMedia)

    }


    fun hasPreloadedNative(uniqueKey: String):Boolean {
        val nativeAd = nativeAdsMap[uniqueKey]

        return nativeAd!=null
    }

    fun loadBanner(act: Context,frameLayout: FrameLayout,collapsable:Boolean=false,mrec: Boolean=false){
        val banner= AdmobBanner(act)


        val id=when {
            mrec -> {
                bannerMrecID
            }

            collapsable -> {
                bannerCollapseID

            }
            else ->{
                bannerID
            }
        }
        banner.loadBanner(frameLayout,id,collapsable, mrec)

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if(event== Lifecycle.Event.ON_START){

            activityCallBackCustom?.onAppOpen()
        }

        if(event== Lifecycle.Event.ON_DESTROY){

            for(ad in nativeAdsMap.values){
                ad.getAd()?.destroy()
            }
            nativeAdsMap.clear()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        if(activity !is AdActivity){
            act=activity
            activityCallBackCustom?.onCreated(activity)
        }

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if(activity !is AdActivity){
            act=activity
        }

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }





   private inner class AdmobAppOpen(app:Application){


        private val application=app
       private var isShowing=false



        private  var isloading=false
        private   var openAd: AppOpenAd?=null
        fun preloadAd(awesomeListener: AwesomeListener?=null){
            if(isloading)return





            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/9257395921"
            }else{
                appOpenID
            }

            isloading=true
            val request=AdRequest.Builder().build()

            AppOpenAd.load(application,id,request,object : AppOpenAd.AppOpenAdLoadCallback(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    isloading=false
                    awesomeListener?.onComplete(false)
                }

                override fun onAdLoaded(p0: AppOpenAd) {
                    super.onAdLoaded(p0)
                    openAd=p0

                    awesomeListener?.onComplete(true)

                }
            })
        }

        fun showAd(runnable: Runnable?=null){

            if(isShowing)return


            if(openAd!=null&&::act.isInitialized){
                isShowing=true
                openAd?.fullScreenContentCallback=object : FullScreenContentCallback(){

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        isloading=false
                        isShowing=false
                        openAd=null
                        preloadAd()
                        runnable?.run()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        isloading=false
                        openAd=null
                        isShowing=false
                        runnable?.run()
                        preloadAd()
                    }
                }
                openAd?.show(act)
            }else{
                runnable?.run()
            }
        }


    }








   private class AdmobBanner(act: Context) : DefaultLifecycleObserver {
        private val activity=act

       init {
           if(activity is AppCompatActivity){
               activity.lifecycle.addObserver(this)
           }

        }

        lateinit var adView: AdView
        var isLoading=false
        fun loadBanner(frameLayout: FrameLayout,id2: String,collapsable:Boolean,mrec:Boolean=false){

            if(isLoading)return


            frameLayout.visibility= View.VISIBLE

            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/9214589741"
            }else{
                id2
            }





            val banner= AdView(activity)
            if(mrec){

                banner.setAdSize(AdSize.MEDIUM_RECTANGLE)
            }else{
                val metrics= activity.resources.displayMetrics

                val densityAdjustment = if (metrics.density > 1) (1.0 / metrics.density) else 1.0
                val width = (metrics.widthPixels * densityAdjustment).toInt()
                val adSize= AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, width)
                banner.setAdSize(adSize)
            }



            banner.adUnitId=id

            banner.adListener=object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if(mrec){
                        frameLayout.setBackgroundColor(Color.TRANSPARENT)
                    }
                    frameLayout.removeAllViews()
                    frameLayout.visibility= View.VISIBLE
                    frameLayout.addView(banner)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)


                    Log.e("ADS", "onAdFailedToLoad: "+p0.message )
                }
            }


            val request:AdRequest = if(collapsable){
                val extras = Bundle()
                extras.putString("collapsible", "bottom")
                AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()

            }else{
                AdRequest.Builder().build()
            }


            banner.loadAd(request)
            isLoading=true


        }


        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            if(::adView.isInitialized){
                adView.pause()
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if(::adView.isInitialized){
                adView.resume()
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            if(::adView.isInitialized){
                adView.destroy()
            }


        }
    }




    companion object{
        private lateinit var nativeBtnBg:Drawable
        private var nativeBtnTextColor=Color.WHITE
        private val TAG="AWESOMEADS"

        private fun inflateNativeView(
            nativeAd: NativeAd,context: Context,
            showMedia:Boolean
        ): NativeAdView {


            val layout=R.layout.native_view
            val nativeAdView = LayoutInflater.from(context).inflate(layout,null,false) as NativeAdView



            nativeAdView.iconView = nativeAdView.findViewById(R.id.ad_app_icon)
            nativeAdView.headlineView = nativeAdView.findViewById(R.id.ad_advertiser)
            nativeAdView.bodyView=nativeAdView.findViewById(R.id.tv_body)
            nativeAdView.starRatingView=nativeAdView.findViewById(R.id.rating_bar)


            nativeAdView.callToActionView =nativeAdView.findViewById(R.id.ad_call_to_action)

            (nativeAdView.headlineView as TextView?)?.text=nativeAd.headline






            if (nativeAd.callToAction == null) {
               nativeAdView.callToActionView?.visibility = View.INVISIBLE
            } else {
                nativeAdView.callToActionView?.visibility = View.VISIBLE

                (nativeAdView.callToActionView as Button?)?.text=nativeAd.callToAction
            }

            if (nativeAd.body == null) {
                nativeAdView.bodyView?.visibility = View.INVISIBLE

                nativeAdView.bodyView?.isSelected=true
            } else {
                (nativeAdView.bodyView as TextView?)?.text=nativeAd.body

            }
            if (nativeAd.icon == null) {
                nativeAdView.iconView?.visibility = View.INVISIBLE
            } else {
                nativeAdView.iconView?.visibility = View.VISIBLE

                (nativeAdView.iconView as ImageView?)?.setImageDrawable(nativeAd.icon?.drawable)

            }
            if (nativeAd.starRating != null) {
                (nativeAdView.starRatingView as RatingBar?)?.rating=nativeAd.starRating!!.toFloat()

            }
            if(showMedia) {

                nativeAdView.mediaView = nativeAdView.findViewById(R.id.mediaview)
                nativeAdView.mediaView?.visibility=View.VISIBLE

                nativeAd.mediaContent?.let {
                    nativeAdView.mediaView?.mediaContent = it
                }
            }else {
                nativeAdView.findViewById<View>(R.id.mediaview).visibility=View.GONE
            }
            nativeAdView.setNativeAd(nativeAd)

            if(showMedia) {
                val mediaContent = nativeAd.mediaContent
                val vc = mediaContent?.videoController

                if (vc != null && mediaContent.hasVideoContent()) {
                    vc.videoLifecycleCallbacks =
                        object : VideoController.VideoLifecycleCallbacks() {
                        }
                }
            }
            nativeAdView.bodyView?.isSelected=true


            nativeAdView.callToActionView?.background= nativeBtnBg
            (nativeAdView.callToActionView as Button?)?.setTextColor(nativeBtnTextColor)

            return nativeAdView


        }
    }






   private class AdmobNative(c:Context,ad: NativeAd?=null):DefaultLifecycleObserver {
        val context=c
        var isLoading=false
        var nativeAd: NativeAd?=ad

        init {
            if(context is AppCompatActivity){
                context.lifecycle.addObserver(this)
            }

        }

       fun loadAndShowNative(
            frameLayout: FrameLayout,
            id2: String,
            showMedia: Boolean,
            loadListener: AwesomeListener?
        ){

            if(nativeAd!=null){
                frameLayout.visibility=View.VISIBLE
                frameLayout.removeAllViews()
                frameLayout.addView(inflateNativeView(nativeAd!!, context,showMedia))
                loadListener?.onComplete(true)
                return
            }


            if(isLoading)return

            val id=if(BuildConfig.DEBUG){
                "ca-app-pub-3940256099942544/2247696110"
            }else{
              id2
            }


            frameLayout.visibility=View.VISIBLE
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val nativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            val adRequest = AdRequest.Builder().build()


            val listener=object :AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)

                    isLoading=false

                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    isLoading=false

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    nativeAd?.apply {

                    }
                    if(nativeAd!=null){
                        frameLayout.visibility=View.VISIBLE
                        frameLayout.removeAllViews()
                        frameLayout.addView(
                            inflateNativeView(nativeAd!!,
                            context,showMedia)
                        )

                    }else{
                        frameLayout.visibility=View.GONE
                    }


                    loadListener?.onComplete(true)


                }
            }
            val adLoader= AdLoader.Builder(context,id).forNativeAd {

                if(nativeAd!=null){
                    nativeAd?.destroy()
                }
                nativeAd=it



            }.withNativeAdOptions(nativeAdOptions)
                .withAdListener(listener)

                .build()


            adLoader.loadAd(adRequest)
            isLoading=true




        }

       fun inflate(frameLayout: FrameLayout,showMedia: Boolean){
           if(nativeAd!=null){
               frameLayout.visibility=View.VISIBLE
               frameLayout.removeAllViews()
               frameLayout.addView(inflateNativeView(nativeAd!!, context,showMedia))
           }
       }

        override fun onDestroy(owner: LifecycleOwner) {
            isLoading=false
            nativeAd?.destroy()

            Log.e(TAG, "onDestroy NATIVE: ")
            super.onDestroy(owner)

        }



    }

    private var interstitialAd: InterstitialAd?=null
    private var isloading=false
    fun preloadInter(listener: AwesomeListener?=null){
        if(isloading|| interstitialAd!=null) {
            listener?.onComplete(interstitialAd!=null)
            return
        }

        val id=if(BuildConfig.DEBUG){
            "ca-app-pub-3940256099942544/1033173712"
        }else{
          interID
        }

        val request=AdRequest.Builder().setHttpTimeoutMillis(15*1000).build()

        InterstitialAd.load(applicationContext,id,request,object :
            InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                isloading=false

                listener?.onComplete(false)
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                interstitialAd=p0
                listener?.onComplete(true)
            }
        })

    }



    fun isInterAvailable():Boolean{
        return interstitialAd!=null
    }
    fun showInterIfAvail( onDismiss:Runnable, showLoading:Boolean=true){

        val params= WindowManager.LayoutParams()
        params.format= PixelFormat.TRANSPARENT
        val view= LoadingAdBinding.inflate(LayoutInflater.from(this))

        if(showLoading){
            act.windowManager.addView(view.root,params)
        }

        if( interstitialAd!=null){


            if(showLoading){
                view.root.postDelayed({

                    act.windowManager.removeView(view.root)
                    showInter(act,onDismiss)
                },1500)
            }else{
                showInter(act,onDismiss)
            }


        }else{

            if(showLoading){
                act.windowManager.removeView(view.root)
            }
            onDismiss.run()

        }
    }

    fun showRunTimeInter(onDismiss:Runnable, showLoading:Boolean=true){
        val params= WindowManager.LayoutParams()

        params.format= PixelFormat.TRANSPARENT
        val view= LoadingAdBinding.inflate(LayoutInflater.from(act))


        if(showLoading){
            act.windowManager.addView(view.root,params)
        }

        if( interstitialAd!=null){

            if(showLoading){
                view.root.postDelayed({
                    act.windowManager.removeView(view.root)
                    showInter(act,onDismiss)
                },1500)
            }else{
                showInter(act,onDismiss)
            }

        }else{


            preloadInter {
                if (showLoading) {
                    act.windowManager.removeView(view.root)
                }

                showInter(act, onDismiss)
            }


        }
    }

    private fun showInter(act: Activity,onDismiss: Runnable,reload:Boolean=true){

        if(interstitialAd!=null){
            interstitialAd?.let {

                it.fullScreenContentCallback=object :FullScreenContentCallback(){
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        interstitialAd=null
                        isloading=false


                        if(reload){
                            preloadInter()
                        }
                        onDismiss.run()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        interstitialAd=null
                        isloading=false
                        onDismiss.run()

                    }
                }

                it.show(act)

            }
        }else{

            onDismiss.run()
        }

    }


    fun interface AwesomeListener{
       fun onComplete(isLoaded:Boolean)
    }

    interface  ActivityCallBackCustom{
        fun onCreated(activity: Activity)
        fun onAppOpen()
    }

    enum class NativeStyle{
        TOP_BTN_STYLE,BOTTOM_BTN_STYLE
    }


}