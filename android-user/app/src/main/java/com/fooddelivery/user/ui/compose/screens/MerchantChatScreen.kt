package com.fooddelivery.user.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.fooddelivery.user.model.ChatMessage
import com.fooddelivery.user.ui.compose.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val brandOrange = Color(0xFFFF8C00)

data class MerchantChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date()
)

data class MerchantQuickQuestion(
    val id: String,
    val question: String
)

@Composable
fun MerchantChatScreen(
    merchantName: String = "商家客服",
    messages: List<ChatMessage> = emptyList(),
    onBack: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onReceiveMerchantMessage: (String) -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showQuickQuestions by remember { mutableStateOf(true) }
    var localMerchantMessages by remember { mutableStateOf(listOf<MerchantChatMessage>()) }
    
    // 将ChatMessage转换为MerchantChatMessage
    val chatMessages = remember(messages, localMerchantMessages) {
        val userMessages = messages.map { msg ->
            val timestamp = try {
                msg.createTime?.let { timeStr ->
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(timeStr)?.time?.let {
                        java.util.Date(it)
                    } ?: java.util.Date()
                } ?: java.util.Date()
            } catch (e: Exception) {
                java.util.Date()
            }
            MerchantChatMessage(
                id = msg.id?.toString() ?: System.currentTimeMillis().toString(),
                content = msg.content ?: "",
                isFromUser = msg.fromUserType == 1,
                timestamp = timestamp
            )
        }
        userMessages + localMerchantMessages
    }
    
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // 商家常见问题
    val quickQuestions = remember {
        listOf(
            MerchantQuickQuestion("1", "菜品口味如何？"),
            MerchantQuickQuestion("2", "可以加辣吗？"),
            MerchantQuickQuestion("3", "分量够不够？"),
            MerchantQuickQuestion("4", "什么时候能送到？"),
            MerchantQuickQuestion("5", "可以取消订单吗？"),
            MerchantQuickQuestion("6", "如何退款？")
        )
    }
    
    // 自动回复功能
    fun getAutoReply(userMessage: String): String {
        val msg = userMessage.lowercase()
        return when {
            msg.contains("辣") || msg.contains("辣") -> "您好，本店支持调整辣度，请在备注中注明您的辣度偏好，微辣/中辣/重辣均可选择。"
            msg.contains("口味") || msg.contains("味道") -> "本店采用新鲜食材烹饪，口味地道独特。如果您有特殊口味需求，可以在备注中说明。"
            msg.contains("分量") || msg.contains("够") -> "本店菜品分量充足，一般一份可以满足1-2人用餐。如果您食量较大，建议多点一份。"
            msg.contains("送") || msg.contains("时间") || msg.contains("多久") -> "一般配送时间为30-50分钟，具体时间受距离和天气影响。您可以实时查看骑手位置。"
            msg.contains("取消") -> "订单未接单前可取消，已接单后请联系商家协商取消。"
            msg.contains("退款") -> "退款申请请联系商家协商处理，商家同意后会自动退款到原支付账户。"
            msg.contains("谢谢") || msg.contains("感谢") -> "不客气！祝您用餐愉快！欢迎下次光临！"
            msg.contains("你好") || msg.contains("您好") -> "您好！请问有什么可以帮您的？"
            else -> "感谢您的留言，我们会尽快回复您。如有紧急问题请联系客服电话。"
        }
    }
    
    // 处理发送消息
    fun handleSendMessage(content: String) {
        if (content.isNotBlank()) {
            onSendMessage(content)
            
            // 隐藏快捷问题
            showQuickQuestions = false
            
            // 自动回复 - 添加到本地消息列表
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val reply = getAutoReply(content)
                val newMerchantMsg = MerchantChatMessage(
                    id = System.currentTimeMillis().toString() + "_auto",
                    content = reply,
                    isFromUser = false, // 商家消息显示在左侧
                    timestamp = java.util.Date()
                )
                localMerchantMessages = localMerchantMessages + newMerchantMsg
                
                // 同时也通过回调通知
                onReceiveMerchantMessage(reply)
            }, 1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Store,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        merchantName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("在线", fontSize = 12.sp, color = SuccessGreen)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂无消息，开始对话吧",
                            color = TextHint,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(count = chatMessages.size, key = { index -> "chat_msg_$index" }) { index ->
                    ChatMessageItem(
                        message = chatMessages[index],
                        timeFormat = dateFormat
                    )
                }
            }
        }

        // 快捷问题区域
        if (showQuickQuestions && chatMessages.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "常见问题",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickQuestions.forEach { question ->
                            SuggestionChip(
                                onClick = { handleSendMessage(question.question) },
                                label = { Text(question.question, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = BackgroundLight
                                )
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showQuickQuestions = !showQuickQuestions }) {
                    Icon(
                        if (showQuickQuestions) Icons.Filled.KeyboardArrowDown else Icons.Filled.Add, 
                        contentDescription = "快捷问题", 
                        tint = Color.Gray
                    )
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...", color = Color.Gray) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandOrange,
                        unfocusedBorderColor = DividerColor
                    ),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        handleSendMessage(messageText)
                        messageText = ""
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(brandOrange, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: MerchantChatMessage,
    timeFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = brandOrange.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Store,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                ),
                color = if (message.isFromUser) brandOrange else Color.White,
                shadowElevation = 1.dp
            ) {
                Text(
                    message.content,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    color = if (message.isFromUser) Color.White else TextDark
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                timeFormat.format(message.timestamp),
                fontSize = 10.sp,
                color = TextHint
            )
        }
        
        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.LightGray
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
