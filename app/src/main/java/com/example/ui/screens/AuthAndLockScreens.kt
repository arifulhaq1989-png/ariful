package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.LocalAppStrings
import com.example.ui.components.LanguageTogglePill

@Composable
fun AuthScreen(
    isRegistered: Boolean,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onRegister: (owner: String, business: String, emailOrPhone: String, password: String) -> Unit,
    onLogin: (emailOrPhone: String, password: String) -> Boolean,
    onQuickStart: () -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedTab by remember(isRegistered) { mutableIntStateOf(if (isRegistered) 1 else 0) }
    var ownerName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                LanguageTogglePill(language = language, onToggle = onToggleLanguage)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 520.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_bizsphere_hero),
                    contentDescription = strings.welcomeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = strings.welcomeTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.welcomeSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(14.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    errorMessage = null
                                },
                                text = { Text(strings.registerTab) },
                                modifier = Modifier.testTag("auth_register_tab")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    errorMessage = null
                                },
                                text = { Text(strings.loginTab) },
                                modifier = Modifier.testTag("auth_login_tab")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(visible = selectedTab == 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = ownerName,
                                    onValueChange = { ownerName = it },
                                    label = { Text(strings.fullName) },
                                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_owner_input")
                                )
                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = { businessName = it },
                                    label = { Text(strings.businessName) },
                                    leadingIcon = { Icon(Icons.Rounded.Business, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_business_input")
                                )
                            }
                        }

                        if (selectedTab == 0) Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = { emailOrPhone = it; errorMessage = null },
                            label = { Text(strings.emailOrPhone) },
                            leadingIcon = { Icon(Icons.Rounded.PhoneAndroid, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_identifier_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text(strings.password) },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    if (ownerName.isBlank() || businessName.isBlank() || emailOrPhone.isBlank() || password.length < 4) {
                                        errorMessage = strings.fillRequiredFields
                                    } else {
                                        onRegister(ownerName, businessName, emailOrPhone, password)
                                    }
                                } else {
                                    if (emailOrPhone.isBlank() || password.isBlank()) {
                                        errorMessage = strings.fillRequiredFields
                                    } else if (!onLogin(emailOrPhone, password)) {
                                        errorMessage = strings.invalidCredentials
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("auth_submit_button")
                        ) {
                            Text(
                                text = if (selectedTab == 0) strings.createAccount else strings.signIn,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onQuickStart,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("quick_start_button")
                ) {
                    Text(strings.continueOffline)
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(
    isRegistered: Boolean,
    onVerifyPin: (String) -> Boolean,
    onUnlockWithPassword: (String) -> Boolean
) {
    val strings = LocalAppStrings.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryPassword by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf(false) }

    fun appendDigit(digit: String) {
        if (enteredPin.length < 4) {
            isError = false
            val next = enteredPin + digit
            enteredPin = next
            if (next.length == 4) {
                if (!onVerifyPin(next)) {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = strings.enterPinTitle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = strings.enterPinTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.enterPinSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                repeat(4) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> MaterialTheme.colorScheme.error
                                    filled -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                1.5.dp,
                                if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }

            if (isError) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.wrongPin,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        row.forEach { key ->
                            when (key) {
                                "" -> Spacer(modifier = Modifier.size(72.dp))
                                "DEL" -> {
                                    Surface(
                                        onClick = {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .testTag("pin_backspace")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                                contentDescription = "Backspace",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        onClick = { appendDigit(key) },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant,
                                                CircleShape
                                            )
                                            .testTag("pin_key_$key")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isRegistered) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = { showRecoveryDialog = true }) {
                    Text(strings.forgotPin)
                }
            }
        }
    }

    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text(strings.forgotPin) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.pinLockedOutHint, style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = recoveryPassword,
                        onValueChange = { recoveryPassword = it; recoveryError = false },
                        label = { Text(strings.password) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = recoveryError
                    )
                    if (recoveryError) {
                        Text(
                            text = strings.invalidCredentials,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (onUnlockWithPassword(recoveryPassword)) {
                        showRecoveryDialog = false
                    } else {
                        recoveryError = true
                    }
                }) {
                    Text(strings.signIn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
