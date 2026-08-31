package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DepartmentRequirementsProvider
import com.example.ui.components.JigawaPolyLogo

private val JSPDarkGreen = Color(0xFF0F5132)
private val JSPPrimaryGreen = Color(0xFF198754)
private val JSPAccentEmerald = Color(0xFF20C997)
private val JSPDeepNavy = Color(0xFF0C2340)

@Composable
fun AuthScreen(
    onLogin: (matric: String, pin: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onRegister: (
        matric: String,
        fullName: String,
        email: String,
        department: String,
        level: String,
        session: String,
        pin: String,
        onResult: (Boolean, String) -> Unit
    ) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JSPDeepNavy,
                        JSPDarkGreen,
                        Color(0xFF08331E)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = width * 0.45f,
                center = Offset(width * 0.95f, height * 0.08f)
            )

            drawCircle(
                color = JSPAccentEmerald.copy(alpha = 0.08f),
                radius = width * 0.35f,
                center = Offset(width * 0.05f, height * 0.96f)
            )

            val path = Path().apply {
                val cx = width * 0.88f
                val cy = height * 0.88f
                val s = width * 0.22f
                moveTo(cx, cy - s)
                lineTo(cx + s, cy)
                lineTo(cx, cy + s)
                lineTo(cx - s, cy)
                close()
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.06f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Header Logo & Institutional Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
            ) {
                JigawaPolyLogo(
                    size = 76.dp,
                    showBorder = true,
                    elevation = 6.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "JIGAWA STATE POLYTECHNIC",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "DUTSE, JIGAWA STATE - NIGERIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JSPAccentEmerald,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Digital Student Clearance & Graduation System",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color.Black.copy(alpha = 0.4f)
                        )
                        .testTag("auth_card_container"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AnimatedContent(
                        targetState = isSignUpMode,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally { it } + fadeIn()).togetherWith(
                                    slideOutHorizontally { -it } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { -it } + fadeIn()).togetherWith(
                                    slideOutHorizontally { it } + fadeOut()
                                )
                            }
                        },
                        label = "auth_form_transition"
                    ) { signUp ->
                        if (signUp) {
                            SignUpForm(
                                onNavigateToLogin = { isSignUpMode = false },
                                onRegisterSubmit = { matric, name, email, dept, lvl, sess, pin, cb ->
                                    onRegister(matric, name, email, dept, lvl, sess, pin, cb)
                                }
                            )
                        } else {
                            LoginForm(
                                onNavigateToSignUp = { isSignUpMode = true },
                                onLoginSubmit = { matric, pin, cb ->
                                    onLogin(matric, pin, cb)
                                }
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
fun LoginForm(
    onNavigateToSignUp: () -> Unit,
    onLoginSubmit: (matric: String, pin: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
    var matricNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Student Login",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = JSPDarkGreen,
            modifier = Modifier.testTag("login_title")
        )

        Text(
            text = "Access your verified clearance profile & records",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Matriculation Number
        Text(
            text = "Matriculation Number",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = matricNo,
            onValueChange = {
                matricNo = it
                errorMessage = null
            },
            placeholder = {
                Text(text = "e.g. JSP/ND/CS/23/001", color = Color(0xFF94A3B8), fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = JSPPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = JSPPrimaryGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF0F172A)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_username_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password / PIN
        Text(
            text = "Clearance PIN / Password",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            placeholder = {
                Text(text = "Enter your secret PIN", color = Color(0xFF94A3B8), fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = JSPPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = JSPPrimaryGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF0F172A)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Login Button
        Button(
            onClick = {
                if (matricNo.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter your Matriculation Number and PIN."
                    return@Button
                }
                isLoading = true
                errorMessage = null
                onLoginSubmit(matricNo.trim(), password.trim()) { success, msg ->
                    isLoading = false
                    if (!success) {
                        errorMessage = msg
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(25.dp), spotColor = JSPDarkGreen)
                .testTag("login_submit_btn"),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JSPDarkGreen,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Don't have account? SignUp footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unregistered student? ",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Text(
                text = "Register Now",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = JSPDarkGreen,
                modifier = Modifier
                    .clickable { onNavigateToSignUp() }
                    .padding(4.dp)
                    .testTag("navigate_signup_btn")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpForm(
    onNavigateToLogin: () -> Unit,
    onRegisterSubmit: (
        matric: String,
        name: String,
        email: String,
        dept: String,
        lvl: String,
        sess: String,
        pin: String,
        onResult: (Boolean, String) -> Unit
    ) -> Unit
) {
    var matricNo by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var department by remember { mutableStateOf(DepartmentRequirementsProvider.jigawaPolyDepartments.first()) }
    var level by remember { mutableStateOf("ND II") }
    var session by remember { mutableStateOf("2023/2024") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isDeptDropdownExpanded by remember { mutableStateOf(false) }
    var isLevelDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val academicLevels = listOf("ND I", "ND II", "HND I", "HND II")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Student Registration",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = JSPDarkGreen,
            modifier = Modifier.testTag("signup_title")
        )

        Text(
            text = "Register your institutional student record on JSP Portal",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Matriculation Number
        Text(
            text = "Matriculation Number *",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = matricNo,
            onValueChange = {
                matricNo = it
                errorMessage = null
            },
            placeholder = {
                Text(text = "e.g. JSP/ND/CS/23/001", color = Color(0xFF94A3B8), fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = JSPPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = JSPPrimaryGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_matric_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Full Name
        Text(
            text = "Full Name (Surname First) *",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                errorMessage = null
            },
            placeholder = {
                Text(text = "e.g. Usman Ibrahim Sani", color = Color(0xFF94A3B8), fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = JSPPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = JSPPrimaryGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_name_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Department Selector
        Text(
            text = "Academic Department *",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = isDeptDropdownExpanded,
            onExpandedChange = { isDeptDropdownExpanded = !isDeptDropdownExpanded }
        ) {
            OutlinedTextField(
                value = department,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDeptDropdownExpanded) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = JSPPrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedBorderColor = JSPPrimaryGreen,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("signup_dept_input")
            )
            ExposedDropdownMenu(
                expanded = isDeptDropdownExpanded,
                onDismissRequest = { isDeptDropdownExpanded = false }
            ) {
                DepartmentRequirementsProvider.jigawaPolyDepartments.forEach { deptName ->
                    DropdownMenuItem(
                        text = { Text(deptName, fontSize = 13.sp) },
                        onClick = {
                            department = deptName
                            isDeptDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level and Session
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = isLevelDropdownExpanded,
                    onExpandedChange = { isLevelDropdownExpanded = !isLevelDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = level,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLevelDropdownExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = JSPPrimaryGreen,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isLevelDropdownExpanded,
                        onDismissRequest = { isLevelDropdownExpanded = false }
                    ) {
                        academicLevels.forEach { lvl ->
                            DropdownMenuItem(
                                text = { Text(lvl, fontSize = 13.sp) },
                                onClick = {
                                    level = lvl
                                    isLevelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Session *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = session,
                    onValueChange = { session = it },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = JSPPrimaryGreen,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Institutional Email
        Text(
            text = "Institutional / Active Email *",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            placeholder = {
                Text(text = "student@jigawapoly.edu.ng", color = Color(0xFF94A3B8), fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AlternateEmail,
                    contentDescription = null,
                    tint = JSPPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = JSPPrimaryGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_email_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Create PIN
        Text(
            text = "Create Password / PIN *",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            placeholder = {
                Text(text = "Minimum 4 characters", color = Color(0xFF94A3B8), fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = JSPPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = JSPPrimaryGreen,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_password_input")
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Register button
        Button(
            onClick = {
                if (matricNo.isBlank() || fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all required fields marked with *."
                    return@Button
                }
                if (password.length < 4) {
                    errorMessage = "Password / PIN must be at least 4 characters."
                    return@Button
                }
                isLoading = true
                errorMessage = null
                onRegisterSubmit(
                    matricNo.trim(),
                    fullName.trim(),
                    email.trim(),
                    department.trim(),
                    level.trim(),
                    session.trim(),
                    password.trim()
                ) { success, msg ->
                    isLoading = false
                    if (!success) {
                        errorMessage = msg
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(25.dp), spotColor = JSPDarkGreen)
                .testTag("signup_register_btn"),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JSPDarkGreen,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "Complete Registration",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Already have account? Login footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already registered? ",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Text(
                text = "Log In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = JSPDarkGreen,
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
                    .padding(4.dp)
                    .testTag("navigate_login_btn")
            )
        }
    }
}

