package com.hrithikvish.curler.ui.screens.home

import com.hrithikvish.curler.data.history.HistoryRepository
import com.hrithikvish.curler.data.update.FakeUpdateManager
import com.hrithikvish.curler.data.update.UpdateState
import com.hrithikvish.curler.ui.screens.newrequest.FakeHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dao: FakeHistoryDao
    private lateinit var updateManager: FakeUpdateManager
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeHistoryDao()
        updateManager = FakeUpdateManager()
        viewModel = HomeViewModel(HistoryRepository(dao), updateManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState reflects update manager state`() = runTest(testDispatcher) {
        val job = launch { viewModel.uiState.collect {} }

        updateManager.emit(UpdateState.Available(7))

        assertEquals(UpdateState.Available(7), viewModel.uiState.value.updateState)
        job.cancel()
    }

    @Test
    fun `onUpdateAction starts the update when available`() {
        updateManager.emit(UpdateState.Available(7))

        viewModel.onUpdateAction()

        assertEquals(1, updateManager.startUpdateCallCount)
        assertEquals(0, updateManager.completeUpdateCallCount)
    }

    @Test
    fun `onUpdateAction completes the update when downloaded`() {
        updateManager.emit(UpdateState.Downloaded)

        viewModel.onUpdateAction()

        assertEquals(1, updateManager.completeUpdateCallCount)
        assertEquals(0, updateManager.startUpdateCallCount)
    }

    @Test
    fun `onUpdateAction is a no-op when idle`() {
        viewModel.onUpdateAction()

        assertEquals(0, updateManager.startUpdateCallCount)
        assertEquals(0, updateManager.completeUpdateCallCount)
    }
}
