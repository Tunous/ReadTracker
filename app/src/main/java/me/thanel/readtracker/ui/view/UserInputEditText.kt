package me.thanel.readtracker.ui.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import me.thanel.readtracker.R

class UserInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var isInputFromCode = false

    var afterUserTextChanged: (String?) -> Unit = {}

    init {
        addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isInputFromCode) {
                    isInputFromCode = false
                    return
                }
                afterUserTextChanged(s?.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        isInputFromCode = true
        super.setText(text, type)
    }
}
