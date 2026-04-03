package com.example.miniprj2


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar

open class BaseActivity : AppCompatActivity() {

    protected fun configureScreen(
        root: View,
        toolbar: MaterialToolbar? = null,
        title: String? = null,
        showBack: Boolean = false
    ) {
        enableEdgeToEdge()
        applyInsets(root)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = title ?: toolbar.title
            supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
            supportActionBar?.setDisplayShowHomeEnabled(showBack)
        }
    }

    private fun applyInsets(root: View) {
        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialLeft + bars.left,
                initialTop + bars.top,
                initialRight + bars.right,
                initialBottom + bars.bottom
            )
            insets
        }
    }

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}