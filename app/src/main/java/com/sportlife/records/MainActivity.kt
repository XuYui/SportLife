package com.sportlife.records

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sportlife.records.ui.navigation.SportLifeApp
import com.sportlife.records.ui.theme.SportLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as SportLifeApplication).container
        setContent {
            SportLifeTheme {
                SportLifeApp(appContainer = appContainer)
            }
        }
    }
}
