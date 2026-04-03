package com.example.miniprj2


import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.miniprj2.data.FruitDbHelper
import com.example.miniprj2.databinding.ActivityCategoriesBinding
import com.example.miniprj2.util.FruitViewFactory

class CategoriesActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private val db by lazy { FruitDbHelper.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.categoriesRoot, binding.toolbar, showBack = true)
        binding.btnSeeAllProducts.setOnClickListener {
            startActivity(ProductsActivity.newIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()
        renderCategories()
    }

    private fun renderCategories() {
        val categories = db.getCategories()
        binding.categoriesContainer.removeAllViews()
        binding.tvEmptyCategories.isVisible = categories.isEmpty()
        categories.forEach { category ->
            binding.categoriesContainer.addView(
                FruitViewFactory.createCategoryCard(layoutInflater, binding.categoriesContainer, category) {
                    startActivity(
                        ProductsActivity.newIntent(
                            context = this,
                            categoryId = it.id,
                            categoryName = it.name
                        )
                    )
                }
            )
        }
    }
}