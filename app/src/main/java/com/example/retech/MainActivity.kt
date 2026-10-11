package com.example.retech

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.annotation.OptIn
import kotlinx.coroutines.delay

private val EcoGreen = Color(0xFF21BE79)
private val DarkGreen = Color(0xFF087653)
private val EcoBlue = Color(0xFF1976D2)
private val PageBackground = Color(0xFFF8FBF9)
private val BorderColor = Color(0xFFE7EFEB)

/** Returns the currently selected app language: en, hi, or mr. */
private fun currentLanguageTag(): String {
    val selected = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
        .substringBefore(',')
        .substringBefore('-')
    val code = selected.ifBlank { java.util.Locale.getDefault().language }
    return when (code) {
        "hi" -> "hi"
        "mr" -> "mr"
        else -> "en"
    }
}

/** Localized text used throughout this single-file prototype. */
private fun tr(en: String, hi: String, mr: String): String = when (currentLanguageTag()) {
    "hi" -> hi
    "mr" -> mr
    else -> en
}

private enum class MainTab { HOME, MAP, SCAN, REWARDS, PROFILE }

private data class RecentActivity(
    val kind: String,
    val timeKind: String,
    val points: Int,
    val icon: String
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = DarkGreen,
                    secondary = EcoGreen,
                    background = PageBackground
                )
            ) {
                ReTechApp()
            }
        }
    }
}

@Composable
fun ReTechApp() {
    var route by rememberSaveable { mutableStateOf("splash") }
    var language by rememberSaveable { mutableStateOf(currentLanguageTag()) }
    var email by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME.name) }
    var bottles by rememberSaveable { mutableIntStateOf(847) }
    var points by rememberSaveable { mutableIntStateOf(2475) }
    var activities by remember {
        mutableStateOf(
            listOf(
                RecentActivity("plastic", "today", 25, "♻"),
                RecentActivity("can", "yesterday", 15, "▤"),
                RecentActivity("plastic", "date", 25, "♻")
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(900)
        if (route == "splash") route = "welcome"
    }

    when (route) {
        "splash" -> SplashScreen()
        "welcome" -> WelcomeScreen { route = "language" }
        "language" -> LanguageScreen(
            language = language,
            onSelect = { tag ->
                language = tag
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            },
            onContinue = { route = "login" }
        )
        "login" -> LoginScreen(
            email = email,
            onEmailChange = { email = it },
            onLogin = { route = "main" }
        )
        else -> MainApp(
            selectedTab = runCatching { MainTab.valueOf(selectedTab) }.getOrDefault(MainTab.HOME),
            onTabSelected = { selectedTab = it.name },
            bottles = bottles,
            points = points,
            activities = activities,
            onDeposit = {
                bottles += 1
                points += 25
                activities = listOf(RecentActivity("plastic", "now", 25, "♻")) + activities.take(2)
            },
            onRedeem = { cost ->
                if (points >= cost) {
                    points -= cost
                    true
                } else false
            },
            onSignOut = { route = "login" }
        )
    }
}

@Composable
private fun SplashScreen() {
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE5F8EC), Color.White))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LogoMark(100)
            Spacer(Modifier.height(12.dp))
            Text("ReTech", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Text(tr("Recycle • Reward • Repeat", "रीसायकल करें • इनाम पाएं • फिर दोहराएं", "पुनर्वापर करा • बक्षीस मिळवा • पुन्हा करा"), color = Color.Gray)
        }
    }
}

@Composable
private fun LogoMark(size: Int) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(Color(0xFFE8F7EC)).border(1.5.dp, DarkGreen, CircleShape),
        contentAlignment = Alignment.Center
    ) { Text("♻", fontSize = (size * 0.48).sp, color = DarkGreen) }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.White).padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LogoMark(110)
        Spacer(Modifier.height(22.dp))
        Text(tr("Welcome to ReTech", "ReTech में आपका स्वागत है", "ReTech मध्ये आपले स्वागत आहे"), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = DarkGreen, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(tr("Living today without harming tomorrow.", "आज ऐसे जिएं कि आने वाला कल सुरक्षित रहे।", "आज असे जगा की उद्याचे भविष्य सुरक्षित राहील."), fontSize = 16.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
        Spacer(Modifier.height(38.dp))
        PrimaryButton(tr("Get Started", "शुरू करें", "सुरू करा"), onContinue)
    }
}

