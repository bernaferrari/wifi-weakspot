package com.bernaferrari.wifiweakspot.feature.settings

import com.bernaferrari.wifiweakspot.core.backup.BackupService
import com.bernaferrari.wifiweakspot.core.backup.RestoreResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    @Test fun cancelPickerDoesNothingAndRepeatedClicksCannotStartConcurrentWrites() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate = CompletableDeferred<Unit>()
            var exports = 0
            val service = object : BackupService {
                override suspend fun exportTo(destination: String) { exports++; gate.await() }
                override suspend fun restoreFrom(source: String) = RestoreResult.Restored
            }
            val vm = BackupViewModel(service)
            vm.exportTo(null)
            assertFalse(vm.busy.value)
            vm.exportTo("file-1")
            vm.exportTo("file-2")
            runCurrent()
            assertEquals(1, exports)
            assertTrue(vm.busy.value)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals(BackupMessage.Exported, vm.message.value)
            assertFalse(vm.busy.value)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun failureIsRecoverableAndDoesNotExposeExceptionText() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var fail = true
            val service = object : BackupService {
                override suspend fun exportTo(destination: String) = Unit
                override suspend fun restoreFrom(source: String): RestoreResult {
                    if (fail) error("private path and contents")
                    return RestoreResult.AlreadyRestored
                }
            }
            val vm = BackupViewModel(service)
            vm.restoreFrom("archive")
            advanceUntilIdle()
            assertEquals(BackupMessage.RestoreFailed, vm.message.value)
            assertFalse(vm.busy.value)
            fail = false
            vm.restoreFrom("archive")
            advanceUntilIdle()
            assertEquals(BackupMessage.AlreadyRestored, vm.message.value)
        } finally { Dispatchers.resetMain() }
    }
}
