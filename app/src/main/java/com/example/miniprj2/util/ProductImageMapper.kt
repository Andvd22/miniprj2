package com.example.miniprj2.util


import com.example.miniprj2.R
import java.text.Normalizer
import java.util.Locale

object ProductImageMapper {

    fun imageRes(name: String): Int {
        val normalized = normalize(name)
        return when {
            "xoai" in normalized -> R.drawable.fruit_mango
            "chuoi" in normalized -> R.drawable.fruit_banana
            "nho" in normalized -> R.drawable.fruit_grapes
            "le" in normalized -> R.drawable.fruit_pear
            "cam" in normalized || "nuoc ep" in normalized -> R.drawable.fruit_orange_juice
            "salad" in normalized -> R.drawable.fruit_salad
            "dua luoi" in normalized -> R.drawable.fruit_melon
            "man" in normalized -> R.drawable.fruit_plum
            else -> R.drawable.ic_launcher_foreground
        }
    }

    private fun normalize(text: String): String {
        return Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("đ", "d")
    }
}