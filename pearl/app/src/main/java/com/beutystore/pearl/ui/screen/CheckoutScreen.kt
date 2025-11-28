package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.annotation.SuppressLint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.viewmodel.CartViewModel
import com.beutystore.pearl.ui.viewmodel.OrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel(),
    ordersViewModel: OrdersViewModel = viewModel(),
    userViewModel: com.beutystore.pearl.ui.viewmodel.UserViewModel = viewModel(),
    accessToken: String? = null,
    onBackClick: () -> Unit = {},
    onOrderSuccess: () -> Unit = {}
) {
    val cartItems by cartViewModel.cartItemsList.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val itemCount by cartViewModel.cartItemCount.collectAsState()
    val user by userViewModel.user.collectAsState()
    
    var deliveryAddress by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Карта") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var createdOrderNumber by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    // Бонусные баллы
    val availableBonusPoints = user?.bonus_card?.bonusPoints ?: 0
    var bonusPointsToUse by remember { mutableStateOf(0) }
    var useBonusPoints by remember { mutableStateOf(false) }
    
    // Загружаем профиль при входе
    LaunchedEffect(accessToken) {
        if (accessToken != null && user == null) {
            userViewModel.loadProfile(accessToken)
        }
    }
    
    // Итоговая сумма с учетом списанных баллов
    val finalPrice = remember(totalPrice, bonusPointsToUse) {
        (totalPrice - bonusPointsToUse).coerceAtLeast(0)
    }

    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    if (showMapDialog) {
        MapDialog(
            onDismiss = { showMapDialog = false },
            onAddressSelected = { address, lat, lng ->
                deliveryAddress = address
                selectedLatitude = lat
                selectedLongitude = lng
                showMapDialog = false
            }
        )
    }

    // Диалог успешного оформления заказа
    if (showSuccessDialog) {
        OrderSuccessDialog(
            orderNumber = createdOrderNumber ?: "Заказ оформлен",
            onDismiss = {
                showSuccessDialog = false
                createdOrderNumber = null
                cartViewModel.clearCart()
                onOrderSuccess()
            }
        )
    }
    
    // Диалог ошибки
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                errorMessage = null
            },
            title = {
                Text(
                    text = "Ошибка оформления заказа",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "Произошла неизвестная ошибка",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.beutystore.pearl.ui.theme.PearlRed
                    )
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header
        TopAppBar(
            title = { 
                Text(
                    text = "Оформление заказа",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (cartItems.isEmpty()) {
            // Пустая корзина
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Корзина пуста",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Добавьте товары в корзину",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Список товаров
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Товары в заказе",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        cartItems.forEach { cartItem ->
                            CheckoutItemRow(cartItem = cartItem)
                            if (cartItem != cartItems.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Всего товаров:",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "$itemCount шт.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Сумма товаров:",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "$totalPrice ₽",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Адрес доставки
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = com.beutystore.pearl.ui.theme.PearlRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Адрес доставки",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Кнопка выбора на карте
                        OutlinedButton(
                            onClick = { showMapDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Выбрать на карте")
                        }
                        
                        // Поле ввода адреса
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Введите адрес доставки или выберите на карте") },
                            singleLine = false,
                            minLines = 2,
                            maxLines = 4,
                            leadingIcon = if (selectedLatitude != null && selectedLongitude != null) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Адрес выбран на карте",
                                        tint = com.beutystore.pearl.ui.theme.PearlRed
                                    )
                                }
                            } else null
                        )
                        
                        if (selectedLatitude != null && selectedLongitude != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📍 Выбрано на карте: ${String.format("%.6f", selectedLatitude)}, ${String.format("%.6f", selectedLongitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = com.beutystore.pearl.ui.theme.PearlRed
                            )
                        }
                    }
                }

                // Способ оплаты (Карта, Наличные, СБП)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = com.beutystore.pearl.ui.theme.PearlRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Способ оплаты",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        val paymentMethods = listOf("Карта", "Наличные", "СБП")
                        paymentMethods.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = paymentMethod == method,
                                    onClick = { 
                                        paymentMethod = method
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = method)
                            }
                        }
                    }
                }

                // Оплата баллами (отдельный переключатель)
                if (availableBonusPoints > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Оплата баллами",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Доступно: $availableBonusPoints баллов",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Switch(
                                    checked = useBonusPoints,
                                    onCheckedChange = { 
                                        useBonusPoints = it
                                        if (it) {
                                            // При включении устанавливаем максимальное количество баллов
                                            bonusPointsToUse = minOf(availableBonusPoints, totalPrice)
                                        } else {
                                            bonusPointsToUse = 0
                                        }
                                    }
                                )
                            }
                            
                            if (useBonusPoints) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Слайдер для выбора количества баллов
                                Column {
                                    Text(
                                        text = "Использовать баллов: $bonusPointsToUse",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Slider(
                                        value = bonusPointsToUse.toFloat(),
                                        onValueChange = { 
                                            bonusPointsToUse = it.toInt().coerceIn(0, minOf(availableBonusPoints, totalPrice))
                                        },
                                        valueRange = 0f..minOf(availableBonusPoints.toFloat(), totalPrice.toFloat()),
                                        steps = if (minOf(availableBonusPoints, totalPrice) > 10) 10 else 0
                                    )
                                    
                                    if (bonusPointsToUse > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Скидка: -$bonusPointsToUse ₽",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PearlRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Итоговая сумма
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.beutystore.pearl.ui.theme.PearlLightPeach
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (bonusPointsToUse > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Сумма товаров:",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "$totalPrice ₽",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Скидка (баллы):",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = com.beutystore.pearl.ui.theme.PearlRed
                                )
                                Text(
                                    text = "-$bonusPointsToUse ₽",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = com.beutystore.pearl.ui.theme.PearlRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Итого к оплате:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$finalPrice ₽",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.beutystore.pearl.ui.theme.PearlRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка оформления заказа
                Button(
                    onClick = {
                        if (deliveryAddress.isBlank()) {
                            return@Button
                        }
                        if (accessToken == null) {
                            return@Button
                        }
                        
                        // Используем выбранное количество баллов
                        var pointsToUse = bonusPointsToUse
                        
                        isProcessing = true
                        ordersViewModel.createOrderFromCart(
                            cartItems = cartItems,
                            shippingAddress = deliveryAddress,
                            customerEmail = user?.email ?: "",
                            customerPhone = user?.phone ?: "",
                            bonusPointsUsed = pointsToUse,
                            accessToken = accessToken,
                            onSuccess = { order ->
                                createdOrderNumber = order.displayOrderNumber
                                isProcessing = false
                                showSuccessDialog = true
                                // Обновляем профиль для получения актуального баланса баллов
                                if (accessToken != null) {
                                    userViewModel.loadProfile(accessToken)
                                    // Обновляем список заказов
                                    ordersViewModel.loadOrders(accessToken)
                                }
                            },
                            onError = { error ->
                                isProcessing = false
                                errorMessage = error
                                showErrorDialog = true
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isProcessing && deliveryAddress.isNotBlank() && accessToken != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.beutystore.pearl.ui.theme.PearlRed
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Оформить заказ на $finalPrice ₽",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (deliveryAddress.isBlank()) {
                    Text(
                        text = "Заполните адрес доставки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutItemRow(
    cartItem: CartViewModel.CartItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = cartItem.product.imageUrl,
            contentDescription = cartItem.product.name,
            modifier = Modifier.size(60.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cartItem.product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${cartItem.product.price} ₽ × ${cartItem.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Text(
            text = "${cartItem.product.price * cartItem.quantity} ₽",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OrderSuccessDialog(
    orderNumber: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = com.beutystore.pearl.ui.theme.PearlRed,
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "Заказ оформлен!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Номер заказа:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = orderNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.beutystore.pearl.ui.theme.PearlRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Спасибо за покупку! Мы свяжемся с вами в ближайшее время.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.beutystore.pearl.ui.theme.PearlRed
                )
            ) {
                Text("Отлично!")
            }
        }
    )
}

@Composable
private fun MapDialog(
    onDismiss: () -> Unit,
    onAddressSelected: (String, Double, Double) -> Unit
) {
    val initialLat = 55.7558 // Москва по умолчанию
    val initialLng = 37.6173
    var mapLatitude by remember { mutableStateOf(initialLat) }
    var mapLongitude by remember { mutableStateOf(initialLng) }
    var zoom by remember { mutableStateOf(15) }
    var selectedAddress by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Автоматическая загрузка адреса при изменении координат (только при клике на карте или перетаскивании маркера)
    // Не загружаем автоматически при инициализации, чтобы не делать лишних запросов

    // Поиск адреса
    fun searchAddress(query: String) {
        if (query.isBlank()) return
        isSearching = true
        scope.launch(Dispatchers.IO) {
            try {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = java.net.URL(
                    "https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=1&addressdetails=1"
                )
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "PearlApp/1.0")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val result = jsonArray.getJSONObject(0)
                        val lat = result.getString("lat").toDouble()
                        val lon = result.getString("lon").toDouble()
                        val displayName = result.optString("display_name", "")
                        
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            mapLatitude = lat
                            mapLongitude = lon
                            selectedAddress = displayName.ifEmpty { "Координаты: ${String.format("%.6f", lat)}, ${String.format("%.6f", lon)}" }
                            isSearching = false
                            // Загружаем адрес по координатам для более точного результата
                            isLoadingAddress = true
                            loadAddressFromCoordinates(lat, lon) { address ->
                                if (address.isNotEmpty() && !address.startsWith("Координаты:")) {
                                    selectedAddress = address
                                }
                                isLoadingAddress = false
                            }
                        }
                    } else {
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            isSearching = false
                        }
                    }
                } else {
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        isSearching = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MapDialog", "Error searching address", e)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    isSearching = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите адрес на карте",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Поиск адреса
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поиск адреса") },
                    placeholder = { Text("Например: Москва, Красная площадь") },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchAddress(searchQuery) }) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Поиск")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                searchAddress(searchQuery)
                            }
                        }
                    ),
                    singleLine = true
                )
                
                // Интерактивная карта через WebView
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Интерактивная карта через WebView
                        InteractiveMapView(
                            latitude = mapLatitude,
                            longitude = mapLongitude,
                            zoom = zoom,
                            onMapClick = { lat, lng ->
                                mapLatitude = lat
                                mapLongitude = lng
                                selectedAddress = ""
                                isLoadingAddress = true
                                loadAddressFromCoordinates(lat, lng) { address ->
                                    selectedAddress = address
                                    isLoadingAddress = false
                                }
                            },
                            onZoomChange = { newZoom ->
                                zoom = newZoom
                            }
                        )
                        
                        // Кнопки управления масштабом (только одна пара)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Кнопка увеличения масштаба
                            IconButton(
                                onClick = { 
                                    if (zoom < 19) {
                                        zoom++
                                    }
                                },
                                enabled = zoom < 19,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    contentDescription = "Увеличить масштаб",
                                    tint = if (zoom < 19) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            // Кнопка уменьшения масштаба
                            IconButton(
                                onClick = { 
                                    if (zoom > 5) {
                                        zoom--
                                    }
                                },
                                enabled = zoom > 5,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            ) {
                                Icon(
                                    Icons.Default.Remove, 
                                    contentDescription = "Уменьшить масштаб",
                                    tint = if (zoom > 5) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                        
                        if (isLoadingAddress) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                
                // Поле с выбранным адресом
                OutlinedTextField(
                    value = selectedAddress.ifEmpty { "Введите адрес в поиске или используйте кнопки навигации" },
                    onValueChange = { selectedAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Выбранный адрес") },
                    readOnly = false,
                    enabled = true,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3
                )
                
                // Координаты
                Text(
                    text = "📍 Координаты: ${String.format("%.6f", mapLatitude)}, ${String.format("%.6f", mapLongitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PearlRed
                )
                
                Text(
                    text = "💡 Используйте поиск адреса для быстрого нахождения. На карте можно увеличивать масштаб и перемещаться",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAddress.isNotEmpty()) {
                        onAddressSelected(selectedAddress, mapLatitude, mapLongitude)
                    } else {
                        // Используем координаты, если адрес не выбран
                        onAddressSelected(
                            "Координаты: ${String.format("%.6f", mapLatitude)}, ${String.format("%.6f", mapLongitude)}",
                            mapLatitude,
                            mapLongitude
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.beutystore.pearl.ui.theme.PearlRed
                )
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// Функция для получения адреса по координатам
private fun loadAddressFromCoordinates(
    latitude: Double,
    longitude: Double,
    onResult: (String) -> Unit
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = java.net.URL(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=18&addressdetails=1"
            )
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "PearlApp/1.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                val jsonObject = org.json.JSONObject(response)
                
                // Используем display_name как основной адрес, он более полный
                val displayName = jsonObject.optString("display_name", "")
                if (displayName.isNotEmpty()) {
                    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                        onResult(displayName)
                    }
                    return@launch
                }
                
                // Если display_name пустой, формируем адрес из частей
                val addressObj = jsonObject.optJSONObject("address")
                if (addressObj != null) {
                    val addressParts = mutableListOf<String>()
                    
                    addressObj.optString("road")?.takeIf { it.isNotEmpty() }?.let { addressParts.add(it) }
                    addressObj.optString("house_number")?.takeIf { it.isNotEmpty() }?.let { addressParts.add(it) }
                    val city = addressObj.optString("city")
                        ?: addressObj.optString("town")
                        ?: addressObj.optString("village")
                        ?: addressObj.optString("state")
                    
                    city?.takeIf { it.isNotEmpty() }?.let { addressParts.add(it) }
                    
                    val address = if (addressParts.isNotEmpty()) {
                        addressParts.joinToString(", ")
                    } else {
                        "Координаты: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
                    }
                    
                    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                        onResult(address)
                    }
                } else {
                    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                        onResult("Координаты: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}")
                    }
                }
            } else {
                kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                    onResult("Координаты: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapDialog", "Error loading address from coordinates", e)
            kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                onResult("Координаты: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}")
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun InteractiveMapView(
    latitude: Double,
    longitude: Double,
    zoom: Int,
    onMapClick: (Double, Double) -> Unit,
    onZoomChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    
    // Инициализация osmdroid
    LaunchedEffect(Unit) {
        try {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
            )
            Configuration.getInstance().userAgentValue = "PearlApp/1.0"
            // Включаем кеширование тайлов
            Configuration.getInstance().cacheMapTileCount = 1000
            Configuration.getInstance().tileFileSystemThreads = 4
            android.util.Log.d("InteractiveMapView", "osmdroid initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("InteractiveMapView", "Error initializing osmdroid", e)
        }
    }
    
    // Управление жизненным циклом MapView
    DisposableEffect(lifecycleOwner) {
        android.util.Log.d("InteractiveMapView", "Setting up lifecycle observer")
        val observer = LifecycleEventObserver { _, event ->
            android.util.Log.d("InteractiveMapView", "Lifecycle event: $event")
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    android.util.Log.d("InteractiveMapView", "Calling mapView.onResume()")
                    mapView?.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    android.util.Log.d("InteractiveMapView", "Calling mapView.onPause()")
                    mapView?.onPause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            android.util.Log.d("InteractiveMapView", "Disposing lifecycle observer")
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onPause()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            android.util.Log.d("InteractiveMapView", "Creating MapView with lat=$latitude, lng=$longitude, zoom=$zoom")
            MapView(ctx).apply {
                try {
                    // Настройка osmdroid
                    setTileSource(TileSourceFactory.MAPNIK) // OpenStreetMap тайлы
                    setMultiTouchControls(true)
                    minZoomLevel = 5.0
                    maxZoomLevel = 19.0
                    isClickable = true
                    isFocusable = true
                    
                    android.util.Log.d("InteractiveMapView", "MapView configured successfully")
                    
                    // Устанавливаем начальную позицию
                    controller.setZoom(zoom.toDouble())
                    controller.setCenter(GeoPoint(latitude, longitude))
                    android.util.Log.d("InteractiveMapView", "MapView center set to lat=$latitude, lng=$longitude")
                } catch (e: Exception) {
                    android.util.Log.e("InteractiveMapView", "Error configuring MapView", e)
                }
                
                // Создаем маркер
                val newMarker = Marker(this).apply {
                    position = GeoPoint(latitude, longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    isDraggable = true
                    
                    // Обработчик перетаскивания маркера
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDragStart(marker: Marker) {}
                        override fun onMarkerDrag(marker: Marker) {}
                        override fun onMarkerDragEnd(marker: Marker) {
                            val geoPoint = marker.position
                            onMapClick(geoPoint.latitude, geoPoint.longitude)
                        }
                    })
                }
                overlays.add(newMarker)
                marker = newMarker
                
                // Обработчик клика на карте через MapEventsReceiver
                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let {
                            // Перемещаем маркер на новую позицию
                            newMarker.position = it
                            invalidate()
                            onMapClick(it.latitude, it.longitude)
                            return true
                        }
                        return false
                    }
                    
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        // Можно добавить обработку долгого нажатия, если нужно
                        return false
                    }
                })
                overlays.add(0, mapEventsOverlay) // Добавляем в начало, чтобы клики обрабатывались первыми
                
                // Обработчик изменения масштаба
                addMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        return false
                    }
                    
                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                        event?.zoomLevel?.toInt()?.let { newZoom ->
                            onZoomChange(newZoom)
                        }
                        return false
                    }
                })
                
                mapView = this
                
                android.util.Log.d("InteractiveMapView", "MapView created, calling onResume")
                
                // Вызываем onResume для правильной инициализации
                post {
                    try {
                        onResume()
                        android.util.Log.d("InteractiveMapView", "MapView onResume called successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("InteractiveMapView", "Error calling onResume", e)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            // Обновляем позицию карты и маркера при изменении координат
            val geoPoint = GeoPoint(latitude, longitude)
            
            // Обновляем позицию карты только если координаты изменились
            val currentCenter = view.mapCenter
            if (kotlin.math.abs(currentCenter.latitude - latitude) > 0.0001 || 
                kotlin.math.abs(currentCenter.longitude - longitude) > 0.0001) {
                view.controller.setCenter(geoPoint)
            }
            
            // Обновляем масштаб только если он изменился (с учетом того, что zoom может быть Int)
            val currentZoom = view.zoomLevelDouble.toInt()
            if (kotlin.math.abs(currentZoom - zoom) > 0) {
                view.controller.setZoom(zoom.toDouble())
            }
            
            // Обновляем маркер, сохраняя все overlays
            marker?.let { currentMarker ->
                if (kotlin.math.abs(currentMarker.position.latitude - latitude) > 0.0001 || 
                    kotlin.math.abs(currentMarker.position.longitude - longitude) > 0.0001) {
                    currentMarker.position = geoPoint
                    view.invalidate()
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    PearlTheme {
        CheckoutScreen()
    }
}


