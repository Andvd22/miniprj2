package com.example.miniprj2

import android.os.Bundle
import androidx.core.view.isVisible
import com.example.miniprj2.util.AppFormatters
import com.example.miniprj2.util.FruitViewFactory

class CartActivity : BaseActivity() {

    private lateinit var binding: ActivityCartBinding
    private val db by lazy { FruitDbHelper.getInstance(this) }
    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.cartRoot, binding.toolbar, showBack = true)
        binding.btnBrowseProducts.setOnClickListener { handlePrimaryAction() }
        binding.btnContinueShopping.setOnClickListener {
            startActivity(ProductsActivity.newIntent(this))
        }
        binding.btnCheckout.setOnClickListener { checkout() }
    }

    override fun onResume() {
        super.onResume()
        renderCart()
    }

    private fun renderCart() {
        val userId = session.getUserId()
        if (userId == null) {
            renderLoggedOutState()
            return
        }

        val summary = db.getCurrentOrderSummary(userId)
        val items = db.getCartItems(userId)
        val isEmpty = items.isEmpty()

        binding.tvCartStatus.text = if (isEmpty) "Đơn hàng đang mở" else "Đơn hàng sẵn sàng"
        binding.tvCartSubtitle.text = if (isEmpty) {
            "Bạn đã đăng nhập, hãy thêm sản phẩm vào đơn hàng hiện tại."
        } else {
            "Theo dõi đơn hàng đang tạo và cập nhật số lượng sản phẩm."
        }
        binding.tvOrderCode.text = summary?.let { AppFormatters.formatOrderCode(it.orderId) } ?: "Chưa có đơn hàng"
        binding.tvOrderTotal.text = AppFormatters.formatCurrency(summary?.totalAmount ?: 0.0)

        binding.emptyStateCard.isVisible = isEmpty
        binding.cartItemsContainer.isVisible = !isEmpty
        binding.totalsCard.isVisible = !isEmpty

        if (isEmpty) {
            binding.tvEmptyCartHint.text = "Chọn sản phẩm trước, sau đó quay lại để thanh toán."
            binding.btnBrowseProducts.text = "Đi xem sản phẩm"
            return
        }

        binding.cartItemsContainer.removeAllViews()
        items.forEach { item ->
            binding.cartItemsContainer.addView(
                FruitViewFactory.createCartItem(
                    inflater = layoutInflater,
                    parent = binding.cartItemsContainer,
                    item = item,
                    onDecrease = {
                        db.changeItemQuantity(userId, it.productId, -1)
                        renderCart()
                    },
                    onIncrease = {
                        db.changeItemQuantity(userId, it.productId, 1)
                        renderCart()
                    }
                )
            )
        }

        val itemCount = items.sumOf { it.quantity }
        val total = items.sumOf { it.lineTotal }
        binding.tvItemsCount.text = itemCount.toString()
        binding.tvSubTotal.text = AppFormatters.formatCurrency(total)
        binding.tvGrandTotal.text = AppFormatters.formatCurrency(total)
    }

    private fun renderLoggedOutState() {
        binding.tvCartStatus.text = "Cần đăng nhập"
        binding.tvCartSubtitle.text = "Đăng nhập để tạo đơn hàng, xem giỏ hàng và thanh toán."
        binding.tvOrderCode.text = "Chưa có đơn hàng"
        binding.tvOrderTotal.text = AppFormatters.formatCurrency(0.0)
        binding.emptyStateCard.isVisible = true
        binding.cartItemsContainer.isVisible = false
        binding.totalsCard.isVisible = false
        binding.tvEmptyCartHint.text = "Đăng nhập trước khi tạo hóa đơn."
        binding.btnBrowseProducts.text = "Đăng nhập ngay"
    }

    private fun handlePrimaryAction() {
        if (session.isLoggedIn()) {
            startActivity(ProductsActivity.newIntent(this))
        } else {
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    putExtra(LoginActivity.EXTRA_OPEN_CART, true)
                }
            )
        }
    }

    private fun checkout() {
        val userId = session.getUserId()
        if (userId == null) {
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    putExtra(LoginActivity.EXTRA_OPEN_CART, true)
                }
            )
            return
        }
        val orderId = db.checkoutOpenOrder(userId)
        if (orderId == null) {
            showToast("Giỏ hàng chưa có sản phẩm để thanh toán")
            return
        }
        startActivity(InvoiceActivity.newIntent(this, orderId))
    }
}