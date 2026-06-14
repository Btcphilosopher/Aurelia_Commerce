package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

// ==========================================
// ROOM ENTITIES (Sovereign Commerce Objects)
// ==========================================

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val merchantId: String,
    val name: String,
    val status: String, // VERIFIED, REPUTED, SUPREME
    val reputationScore: Float,
    val publicKey: String, // did:aurelia:pub_xxxx
    val privateKey: String, // concealed private key representation
    val balance: Double,
    val isSelf: Boolean = false // to distinguish current user as merchant
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: String,
    val merchantId: String,
    val merchantName: String,
    val title: String,
    val description: String,
    val price: Double,
    val inventory: Int,
    val type: String, // PHYSICAL, DIGITAL, SERVICE, API
    val rating: Float,
    val reviewsCount: Int,
    val shippingProfile: String,
    val hash: String, // Cryptographic offer verification hash
    val apiBillingModel: String = "NONE", // NONE, PER_REQUEST, SUBSCRIPTION, MICROPAYMENT
    val apiEndpoint: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val productId: String,
    val productTitle: String,
    val productType: String,
    val price: Double,
    val merchantId: String,
    val merchantName: String,
    val timestamp: Long,
    val status: String, // COMPLETED, SHIPPED, TRANSIT, REGISTERED
    val trackingNumber: String,
    val fulfillmentNode: String,
    val crypticSignature: String // SHA-256 trade settlement proof
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val subscriptionId: String,
    val productId: String,
    val productTitle: String,
    val merchantId: String,
    val merchantName: String,
    val price: Double,
    val billingModel: String, // MONTHLY, MICROPAYMENT
    val status: String, // ACTIVE, COMPROMISED, REVOKED
    val lastBillingTime: Long,
    val nextBillingTime: Long,
    val apiRequestsUsed: Int
)

@Entity(tableName = "logistics_nodes")
data class LogisticsNodeEntity(
    @PrimaryKey val nodeId: String,
    val name: String,
    val location: String,
    val type: String, // REGIONAL, SATELLITE, COMMUNITY, INDEPENDENT
    val status: String // ONLINE, DISPATCHING, OFFLINE
)

@Entity(tableName = "news_items")
data class NewsItemEntity(
    @PrimaryKey val id: String,
    val merchantId: String,
    val merchantName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isLiveStream: Boolean,
    val streamUrl: String = ""
)

// ==========================================
// DATA ACCESS OBJECT (DAO) DEFINITIONS
// ==========================================

@Dao
interface SovereignCommerceDao {
    
    // --- Merchant Queries ---
    @Query("SELECT * FROM merchants")
    fun getAllMerchants(): Flow<List<MerchantEntity>>

    @Query("SELECT * FROM merchants WHERE isSelf = 1 LIMIT 1")
    fun getSelfMerchant(): Flow<MerchantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: MerchantEntity)

    @Query("UPDATE merchants SET balance = balance + :amount WHERE merchantId = :merchantId")
    suspend fun creditMerchantBalance(merchantId: String, amount: Double)

    @Query("UPDATE merchants SET balance = balance - :amount WHERE merchantId = :merchantId")
    suspend fun debitMerchantBalance(merchantId: String, amount: Double)

    // --- Product Queries ---
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE merchantId = :merchantId")
    fun getProductsByMerchant(merchantId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)

    @Query("UPDATE products SET inventory = inventory - 1 WHERE productId = :productId AND inventory > 0")
    suspend fun decrementProductInventory(productId: String)

    // --- Order Queries ---
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE merchantId = :merchantId ORDER BY timestamp DESC")
    fun getOrdersByMerchant(merchantId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    // --- Subscription Queries ---
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: SubscriptionEntity)

    @Query("UPDATE subscriptions SET apiRequestsUsed = apiRequestsUsed + 1 WHERE subscriptionId = :subId")
    suspend fun incrementApiRequests(subId: String)

    // --- Logistics Queries ---
    @Query("SELECT * FROM logistics_nodes")
    fun getAllLogisticsNodes(): Flow<List<LogisticsNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogisticsNode(node: LogisticsNodeEntity)

    // --- News Feed Queries ---
    @Query("SELECT * FROM news_items ORDER BY timestamp DESC")
    fun getAllNewsItems(): Flow<List<NewsItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsItem(news: NewsItemEntity)
}

