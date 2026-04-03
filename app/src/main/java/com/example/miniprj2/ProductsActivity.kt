package com.example.miniprj2


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.miniprj2.data.FruitDbHelper
import com.example.miniprj2.data.SessionManager
import com.example.miniprj2.databinding.ActivityProductsBinding
import com.example.miniprj2.util.FruitViewFactory

class ProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductsBinding
    private val db by lazy { FruitDbHelper.getInstance(this) }
    private val session by lazy { SessionManager(this) }

    private val categoryId by lazy {
        intent.getLongExtra(EXTRA_CATEGORY_ID, -1L).takeIf { it != -1L }
    }
    private val categoryName by lazy {
        intent.getStringExtra(EXTRA_CATEGORY_NAME)
    }
    private val onlyToday by lazy {
        intent.getBooleanExtra(EXTRA_ONLY_TODAY, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.productsRoot, binding.toolbar, showBack = true)
        binding.btnOpenCart.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }
        binding.btnOpenLogin.setOnClickListener { handleAccountAction() }
    }

    override fun onResume() {
        super.onResume()
        renderHeader()
        renderProducts()
    }

    private fun renderHeader() {
        binding.tvProductsTitle.text = when {
            categoryName != null -> categoryName
            onlyToday -> "Sản phẩm hôm nay"
            else -> "Tất cả sản phẩm"
        }
        binding.tvProductsSubtitle.text = when {
            categoryName != null -> "Danh sách sản phẩm trong danh mục $categoryName."
            onlyToday -> "Danh sách những món đang được ưu tiên bán trong ngày."
            else -> "Toàn bộ sản phẩm hiện có trong cửa hàng."
        }
        binding.tvFilterTag.text = when {
            categoryName != null -> "Lọc theo danh mục"
            onlyToday -> "Bán trong ngày"
            else -> "Tất cả sản phẩm"
        }
        binding.btnOpenLogin.text = if (session.isLoggedIn()) "Đăng xuất" else "Đăng nhập"
    }

    private fun renderProducts() {
        val products = db.getProducts(categoryId = categoryId, onlyToday = onlyToday)
        binding.productsContainer.removeAllViews()
        binding.tvEmptyProducts.isVisible = products.isEmpty()
        products.forEach { product ->
            binding.productsContainer.addView(
                FruitViewFactory.createProductCard(layoutInflater, binding.productsContainer, product) {
                    startActivity(ProductDetailActivity.newIntent(this, it.id))
                }
            )
        }
    }

    private fun handleAccountAction() {
        if (session.isLoggedIn()) {
            session.logout()
            showToast("Đã đăng xuất")
            renderHeader()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    companion object {
        private const val EXTRA_CATEGORY_ID = "extra_category_id"
        private const val EXTRA_CATEGORY_NAME = "extra_category_name"
        private const val EXTRA_ONLY_TODAY = "extra_only_today"

        fun newIntent(
            context: Context,
            categoryId: Long? = null,
            categoryName: String? = null,
            onlyToday: Boolean = false
        ): Intent {
            return Intent(context, ProductsActivity::class.java).apply {
                putExtra(EXTRA_ONLY_TODAY, onlyToday)
                if (categoryId != null) putExtra(EXTRA_CATEGORY_ID, categoryId)
                if (categoryName != null) putExtra(EXTRA_CATEGORY_NAME, categoryName)
            }
        }
    }
}