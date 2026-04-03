package com.example.miniprj2


import android.content.Intent
import android.os.Bundle
import com.example.miniprj2.data.FruitDbHelper
import com.example.miniprj2.data.SessionManager
import com.example.miniprj2.databinding.ActivityMainBinding
import com.example.miniprj2.util.FruitViewFactory

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db by lazy { FruitDbHelper.getInstance(this) }
    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.mainRoot, showBack = false)
        setupClicks()
    }

    override fun onResume() {
        super.onResume()
        renderDashboard()
    }

    private fun setupClicks() {
        binding.btnLogin.setOnClickListener { handleAccountAction() }
        binding.cardLogin.setOnClickListener { handleAccountAction() }
        binding.btnOpenCart.setOnClickListener { openCart() }
        binding.cardCheckout.setOnClickListener { openCart() }
        binding.cardProducts.setOnClickListener { openProducts(onlyToday = true) }
        binding.cardCategories.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)) }
    }

    private fun renderDashboard() {
        val isLoggedIn = session.isLoggedIn()
        binding.tvGreeting.text = if (isLoggedIn) {
            "Xin chào, ${session.getFullName()}"
        } else {
            "Chào mừng bạn đến Fruit App"
        }
        binding.tvLoginState.text = if (isLoggedIn) {
            "Bạn đã đăng nhập với tài khoản ${session.getUsername()}. Có thể vào giỏ hàng để thanh toán ngay."
        } else {
            "Bạn chưa đăng nhập. Hãy đăng nhập để tạo hóa đơn và thanh toán."
        }
        binding.btnLogin.text = if (isLoggedIn) "Đăng xuất" else "Đăng nhập"
        binding.tvAccountHint.text = if (isLoggedIn) {
            "Đang làm việc với ${session.getFullName()}"
        } else {
            "Đăng nhập để tạo hóa đơn"
        }
        binding.tvTodaySubtitle.text = if (isLoggedIn) {
            "Nhanh tay lên đơn cho ${session.getFullName()} với những món đang bán tốt hôm nay."
        } else {
            "Bạn vẫn có thể xem sản phẩm và danh mục, nhưng cần đăng nhập để tạo đơn hàng."
        }

        val todayProducts = db.getTodayProducts()
        binding.tvTodayCount.text = "${todayProducts.size} món"
        binding.todayProductsContainer.removeAllViews()
        todayProducts.take(4).forEach { product ->
            binding.todayProductsContainer.addView(
                FruitViewFactory.createProductCard(layoutInflater, binding.todayProductsContainer, product) {
                    startActivity(ProductDetailActivity.newIntent(this, it.id))
                }
            )
        }
    }

    private fun handleAccountAction() {
        if (session.isLoggedIn()) {
            session.logout()
            showToast("Đã đăng xuất")
            renderDashboard()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun openCart() {
        startActivity(Intent(this, CartActivity::class.java))
    }

    private fun openProducts(onlyToday: Boolean) {
        startActivity(ProductsActivity.newIntent(this, onlyToday = onlyToday))
    }
}