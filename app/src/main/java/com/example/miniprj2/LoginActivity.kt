package com.example.miniprj2

import android.os.Bundle
import com.example.miniprj2.data.FruitDbHelper
import com.example.miniprj2.data.SessionManager

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db by lazy { FruitDbHelper.Companion.getInstance(this) }
    private val session by lazy { SessionManager(this) }
    private val pendingProductId by lazy {
        intent.getLongExtra(EXTRA_PRODUCT_ID, -1L).takeIf { it != -1L }
    }
    private val openCartAfterLogin by lazy {
        intent.getBooleanExtra(EXTRA_OPEN_CART, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.loginRoot, binding.toolbar, showBack = true)
        bindState()
        binding.btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun bindState() {
        if (session.isLoggedIn()) {
            binding.tvLoginSubtitle.text =
                "Bạn đang đăng nhập với ${session.getFullName()}. Có thể đăng nhập lại bằng tài khoản khác nếu cần."
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        if (username.isBlank() || password.isBlank()) {
            showToast("Nhập đầy đủ tài khoản và mật khẩu")
            return
        }

        val user = db.authenticateUser(username, password)
        if (user == null) {
            showToast("Tài khoản hoặc mật khẩu không đúng")
            return
        }

        session.login(user)
        when {
            pendingProductId != null -> {
                db.addProductToOpenOrder(user.id, pendingProductId!!)
                showToast("Đăng nhập thành công, đã thêm sản phẩm vào giỏ hàng")
                startActivity(Intent(this, CartActivity::class.java))
                finish()
            }

            openCartAfterLogin -> {
                showToast("Đăng nhập thành công")
                startActivity(Intent(this, CartActivity::class.java))
                finish()
            }

            else -> {
                showToast("Đăng nhập thành công")
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
        const val EXTRA_OPEN_CART = "extra_open_cart"
    }
}