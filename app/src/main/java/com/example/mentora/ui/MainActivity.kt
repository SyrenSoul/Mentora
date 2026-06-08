package com.example.mentora.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mentora.R

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0)

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val cardHuruf = findViewById<CardView>(R.id.cardHuruf)
        val cardAngka = findViewById<CardView>(R.id.cardAngka)
        val cardWarna = findViewById<CardView>(R.id.cardWarna)

        cardHuruf.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this, HurufActivity::class.java)
                showLoading()

                Handler(Looper.getMainLooper()).postDelayed({

                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    hideLoading()

                }, 350)
            }
        }

        cardAngka.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this, AngkaActivity::class.java)
                showLoading()
                Handler(Looper.getMainLooper()).postDelayed({

                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    hideLoading()

                }, 350)
            }
        }

        cardWarna.setOnClickListener {
            it.clickAnimation {
                val intent = Intent(this, WarnaActivity::class.java)
                showLoading()
                Handler(Looper.getMainLooper()).postDelayed({

                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    hideLoading()

                }, 350)
            }
        }
    }

    private fun showLoading() {
        findViewById<View>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<View>(R.id.loadingOverlay).visibility = View.GONE
    }

    fun View.clickAnimation(onEnd: () -> Unit = {}) {
        this.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(80)
            .withEndAction {
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .withEndAction { onEnd() }
            }
    }
}