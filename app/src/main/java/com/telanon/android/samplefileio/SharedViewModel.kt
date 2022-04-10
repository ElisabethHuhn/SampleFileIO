package com.telanon.android.samplefileio

import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var textFromPublicRead  : String? = null
    var textToPublicWrite   : String? = null
    var textFromPrivateRead : String? = null
    var textToPrivateWrite  : String? = null

    var lastActionStatus: String = "No previous action"


}
