package com.example.miniprj2


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.miniprj2.data.FruitDbHelper
import com.example.miniprj2.data.SessionManager
import com.example.miniprj2.databinding.ActivityProductDetailBinding
import com.example.miniprj2.model.Product
import com.example.miniprj2.util.AppFormatters
import com.example.miniprj2.util.ProductImageMapper
import com.example.miniprj2.util.ViewStyler

class ProductDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val db by lazy { FruitDbHelper.getInstance(this) }
    private val session by lazy { SessionManager(this) }
    private val productId by lazy {
        intent.getLongExtra(EXTRA_PRODUCT_ID, -1L)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.detailRoot, binding.toolbar, showBack = true)
        binding.btnSeeProducts.setOnClickListener {
            startActivity(ProductsActivity.newIntent(this))
        }
        binding.btnViewCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.btnAddToCart.setOnClickListener { addToCart() }
    }

    override fun onResume() {
        super.onResume()
        val product = db.getProductById(productId)
        if (product == null) {
            showToast("Không tìm thấy sản phẩm")
            finish()
            return
        }
        renderProduct(product)
    }

    private fun renderProduct(product: Product) {
        binding.tvBadge.text = AppFormatters.toBadge(product.name)
        binding.tvCategory.text = product.categoryName
        binding.tvName.text = product.name
        binding.tvPrice.text = AppFormatters.formatCurrency(product.price)
        binding.tvStock.text = "Tồn kho: ${product.stock} ${product.unit}"
        binding.tvDescription.text = product.description
        binding.ivProduct.setImageResource(ProductImageMapper.imageRes(product.name))
        binding.tvLoginHint.isVisible = !session.isLoggedIn()
        binding.tvLoginHint.text = if (session.isLoggedIn()) {
            "Bạn đã sẵn sàng tạo đơn hàng. Nhấn Thêm vào giỏ hàng để tiếp tục."
        } else {
            "Đăng nhập để tạo đơn hàng và thêm sản phẩm vào hóa đơn."
        }
        binding.btnAddToCart.text = if (session.isLoggedIn()) "Thêm vào giỏ hàng" else "Đăng nhập để mua"
        ViewStyler.styleBadge(binding.tvBadge, product.accentColor)
    }

    private fun addToCart() {
        val product = db.getProductById(productId) ?: return
        val userId = session.getUserId()
        if (userId == null) {
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    putExtra(LoginActivity.EXTRA_PRODUCT_ID, product.id)
                }
            )
            return
        }
        db.addProductToOpenOrder(userId, product.id)
        showToast("Đã thêm ${product.name} vào giỏ hàng")
    }

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"

        fun newIntent(context: Context, productId: Long): Intent {
            return Intent(context, ProductDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
            }
        }
    }
}