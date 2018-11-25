package me.thanel.readtracker.ui.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar

fun EditText.afterTextChanged(listener: (Editable?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) = listener(editable)

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
}

fun SeekBar.onProgressChanged(listener: (progress: Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) =
            listener(progress)

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    })
}

fun SeekBar.connectTo(editText: EditText) {
    var skipChange = false
    onProgressChanged {
        if (!skipChange) {
            editText.setText(it.toString())
        }
    }
    editText.afterTextChanged {
        skipChange = true
        progress = it.toIntOrElse { 0 }
        skipChange = false
    }
}

