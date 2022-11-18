package com.example.jiaqing_hu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.jiaqing_hu.Util.checkPermissions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    private lateinit var startFragment: StartFragment
    private lateinit var historyFragment:HistoryFragment
    private lateinit var settingFragment:SettingsFragment
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fragadapter:Adapter
    private lateinit var fragmentlist:ArrayList<Fragment>

    private val tabTitles = arrayOf("START","HISTORY","SETTINGS")
    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    /* Instantiate tabs and corresponding fragments,
    * Using idea from class sample (Adapter, Pageviewer) */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions(this)

        viewPager2 = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabs)
        startFragment = StartFragment()
        historyFragment = HistoryFragment()
        settingFragment = SettingsFragment()

        fragmentlist = ArrayList()
        fragmentlist.add(startFragment)
        fragmentlist.add(historyFragment)
        fragmentlist.add(settingFragment)

        fragadapter = Adapter(this,fragmentlist)
        viewPager2.adapter = fragadapter
        tabConfigurationStrategy =
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = tabTitles[position]
            }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}

