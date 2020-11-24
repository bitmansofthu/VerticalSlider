package com.hmomeni.verticalslider.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hmomeni.verticalslider.VerticalSlider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verticalSlider.max = 100
        verticalSlider.progress = 10
        verticalSlider.cornerRadius = resources.displayMetrics.density * 30

        verticalSlider.setIconHighResource(R.drawable.ic_volume_high_grey600_36dp)
        verticalSlider.setIconMediumResource(R.drawable.ic_volume_medium_grey600_36dp)
        verticalSlider.setIconLowResource(R.drawable.ic_volume_low_grey600_36dp)

        verticalSlider.onProgressChangeListener = object : VerticalSlider.OnSliderProgressChangeListener {
            override fun onChanged(verticalSlider: VerticalSlider, progress: Int, fromUser: Boolean) {
                progressText.text = "%.0f%%".format(Locale.ENGLISH, progress.toFloat() / verticalSlider.max.toFloat() * 100)
            }

            override fun onPressed(slider: VerticalSlider) {
                progressText.text = "Pressed %.0f%%".format(Locale.ENGLISH, verticalSlider.progress.toFloat() / verticalSlider.max.toFloat() * 100)
            }

            override fun onReleased(slider: VerticalSlider) {
                progressText.text = "Released %.0f%%".format(Locale.ENGLISH, verticalSlider.progress.toFloat() / verticalSlider.max.toFloat() * 100)
            }
        }
    }
}