@Composable
private fun LanguageScreen(language: String, onSelect: (String) -> Unit, onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(tr("Choose your language", "अपनी भाषा चुनें", "तुमची भाषा निवडा"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
        Spacer(Modifier.height(8.dp))
        Text(tr("Select your preferred language.", "अपनी पसंदीदा भाषा चुनें।", "तुमची पसंतीची भाषा निवडा."), color = Color.DarkGray)
        Spacer(Modifier.height(25.dp))
        listOf("English" to "en", "हिन्दी" to "hi", "मराठी" to "mr").forEach { (label, tag) ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onSelect(tag) },
                colors = CardDefaults.cardColors(containerColor = if (language == tag) Color(0xFFE4F7EB) else Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (language == tag) EcoGreen else BorderColor)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 17.sp)
                    Spacer(Modifier.weight(1f))
                    if (language == tag) Text("✓", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        PrimaryButton(tr("Continue", "आगे बढ़ें", "पुढे चला"), onContinue)
    }
}

@Composable
private fun LoginScreen(email: String, onEmailChange: (String) -> Unit, onLogin: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(26.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(tr("Welcome back!", "वापसी पर स्वागत है!", "पुन्हा स्वागत आहे!"), fontSize = 31.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
        Spacer(Modifier.height(8.dp))
        Text(tr("Join the ReTech recycling community.", "ReTech रीसाइक्लिंग समुदाय से जुड़ें।", "ReTech पुनर्वापर समुदायात सामील व्हा."))
        Spacer(Modifier.height(25.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(tr("Email address", "ईमेल पता", "ईमेल पत्ता")) },
            placeholder = { Text(tr("Enter your email", "अपना ईमेल दर्ज करें", "तुमचा ईमेल टाका")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(22.dp))
        PrimaryButton(tr("Continue to ReTech", "ReTech पर आगे बढ़ें", "ReTech वर पुढे चला"), onLogin)
        Spacer(Modifier.height(10.dp))
        Text(
            tr("Prototype mode: account authentication is not connected yet.", "प्रोटोटाइप मोड: खाता लॉगिन अभी जुड़ा नहीं है।", "प्रोटोटाइप मोड: खातेचे प्रमाणीकरण अद्याप जोडलेले नाही."),
            fontSize = 12.sp, color = Color.Gray
        )
    }
}

@Composable
private fun MainApp(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    bottles: Int,
    points: Int,
    activities: List<RecentActivity>,
    onDeposit: () -> Unit,
    onRedeem: (Int) -> Boolean,
    onSignOut: () -> Unit
) {
    var assistantOpen by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            if (!assistantOpen) {
                NavigationBar(containerColor = Color.White) {
                    MainTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            icon = { Text(tabIcon(tab), fontSize = 23.sp, color = if (selectedTab == tab) DarkGreen else Color.Gray) },
                            label = { Text(tabLabel(tab), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkGreen,
                                selectedTextColor = DarkGreen,
                                indicatorColor = Color(0xFFDDF5E7),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(if (assistantOpen) PaddingValues(0.dp) else padding)) {
            if (assistantOpen) {
                AssistantScreen(onBack = { assistantOpen = false })
            } else {
                when (selectedTab) {
                    MainTab.HOME -> HomeDashboard(bottles, points, activities, onTabSelected, onAssistantClick = { assistantOpen = true })
                    MainTab.MAP -> MapScreen()
                    MainTab.SCAN -> ScanScreen(onDeposit)
                    MainTab.REWARDS -> RewardsScreen(bottles, points, onRedeem)
                    MainTab.PROFILE -> ProfileScreen(bottles, points, onSignOut)
                }
            }
        }
    }
}

private fun tabIcon(tab: MainTab): String = when (tab) {
    MainTab.HOME -> "⌂"
    MainTab.MAP -> "▧"
    MainTab.SCAN -> "◎"
    MainTab.REWARDS -> "♧"
    MainTab.PROFILE -> "♙"
}

private fun tabLabel(tab: MainTab): String = when (tab) {
    MainTab.HOME -> tr("Home", "होम", "मुख्यपृष्ठ")
    MainTab.MAP -> tr("Map", "मानचित्र", "नकाशा")
    MainTab.SCAN -> tr("Scan", "स्कैन", "स्कॅन")
    MainTab.REWARDS -> tr("Rewards", "इनाम", "बक्षिसे")
    MainTab.PROFILE -> tr("Profile", "प्रोफ़ाइल", "प्रोफाइल")
}

@Composable
private fun HomeDashboard(bottles: Int, points: Int, activities: List<RecentActivity>, onTabSelected: (MainTab) -> Unit, onAssistantClick: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LogoMark(68)
                Text("RETECH", color = DarkGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tr("Welcome back!", "वापसी पर स्वागत है!", "पुन्हा स्वागत आहे!"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(tr("Level 5 • Eco Warrior", "लेवल 5 • पर्यावरण रक्षक", "स्तर 5 • पर्यावरण रक्षक"), fontSize = 12.sp, color = Color.Gray)
            }
            Text("♧", fontSize = 25.sp, color = DarkGreen, modifier = Modifier.clickable { onTabSelected(MainTab.PROFILE) })
        }
        Spacer(Modifier.height(20.dp))
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Eco Impact", "पर्यावरण पर प्रभाव", "पर्यावरणीय प्रभाव"), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Text("ⓘ", color = DarkGreen)
            }
            Spacer(Modifier.height(18.dp))
            Row {
                ImpactMetric("$bottles", tr("Recycled", "रीसायकल की गईं", "पुनर्वापर केलेल्या"), Modifier.weight(1f))
                ImpactMetric("12.4 kg", tr("CO₂ Saved", "CO₂ बचाई गई", "CO₂ वाचवले"), Modifier.weight(1f))
                ImpactMetric("2.1", tr("Trees Eq.", "पेड़ों के बराबर", "झाडांच्या समतुल्य"), Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(18.dp))
        MachineDemoVideo(Modifier.fillMaxWidth().height(145.dp))
        Spacer(Modifier.height(18.dp))
        Card(
            Modifier.fillMaxWidth().clickable(onClick = onAssistantClick),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE5F7EC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7EBD5))
        ) {
            Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tr("Ask ReTech Assistant", "ReTech सहायक से पूछें", "ReTech सहाय्यकाला विचारा"), fontWeight = FontWeight.Bold, color = DarkGreen)
                    Text(tr("Get recycling tips and app guidance", "रीसाइक्लिंग सुझाव और ऐप की मदद पाएं", "पुनर्वापर टिप्स आणि ॲपची मदत मिळवा"), fontSize = 11.sp, color = Color.DarkGray)
                }
                Text("›", fontSize = 28.sp, color = DarkGreen)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Recent Activity", "हाल की गतिविधि", "अलीकडील क्रिया"), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(tr("See All", "सभी देखें", "सर्व पहा"), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onTabSelected(MainTab.PROFILE) })
        }
        Spacer(Modifier.height(10.dp))
        activities.take(3).forEach { ActivityCard(it) }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun ImpactMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun MachineDemoVideo(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            val uri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.machine_demo}")
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    val exoPlayer = player
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                this.player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        update = { view -> if (view.player !== exoPlayer) view.player = exoPlayer }
    )
}

@Composable
private fun ActivityCard(item: RecentActivity) {
    val title = when (item.kind) {
        "can" -> tr("Aluminum Can", "एल्युमिनियम कैन", "ॲल्युमिनियम कॅन")
        else -> tr("Plastic Bottle", "प्लास्टिक की बोतल", "प्लास्टिकची बाटली")
    }
    val time = when (item.timeKind) {
        "now" -> tr("Just now", "अभी-अभी", "आत्ताच")
        "yesterday" -> tr("Yesterday, 4:20 PM", "कल, शाम 4:20 बजे", "काल, संध्याकाळी 4:20")
        "date" -> tr("Oct 24, 2023", "24 अक्टूबर, 2023", "24 ऑक्टोबर, 2023")
        else -> tr("Today, 10:45 AM", "आज, सुबह 10:45 बजे", "आज, सकाळी 10:45")
    }
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE8F8F0)), contentAlignment = Alignment.Center) {
                Text(item.icon, fontSize = 21.sp, color = DarkGreen)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(time, fontSize = 10.sp, color = Color.Gray)
            }
            Text("+${item.points} ${tr("pts", "अंक", "गुण")}", color = EcoGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MapScreen() {
    var search by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(245.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color(0xFFE7F0E1))
                drawLine(Color.White, Offset(0f, size.height * 0.24f), Offset(size.width, size.height * 0.62f), strokeWidth = 28f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(size.width * 0.14f, 0f), Offset(size.width * 0.62f, size.height), strokeWidth = 21f, cap = StrokeCap.Round)
                drawLine(Color(0xFFB5D6EF), Offset(0f, size.height * 0.80f), Offset(size.width, size.height * 0.36f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(size.width * 0.85f, 0f), Offset(size.width * 0.32f, size.height), strokeWidth = 14f, cap = StrokeCap.Round)
            }
            Text("📍", Modifier.align(Alignment.Center).offset(x = 20.dp, y = 20.dp), fontSize = 30.sp)
            Text("♻️", Modifier.align(Alignment.Center).offset(x = (-68).dp, y = (-24).dp), fontSize = 25.sp)
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(12.dp),
                placeholder = { Text(tr("Search for RVMs...", "रीसाइक्लिंग मशीन खोजें...", "पुनर्वापर मशीन शोधा..."), fontSize = 13.sp) },
                singleLine = true, shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
        }
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Nearby RVMs", "आस-पास की रीसाइक्लिंग मशीनें", "जवळच्या पुनर्वापर मशीन"), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(tr("Demo locations", "डेमो स्थान", "डेमो ठिकाणे"), fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))
            RvmCard("RVM #12 – Central Mall", tr("Level 2, Food Court", "दूसरी मंज़िल, फूड कोर्ट", "दुसरा मजला, फूड कोर्ट"), "operational")
            RvmCard("RVM #08 – Green Park", tr("Main Entrance Plaza", "मुख्य प्रवेश द्वार", "मुख्य प्रवेशद्वार"), "operational")
            RvmCard("RVM #15 – City Center", tr("North Gate, Near ATM", "उत्तर द्वार, ATM के पास", "उत्तर गेट, ATM जवळ"), "almost")
            Spacer(Modifier.height(8.dp))
            Text(tr("Map and machine statuses are sample data. Live location is not connected.", "नक्शा और मशीन की स्थिति नमूना डेटा है। लाइव लोकेशन अभी जुड़ी नहीं है।", "नकाशा आणि मशीनची स्थिती नमुना माहिती आहे. प्रत्यक्ष लोकेशन जोडलेले नाही."), fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun RvmCard(name: String, place: String, status: String) {
    val statusText = if (status == "almost") tr("Almost Full", "लगभग भरी हुई", "जवळपास भरलेली") else tr("Operational", "चालू है", "कार्यरत")
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("♻", color = EcoGreen, fontSize = 26.sp)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(place, color = Color.Gray, fontSize = 11.sp)
                Text("• $statusText", fontSize = 11.sp, color = if (status == "almost") Color(0xFFE39A19) else EcoGreen)
            }
            Text("◆", color = EcoGreen)
        }
    }
}

