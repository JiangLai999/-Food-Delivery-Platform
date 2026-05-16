package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.model.ChatMessage
import com.fooddelivery.user.ui.compose.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CustomerServiceScreen(
    onBack: () -> Unit = {},
    viewModel: com.fooddelivery.user.ui.compose.viewmodel.MainViewModel? = null
) {
    var messageText by remember { mutableStateOf("") }
    
    val chatMessages = viewModel?.customerChatMessages?.collectAsStateWithLifecycle()?.value ?: emptyList()
    
    val quickQuestions = remember {
        listOf(
            "如何查看订单状态？",
            "如何申请退款？",
            "配送时间问题",
            "优惠券使用问题",
            "账号绑定问题",
            "其他问题"
        )
    }

    LaunchedEffect(Unit) {
        viewModel?.fetchCustomerServiceChatHistory()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(BackgroundLight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "在线客服",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Phone, contentDescription = "电话客服")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(count = chatMessages.size, key = { index -> "msg_$index" }) { index ->
                MessageBubble(message = chatMessages[index])
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("常见问题", fontSize = 12.sp, color = TextGray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickQuestions.forEach { question ->
                    SuggestionChip(
                        onClick = {
                            messageText = question
                        },
                        label = { Text(question, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = BackgroundLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("输入消息...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = DividerColor
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                FilledIconButton(
                    onClick = {
                        if (messageText.isNotBlank() && viewModel != null) {
                            viewModel.sendCustomerServiceMessage(
                                content = messageText,
                                onSuccess = {
                                    messageText = ""
                                },
                                onError = { error ->
                                    android.util.Log.e("CustomerService", "发送失败: $error")
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BrandOrange
                    )
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUserType == 1) Arrangement.End else Arrangement.Start
    ) {
        if (message.fromUserType != 1) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BrandOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.fromUserType == 1) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.fromUserType == 1) 16.dp else 4.dp,
                    bottomEnd = if (message.fromUserType == 1) 4.dp else 16.dp
                ),
                color = if (message.fromUserType == 1) BrandOrange else Color.White
            ) {
                Text(
                    message.content ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = if (message.fromUserType == 1) Color.White else TextDark,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatTime(message.createTime),
                fontSize = 10.sp,
                color = TextHint
            )
        }

        if (message.fromUserType == 1) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatTime(time: String?): String {
    if (time.isNullOrEmpty()) return ""
    try {
        val dt = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val now = LocalDateTime.now()
        val diff = java.time.Duration.between(dt, now).toMinutes()
        
        return when {
            diff < 1 -> "刚刚"
            diff < 60 -> "${diff}分钟前"
            diff < 1440 -> "${diff / 60}小时前"
            else -> dt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        }
    } catch (e: Exception) {
        return time
    }
}
