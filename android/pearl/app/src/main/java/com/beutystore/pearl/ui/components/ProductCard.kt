package com.beutystore.pearl.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.theme.PearlTheme

@Composable
fun ProductCard(
    product: Product,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onProductClick(product) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Отображаем цену со скидкой если есть
                if (product.originalPrice != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.price} ₽",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${product.originalPrice} ₽",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    Text(
                        text = "${product.price} ₽",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Рейтинг
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "★ ${"%.1f".format(product.rating)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = " (${product.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isUserLoggedIn) {
                        onAddToCart()
                    } else {
                        onAuthRequired()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to cart",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    val sampleProduct = Product(
        id = 1,
        name = "Увлажняющий крем для лица",
        price = 2500,
        originalPrice = 3000,
        imageUrl = "https://via.placeholder.com/150",
        description = "Интенсивно увлажняющий крем",
        category = "Уход за лицом",
        brand = "L'Oreal",
        rating = 4.7f,
        reviewCount = 128,
        inStock = true,
        features = listOf("Увлажнение", "Защита"),
        colors = listOf("Белый"),
        sizes = listOf("50 мл")
    )

    PearlTheme {
        ProductCard(
            product = sampleProduct,
            isUserLoggedIn = true,
            onAuthRequired = {},
            onAddToCart = {},
            onProductClick = {}
        )
    }
}