package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed

data class FAQItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val faqItems = remember {
        mutableStateListOf(
            FAQItem(
                question = "Как оформить заказ?",
                answer = "Выберите товары в каталоге, добавьте их в корзину и перейдите к оформлению заказа. Заполните данные доставки и выберите способ оплаты."
            ),
            FAQItem(
                question = "Какие способы оплаты доступны?",
                answer = "Мы принимаем оплату банковскими картами, наличными при получении, а также бонусными баллами с вашей Pearl Card."
            ),
            FAQItem(
                question = "Как отследить заказ?",
                answer = "Вы можете отследить статус заказа в разделе 'Мои заказы' в вашем профиле. Там отображается текущий статус и информация о доставке."
            ),
            FAQItem(
                question = "Можно ли вернуть товар?",
                answer = "Да, вы можете вернуть товар в течение 14 дней с момента покупки при условии сохранения товарного вида и упаковки."
            ),
            FAQItem(
                question = "Как начисляются бонусы?",
                answer = "Бонусы начисляются автоматически при каждой покупке. 1% от суммы покупки зачисляется на вашу Pearl Card. При покупке от 5000 ₽ начисляется дополнительно 100 бонусов."
            )
        )
    }

    // Определяем фон: светло-лавандовый для светлой темы, темный для темной
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // TopBar
        TopAppBar(
            title = { 
                Text(
                    text = "Помощь",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contact section
            item {
                Text(
                    text = "Свяжитесь с нами",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                ContactCard(
                    icon = Icons.Default.Phone,
                    title = "Телефон",
                    subtitle = "+7 (800) 123-45-67",
                    description = "Ежедневно с 9:00 до 21:00"
                )
            }

            item {
                ContactCard(
                    icon = Icons.Default.Email,
                    title = "Email",
                    subtitle = "support@pearl.ru",
                    description = "Ответим в течение 24 часов"
                )
            }

            // FAQ section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Часто задаваемые вопросы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(faqItems.size) { index ->
                val faq = faqItems[index]
                FAQCard(
                    faq = faq,
                    onExpandedChange = { expanded ->
                        faqItems[index] = faq.copy(isExpanded = expanded)
                    }
                )
            }
        }
    }
}

@Composable
private fun ContactCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PearlRed,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PearlRed
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun FAQCard(
    faq: FAQItem,
    onExpandedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onExpandedChange(!faq.isExpanded) }
                ) {
                    Icon(
                        imageVector = if (faq.isExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (faq.isExpanded) "Свернуть" else "Развернуть"
                    )
                }
            }
            
            if (faq.isExpanded) {
                Divider()
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelpScreenPreview() {
    PearlTheme {
        HelpScreen(onBackClick = {})
    }
}

