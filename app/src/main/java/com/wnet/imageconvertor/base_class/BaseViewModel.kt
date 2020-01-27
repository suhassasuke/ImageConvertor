package com.luminaire.apolloar.base_class

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.ConnectException
import java.util.regex.Matcher
import java.util.regex.Pattern


enum class ApiStatus { LOADING, ERROR, DONE }

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    // The internal MutableLiveData that stores the status of the most recent request
    protected val _isError = MutableLiveData<Boolean>()

    // The external immutable LiveData for the request status
    val isError: LiveData<Boolean>
        get() = _isError

    // The external immutable LiveData for the request status
    lateinit var errorMessage: String

    // The external immutable LiveData for the request status
    lateinit var successMessage: String

    // The internal MutableLiveData that stores the status of the most recent request
    protected val _apiStatus = MutableLiveData<ApiStatus>()


    // The external immutable LiveData for the request status
    val apiStatus: LiveData<ApiStatus>
        get() = _apiStatus



    /***************************************** Job and  Coroutine ***********************************************/

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    protected val coroutineScope = CoroutineScope(Dispatchers.IO + viewModelJob)


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Set default error status
     *
     */
    fun clearError() {
        _isError.postValue(false)
        errorMessage = ""
    }

    /**
     * Set default error status
     *
     */
    fun clearApiStatus() {
        _apiStatus.postValue(null)
        errorMessage = ""
    }

    fun isValidPassword(password: String): Boolean {

        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$)$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)

        return matcher.matches()

    }
}