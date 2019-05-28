package me.thanel.readtracker.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import me.thanel.readtracker.R
import me.thanel.readtracker.ui.util.addAfterTextChangedListener

class UserInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var isInputFromCode = false

    var onAfterUserTextChangeListener: (String?) -> Unit = {}

    init {
        addAfterTextChangedListener {
            if (isInputFromCode) {
                isInputFromCode = false
            } else {
                onAfterUserTextChangeListener(it?.toString())
            }
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        isInputFromCode = true
        super.setText(text, type)
    }
}
