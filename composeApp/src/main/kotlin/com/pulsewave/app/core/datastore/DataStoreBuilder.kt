package com.pulsewave.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

fun createDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE_NAME) }
    )
}

const val DATASTORE_FILE_NAME = "pulsewave.preferences_pb"
