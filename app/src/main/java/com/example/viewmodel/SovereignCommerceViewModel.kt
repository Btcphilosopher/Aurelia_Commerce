package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SovereignCommerceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SovereignCommerceDb.getDatabase(application)
    private val repository = SovereignCommerceRepository(db.commerceDao())

    // --- State variables ---
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("ALL")
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    private val _reputationFilter = MutableStateFlow(false)
    val reputationFilter: StateFlow<String> = _reputationFilter.map { if (it) "EXCLUSIVE" else "ALL" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ALL")

    // Trace user credit wallet balance
    private val _userCreditsBalance = MutableStateFlow(1250.00)
    val userCreditsBalance: StateFlow<Double> = _userCreditsBalance.asStateFlow()

    // --- Database observed flows ---
    val selfMerchant: StateFlow<MerchantEntity?> = repository.selfMerchant
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMerchants: StateFlow<List<MerchantEntity>> = repository.allMerchants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubscriptions: StateFlow<List<SubscriptionEntity>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogisticsNodes: StateFlow<List<LogisticsNodeEntity>> = repository.allLogisticsNodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newsItems: StateFlow<List<NewsItemEntity>> = repository.allNewsItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine filters with source products flow
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchText,
        _selectedTypeFilter,
        _reputationFilter,
        allMerchants
    ) { products, search, typeFilter, repOnly, merchants ->
        products.filter { p ->
            val matchSearch = p.title.contains(search, ignoreCase = true) ||
                    p.description.contains(search, ignoreCase = true) ||
                    p.merchantName.contains(search, ignoreCase = true)

            val matchType = typeFilter == "ALL" || p.type.uppercase() == typeFilter.uppercase()

            val merchantRep = merchants.find { m -> m.merchantId == p.merchantId }?.reputationScore ?: 0f
            val matchRep = !repOnly || merchantRep >= 4.8f

            matchSearch && matchType && matchRep
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // System Log feeds for terminal visual effect
    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    init {
        // Ensure starting database contains core datasets
        viewModelScope.launch {
            repository.seedMockDataIfEmpty(_userCreditsBalance.value)
            pushLog("System Initialization Complete. Sovereign protocols loaded.")
            pushLog("Identity keys checked. Public Ring containing ${allMerchants.value.size} nodes mapped.")
        }
    }

    fun pushLog(message: String) {
        val timeStamp = java.text.SimpleDateFormat("HH:mm:ss.S", java.util.Locale.US).format(java.util.Date())
        _terminalLogs.update { current ->
            (listOf("[$timeStamp] $message") + current).take(20)
        }
    }

    // --- Action Handlers ---

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    fun setTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun toggleReputationFilter() {
        _reputationFilter.update { !it }
    }

    fun addFaucetCredits() {
        viewModelScope.launch {
            val amount = 500.0
            _userCreditsBalance.update { it + amount }
            pushLog("Faucet trigger: Credited account $+amount.00 Sovereign credits")
        }
    }

    fun registerAsMerchant(name: String) {
        viewModelScope.launch {
            val id = "did:aurelia:merchant_self"
            val pub = "did:aurelia:pub_" + UUID.randomUUID().toString().take(12)
            val priv = "0x" + SovereignCommerceDb.generateSha256(id + pub)
            val newSelf = MerchantEntity(
                merchantId = id,
                name = name,
                status = "VERIFIED MERCHANT",
                reputationScore = 5.0f,
                publicKey = pub,
                privateKey = priv,
                balance = 100.0, // Starting merchant deposit
                isSelf = true
            )
            repository.registerMerchant(newSelf)
            pushLog("Identity Registered. Merkle proofs generated for storefront '$name'.")
        }
    }

    fun addProduct(
        title: String,
        description: String,
        price: Double,
        inventory: Int,
        type: String,
        shippingProfile: String,
        apiBillingModel: String = "NONE",
        apiEndpoint: String = ""
    ) {
        viewModelScope.launch {
            val self = selfMerchant.value ?: return@launch
            val prodId = "prod_" + UUID.randomUUID().toString().take(8)
            val productHash = SovereignCommerceDb.generateSha256(prodId + self.merchantId + price + title)

            val p = ProductEntity(
                productId = prodId,
                merchantId = self.merchantId,
                merchantName = self.name,
                title = title,
                description = description,
                price = price,
                inventory = inventory,
                type = type,
                rating = 5.0f,
                reviewsCount = 1,
                shippingProfile = if (type == "PHYSICAL") shippingProfile else "INSTANT DIGITAL PORTAL",
                hash = productHash,
                apiBillingModel = if (type == "API") apiBillingModel else "NONE",
                apiEndpoint = apiEndpoint
            )
            repository.insertProduct(p)
            pushLog("Listed product: '${title}' | Block Hash: ${productHash.take(12)}... successfully signed.")
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            pushLog("Delisted product: $productId. Offers revoked globally.")
        }
    }

    fun buyProduct(product: ProductEntity, deliveryLocationNode: String) {
        viewModelScope.launch {
            val buyerBalance = _userCreditsBalance.value
            if (buyerBalance < product.price) {
                pushLog("ERROR: Deficit of sovereign credits! Required: ${product.price}, Available: $buyerBalance")
                return@launch
            }

            // Deduct balance
            _userCreditsBalance.update { it - product.price }

            // Execute purchase
            val order = repository.executePreSignedPurchase(
                product = product,
                buyerName = "Aurelia Sovereign Buyer",
                settlementMethod = "Browser-Native Commercial Balance",
                deliveryNode = deliveryLocationNode
            )

            pushLog("Trade settled! SKU [${product.productId}]. Sig: ${order.crypticSignature.take(16)}... credited ${product.merchantName}")
            if (product.type == "PHYSICAL") {
                pushLog("Fulfillment: Ordered routing from Node '$deliveryLocationNode' | Waybill: ${order.trackingNumber}")
            } else if (product.type == "API" || product.type == "SERVICE") {
                pushLog("License generated. Instant active RPC channels unlocked.")
            }
        }
    }

    // Direct fulfillment flow simulation inside command dashboard
    fun cycleOrderStatus(order: OrderEntity) {
        viewModelScope.launch {
            val nextStatus = when (order.status) {
                "TRANSIT" -> "SHIPPED"
                "SHIPPED" -> "COMPLETED"
                else -> "COMPLETED"
            }
            repository.updateOrderStatus(order.orderId, nextStatus)
            pushLog("Logistics Track [${order.orderId}]: Status escalated to '$nextStatus'")
        }
    }

    // Call API Marketplace with Micropayment
    fun callApiRequest(sub: SubscriptionEntity) {
        viewModelScope.launch {
            val charge = sub.price // Billing micro amount
            val buyerBalance = _userCreditsBalance.value
            if (buyerBalance < charge) {
                pushLog("Micropayment error: Balance depleted to pay API route.")
                return@launch
            }

            // Deduct micropayment from buyer credit
            _userCreditsBalance.update { it - charge }

            // Credit merchant
            db.commerceDao().creditMerchantBalance(sub.merchantId, charge)

            // Increment usage
            repository.incrementApiRequests(sub.subscriptionId)

            val payloadHash = SovereignCommerceDb.generateSha256("api_call:${sub.subscriptionId}:${System.currentTimeMillis()}")
            pushLog("API RPC CALL SUCCESS: ${sub.productTitle} served. Micropayment of $charge credit resolved. Payload Verification Hash: ${payloadHash.take(10)}")
        }
    }

    fun addLogisticsNode(name: String, location: String, type: String) {
        viewModelScope.launch {
            val nodeId = "node_" + UUID.randomUUID().toString().take(8)
            val node = LogisticsNodeEntity(
                nodeId = nodeId,
                name = name,
                location = location,
                type = type,
                status = "ONLINE"
            )
            repository.addLogisticsNode(node)
            pushLog("Logistics infrastructure expanded: Node '$name' registered at '$location'.")
        }
    }

    fun getProductsByMerchant(merchantId: String): Flow<List<ProductEntity>> {
        return repository.getProductsByMerchant(merchantId)
    }

    fun createAnnouncement(title: String, content: String, isLive: Boolean) {
        viewModelScope.launch {
            val self = selfMerchant.value
            val merchantId = self?.merchantId ?: "did:aurelia:merchant_self"
            val merchantName = self?.name ?: "Sovereign Operator [Self]"
            val id = "news_" + UUID.randomUUID().toString().take(8)
            val news = NewsItemEntity(
                id = id,
                merchantId = merchantId,
                merchantName = merchantName,
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                isLiveStream = isLive,
                streamUrl = if (isLive) "rtmp://sovereign.aurelia/live/${id}" else ""
            )
            repository.insertNewsItem(news)
            pushLog("Broadcast dispatched: '$title' transmitted to global commerce stream.")
        }
    }
}
