package com.example.miniprj2.util


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.miniprj2.databinding.ItemCartItemBinding
import com.example.miniprj2.databinding.ItemCategoryCardBinding
import com.example.miniprj2.databinding.ItemInvoiceItemBinding
import com.example.miniprj2.databinding.ItemProductCardBinding
import com.example.miniprj2.model.CartItem
import com.example.miniprj2.model.Category
import com.example.miniprj2.model.Product

object FruitViewFactory {

    fun createProductCard(
        inflater: LayoutInflater,
        parent: ViewGroup,
        product: Product,
        onOpenDetail: (Product) -> Unit
    ): View {
        val binding = ItemProductCardBinding.inflate(inflater, parent, false)
        binding.tvBadge.text = AppFormatters.toBadge(product.name)
        binding.tvName.text = product.name
        binding.tvCategory.text = product.categoryName
        binding.tvStock.text = "Tồn kho: ${product.stock} ${product.unit}"
        binding.tvPrice.text = AppFormatters.formatCurrency(product.price)
        binding.tvTodayBadge.isVisible = product.isToday
        binding.ivProduct.setImageResource(ProductImageMapper.imageRes(product.name))
        ViewStyler.styleBadge(binding.tvBadge, product.accentColor)
        binding.root.setOnClickListener { onOpenDetail(product) }
        binding.btnViewDetail.setOnClickListener { onOpenDetail(product) }
        return binding.root
    }

    fun createCategoryCard(
        inflater: LayoutInflater,
        parent: ViewGroup,
        category: Category,
        onOpenCategory: (Category) -> Unit
    ): View {
        val binding = ItemCategoryCardBinding.inflate(inflater, parent, false)
        binding.tvBadge.text = AppFormatters.toBadge(category.name)
        binding.tvName.text = category.name
        binding.tvDescription.text = category.description
        binding.tvCount.text = "${category.productCount} san pham"
        ViewStyler.styleBadge(binding.tvBadge, category.accentColor)
        binding.root.setOnClickListener { onOpenCategory(category) }
        binding.btnOpen.setOnClickListener { onOpenCategory(category) }
        return binding.root
    }

    fun createCartItem(
        inflater: LayoutInflater,
        parent: ViewGroup,
        item: CartItem,
        onDecrease: (CartItem) -> Unit,
        onIncrease: (CartItem) -> Unit
    ): View {
        val binding = ItemCartItemBinding.inflate(inflater, parent, false)
        binding.tvBadge.text = AppFormatters.toBadge(item.name)
        binding.tvName.text = item.name
        binding.tvCategory.text = item.categoryName
        binding.tvUnitPrice.text = AppFormatters.formatUnitPrice(item.unitPrice, item.unit)
        binding.tvQuantity.text = item.quantity.toString()
        binding.tvLineTotal.text = AppFormatters.formatCurrency(item.lineTotal)
        ViewStyler.styleBadge(binding.tvBadge, item.accentColor)
        binding.btnMinus.setOnClickListener { onDecrease(item) }
        binding.btnPlus.setOnClickListener { onIncrease(item) }
        return binding.root
    }

    fun createInvoiceItem(
        inflater: LayoutInflater,
        parent: ViewGroup,
        item: CartItem
    ): View {
        val binding = ItemInvoiceItemBinding.inflate(inflater, parent, false)
        binding.tvBadge.text = AppFormatters.toBadge(item.name)
        binding.tvName.text = item.name
        binding.tvQuantity.text = "${item.quantity} x ${AppFormatters.formatUnitPrice(item.unitPrice, item.unit)}"
        binding.tvLineTotal.text = AppFormatters.formatCurrency(item.lineTotal)
        ViewStyler.styleBadge(binding.tvBadge, item.accentColor)
        return binding.root
    }
}