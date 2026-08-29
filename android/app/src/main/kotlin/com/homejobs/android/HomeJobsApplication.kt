package com.homejobs.android

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HomeJobsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // pdfbox-android loads font metrics etc. from its bundled assets and needs a Context to
        // do that before any PDDocument is opened.
        PDFBoxResourceLoader.init(applicationContext)
    }
}
