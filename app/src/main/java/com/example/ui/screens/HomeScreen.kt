package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier,
    firebaseConfigured: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF004D42),
                        Color(0xFF006B5B),
                        Color(0xFFEAF6F3),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Subtle Nursing Heartbeat ECG & Medical Cross Canvas Pattern
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            val w = size.width
            val h = size.height

            // Soft circular halo
            drawCircle(
                color = Color(0x18FFFFFF),
                radius = w * 0.36f,
                center = Offset(w * 0.85f, h * 0.18f)
            )
            drawCircle(
                color = Color(0x12A7F3DF),
                radius = w * 0.24f,
                center = Offset(w * 0.12f, h * 0.42f)
            )

            // Subtle Medical Cross in top-left watermark
            val crossCenter = Offset(w * 0.15f, h * 0.22f)
            val arm = 22.dp.toPx()
            val thick = 7.dp.toPx()
            drawLine(
                color = Color(0x20FFFFFF),
                start = Offset(crossCenter.x - arm, crossCenter.y),
                end = Offset(crossCenter.x + arm, crossCenter.y),
                strokeWidth = thick,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0x20FFFFFF),
                start = Offset(crossCenter.x, crossCenter.y - arm),
                end = Offset(crossCenter.x, crossCenter.y + arm),
                strokeWidth = thick,
                cap = StrokeCap.Round
            )

            // ECG Heartbeat Pulse Line
            val pulseY = h * 0.62f
            val ecgPath = Path().apply {
                moveTo(0f, pulseY)
                lineTo(w * 0.22f, pulseY)
                lineTo(w * 0.27f, pulseY - 14.dp.toPx())
                lineTo(w * 0.31f, pulseY + 10.dp.toPx())
                lineTo(w * 0.36f, pulseY - 38.dp.toPx())
                lineTo(w * 0.41f, pulseY + 24.dp.toPx())
                lineTo(w * 0.46f, pulseY - 10.dp.toPx())
                lineTo(w * 0.50f, pulseY)
                lineTo(w * 0.72f, pulseY)
                lineTo(w * 0.76f, pulseY - 18.dp.toPx())
                lineTo(w * 0.80f, pulseY + 12.dp.toPx())
                lineTo(w * 0.84f, pulseY)
                lineTo(w, pulseY)
            }
            drawPath(
                path = ecgPath,
                color = Color(0x28A7F3DF),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Brand on left, Admin Button on top-right ("upore dan pashe")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x26FFFFFF)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_nurse_emblem),
                            contentDescription = "Nurses BD Logo",
                            modifier = Modifier
                                .padding(4.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Nurses BD",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Bangladesh Nursing Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD2F5EC)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAdminClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0x2EFFFFFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("home_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!firebaseConfigured) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firebase_missing_warning"),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Configuration Warning",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "এই APK-তে Firebase কনফিগ (google-services.json) যুক্ত নেই। লাইভ লগইন ও ডাটাবেজ চালু করতে AI Studio থেকে APK ডাউনলোড করুন অথবা GitHub Secrets-এ GOOGLE_SERVICES_JSON যোগ করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Nursing Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_banner),
                                contentDescription = stringResource(id = R.string.hero_banner_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x33003B32),
                                                Color(0xDD003B32)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0x36FFFFFF)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Favorite,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB4B4),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "সেবা, যত্ন ও পেশাগত ঐক্য • ৮ বিভাগ",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Bangladesh Nurses E-Directory",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "সারা বাংলাদেশের নিবন্ধিত নার্সদের ভেরিফায়েড নেটওয়ার্ক ও দ্রুত যোগাযোগ মাধ্যম",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFE0F7F1)
                                    )
                                }
                            }
                        }

                        // Protected Privacy & Nursing Highlights inside Hero Card
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "নিরাপত্তার স্বার্থে লগইন ছাড়া অন্য নার্সদের তথ্য ও সার্চ দেখা যাবে না। লগইন করে ডিরেক্টরি সার্চ করুন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NursingHighlightTile(
                                    icon = Icons.Filled.VerifiedUser,
                                    title = "BNMC Verified",
                                    subtitle = "অ্যাডমিন অনুমোদিত",
                                    modifier = Modifier.weight(1f)
                                )
                                NursingHighlightTile(
                                    icon = Icons.Filled.MedicalServices,
                                    title = "64 Districts",
                                    subtitle = "সারা বাংলাদেশ",
                                    modifier = Modifier.weight(1f)
                                )
                                NursingHighlightTile(
                                    icon = Icons.Filled.HealthAndSafety,
                                    title = "Quick Connect",
                                    subtitle = "কল ও হোয়াটসঅ্যাপ",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Primary Action Buttons: Login & Register
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "শুরু করুন (Get Started)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("home_login_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Nurse Login (লগইন করুন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("home_register_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAddAlt1,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Register New Nurse (নিবন্ধন করুন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun NursingHighlightTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