@Composable
private fun ScanScreen(onDeposit: () -> Unit) {
    var scanComplete by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("‹     ${tr("Scan", "स्कैन", "स्कॅन")}                         ⓘ", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepLabel("1", tr("Position", "स्थिति", "स्थिती"), true, Modifier.weight(1f))
            Box(Modifier.weight(0.5f).height(1.dp).background(BorderColor))
            StepLabel("2", tr("Verify", "जांचें", "पडताळा"), scanComplete, Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth().height(295.dp).clip(RoundedCornerShape(25.dp)).background(Brush.verticalGradient(listOf(Color(0xFF262C2B), Color(0xFF53635A)))), contentAlignment = Alignment.Center) {
            Text(if (scanComplete) "✅" else "", fontSize = 76.sp)
            Box(Modifier.fillMaxWidth(0.70f).height(175.dp).border(1.5.dp, EcoGreen, RoundedCornerShape(18.dp)))
            Box(Modifier.fillMaxWidth(0.65f).height(3.dp).background(EcoGreen))
            Text(
                if (scanComplete) tr("Bottle verified (demo)", "बोतल सत्यापित (डेमो)", "बाटली पडताळली (डेमो)") else tr("◎  Align bottle within frame", "◎  बोतल को फ्रेम में रखें", "◎  बाटली चौकटीत ठेवा"),
                Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).clip(RoundedCornerShape(30.dp)).background(Color(0xCC171A19)).padding(horizontal = 15.dp, vertical = 9.dp),
                color = Color.White, fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(18.dp))
        SectionCard {
            Text("ⓘ   ${tr("Tips for better scanning", "बेहतर स्कैनिंग के सुझाव", "चांगल्या स्कॅनिंगसाठी टिप्स")}", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(7.dp))
            Text(tr("Ensure good lighting and remove any labels or caps for the most accurate detection.", "सही पहचान के लिए पर्याप्त रोशनी रखें और लेबल या ढक्कन हटा दें।", "अचूक ओळखीसाठी पुरेसा प्रकाश ठेवा आणि लेबल किंवा झाकण काढा."), fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(if (scanComplete) tr("Scan Another Bottle", "दूसरी बोतल स्कैन करें", "दुसरी बाटली स्कॅन करा") else tr("▣  Start Scanning", "▣  स्कैन शुरू करें", "▣  स्कॅन सुरू करा")) {
            if (!scanComplete) { onDeposit(); scanComplete = true } else scanComplete = false
        }
        Spacer(Modifier.height(10.dp))
        Text(if (scanComplete) tr("Demo deposit added: +25 points", "डेमो जमा हुआ: +25 अंक", "डेमो जमा: +25 गुण") else tr("Camera and QR/barcode verification will be connected later.", "कैमरा और QR/बारकोड सत्यापन बाद में जोड़ा जाएगा।", "कॅमेरा आणि QR/बारकोड पडताळणी नंतर जोडली जाईल."), fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun StepLabel(number: String, label: String, active: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(if (active) EcoGreen else Color(0xFFEDF1EF)), contentAlignment = Alignment.Center) {
            Text(number, color = if (active) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun RewardsScreen(bottles: Int, points: Int, onRedeem: (Int) -> Boolean) {
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text(tr("Your Rewards", "आपके इनाम", "तुमची बक्षिसे"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(tr("ReTech Rewards Catalog", "ReTech इनाम सूची", "ReTech बक्षीस सूची"), color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(165.dp).clip(RoundedCornerShape(23.dp)).background(Brush.horizontalGradient(listOf(DarkGreen, EcoGreen, Color(0xFF20C982)))).padding(20.dp)) {
            Column {
                Text(tr("Available Balance", "उपलब्ध अंक", "उपलब्ध गुण"), color = Color.White, fontSize = 13.sp)
                Text("$points ${tr("pts", "अंक", "गुण")}", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(14.dp))
                Text(tr("Next milestone: 2,750 points to Level 6", "अगला स्तर: लेवल 6 के लिए 2,750 अंक", "पुढील टप्पा: स्तर 6 साठी 2,750 गुण"), color = Color.White, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row {
            SmallImpact("♻", "$bottles", tr("Bottles", "बोतलें", "बाटल्या"), Modifier.weight(1f))
            SmallImpact("CO₂", "12.4 kg", tr("CO₂ Saved", "CO₂ बचाई", "CO₂ वाचवले"), Modifier.weight(1f))
            SmallImpact("♟", "2.1", tr("Trees Eq.", "पेड़ों के बराबर", "झाडांच्या समतुल्य"), Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Redeem Points", "अंक भुनाएं", "गुण वापरा"), fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Spacer(Modifier.weight(1f))
            Text(tr("History", "इतिहास", "इतिहास"), fontSize = 12.sp, color = DarkGreen)
        }
        Spacer(Modifier.height(10.dp))
        RewardOption("▣", tr("UPI Cashback", "UPI कैशबैक", "UPI कॅशबॅक"), tr("Direct transfer to UPI", "सीधे UPI में ट्रांसफर", "थेट UPI मध्ये हस्तांतरण"), 1500) {
            dialogMessage = redeemMessage(onRedeem(1500), 1500, false)
        }
        RewardOption("🎟", tr("Shopping Coupons", "शॉपिंग कूपन", "खरेदी कूपन"), tr("Participating stores", "भाग लेने वाली दुकानें", "सहभागी दुकाने"), 800) {
            dialogMessage = redeemMessage(onRedeem(800), 800, false)
        }
        RewardOption("🎁", tr("Gift Vouchers", "गिफ्ट वाउचर", "भेट व्हाउचर"), tr("Popular brands", "लोकप्रिय ब्रांड", "लोकप्रिय ब्रँड"), 2000) {
            dialogMessage = redeemMessage(onRedeem(2000), 2000, false)
        }
        RewardOption("♥", tr("Donate Points", "अंक दान करें", "गुण दान करा"), tr("Support green initiatives", "हरित पहलों का समर्थन करें", "हरित उपक्रमांना पाठिंबा द्या"), 500) {
            dialogMessage = redeemMessage(onRedeem(500), 500, true)
        }
        Spacer(Modifier.height(10.dp))
        SectionCard {
            Text("✦   ${tr("Eco Tip", "पर्यावरण सुझाव", "पर्यावरण टिप")}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(tr("Every responsible recycling action helps reduce waste.", "जिम्मेदारी से रीसाइक्लिंग करने पर कचरा कम होता है।", "जबाबदारीने पुनर्वापर केल्याने कचरा कमी होतो."), fontSize = 12.sp, color = Color.Gray)
        }
    }
    dialogMessage?.let { message ->
        AlertDialog(onDismissRequest = { dialogMessage = null }, title = { Text(tr("ReTech Rewards", "ReTech इनाम", "ReTech बक्षिसे")) }, text = { Text(message) }, confirmButton = { TextButton(onClick = { dialogMessage = null }) { Text(tr("OK", "ठीक है", "ठीक आहे")) } })
    }
}

private fun redeemMessage(success: Boolean, cost: Int, donation: Boolean): String = when {
    !success -> tr("You don't have enough points for this reward.", "इस इनाम के लिए आपके पास पर्याप्त अंक नहीं हैं।", "या बक्षिसासाठी तुमच्याकडे पुरेसे गुण नाहीत.")
    donation -> tr("Demo donation recorded! $cost points used.", "डेमो दान दर्ज हुआ! $cost अंक उपयोग किए गए।", "डेमो देणगी नोंदवली! $cost गुण वापरले.")
    else -> tr("Demo redemption successful! $cost points used.", "डेमो रिडेम्प्शन सफल! $cost अंक उपयोग किए गए।", "डेमो रिडेम्प्शन यशस्वी! $cost गुण वापरले.")
}

@Composable
private fun SmallImpact(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier.padding(3.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 3.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, color = EcoGreen, fontSize = 18.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(label, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RewardOption(icon: String, title: String, description: String, cost: Int, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 26.sp, color = EcoGreen)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(description, color = Color.Gray, fontSize = 10.sp)
                Text("$cost ${tr("pts", "अंक", "गुण")}", color = EcoGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)) { Text(tr("Redeem", "भुनाएं", "वापरा"), fontSize = 11.sp) }
        }
    }
}

@Composable
private fun ProfileScreen(bottles: Int, points: Int, onSignOut: () -> Unit) {
    var settingMessage by remember { mutableStateOf<String?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Box(Modifier.fillMaxWidth().height(135.dp).clip(RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(EcoGreen, DarkGreen))))
        Column(Modifier.fillMaxWidth().offset(y = (-42).dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(88.dp).clip(CircleShape).background(Color(0xFFE5F7EC)).border(3.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text("🧑‍🌾", fontSize = 43.sp) }
            Text("ReTech User", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(tr("Eco Warrior • Level 5", "पर्यावरण रक्षक • लेवल 5", "पर्यावरण रक्षक • स्तर 5"), color = Color.Gray, fontSize = 12.sp)
        }
        Card(Modifier.fillMaxWidth().offset(y = (-25).dp), colors = CardDefaults.cardColors(containerColor = EcoGreen), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text(tr("Total Balance", "कुल बैलेंस", "एकूण शिल्लक"), color = Color.White, fontSize = 13.sp)
                Text("$points ${tr("Points", "अंक", "गुण")}", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.offset(y = (-10).dp)) {
            Text(tr("Environmental Impact", "पर्यावरण पर प्रभाव", "पर्यावरणीय प्रभाव"), fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                ProfileMetric("♻", "$bottles", tr("Bottles Recycled", "रीसायकल की गई बोतलें", "पुनर्वापर केलेल्या बाटल्या"), Modifier.weight(1f))
                ProfileMetric("CO₂", "12.4 kg", tr("CO₂ Saved", "CO₂ बचाई", "CO₂ वाचवले"), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                ProfileMetric("🌲", "2.1", tr("Trees Equivalent", "पेड़ों के बराबर", "झाडांच्या समतुल्य"), Modifier.weight(1f))
                ProfileMetric("💧", "420 L", tr("Water Saved", "पानी बचाया", "पाणी वाचवले"), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text(tr("Account Settings", "खाता सेटिंग्स", "खाते सेटिंग्ज"), fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            SectionCard {
                val settings = listOf(
                    tr("Personal Information", "व्यक्तिगत जानकारी", "वैयक्तिक माहिती"),
                    tr("Payout Methods", "भुगतान के तरीके", "पेमेंट पद्धती"),
                    tr("Recycling History", "रीसाइक्लिंग इतिहास", "पुनर्वापर इतिहास"),
                    tr("Notification Preferences", "सूचना प्राथमिकताएं", "सूचना प्राधान्ये"),
                    tr("App Language", "ऐप की भाषा", "ॲपची भाषा"),
                    tr("Help & Support", "सहायता और समर्थन", "मदत आणि समर्थन")
                )
                settings.forEachIndexed { index, setting ->
                    Row(Modifier.fillMaxWidth().clickable {
                        if (index == 4) showLanguageDialog = true else settingMessage = setting
                    }.padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(listOf("♙", "▣", "◷", "♧", "文", "ⓘ")[index], color = DarkGreen, fontSize = 19.sp)
                        Spacer(Modifier.width(14.dp))
                        Text(setting, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("›", color = Color.Gray, fontSize = 23.sp)
                    }
                    if (index < settings.lastIndex) HorizontalDivider(color = BorderColor)
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("↪   ${tr("Sign Out", "लॉग आउट", "साइन आउट")}") }
        }
    }
    settingMessage?.let { setting ->
        AlertDialog(onDismissRequest = { settingMessage = null }, title = { Text(setting) }, text = { Text(tr("This feature will be connected in a future ReTech version.", "यह सुविधा ReTech के अगले संस्करण में जोड़ी जाएगी।", "हे वैशिष्ट्य ReTech च्या पुढील आवृत्तीत जोडले जाईल.")) }, confirmButton = { TextButton(onClick = { settingMessage = null }) { Text(tr("OK", "ठीक है", "ठीक आहे")) } })
    }
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(tr("Choose app language", "ऐप की भाषा चुनें", "ॲपची भाषा निवडा")) },
            text = {
                Column {
                    listOf("English" to "en", "हिन्दी" to "hi", "मराठी" to "mr").forEach { (label, tag) ->
                        Text(label, Modifier.fillMaxWidth().clickable {
                            showLanguageDialog = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }.padding(14.dp), color = DarkGreen, fontSize = 17.sp)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text(tr("Close", "बंद करें", "बंद करा")) } }
        )
    }
}

@Composable
private fun ProfileMetric(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier.padding(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(icon, color = EcoGreen, fontSize = 19.sp)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

/** UI prototype only: responses below are predefined. Connect an AI backend for real generated answers. */
@Composable
private fun AssistantScreen(onBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(false, tr("Hi! I'm the ReTech Assistant. Ask me about recycling, points, or using the app.", "नमस्ते! मैं ReTech सहायक हूँ। रीसाइक्लिंग, अंकों या ऐप के उपयोग के बारे में पूछें।", "नमस्कार! मी ReTech सहाय्यक आहे. पुनर्वापर, गुण किंवा ॲप वापरण्याबद्दल विचारा."))
            )
        )
    }
    val suggestions = listOf(
        tr("How do I recycle a bottle?", "मैं बोतल को रीसायकल कैसे करूं?", "मी बाटलीचा पुनर्वापर कसा करू?"),
        tr("How do I earn points?", "मुझे अंक कैसे मिलेंगे?", "मला गुण कसे मिळतील?"),
        tr("Where can I find a machine?", "मशीन कहां मिलेगी?", "मशीन कुठे मिळेल?")
    )
    fun ask(question: String) {
        if (question.isBlank()) return
        val reply = assistantDemoAnswer(question)
        messages = messages + ChatMessage(true, question.trim()) + ChatMessage(false, reply)
        input = ""
    }
    Column(Modifier.fillMaxSize().background(PageBackground)) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clickable(onClick = onBack).padding(end = 16.dp), fontSize = 30.sp, color = DarkGreen)
            Column(Modifier.weight(1f)) {
                Text(tr("ReTech Assistant", "ReTech सहायक", "ReTech सहाय्यक"), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = DarkGreen)
                Text(tr("Recycling help", "रीसाइक्लिंग सहायता", "पुनर्वापर मदत"), color = Color.Gray, fontSize = 11.sp)
            }
            Text("♻", fontSize = 25.sp, color = DarkGreen)
        }
        Text(tr("Prototype assistant: replies are prewritten; live AI is not connected yet.", "प्रोटोटाइप सहायक: उत्तर पहले से लिखे गए हैं; लाइव AI अभी जुड़ा नहीं है।", "प्रोटोटाइप सहाय्यक: उत्तरे आधीच तयार आहेत; लाईव्ह AI अद्याप जोडलेले नाही."), Modifier.fillMaxWidth().background(Color(0xFFEAF6EE)).padding(10.dp), color = DarkGreen, fontSize = 11.sp, textAlign = TextAlign.Center)
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(messages) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
                    Box(Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(16.dp)).background(if (message.fromUser) DarkGreen else Color.White).padding(13.dp)) {
                        Text(message.text, color = if (message.fromUser) Color.White else Color(0xFF26332C), fontSize = 14.sp)
                    }
                }
            }
        }
        if (messages.size <= 1) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                suggestions.forEach { suggestion ->
                    OutlinedButton(onClick = { ask(suggestion) }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), contentPadding = PaddingValues(10.dp)) { Text(suggestion, fontSize = 12.sp) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Color.White).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text(tr("Ask a question...", "अपना सवाल लिखें...", "तुमचा प्रश्न लिहा...")) }, shape = RoundedCornerShape(24.dp), maxLines = 3)
            Spacer(Modifier.width(8.dp))
            Button(onClick = { ask(input) }, shape = CircleShape, contentPadding = PaddingValues(14.dp)) { Text("➤") }
        }
    }
}

private data class ChatMessage(val fromUser: Boolean, val text: String)

private fun assistantDemoAnswer(question: String): String {
    val q = question.lowercase()
    return when {
        listOf("point", "reward", "अंक", "इनाम", "गुण", "बक्षीस").any { q.contains(it) } -> tr(
            "In this demo, scanning a bottle adds 25 points. Visit Rewards to see sample redemption options. Actual rewards are not connected to a live service yet.",
            "इस डेमो में बोतल स्कैन करने पर 25 अंक मिलते हैं। नमूना इनाम देखने के लिए 'इनाम' खोलें। वास्तविक इनाम सेवा अभी जुड़ी नहीं है।",
            "या डेमोमध्ये बाटली स्कॅन केल्यावर 25 गुण मिळतात. नमुना बक्षिसांसाठी 'बक्षिसे' उघडा. प्रत्यक्ष बक्षीस सेवा अद्याप जोडलेली नाही."
        )
        listOf("where", "machine", "rvm", "कहां", "मशीन", "कुठे").any { q.contains(it) } -> tr(
            "Open the Map tab to view sample recycling-machine locations. These are demo locations; live GPS and machine status are not connected yet.",
            "नमूना रीसाइक्लिंग मशीनों के स्थान देखने के लिए Map टैब खोलें। ये डेमो स्थान हैं; लाइव GPS और मशीन की स्थिति अभी जुड़ी नहीं है।",
            "नमुना पुनर्वापर मशीनची ठिकाणे पाहण्यासाठी Map टॅब उघडा. ही डेमो ठिकाणे आहेत; प्रत्यक्ष GPS आणि मशीनची स्थिती अद्याप जोडलेली नाही."
        )
        listOf("bottle", "recycle", "scan", "बोतल", "रीसायकल", "स्कॅन", "बाटली", "पुनर्वापर").any { q.contains(it) } -> tr(
            "Open Scan and tap Start Scanning to try the demo. Camera and QR/barcode validation are not connected yet. In a real machine, follow the on-screen instructions and use only accepted empty containers.",
            "डेमो आज़माने के लिए Scan खोलें और स्कैन शुरू करें पर टैप करें। कैमरा और QR/बारकोड जांच अभी जुड़ी नहीं है। वास्तविक मशीन पर स्क्रीन के निर्देशों का पालन करें और केवल स्वीकार्य खाली कंटेनर डालें।",
            "डेमो वापरण्यासाठी Scan उघडा आणि स्कॅन सुरू करा वर टॅप करा. कॅमेरा आणि QR/बारकोड पडताळणी अद्याप जोडलेली नाही. प्रत्यक्ष मशीनवरील सूचना पाळा आणि फक्त स्वीकारल्या जाणाऱ्या रिकाम्या बाटल्या/कॅन वापरा."
        )
        else -> tr(
            "I can help with the demo's scanning, points, rewards, and map. Try asking how to recycle a bottle, how points work, or where to find a machine. This assistant is not connected to a live AI service yet.",
            "मैं डेमो में स्कैनिंग, अंक, इनाम और नक्शे के बारे में मदद कर सकता हूँ। बोतल रीसायकल करने, अंक पाने या मशीन खोजने के बारे में पूछें। यह सहायक अभी लाइव AI सेवा से जुड़ा नहीं है।",
            "मी डेमोमधील स्कॅनिंग, गुण, बक्षिसे आणि नकाशाबद्दल मदत करू शकतो. बाटलीचा पुनर्वापर, गुण मिळवणे किंवा मशीन शोधण्याबद्दल विचारा. हा सहाय्यक अद्याप लाईव्ह AI सेवेशी जोडलेला नाही."
        )
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
        Text(text, fontSize = 15.sp)
    }
}