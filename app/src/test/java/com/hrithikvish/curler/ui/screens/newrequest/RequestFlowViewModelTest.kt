package com.hrithikvish.curler.ui.screens.newrequest

import com.hrithikvish.curler.data.history.HistoryRepository
import com.hrithikvish.curler.data.model.HttpMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RequestFlowViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dao: FakeHistoryDao
    private lateinit var executor: FakeRequestExecutor
    private lateinit var viewModel: RequestFlowViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeHistoryDao()
        executor = FakeRequestExecutor()
        viewModel = RequestFlowViewModel(HistoryRepository(dao), executor, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `switching tabs retains paste and build state`() {
        viewModel.updatePasteText("curl https://x.com")
        viewModel.selectTab(NewRequestTab.Build)
        viewModel.updateBuildUrl("https://y.com")

        viewModel.selectTab(NewRequestTab.Paste)
        assertEquals("curl https://x.com", viewModel.uiState.value.pasteText)

        viewModel.selectTab(NewRequestTab.Build)
        assertEquals("https://y.com", viewModel.uiState.value.buildUrl)
    }

    @Test
    fun `parse failure returns false and sets inline error without building a request`() = runTest(testDispatcher) {
        viewModel.updatePasteText("not a curl command")

        val success = viewModel.validateInternal()

        assertFalse(success)
        assertNotNull(viewModel.uiState.value.pasteError)
    }

    @Test
    fun `valid paste input builds request and clears error`() = runTest(testDispatcher) {
        viewModel.updatePasteText("curl https://api.example.com/users")

        val success = viewModel.validateInternal()

        assertTrue(success)
        assertNull(viewModel.uiState.value.pasteError)
        assertEquals("https://api.example.com/users", viewModel.request.value.url)
    }

    @Test
    fun `build tab with blank url fails validation`() = runTest(testDispatcher) {
        viewModel.selectTab(NewRequestTab.Build)

        val success = viewModel.validateInternal()

        assertFalse(success)
        assertEquals(BuildUrlError.REQUIRED, viewModel.uiState.value.buildError)
    }

    @Test
    fun `build tab with valid url succeeds`() = runTest(testDispatcher) {
        viewModel.selectTab(NewRequestTab.Build)
        viewModel.updateBuildUrl("https://api.example.com/orders")
        viewModel.updateBuildMethod(HttpMethod.POST)

        val success = viewModel.validateInternal()

        assertTrue(success)
        assertEquals(HttpMethod.POST, viewModel.request.value.method)
        assertEquals("https://api.example.com/orders", viewModel.request.value.url)
    }

    @Test
    fun `send executes request and persists a history entry`() = runTest(testDispatcher) {
        viewModel.updatePasteText("curl https://api.example.com/users")
        viewModel.validateInternal()

        val result = viewModel.send()

        assertTrue(result.isSuccess)
        assertFalse(viewModel.uiState.value.isSending)
        assertEquals(1, dao.currentEntities().size)
        assertEquals("curl https://api.example.com/users", dao.currentEntities().single().rawCurlText)
    }
}
