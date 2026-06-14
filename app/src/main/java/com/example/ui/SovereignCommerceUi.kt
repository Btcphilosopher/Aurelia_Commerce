package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.SovereignCommerceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SovereignCommerceMainScreen(
    viewModel: SovereignCommerceViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }
    val userCredits by viewModel.userCreditsBalance.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val selfMerchant by viewModel.selfMerchant.collectAsStateWithLifecycle()

    var selectedProductToBuy by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
        topBar = {
            Column(
                modifier = Modifier
                    .background(DeepNavy)
                    .drawBehind {
                        drawLine(
                            color = Color.White.copy(alpha = 0.1f),
                            start = Offset(0f, size.height - 1f),
                            end = Offset(size.width, size.height - 1f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOVEREIGN NETWORK",
                            color = GrayText,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            style = LocalTextStyle.current.copy(letterSpacing = 2.sp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AURELIA ",
                                color = BrassAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(letterSpacing = 0.5.sp)
                            )
                            Text(
                                text = "COMMERCE",
                                color = WhiteTech.copy(alpha = 0.5f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                style = LocalTextStyle.current.copy(letterSpacing = 0.5.sp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // System Status Node
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ACTIVE_NODE",
                                color = NeonGreen,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "0x4F...B921",
                                color = WhiteTech,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Pulser Node Dot
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WhiteTech.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(NeonGreen, RoundedCornerShape(4.dp))
                            )
                        }

                        // Credits Faucet Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavyMedium),
                            border = BorderStroke(1.dp, BrassAccent.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { viewModel.addFaucetCredits() }
                                .testTag("credit_faucet_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Credits balance",
                                    tint = BrassAlert,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${String.format("%.2f", userCredits)} AMAC",
                                    color = BrassAlert,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.AddBox,
                                    contentDescription = "Faucet button",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf(
                        "01. EXCHANGE" to Icons.Default.ShoppingCart,
                        "02. MERCHANT" to Icons.Default.Dns,
                        "03. MY VAULT" to Icons.Default.VpnKey,
                        "04. LOGISTICS" to Icons.Default.LocalShipping
                    )

                    tabs.forEachIndexed { index, (label, icon) ->
                        val selected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (selected) NavyMedium else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) BrassAccent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { activeTab = index }
                                .padding(vertical = 10.dp)
                                .testTag("tab_btn_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) BrassAccent else GrayText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    color = if (selected) WhiteTech else GrayText,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selected) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height(2.dp)
                                            .background(BrassAccent, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepNavy)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                when (activeTab) {
                    0 -> SovereignExchangeView(
                        viewModel = viewModel,
                        onBuyRequest = { selectedProductToBuy = it }
                    )
                    1 -> SovereignMerchantPortalView(
                        viewModel = viewModel
                    )
                    2 -> SovereignCustomerVaultView(
                        viewModel = viewModel
                    )
                    3 -> SovereignLogisticsNetworkView(
                        viewModel = viewModel
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .padding(top = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOVEREIGN SYSTEM LEDGER & AUDIT LOGS",
                            color = CrypticKeyColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(NeonGreen, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PEER ACTIVE",
                                color = NeonGreen,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("terminal_log_output")
                    ) {
                        if (terminalLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "> Waiting for transactions to populate verification queues...",
                                    color = GrayText,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            items(terminalLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("ERROR") || log.contains("failed")) RedAlert else if (log.contains("settled") || log.contains("SUCCESS")) CrypticKeyColor else WhiteTech,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedProductToBuy?.let { product ->
        SovereignTradeSettlementDialog(
            product = product,
            viewModel = viewModel,
            onDismiss = { selectedProductToBuy = null }
        )
    }
}

// ==========================================
// TAB 01: SOVEREIGN EXCHANGE & MARKETPLACE
// ==========================================

@Composable
fun SovereignExchangeView(
    viewModel: SovereignCommerceViewModel,
    onBuyRequest: (ProductEntity) -> Unit
) {
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val typeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val reputationFilterState by viewModel.reputationFilter.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val newsItems by viewModel.newsItems.collectAsStateWithLifecycle()

    var activeLiveStreamToShow by remember { mutableStateOf<NewsItemEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("exchange_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE ECOSYSTEM BROADCASTS & FEEDS",
                        color = BrassAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "[AURELIA STREAM]",
                        color = GrayText,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (newsItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "No updates at index nodes.",
                            color = GrayText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        newsItems.forEach { feed ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                                border = BorderStroke(1.dp, if (feed.isLiveStream) RedAlert else Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(280.dp)
                                    .clickable {
                                        if (feed.isLiveStream) {
                                            activeLiveStreamToShow = feed
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(if (feed.isLiveStream) RedAlert else BrassAccent)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = feed.merchantName,
                                                color = if (feed.isLiveStream) RedAlert else BrassAccent,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (feed.isLiveStream) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = RedAlert),
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
                                                Text(
                                                    text = "LIVE DEMO",
                                                    color = WhiteTech,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = feed.title,
                                        color = WhiteTech,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = feed.content,
                                        color = GrayText,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        activeLiveStreamToShow?.let { stream ->
            item {
                SimulatedLiveStreamPlayerSection(
                    stream = stream,
                    onClose = { activeLiveStreamToShow = null }
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyMedium)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.setSearchText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            "Filter catalog hash contents...",
                            color = GrayText,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BrassAccent
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteTech,
                        unfocusedTextColor = WhiteTech,
                        focusedBorderColor = BrassAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf("ALL", "PHYSICAL", "DIGITAL", "SERVICE", "API")
                        types.forEach { type ->
                            val isSelected = typeFilter == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrassAccent else DeepNavy)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrassAccent else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setTypeFilter(type) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("filter_type_$type")
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) DeepNavy else WhiteTech,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (reputationFilterState == "EXCLUSIVE") NavySurface else DeepNavy)
                            .border(
                                width = 1.dp,
                                color = if (reputationFilterState == "EXCLUSIVE") BrassAlert else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.toggleReputationFilter() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("reputation_toggle")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Reputation filter",
                                tint = if (reputationFilterState == "EXCLUSIVE") BrassAlert else GrayText,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "4.8+",
                                color = if (reputationFilterState == "EXCLUSIVE") BrassAlert else WhiteTech,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        if (products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty Marketplace",
                            tint = GrayText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No compatible commerce listings matching node requirements found.",
                            color = GrayText,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        } else {
            items(products) { product ->
                EcosystemProductCard(
                    product = product,
                    onBuyClick = { onBuyRequest(product) }
                )
            }
        }
    }
}

@Composable
fun EcosystemProductCard(
    product: ProductEntity,
    onBuyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = BrassAccent,
                    topLeft = Offset.Zero,
                    size = size.copy(width = 3.dp.toPx())
                )
            }
            .testTag("product_card_${product.productId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (product.type.uppercase()) {
                        "PHYSICAL" -> BrassAccent
                        "DIGITAL" -> CrypticKeyColor
                        "API" -> NeonGreen
                        else -> NeonOrange
                    }
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.type,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (product.type == "API" && product.apiBillingModel != "NONE") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, GrayText)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.apiBillingModel,
                                color = GrayText,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Text(
                    text = "ID: ${product.productId}",
                    color = GrayText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                color = WhiteTech,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.description,
                color = GrayText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, NavySurface)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Offer Hash Code",
                    tint = CrypticKeyColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OFFER HASH: [0x${product.hash.take(18)}...]",
                    color = CrypticKeyColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = BrassAlert,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${product.rating} (${product.reviewsCount})",
                    color = WhiteTech,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MERCHANT NODE",
                        color = GrayText,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = product.merchantName,
                        color = BrassAccent,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "OFFER PRICE",
                            color = GrayText,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (product.price < 0.1) "${product.price} AMAC" else "$${String.format("%.2f", product.price)} AMAC",
                            color = BrassAlert,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = onBuyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrassAccent, contentColor = DeepNavy),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("buy_trigger_${product.productId}")
                    ) {
                        Text(
                            text = "ACQUIRE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedLiveStreamPlayerSection(
    stream: NewsItemEntity,
    onClose: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var waveScale by remember { mutableStateOf(1f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            waveScale = (8..15).random() / 10f
            delay(150)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.dp, RedAlert),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("stream_player_container")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(RedAlert, RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AURELIA LIVE CHANNEL: ${stream.merchantName}",
                        color = RedAlert,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Broadcast",
                        tint = GrayText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(NavyMedium)
                    .border(1.dp, NavySurface),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val bars = 25
                    val spacing = size.width / bars
                    val center = size.height / 2f

                    for (i in 0 until bars) {
                        val barHeight = (((i * 7) % 30) + 10) * waveScale
                        val x = i * spacing
                        drawLine(
                            color = if (i % 2 == 0) BrassAccent else RedAlert,
                            start = Offset(x, center - barHeight / 2),
                            end = Offset(x, center + barHeight / 2),
                            strokeWidth = 6f
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "FEED: ${stream.streamUrl}\nFORMAT: DECENTRALIZED RTMP P2P // PEER_COUNT: ${(15..88).random()}",
                        color = WhiteTech,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        style = LocalTextStyle.current.copy(lineHeight = 10.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stream.title,
                    color = WhiteTech,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "AUDIO DECODER: SOVEREIGN LOSSLESS OK",
                    color = NeonGreen,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==========================================
// DECORATOR DIALOG: SETTLEMENT CHECKS
// ==========================================

@Composable
fun SovereignTradeSettlementDialog(
    product: ProductEntity,
    viewModel: SovereignCommerceViewModel,
    onDismiss: () -> Unit
) {
    val logisticsNodes by viewModel.allLogisticsNodes.collectAsStateWithLifecycle()
    var selectedFulfillmentIndex by remember { mutableStateOf(0) }

    val activeNodeId = if (logisticsNodes.isNotEmpty() && selectedFulfillmentIndex < logisticsNodes.size) {
        logisticsNodes[selectedFulfillmentIndex].nodeId
    } else "DIRECT_DEPARTURE"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyMedium),
            border = BorderStroke(2.dp, BrassAccent),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("buying_agreement_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Signature verification scan",
                        tint = BrassAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRADE SETTLEMENT PORTAL",
                        color = BrassAccent,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = NavySurface)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SPECIFICATION BLOCK",
                    color = GrayText,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = product.title,
                    color = WhiteTech,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SKU Category: ${product.type} | Vendor: ${product.merchantName}",
                    color = GrayText,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (product.type == "PHYSICAL") {
                    Text(
                        text = "CHOOSE FULFILLMENT DISTRIBUTION HUB",
                        color = GrayText,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (logisticsNodes.isEmpty()) {
                        Text(
                            text = "No tracking hubs connected. Defaulting to Sovereign Self-Pick.",
                            color = RedAlert,
                            fontSize = 11.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            logisticsNodes.forEachIndexed { idx, node ->
                                val current = selectedFulfillmentIndex == idx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (current) NavySurface else Color.Transparent)
                                        .border(1.dp, if (current) BrassAccent else NavySurface)
                                        .clickable { selectedFulfillmentIndex = idx }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = current,
                                        onClick = { selectedFulfillmentIndex = idx },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrassAccent)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = node.name,
                                            color = WhiteTech,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Location: ${node.location} | Node Type: ${node.type}",
                                            color = GrayText,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, NavySurface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "INFO: Digital asset delivery. Key distribution hashes will be generated and seeded instantly to your Sovereign Customer Vault.",
                            color = CrypticKeyColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val testSigString = "0x" + SovereignCommerceDb.generateSha256(product.productId + "selfBuyer")
                Text(
                    text = "TRANSACTION PROOF GENERATION",
                    color = GrayText,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "PRE-SIGNATURE:\n$testSigString",
                        color = CrypticKeyColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, GrayText),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            "CANCEL ARRANGEMENT",
                            color = WhiteTech,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.buyProduct(product, activeNodeId)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrassAccent),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            "CONFIRM TRADE",
                            color = DeepNavy,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 02: SOVEREIGN MERCHANT PORTAL
// ==========================================

@Composable
fun SovereignMerchantPortalView(
    viewModel: SovereignCommerceViewModel
) {
    val selfMerchant by viewModel.selfMerchant.collectAsStateWithLifecycle()
    val merchantId = selfMerchant?.merchantId ?: ""
    val merchantsProductStream by viewModel.getProductsByMerchant(merchantId).collectAsStateWithLifecycle(emptyList())

    var registerNameText by remember { mutableStateOf("") }
    var isMintingActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("merchant_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (selfMerchant == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, BrassAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "Sovereign Register Identity",
                                tint = BrassAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MERCHANT IDENTITY GENERATOR",
                                color = BrassAccent,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = NavySurface)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "To participate as an independent sovereign storefront provider, you must generate a cryptographic key pair matching standard index protocols on the Aurelia Web.",
                            color = GrayText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registerNameText,
                            onValueChange = { registerNameText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_register_name"),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text("Storefront Operator Alias", color = GrayText, fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteTech,
                                unfocusedTextColor = WhiteTech,
                                focusedBorderColor = BrassAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = DeepNavy,
                                unfocusedContainerColor = DeepNavy
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (registerNameText.isNotBlank()) {
                                    viewModel.registerAsMerchant(registerNameText)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_merchant_identity_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrassAccent, contentColor = DeepNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "GENERATE PROTOCOL IDENTIFIER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            val merch = selfMerchant!!
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, BrassAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(NeonGreen, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = merch.name.uppercase(),
                                    color = WhiteTech,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(BrassAccent.copy(alpha = 0.1f))
                                    .border(1.dp, BrassAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = merch.status,
                                    color = BrassAccent,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = "SOVEREIGN KEY RING (DID)",
                                    color = GrayText,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = merch.publicKey,
                                    color = CrypticKeyColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(
                                    text = "LEDGER ACCUMULATION",
                                    color = GrayText,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$${String.format("%.2f", merch.balance)} AMAC",
                                    color = BrassAlert,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { isMintingActive = !isMintingActive },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_minting_form_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = BrassAccent),
                    border = BorderStroke(1.dp, BrassAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMintingActive) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add catalog trigger",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMintingActive) "CLOSE MINTING PORT" else "MINT NEW COMMERCIAL OFFER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            if (isMintingActive) {
                item {
                    MerchantMintProductFormCard(
                        viewModel = viewModel,
                        onMintSuccess = { isMintingActive = false }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISPATCH AND ACTIVE OFFER HOARDS",
                        color = BrassAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${merchantsProductStream.size} active",
                        color = GrayText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (merchantsProductStream.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyMedium)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(25.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No offers currently minted on verification servers under this identity.",
                            color = GrayText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(merchantsProductStream) { product ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyMedium),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(BrassAccent.copy(alpha = 0.1f))
                                            .border(1.dp, BrassAccent)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            product.type,
                                            color = BrassAccent,
                                            fontSize = 7.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ID: ${product.productId}",
                                        color = GrayText,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = product.title,
                                    color = WhiteTech,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$${String.format("%.2f", product.price)} AMAC | Qty: ${product.inventory}",
                                    color = BrassAlert,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.deleteProduct(product.productId) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("revoke_listing_btn_${product.productId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delist product",
                                    tint = RedAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantMintProductFormCard(
    viewModel: SovereignCommerceViewModel,
    onMintSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var inventoryStr by remember { mutableStateOf("") }
    var mediaShipProfile by remember { mutableStateOf("Postal Route Standard Air") }
    var activeTypeIndex by remember { mutableStateOf(0) }
    var apiBillingIndex by remember { mutableStateOf(0) }
    var apiEndpointStr by remember { mutableStateOf("/api/v1/omniscribe/verify") }

    val productTypes = listOf("PHYSICAL", "DIGITAL", "SERVICE", "API")
    val selectedType = productTypes[activeTypeIndex]

    val billingModels = listOf("PER_REQUEST", "SUBSCRIPTION", "MICROPAYMENT")
    val selectedBilling = billingModels[apiBillingIndex]

    Card(
        colors = CardDefaults.cardColors(containerColor = NavyMedium),
        border = BorderStroke(1.dp, BrassAccent),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_minting_form")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "COMMERCIAL CATALOG PROTOCOL DEF",
                color = BrassAccent,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Asset Trade Type:", color = GrayText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                productTypes.forEachIndexed { idx, type ->
                    val selected = idx == activeTypeIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selected) BrassAccent else DeepNavy)
                            .border(1.dp, if (selected) BrassAccent else NavySurface)
                            .clickable {
                                activeTypeIndex = idx
                                if (type == "API") {
                                    priceStr = "0.005"
                                }
                            }
                            .padding(vertical = 6.dp)
                            .testTag("form_type_selector_$type"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            color = if (selected) DeepNavy else WhiteTech,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mint_title_input"),
                label = { Text("Product/API Offer Title", color = GrayText, fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhiteTech,
                    unfocusedTextColor = WhiteTech,
                    focusedBorderColor = BrassAccent,
                    unfocusedBorderColor = NavySurface,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mint_desc_input"),
                label = { Text("Ecosystem Description Specs", color = GrayText, fontSize = 11.sp) },
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhiteTech,
                    unfocusedTextColor = WhiteTech,
                    focusedBorderColor = BrassAccent,
                    unfocusedBorderColor = NavySurface,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mint_price_input"),
                    label = { Text("Price (AMAC)", color = GrayText, fontSize = 10.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteTech,
                        unfocusedTextColor = WhiteTech,
                        focusedBorderColor = BrassAccent,
                        unfocusedBorderColor = NavySurface,
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    )
                )

                OutlinedTextField(
                    value = inventoryStr,
                    onValueChange = { inventoryStr = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mint_inventory_input"),
                    label = { Text("Inventory (Pcs)", color = GrayText, fontSize = 10.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteTech,
                        unfocusedTextColor = WhiteTech,
                        focusedBorderColor = BrassAccent,
                        unfocusedBorderColor = NavySurface,
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedType == "PHYSICAL") {
                OutlinedTextField(
                    value = mediaShipProfile,
                    onValueChange = { mediaShipProfile = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Logistics Delivery Channel Profile", color = GrayText, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteTech,
                        unfocusedTextColor = WhiteTech,
                        focusedBorderColor = BrassAccent,
                        unfocusedBorderColor = NavySurface,
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    )
                )
            } else if (selectedType == "API") {
                Column {
                    Text("API Marketplace Billing Scheme:", color = GrayText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        billingModels.forEachIndexed { idx, model ->
                            val selected = idx == apiBillingIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (selected) StringToColor(model) else DeepNavy)
                                    .border(1.dp, if (selected) BrassAccent else NavySurface)
                                    .clickable { apiBillingIndex = idx }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = model.replace("_", " "),
                                    color = if (selected) DeepNavy else WhiteTech,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = apiEndpointStr,
                        onValueChange = { apiEndpointStr = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Server Routing Path Endpoint (URI)", color = GrayText, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteTech,
                            unfocusedTextColor = WhiteTech,
                            focusedBorderColor = BrassAccent,
                            unfocusedBorderColor = NavySurface,
                            focusedContainerColor = DeepNavy,
                            unfocusedContainerColor = DeepNavy
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 1.0
                    val inventory = inventoryStr.toIntOrNull() ?: 100
                    if (title.isNotBlank()) {
                        viewModel.addProduct(
                            title = title,
                            description = description,
                            price = price,
                            inventory = inventory,
                            type = selectedType,
                            shippingProfile = mediaShipProfile,
                            apiBillingModel = if (selectedType == "API") selectedBilling else "NONE",
                            apiEndpoint = if (selectedType == "API") apiEndpointStr else ""
                        )
                        onMintSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_mint_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BrassAccent, contentColor = DeepNavy),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = "MINT SECURE COMMERCE CONTRACT OFFER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun StringToColor(model: String): Color {
    return when (model) {
        "PER_REQUEST" -> NeonGreen
        "SUBSCRIPTION" -> CrypticKeyColor
        else -> BrassAccent
    }
}

// ==========================================
// TAB 03: SOVEREIGN CUSTOMER VAULT VIEW
// ==========================================

@Composable
fun SovereignCustomerVaultView(
    viewModel: SovereignCommerceViewModel
) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vault_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, CrypticKeyColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "Keys block",
                                tint = CrypticKeyColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AURELIA // CUSTOMER SECURE VAULT",
                                color = WhiteTech,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(CrypticKeyColor.copy(alpha = 0.15f))
                                .border(1.dp, CrypticKeyColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DECRYPTED",
                                color = CrypticKeyColor,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your purchase histories, receipts, warranty signatures, and digital licenses are cryptographically locked inside your sovereign device. No advertising tracking profiles are emitted.",
                        color = GrayText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "USER PRIMARY VERIFICATION ID (DID)",
                        color = GrayText,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "did:aurelia:usr_8fk3j9a1qm9kdf72h",
                        color = CrypticKeyColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Text(
                text = "ACTIVE API CHANNELS & MICROPAYMENT ENGAGEMENTS",
                color = BrassAccent,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (subscriptions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyMedium)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active programmable APIs subscribing under this vault.",
                        color = GrayText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(subscriptions) { sub ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(StringToColor(sub.billingModel))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sub.productTitle,
                                    color = WhiteTech,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(StringToColor(sub.billingModel).copy(alpha = 0.1f))
                                    .border(1.dp, StringToColor(sub.billingModel))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sub.billingModel,
                                    color = StringToColor(sub.billingModel),
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Provider DID: ${sub.merchantId} [${sub.merchantName}]",
                            color = GrayText,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, NavySurface)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = "ROUTED RESOLVED RPC CALLS",
                                    color = GrayText,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${sub.apiRequestsUsed} REQUESTS",
                                    color = CrypticKeyColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Button(
                                onClick = { viewModel.callApiRequest(sub) },
                                colors = ButtonDefaults.buttonColors(containerColor = CrypticKeyColor, contentColor = DeepNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("test_rpc_btn_${sub.subscriptionId}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Trigger API RPC",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "CALL RPC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "ACQUISITION INDEX & ORDER WAYBILLS",
                color = BrassAccent,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyMedium)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "History empty. Secure trade operations to populate this crypt block.",
                        color = GrayText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(orders) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_order_card_${order.orderId}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BLOCK ID: ${order.orderId}",
                                color = CrypticKeyColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            val (statColor, textDesc) = when (order.status) {
                                "TRANSIT" -> NeonOrange to "IN TRANSIT"
                                "SHIPPED" -> BrassAlert to "OUT FOR DELIVERY"
                                else -> NeonGreen to "COMPLETED"
                            }

                            Box(
                                modifier = Modifier
                                    .background(statColor.copy(alpha = 0.15f))
                                    .border(1.dp, statColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = textDesc,
                                    color = statColor,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = order.productTitle,
                            color = WhiteTech,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Merchant node: ${order.merchantName}",
                            color = GrayText,
                            fontSize = 11.sp
                        )

                        if (order.productType == "PHYSICAL") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DeepNavy)
                                    .border(1.dp, NavySurface)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "ROUTED DEPOT NODE",
                                        color = GrayText,
                                        fontSize = 7.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = order.fulfillmentNode.replace("node_", "NODE: ").uppercase(),
                                        color = WhiteTech,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Waybill: ${order.trackingNumber}",
                                        color = GrayText,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (order.status != "COMPLETED") {
                                    Button(
                                        onClick = { viewModel.cycleOrderStatus(order) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrassAccent, contentColor = DeepNavy),
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier
                                            .height(26.dp)
                                            .testTag("cycle_status_${order.orderId}"),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SIMULATE STEP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "SIGNATURE PROOF BLOCK SHA-256",
                            color = GrayText,
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = order.crypticSignature,
                            color = CrypticKeyColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 04: LOGISTICS HUB MAPS VIEW
// ==========================================

@Composable
fun SovereignLogisticsNetworkView(
    viewModel: SovereignCommerceViewModel
) {
    val logisticsNodes by viewModel.allLogisticsNodes.collectAsStateWithLifecycle()

    var nodeName by remember { mutableStateOf("") }
    var nodeLocation by remember { mutableStateOf("") }
    var activeNodeTypeIndex by remember { mutableStateOf(0) }

    val nodeTypes = listOf("REGIONAL", "SATELLITE", "COMMUNITY", "INDEPENDENT")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("logistics_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, BrassAccent.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Logistics command center map",
                            tint = BrassAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AURELIA LOGISTICS CONTROL NETWORK",
                            color = BrassAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "The physical distribution of sovereign goods flows across registered logistics routing grids. Merchants self-dispatch through these independent hubs to coordinate community delivery.",
                        color = GrayText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyMedium),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "REGISTER NEW COURIER/FULFILLMENT DEPOT",
                        color = WhiteTech,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nodeName,
                        onValueChange = { nodeName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("node_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        label = { Text("Fulfillment Station Name", color = GrayText, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteTech,
                            unfocusedTextColor = WhiteTech,
                            focusedBorderColor = BrassAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = DeepNavy,
                            unfocusedContainerColor = DeepNavy
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = nodeLocation,
                        onValueChange = { nodeLocation = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("node_location_input"),
                        shape = RoundedCornerShape(12.dp),
                        label = { Text("Coordinates / Location City", color = GrayText, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteTech,
                            unfocusedTextColor = WhiteTech,
                            focusedBorderColor = BrassAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = DeepNavy,
                            unfocusedContainerColor = DeepNavy
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Select Distribution Node Type:", color = GrayText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        nodeTypes.forEachIndexed { idx, type ->
                            val selected = idx == activeNodeTypeIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) BrassAccent else DeepNavy)
                                    .border(1.dp, if (selected) BrassAccent else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .clickable { activeNodeTypeIndex = idx }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (selected) DeepNavy else WhiteTech,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (nodeName.isNotBlank() && nodeLocation.isNotBlank()) {
                                viewModel.addLogisticsNode(nodeName, nodeLocation, nodeTypes[activeNodeTypeIndex])
                                nodeName = ""
                                nodeLocation = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_node_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrassAccent, contentColor = DeepNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "PROPAGATE HUB INFRASTRUCTURE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "ACTIVE PHYSICAL LOGISTICAL SATELLITES",
                color = BrassAccent,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (logisticsNodes.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyMedium)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(25.dp)
                ) {
                    Text("Zero logistics nodes indexed locally.", color = GrayText, fontFamily = FontFamily.Monospace)
                }
            }
        } else {
            items(logisticsNodes) { node ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyMedium),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logistics_node_card_${node.nodeId}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NODEID: ${node.nodeId}",
                                color = GrayText,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Box(
                                modifier = Modifier
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .border(1.dp, NeonGreen)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = node.status,
                                    color = NeonGreen,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = node.name,
                            color = WhiteTech,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Station location coords: ${node.location} | Division: ${node.type}",
                            color = GrayText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
