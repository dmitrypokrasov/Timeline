package com.dmitrypokrasov.timeline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val samples = TimelineSample.entries.toList()
        val pager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.timeline_pager)
        val tabs = findViewById<com.google.android.material.tabs.TabLayout>(R.id.timeline_tabs)

        pager.adapter = TimelineSamplePagerAdapter(this, samples)

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.setText(samples[position].titleRes)
        }.attach()
    }
}
