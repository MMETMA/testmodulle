package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "MIUI Whitelist Disabler (LSPosed Module)", Toast.LENGTH_SHORT).show()
        finish()
    }
}
