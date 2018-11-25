package me.thanel.readtracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import me.thanel.readtracker.ui.updateprogress.UpdateProgressFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, UpdateProgressFragment.newInstance())
                .commit()
        }
    }
}
