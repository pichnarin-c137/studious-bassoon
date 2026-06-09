package com.gymapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. [HiltAndroidApp] triggers Hilt's code generation. */
@HiltAndroidApp
class GymApp : Application()
