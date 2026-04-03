package com.example.miniprj2


import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.miniprj2.data.FruitDbHelper
import com.example.miniprj2.databinding.ActivityInvoiceBinding
import com.example.miniprj2.util.AppFormatters
import com.example.miniprj2.util.FruitViewFactory

class InvoiceActivity : BaseActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private val db by lazy { FruitDbHelper.getInstance(this) }
    private val orderId by lazy {
        intent.getLongExtra(EXTRA_ORDER_ID, -1L)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureScreen(binding.invoiceRoot, binding.toolbar, showBack = true)
        binding.btnBackHome.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
            )
            finish()
        }
        renderInvoice()
    }

    private fun renderInvoice() {
        val invoice = db.getInvoice(orderId)
        if (invoice == null) {
            showToast("Không tìm thấy hóa đơn")
            finish()
            return
        }

        binding.tvInvoiceCode.text = AppFormatters.formatInvoiceCode(invoice.orderId)
        binding.tvPaymentStatus.text = "Trạng thái: ${if (invoice.status == "PAID") "Đã thanh toán" else invoice.status}"
        binding.tvInvoiceTotal.text = AppFormatters.formatCurrency(invoice.totalAmount)
        binding.tvCustomerName.text = "Khách hàng: ${invoice.customerName}"
        binding.tvCreatedAt.text = "Thời gian: ${invoice.createdAt}"

        binding.invoiceItemsContainer.removeAllViews()
        invoice.items.forEach { item ->
            binding.invoiceItemsContainer.addView(
                FruitViewFactory.createInvoiceItem(layoutInflater, binding.invoiceItemsContainer, item)
            )
        }
    }

    companion object {
        private const val EXTRA_ORDER_ID = "extra_order_id"

        fun newIntent(context: Context, orderId: Long): Intent {
            return Intent(context, InvoiceActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }
}