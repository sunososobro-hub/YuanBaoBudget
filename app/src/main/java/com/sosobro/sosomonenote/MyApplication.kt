package com.sosobro.sosomonenote

import android.app.Application
import com.sosobro.sosomonenote.database.DatabaseInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ⭐ 在背景線程預先初始化 Room（超級有效）
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseInstance.getDatabase(applicationContext)
        }
    }
}