// ==========================================
// DATABASE CONTROLLER WITH SEED GENERATOR
// ==========================================

@Database(
    entities = [
        MerchantEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        SubscriptionEntity::class,
        LogisticsNodeEntity::class,
        NewsItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SovereignCommerceDb : RoomDatabase() {
    abstract fun commerceDao(): SovereignCommerceDao

    companion object {
        @Volatile
        private var INSTANCE: SovereignCommerceDb? = null

        fun getDatabase(context: Context): SovereignCommerceDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SovereignCommerceDb::class.java,
                    "sovereign_commerce.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Crypto Helpers to generate signature display values
        fun generateSha256(text: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(text.toByteArray())
                hash.joinToString("") { "%02x".format(it) }.take(32)
            } catch (e: Exception) {
                text.hashCode().toString()
            }
        }
    }
}

// ==========================================
// REPOSITORY LAYER (MVVM Architecture)
// ==========================================

class SovereignCommerceRepository(private val dao: SovereignCommerceDao) {

    val allMerchants: Flow<List<MerchantEntity>> = dao.getAllMerchants()
    val selfMerchant: Flow<MerchantEntity?> = dao.getSelfMerchant()
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = dao.getAllSubscriptions()
    val allLogisticsNodes: Flow<List<LogisticsNodeEntity>> = dao.getAllLogisticsNodes()
    val allNewsItems: Flow<List<NewsItemEntity>> = dao.getAllNewsItems()

    fun getProductsByMerchant(merchantId: String): Flow<List<ProductEntity>> = dao.getProductsByMerchant(merchantId)
    fun getOrdersByMerchant(merchantId: String): Flow<List<OrderEntity>> = dao.getOrdersByMerchant(merchantId)

    suspend fun registerMerchant(merchant: MerchantEntity) = dao.insertMerchant(merchant)
    suspend fun insertProduct(product: ProductEntity) = dao.insertProduct(product)
    suspend fun deleteProduct(productId: String) = dao.deleteProduct(productId)

    suspend fun executePreSignedPurchase(
        product: ProductEntity,
        buyerName: String,
        settlementMethod: String,
        deliveryNode: String
    ): OrderEntity {
        // Decrement stock if physical
        if (product.type == "PHYSICAL" && product.inventory > 0) {
            dao.decrementProductInventory(product.productId)
        }

        // Generate trade cryptographic attributes
        val orderId = "order_" + UUID.randomUUID().toString().take(8)
        val timestamp = System.currentTimeMillis()
        val payload = "${orderId}:${product.productId}:${product.price}:${timestamp}:${buyerName}"
        val signature = "sig_0x" + SovereignCommerceDb.generateSha256(payload)

        val trackingNum = if (product.type == "PHYSICAL") {
            "TRK-" + (100000..999999).random().toString()
        } else "N/A - INSTANT DOWNLOAD"

        val order = OrderEntity(
            orderId = orderId,
            productId = product.productId,
            productTitle = product.title,
            productType = product.type,
            price = product.price,
            merchantId = product.merchantId,
            merchantName = product.merchantName,
            timestamp = timestamp,
            status = if (product.type == "PHYSICAL") "TRANSIT" else "COMPLETED",
            trackingNumber = trackingNum,
            fulfillmentNode = deliveryNode,
            crypticSignature = signature
        )

        // Save order
        dao.insertOrder(order)

        // Credit Merchant
        dao.creditMerchantBalance(product.merchantId, product.price)

        // If it's an API subscription, create structural license record
        if (product.type == "API" || product.type == "SERVICE") {
            val subId = "sub_" + UUID.randomUUID().toString().take(8)
            val billingModelString = if (product.apiBillingModel != "NONE") product.apiBillingModel else "MONTHLY"
            val subscription = SubscriptionEntity(
                subscriptionId = subId,
                productId = product.productId,
                productTitle = product.title,
                merchantId = product.merchantId,
                merchantName = product.merchantName,
                price = product.price,
                billingModel = billingModelString,
                status = "ACTIVE",
                lastBillingTime = timestamp,
                nextBillingTime = timestamp + 30L * 24 * 60 * 60 * 1000, // 30 Days Out
                apiRequestsUsed = 0
            )
            dao.insertSubscription(subscription)
        }

        return order
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = dao.updateOrderStatus(orderId, status)
    suspend fun incrementApiRequests(subId: String) = dao.incrementApiRequests(subId)
    suspend fun addLogisticsNode(node: LogisticsNodeEntity) = dao.insertLogisticsNode(node)
    suspend fun insertNewsItem(news: NewsItemEntity) = dao.insertNewsItem(news)

    // Seed Initial Data to give users an immersive system loaded starting point
    suspend fun seedMockDataIfEmpty(currentSelfBalance: Double) {
        // We will do this inside safety transaction block
        // Seed self merchant if not exists
        val selfKeyPub = "did:aurelia:pub_k9x2m48sjlqn"
        val selfKeyPriv = "0x89b65fc8de4..."
        val selfMerch = MerchantEntity(
            merchantId = "did:aurelia:merchant_self",
            name = "Sovereign Operator [Self]",
            status = "SUPREME",
            reputationScore = 5.0f,
            publicKey = selfKeyPub,
            privateKey = selfKeyPriv,
            balance = currentSelfBalance,
            isSelf = true
        )
        dao.insertMerchant(selfMerch)

        // Seed other merchants representing the ecosystem network
        val seedMerchants = listOf(
            MerchantEntity("did:aurelia:merchant_novatech", "Novatech Instruments", "VERIFIED", 4.9f, "did:aurelia:pub_3kdf8s2kl", "Concealed", 12500.0),
            MerchantEntity("did:aurelia:merchant_cyberfeed", "Sovereign Media Stream", "REPUTED", 4.7f, "did:aurelia:pub_sd9f823ks", "Concealed", 8940.0),
            MerchantEntity("did:aurelia:merchant_synthapi", "Synthetix API Hub", "SOCIALLY_VERIFIED", 4.8f, "did:aurelia:pub_m9kdf72hs", "Concealed", 24300.0)
        )
        for (m in seedMerchants) {
            dao.insertMerchant(m)
        }

        // Seed products
        val seedProducts = listOf(
            // Physical
            ProductEntity(
                productId = "prod_tac_radio",
                merchantId = "did:aurelia:merchant_novatech",
                merchantName = "Novatech Instruments",
                title = "Tactical Mesh Radio Hub v3",
                description = "Encrypted offgrid messaging node designed for multi-frequency routing across sovereign channels. Built-in GPS.",
                price = 349.99,
                inventory = 14,
                type = "PHYSICAL",
                rating = 4.9f,
                reviewsCount = 42,
                shippingProfile = "Postal Air Corridor Tracked",
                hash = SovereignCommerceDb.generateSha256("prod_tac_radio_nova")
            ),
            ProductEntity(
                productId = "prod_field_sensor",
                merchantId = "did:aurelia:merchant_novatech",
                merchantName = "Novatech Instruments",
                title = "EMF Environmental Sensor",
                description = "High precision, military-grade radiation and EMF monitor. Transmits reports over sovereign LoRa networks.",
                price = 189.00,
                inventory = 8,
                type = "PHYSICAL",
                rating = 4.8f,
                reviewsCount = 19,
                shippingProfile = "Secure Courier Escort",
                hash = SovereignCommerceDb.generateSha256("prod_field_sensor_nova")
            ),
            // Digital
            ProductEntity(
                productId = "prod_sovereign_kernel",
                merchantId = "did:aurelia:merchant_synthapi",
                merchantName = "Synthetix API Hub",
                title = "Sovereign Kernel v1.0 [Source License Key]",
                description = "A portable, secure, micro-kernel operating system standard compiled for secure hardware architectures. 100% Rust.",
                price = 99.00,
                inventory = 999,
                type = "DIGITAL",
                rating = 5.0f,
                reviewsCount = 112,
                shippingProfile = "Instant Decentralized Sync",
                hash = SovereignCommerceDb.generateSha256("prod_sovereign_kernel_synth")
            ),
            ProductEntity(
                productId = "prod_crypto_handbook",
                merchantId = "did:aurelia:merchant_cyberfeed",
                merchantName = "Sovereign Media Stream",
                title = "Offgrid Commerce Cryptography Manifesto",
                description = "A core cryptographic guidebook dealing with zero-trust transactions, peer routing, and private communications.",
                price = 12.50,
                inventory = 500,
                type = "DIGITAL",
                rating = 4.7f,
                reviewsCount = 37,
                shippingProfile = "Direct Torrent Torrent Magnet Link",
                hash = SovereignCommerceDb.generateSha256("prod_crypto_handbook_cyber")
            ),
            // Services
            ProductEntity(
                productId = "prod_audit_service",
                merchantId = "did:aurelia:merchant_synthapi",
                merchantName = "Synthetix API Hub",
                title = "Smart Contract Cryptographic Audit",
                description = "A master review of your deployment's security by standard Rust engineers. Includes structural verification reports.",
                price = 1200.00,
                inventory = 5,
                type = "SERVICE",
                rating = 4.9f,
                reviewsCount = 9,
                shippingProfile = "Manual Handheld Delivery",
                hash = SovereignCommerceDb.generateSha256("prod_audit_service_synth")
            ),
            // API
            ProductEntity(
                productId = "prod_geo_api",
                merchantId = "did:aurelia:merchant_synthapi",
                merchantName = "Synthetix API Hub",
                title = "Aurelia Secure Geo-Location API Feed",
                description = "Decentralized coordinates cross-referencing feeds reporting live satellite telemetry. Ideal for logistics nodes.",
                price = 0.005, // micropayment!
                inventory = 99999,
                type = "API",
                rating = 4.8f,
                reviewsCount = 82,
                shippingProfile = "Programmable Token Call Header",
                hash = SovereignCommerceDb.generateSha256("prod_geo_api_synth"),
                apiBillingModel = "PER_REQUEST",
                apiEndpoint = "/api/v1/telemetry/geolocation"
            ),
            ProductEntity(
                productId = "prod_translation_api",
                merchantId = "did:aurelia:merchant_self", // User's own API!
                merchantName = "Sovereign Operator [Self]",
                title = "Omniscribe Text Signer & Verification API",
                description = "The prime hashing and zero-knowledge message text verification API. Allows secure cross-store proof signing.",
                price = 0.01, // micropayment
                inventory = 100000,
                type = "API",
                rating = 5.0f,
                reviewsCount = 3,
                shippingProfile = "Instant RPC Authorization Header",
                hash = SovereignCommerceDb.generateSha256("prod_translation_api_self"),
                apiBillingModel = "PER_REQUEST",
                apiEndpoint = "/api/v1/omniscribe/sign"
            )
        )
        for (p in seedProducts) {
            dao.insertProduct(p)
        }

        // Seed Logistics nodes
        val seedLogistics = listOf(
            LogisticsNodeEntity("node_london_hq", "London Royal Sovereign Wharf", "London, UK", "REGIONAL", "ONLINE"),
            LogisticsNodeEntity("node_ny_terminal", "New York Air Corridor Terminal 4", "New York, USA", "SATELLITE", "ONLINE"),
            LogisticsNodeEntity("node_singapore_dock", "Singapore Straits Maritime Depot", "Singapore", "REGIONAL", "DISPATCHING"),
            LogisticsNodeEntity("node_mesh_berlin", "Berlin Decentralized Mesh Depot", "Berlin, DE", "COMMUNITY", "ONLINE")
        )
        for (l in seedLogistics) {
            dao.insertLogisticsNode(l)
        }

        // Seed News items / Broadcast sessions
        val seedNews = listOf(
            NewsItemEntity(
                "news_1",
                "did:aurelia:merchant_novatech",
                "Novatech Instruments",
                "Novatech releases Mesh Radio firmware update",
                "We have finalized our mesh transport logic. Out-of-the-box packet optimization is boosted by 30%. Download the updated payload from your Vault.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2h ago
                isLiveStream = false
            ),
            NewsItemEntity(
                "news_2",
                "did:aurelia:merchant_cyberfeed",
                "Sovereign Media Stream",
                "LIVE BROADCAST: Hardware Security Hacks & Merchant Identity Setup",
                "Learn how to secure your storefront's private keys and configure air-gapped cold signatures for high-value orders.",
                timestamp = System.currentTimeMillis() + 1000 * 60 * 15, // Starts in 15 min!
                isLiveStream = true,
                streamUrl = "rtmp://sovereign.aurelia/live/merchant_identity_setup"
            ),
            NewsItemEntity(
                "news_3",
                "did:aurelia:merchant_synthapi",
                "Synthetix API Hub",
                "Micropayments settlement now fully active",
                "Merchants can begin configuring requests priced at under 0.01 Commerce Credits. Standardized billing routes have settled instantly.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // 1 day ago
                isLiveStream = false
            )
        )
        for (n in seedNews) {
            dao.insertNewsItem(n)
        }
    }
}
