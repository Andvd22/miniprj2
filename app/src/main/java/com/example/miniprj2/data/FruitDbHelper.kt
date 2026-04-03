package com.example.miniprj2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.miniprj2.model.CartItem
import com.example.miniprj2.model.Category
import com.example.miniprj2.model.InvoiceData
import com.example.miniprj2.model.OrderSummary
import com.example.miniprj2.model.Product
import com.example.miniprj2.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FruitDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                accent_color TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                unit TEXT NOT NULL,
                stock INTEGER NOT NULL,
                description TEXT NOT NULL,
                accent_color TEXT NOT NULL,
                is_today INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(category_id) REFERENCES categories(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT NOT NULL,
                total_amount REAL NOT NULL DEFAULT 0,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE order_details (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                unit_price REAL NOT NULL,
                line_total REAL NOT NULL,
                UNIQUE(order_id, product_id),
                FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
            """.trimIndent()
        )
        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS order_details")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun authenticateUser(username: String, password: String): User? {
        val normalizedUsername = username.trim()
        val normalizedPassword = password.trim()
        return readableDatabase.rawQuery(
            "SELECT id, username, password, full_name FROM users WHERE username = ? AND password = ? LIMIT 1",
            arrayOf(normalizedUsername, normalizedPassword)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                User(
                    id = cursor.getLong(0),
                    username = cursor.getString(1),
                    password = cursor.getString(2),
                    fullName = cursor.getString(3)
                )
            } else {
                null
            }
        }
    }

    fun getUserById(userId: Long): User? {
        return readableDatabase.rawQuery(
            "SELECT id, username, password, full_name FROM users WHERE id = ? LIMIT 1",
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                User(
                    id = cursor.getLong(0),
                    username = cursor.getString(1),
                    password = cursor.getString(2),
                    fullName = cursor.getString(3)
                )
            } else {
                null
            }
        }
    }

    fun getCategories(): List<Category> {
        return readableDatabase.rawQuery(
            """
            SELECT c.id, c.name, c.description, c.accent_color, COUNT(p.id) AS product_count
            FROM categories c
            LEFT JOIN products p ON p.category_id = c.id
            GROUP BY c.id, c.name, c.description, c.accent_color
            ORDER BY c.name
            """.trimIndent(),
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Category(
                            id = cursor.getLong(0),
                            name = cursor.getString(1),
                            description = cursor.getString(2),
                            accentColor = cursor.getString(3),
                            productCount = cursor.getInt(4)
                        )
                    )
                }
            }
        }
    }

    fun getTodayProducts(): List<Product> = getProducts(onlyToday = true)

    fun getProducts(categoryId: Long? = null, onlyToday: Boolean = false): List<Product> {
        val args = mutableListOf<String>()
        val query = StringBuilder(
            """
            SELECT p.id, p.category_id, c.name, p.name, p.price, p.unit, p.stock,
                   p.description, p.accent_color, p.is_today
            FROM products p
            INNER JOIN categories c ON c.id = p.category_id
            WHERE 1 = 1
            """.trimIndent()
        )
        if (categoryId != null) {
            query.append(" AND p.category_id = ?")
            args.add(categoryId.toString())
        }
        if (onlyToday) {
            query.append(" AND p.is_today = 1")
        }
        query.append(" ORDER BY p.name")
        return readableDatabase.rawQuery(query.toString(), args.toTypedArray()).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(mapProduct(cursor))
                }
            }
        }
    }

    fun getProductById(productId: Long): Product? {
        return readableDatabase.rawQuery(
            """
            SELECT p.id, p.category_id, c.name, p.name, p.price, p.unit, p.stock,
                   p.description, p.accent_color, p.is_today
            FROM products p
            INNER JOIN categories c ON c.id = p.category_id
            WHERE p.id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(productId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) mapProduct(cursor) else null
        }
    }

    fun getCurrentOrderSummary(userId: Long): OrderSummary? {
        return readableDatabase.rawQuery(
            """
            SELECT o.id, o.status, o.created_at, o.total_amount,
                   COALESCE((SELECT SUM(quantity) FROM order_details WHERE order_id = o.id), 0)
            FROM orders o
            WHERE o.user_id = ? AND o.status = 'OPEN'
            ORDER BY o.id DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                OrderSummary(
                    orderId = cursor.getLong(0),
                    status = cursor.getString(1),
                    createdAt = cursor.getString(2),
                    totalAmount = cursor.getDouble(3),
                    itemCount = cursor.getInt(4)
                )
            } else {
                null
            }
        }
    }

    fun getCartItems(userId: Long): List<CartItem> {
        return readableDatabase.rawQuery(
            """
            SELECT p.id, p.name, c.name, p.accent_color, od.quantity, od.unit_price, od.line_total, p.unit
            FROM order_details od
            INNER JOIN orders o ON o.id = od.order_id
            INNER JOIN products p ON p.id = od.product_id
            INNER JOIN categories c ON c.id = p.category_id
            WHERE o.user_id = ? AND o.status = 'OPEN'
            ORDER BY od.id DESC
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CartItem(
                            productId = cursor.getLong(0),
                            name = cursor.getString(1),
                            categoryName = cursor.getString(2),
                            accentColor = cursor.getString(3),
                            quantity = cursor.getInt(4),
                            unitPrice = cursor.getDouble(5),
                            lineTotal = cursor.getDouble(6),
                            unit = cursor.getString(7)
                        )
                    )
                }
            }
        }
    }

    fun addProductToOpenOrder(userId: Long, productId: Long, quantity: Int = 1): Long? {
        if (quantity <= 0) return null
        val product = getProductById(productId) ?: return null
        val orderId = getOrCreateOpenOrder(userId)
        val db = writableDatabase
        db.beginTransaction()
        return try {
            db.rawQuery(
                "SELECT id, quantity FROM order_details WHERE order_id = ? AND product_id = ? LIMIT 1",
                arrayOf(orderId.toString(), productId.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val detailId = cursor.getLong(0)
                    val newQuantity = cursor.getInt(1) + quantity
                    val values = ContentValues().apply {
                        put("quantity", newQuantity)
                        put("line_total", newQuantity * product.price)
                    }
                    db.update("order_details", values, "id = ?", arrayOf(detailId.toString()))
                } else {
                    val values = ContentValues().apply {
                        put("order_id", orderId)
                        put("product_id", productId)
                        put("quantity", quantity)
                        put("unit_price", product.price)
                        put("line_total", quantity * product.price)
                    }
                    db.insert("order_details", null, values)
                }
            }
            updateOrderTotal(db, orderId)
            db.setTransactionSuccessful()
            orderId
        } finally {
            db.endTransaction()
        }
    }

    fun changeItemQuantity(userId: Long, productId: Long, delta: Int): Boolean {
        val summary = getCurrentOrderSummary(userId) ?: return false
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val handled = db.rawQuery(
                """
                SELECT od.id, od.quantity, od.unit_price
                FROM order_details od
                WHERE od.order_id = ? AND od.product_id = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(summary.orderId.toString(), productId.toString())
            ).use { cursor ->
                if (!cursor.moveToFirst()) {
                    false
                } else {
                    val detailId = cursor.getLong(0)
                    val newQuantity = cursor.getInt(1) + delta
                    val unitPrice = cursor.getDouble(2)
                    if (newQuantity <= 0) {
                        db.delete("order_details", "id = ?", arrayOf(detailId.toString()))
                    } else {
                        val values = ContentValues().apply {
                            put("quantity", newQuantity)
                            put("line_total", newQuantity * unitPrice)
                        }
                        db.update("order_details", values, "id = ?", arrayOf(detailId.toString()))
                    }
                    true
                }
            }
            if (handled) {
                updateOrderTotal(db, summary.orderId)
                db.setTransactionSuccessful()
            }
            handled
        } finally {
            db.endTransaction()
        }
    }

    fun checkoutOpenOrder(userId: Long): Long? {
        val summary = getCurrentOrderSummary(userId) ?: return null
        if (summary.itemCount == 0) return null
        val values = ContentValues().apply {
            put("status", "PAID")
        }
        writableDatabase.update("orders", values, "id = ?", arrayOf(summary.orderId.toString()))
        return summary.orderId
    }

    fun getInvoice(orderId: Long): InvoiceData? {
        val invoice = readableDatabase.rawQuery(
            """
            SELECT o.id, u.full_name, o.created_at, o.status, o.total_amount
            FROM orders o
            INNER JOIN users u ON u.id = o.user_id
            WHERE o.id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(orderId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                InvoiceData(
                    orderId = cursor.getLong(0),
                    customerName = cursor.getString(1),
                    createdAt = cursor.getString(2),
                    status = cursor.getString(3),
                    totalAmount = cursor.getDouble(4),
                    items = emptyList()
                )
            } else {
                null
            }
        } ?: return null

        val items = readableDatabase.rawQuery(
            """
            SELECT p.id, p.name, c.name, p.accent_color, od.quantity, od.unit_price, od.line_total, p.unit
            FROM order_details od
            INNER JOIN products p ON p.id = od.product_id
            INNER JOIN categories c ON c.id = p.category_id
            WHERE od.order_id = ?
            ORDER BY od.id DESC
            """.trimIndent(),
            arrayOf(orderId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CartItem(
                            productId = cursor.getLong(0),
                            name = cursor.getString(1),
                            categoryName = cursor.getString(2),
                            accentColor = cursor.getString(3),
                            quantity = cursor.getInt(4),
                            unitPrice = cursor.getDouble(5),
                            lineTotal = cursor.getDouble(6),
                            unit = cursor.getString(7)
                        )
                    )
                }
            }
        }
        return invoice.copy(items = items)
    }

    private fun getOrCreateOpenOrder(userId: Long): Long {
        val existingOrderId = readableDatabase.rawQuery(
            """
            SELECT id
            FROM orders
            WHERE user_id = ? AND status = 'OPEN'
            ORDER BY id DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
        if (existingOrderId != null) {
            return existingOrderId
        }

        val values = ContentValues().apply {
            put("user_id", userId)
            put("status", "OPEN")
            put("created_at", nowLabel())
            put("total_amount", 0.0)
        }
        return writableDatabase.insert("orders", null, values)
    }

    private fun updateOrderTotal(db: SQLiteDatabase, orderId: Long) {
        val total = db.rawQuery(
            "SELECT COALESCE(SUM(line_total), 0) FROM order_details WHERE order_id = ?",
            arrayOf(orderId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        }
        val values = ContentValues().apply { put("total_amount", total) }
        db.update("orders", values, "id = ?", arrayOf(orderId.toString()))
    }

    private fun mapProduct(cursor: android.database.Cursor): Product {
        return Product(
            id = cursor.getLong(0),
            categoryId = cursor.getLong(1),
            categoryName = cursor.getString(2),
            name = cursor.getString(3),
            price = cursor.getDouble(4),
            unit = cursor.getString(5),
            stock = cursor.getInt(6),
            description = cursor.getString(7),
            accentColor = cursor.getString(8),
            isToday = cursor.getInt(9) == 1
        )
    }

    private fun seedData(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("INSERT INTO users (username, password, full_name) VALUES ('an', '123', 'Đỗ Văn An')")
            db.execSQL("INSERT INTO users (username, password, full_name) VALUES ('seller', '123456', 'Trần Minh Seller')")

            db.execSQL("INSERT INTO categories (name, description, accent_color) VALUES ('Trái cây nhiệt đới', 'Nhóm trái cây tươi, dễ bán quanh năm.', '#F97316')")
            db.execSQL("INSERT INTO categories (name, description, accent_color) VALUES ('Hoa quả cao cấp', 'Sản phẩm đẹp mã, phù hợp đơn quà.', '#22C55E')")
            db.execSQL("INSERT INTO categories (name, description, accent_color) VALUES ('Nước ép và món trộn', 'Gợi ý nhóm hàng bán nhanh theo set.', '#0EA5E9')")
            db.execSQL("INSERT INTO categories (name, description, accent_color) VALUES ('Đặc sản theo mùa', 'Hàng nổi bật để đẩy doanh số trong ngày.', '#E11D48')")

            db.execSQL(
                """
                INSERT INTO products (category_id, name, price, unit, stock, description, accent_color, is_today) VALUES
                (1, 'Xoài Cát Chu', 45000, 'kg', 24, 'Xoài ngọt đậm, màu đẹp, phù hợp bán lẻ và lên combo.', '#F59E0B', 1),
                (1, 'Chuối Cau', 28000, 'nải', 18, 'Chuối mềm ngọt, dễ bán nhanh, phù hợp khách mua hằng ngày.', '#EAB308', 1),
                (2, 'Nho Mỹ', 115000, 'hộp', 12, 'Nho chất lượng cao, đóng hộp đẹp, phù hợp quà tặng.', '#8B5CF6', 1),
                (2, 'Lê Hàn Quốc', 98000, 'hộp', 10, 'Lê giòn ngọt, hình thức đẹp, dễ lên gian trưng bày.', '#10B981', 1),
                (3, 'Combo Nước Ép Cam', 52000, 'ly', 15, 'Combo nước ép đóng sẵn, dễ bán trong giờ cao điểm.', '#F97316', 1),
                (3, 'Set Salad Trái Cây', 68000, 'hộp', 14, 'Set mix sẵn để phục vụ văn phòng và khách mua nhanh.', '#06B6D4', 0),
                (4, 'Dưa Lưới Mini', 75000, 'quả', 9, 'Dưa lưới ngọt thanh, dễ trưng bày gian theo mùa.', '#EF4444', 1),
                (4, 'Mận Đỏ Đà Lạt', 56000, 'hộp', 20, 'Mận đỏ tươi, màu sắc nổi bật, hút mắt trên kệ hàng.', '#DB2777', 0)
                """.trimIndent()
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun nowLabel(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))
        return formatter.format(Date())
    }

    companion object {
        private const val DATABASE_NAME = "fruit_app.db"
        private const val DATABASE_VERSION = 2

        @Volatile
        private var instance: FruitDbHelper? = null

        fun getInstance(context: Context): FruitDbHelper {
            return instance ?: synchronized(this) {
                instance ?: FruitDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}