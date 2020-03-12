/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {


    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality


    fun doneNavigation() {
        _navigateToSleepQuality.value = null
    }

    private var viewModelJop = Job()
    override fun onCleared() {
        super.onCleared()
        viewModelJop.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJop)

    private var toNight = MutableLiveData<SleepNight?>()

    private val nights = database.getAllNight()

    init {
        initaliztionToNight()
    }
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent
    fun doneShowingSnackbar() {

        _showSnackbarEvent.value = false
    }

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }
    val startButtonVisible = Transformations.map(toNight) {
        null == it
    }
    val stopButtonVisible = Transformations.map(toNight) {
        null != it
    }
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    private fun initaliztionToNight() {
        uiScope.launch {
            toNight.value = toNightFromDataBse()
        }
    }

    private suspend fun toNightFromDataBse(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var toNight = database.getTONight()
            if (toNight?.endTimeMill != toNight?.startTimeMill) {
                toNight = null
            }
            toNight
        }
    }

    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            toNight.value = toNightFromDataBse()
        }
    }

    private suspend fun insert(newNight: SleepNight) {
        return withContext(Dispatchers.IO) {
            database.addNight(newNight)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = toNight.value ?: return@launch
            oldNight.endTimeMill = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(oldNight: SleepNight) {
        withContext(Dispatchers.IO) {
            database.updateNight(oldNight)
        }

    }

    fun onClear() {
        uiScope.launch {
            clear()
            toNight.value = null
        }
        _showSnackbarEvent.value = true
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()

        }

    }


}

