package com.dmitrypokrasov.timeline

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TimelineSamplePagerAdapter(
    activity: FragmentActivity,
    private val samples: List<TimelineSample>
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = samples.size

    override fun createFragment(position: Int): Fragment {
        return TimelineSampleFragment.newInstance(samples[position])
    }
}
