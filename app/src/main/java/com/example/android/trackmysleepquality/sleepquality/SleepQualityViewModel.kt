package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

class SleepQualityViewModel(private val sleepNightKey: Long = 0L,
                            val database: SleepDatabaseDao) : ViewModel() {

    private val viewModelJop = Job()
    override fun onCleared() {
        super.onCleared()
        viewModelJop.cancel()
    }

    private val uiScop = CoroutineScope(Dispatchers.Main + viewModelJop)


    private val _navigationToSleepTracker = MutableLiveData<Boolean?>()
    val navigationToSleepTracker: LiveData<Boolean?>
        get() = _navigationToSleepTracker

    fun doneNavigation() {
        _navigationToSleepTracker.value = null
    }

    fun onSetQuaility(qualiy: Int) {
        uiScop.launch {
            withContext(Dispatchers.IO) {
                val toNight = database.get(sleepNightKey) ?: return@withContext
                toNight.sleepQuality = qualiy
                database.updateNight(toNight)
            }
            _navigationToSleepTracker.value = true
        }
    }

}
