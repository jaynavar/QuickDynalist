package com.louiskirsch.quickdynalist

import android.app.Activity
import android.content.Context
import android.content.pm.ShortcutManager
import android.text.InputType
import android.text.Spannable
import android.text.TextUtils
import android.text.util.Linkify
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutManagerCompat
import org.jetbrains.anko.find
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.ANTI_ALIAS_FLAG
import com.louiskirsch.quickdynalist.objectbox.DynalistItem
import com.louiskirsch.quickdynalist.objectbox.DynalistItem_
import io.objectbox.Box
import io.objectbox.kotlin.query
import kotlin.math.roundToInt


fun EditText.setupGrowingMultiline(maxLines: Int) {
    // These properties must be set programmatically because order of execution matters
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    setSingleLine(true)
    this.maxLines = maxLines
    setHorizontallyScrolling(false)
    imeOptions = EditorInfo.IME_ACTION_DONE
}

fun Activity.fixedFinishAfterTransition() {
    if (currentFocus != null) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }
    finishAfterTransition()
}

val AppCompatActivity.actionBarView: View
    get() = window.decorView.find(R.id.action_bar_container)

val IntRange.size: Int get() = endInclusive - start + 1

val <A, B> Pair<A?, B?>.selfNotNull: Pair<A, B>? get() {
    return if (first != null && second != null)
        Pair(first!!, second!!)
    else
        null
}

val Boolean.int: Int get() = if (this) 1 else 0

fun CharSequence.prependIfNotBlank(text: CharSequence) =
        if (isNotBlank()) TextUtils.concat(text, this) else this

fun Spannable.linkify(mask: Int = Linkify.ALL): Spannable
        = Linkify.addLinks(this, mask).let { this }

val Context.inputMethodManager: InputMethodManager
    get() = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

fun CharSequence.ellipsis(maxLength: Int): CharSequence {
    return if (length > maxLength)
        "${take(maxLength - 1)}…"
    else
        this
}

fun String.toBitmap(textSize: Float, textColor: Int): Bitmap {
    val paint = Paint(ANTI_ALIAS_FLAG)
    paint.textSize = textSize
    paint.color = textColor
    paint.textAlign = Paint.Align.LEFT
    val baseline = -paint.ascent() // ascent() is negative
    val width = (paint.measureText(this) + 0.5f).roundToInt()
    val height = (baseline + paint.descent() + 0.5f).roundToInt()
    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(image)
    canvas.drawText(this, 0f, baseline, paint)
    return image
}

fun Box<DynalistItem>.getByServerId(serverFileId: String, serverItemId: String): DynalistItem {
    return query {
        equal(DynalistItem_.serverFileId, serverFileId)
        and()
        equal(DynalistItem_.serverItemId, serverItemId)
    }.findFirst()!!
}