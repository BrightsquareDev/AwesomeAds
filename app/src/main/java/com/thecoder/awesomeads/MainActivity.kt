package com.thecoder.awesomeads

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thecoder.awesomeads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app=application as App

        binding.loadBanner.setOnClickListener {

            app.loadBanner(this,binding.frameBanner)
        }
        binding.loadNativeNomedia.setOnClickListener {

            app.showLoadNative(this,binding.frameNativeNomedia,false)
        }

        binding.loadNativeNomedia.setOnClickListener {

            app.showLoadNative(this,binding.frameNative,true)
        }

        binding.loadInter.setOnClickListener {

            app.showRunTimeInter({})
        }
    }
}