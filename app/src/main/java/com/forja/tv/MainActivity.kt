package com.forja.tv

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    // ===== مشغّل ExoPlayer =====
    private lateinit var player: ExoPlayer
    private var currentIndex = -1
    private val buttons = mutableListOf<Button>()

    // ===== بيانات القنوات =====
    data class Channel(val name: String, val url: String, val emoji: String)

    private val channels = listOf(
        Channel("FORJA KIDS HD",          "http://supaapp.xyz/live/462738737337/352672622626/414028.ts", "🧒"),
        Channel("FORJA MASRAH HD",         "http://supaapp.xyz/live/462738737337/352672622626/414029.ts", "🎭"),
        Channel("FORJA ACTION HD",         "http://supaapp.xyz/live/462738737337/352672622626/414030.ts", "🎬"),
        Channel("FORJA COMEDY HD",         "http://supaapp.xyz/live/462738737337/352672622626/414031.ts", "😂"),
        Channel("FORJA COMEDY 2 HD",       "http://supaapp.xyz/live/462738737337/352672622626/414032.ts", "😄"),
        Channel("FORJA DOCUMENTAIRE 1 HD", "http://supaapp.xyz/live/462738737337/352672622626/414033.ts", "🎥"),
        Channel("FORJA DOCUMENTAIRE 2 HD", "http://supaapp.xyz/live/462738737337/352672622626/414034.ts", "📽"),
        Channel("FORJA DOCUMENTAIRE 3 HD", "http://supaapp.xyz/live/462738737337/352672622626/414035.ts", "🌍"),
        Channel("FORJA DRAMA 1 HD",        "http://supaapp.xyz/live/462738737337/352672622626/414036.ts", "🎞"),
        Channel("FORJA DRAMA 2 HD",        "http://supaapp.xyz/live/462738737337/352672622626/414037.ts", "🎦"),
        Channel("FORJA DRAMA 3 HD",        "http://supaapp.xyz/live/462738737337/352672622626/414038.ts", "📺")
    )

    // ===== ألوان التطبيق =====
    private val colorBg         = Color.parseColor("#0A0A0F")
    private val colorSidebar    = Color.parseColor("#0D0D18")
    private val colorHeader     = Color.parseColor("#1A0800")
    private val colorAccent     = Color.parseColor("#FF6A00")
    private val colorCardNormal = Color.parseColor("#13131F")
    private val colorCardActive = Color.parseColor("#1F0A00")
    private val colorBorder     = Color.parseColor("#222233")
    private val colorText       = Color.parseColor("#CCCCCC")
    private val colorMuted      = Color.parseColor("#888888")

    // ===== إنشاء الواجهة =====
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // بناء الواجهة برمجياً بدون XML معقّد
        val root = buildUI()
        setContentView(root)

        setupPlayer()
    }

    // ===== بناء واجهة المستخدم =====
    private fun buildUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(colorBg)
        }

        // --- الشريط الجانبي (القنوات) ---
        val sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colorSidebar)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.32f)
        }

        // ترويسة
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(colorHeader)
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(58)
            )
            setPadding(dpToPx(14), 0, dpToPx(14), 0)
        }
        val logoText = TextView(this).apply {
            text = "📺  FORJA TV"
            textSize = 17f
            setTextColor(colorAccent)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        header.addView(logoText)

        // شريط "يُعرض الآن"
        val nowPlayingBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#13131F"))
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
        }
        val liveLabel = TextView(this).apply {
            text = "● "
            setTextColor(Color.parseColor("#FF4444"))
            textSize = 11f
        }
        val nowPlayingText = TextView(this).apply {
            id = R.id.now_playing
            text = "اختر قناة للمشاهدة"
            setTextColor(colorMuted)
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        nowPlayingBar.addView(liveLabel)
        nowPlayingBar.addView(nowPlayingText)

        // قائمة القنوات
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val channelList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
        }

        channels.forEachIndexed { idx, ch ->
            val btn = Button(this).apply {
                text = "${ch.emoji}  ${ch.name}"
                textSize = 12.5f
                setTextColor(colorText)
                background = buildButtonBg(false)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(dpToPx(4), dpToPx(3), dpToPx(4), dpToPx(3)) }
                setPadding(dpToPx(12), dpToPx(11), dpToPx(12), dpToPx(11))
                gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                isAllCaps = false
                setOnClickListener { playChannel(idx) }
            }
            channelList.addView(btn)
            buttons.add(btn)
        }

        scrollView.addView(channelList)
        sidebar.addView(header)
        sidebar.addView(nowPlayingBar)
        sidebar.addView(scrollView)

        // --- منطقة الفيديو ---
        val videoArea = RelativeLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.68f)
        }

        val playerView = PlayerView(this).apply {
            id = R.id.player_view
            useController = true
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        }

        val spinner = ProgressBar(this).apply {
            id = R.id.loading_spinner
            visibility = View.GONE
            indeterminateTintList = android.content.res.ColorStateList.valueOf(colorAccent)
            layoutParams = RelativeLayout.LayoutParams(dpToPx(56), dpToPx(56)).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }
        }

        // شاشة ترحيب
        val placeholder = LinearLayout(this).apply {
            id = R.id.placeholder
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        }
        val tvEmoji = TextView(this).apply {
            text = "📺"
            textSize = 60f
            gravity = android.view.Gravity.CENTER
        }
        val hintText = TextView(this).apply {
            text = "اختر قناة من القائمة"
            setTextColor(colorMuted)
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setPadding(0, dpToPx(14), 0, 0)
        }
        placeholder.addView(tvEmoji)
        placeholder.addView(hintText)

        videoArea.addView(playerView)
        videoArea.addView(spinner)
        videoArea.addView(placeholder)

        root.addView(sidebar)
        root.addView(videoArea)
        return root
    }

    // ===== تهيئة المشغّل =====
    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        val playerView = findViewById<PlayerView>(R.id.player_view)
        playerView.player = player

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val spinner = findViewById<ProgressBar>(R.id.loading_spinner)
                spinner.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                if (state == Player.STATE_READY) {
                    findViewById<View>(R.id.placeholder).visibility = View.GONE
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Toast.makeText(this@MainActivity, "⚠️ تعذّر تشغيل القناة", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ===== تشغيل قناة =====
    private fun playChannel(index: Int) {
        if (index == currentIndex) return
        currentIndex = index

        // تحديث أزرار القنوات
        buttons.forEachIndexed { i, btn ->
            val active = (i == index)
            btn.background = buildButtonBg(active)
            btn.setTextColor(if (active) colorAccent else colorText)
        }

        // تحديث نص "يُعرض الآن"
        val nowPlaying = findViewById<TextView>(R.id.now_playing)
        nowPlaying.text = channels[index].name
        nowPlaying.setTextColor(Color.WHITE)

        // تشغيل البث عبر ExoPlayer
        val mediaItem = MediaItem.fromUri(channels[index].url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    // ===== بناء خلفية الزر =====
    private fun buildButtonBg(active: Boolean): StateListDrawable {
        val normalShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(if (active) colorCardActive else colorCardNormal)
            setStroke(dpToPx(1), if (active) colorAccent else colorBorder)
            cornerRadius = dpToPx(8).toFloat()
        }
        return StateListDrawable().apply { addState(intArrayOf(), normalShape) }
    }

    // ===== تحويل dp إلى px =====
    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    // ===== دورة حياة التطبيق =====
    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        if (currentIndex >= 0) player.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
