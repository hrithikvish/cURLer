package com.hrithikvish.curler.ui.screens.review

import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared by both RequestFlowViewModel and HistoryReviewViewModel so a single
 * ReviewScreen/ResponseScreen implementation can serve both nav graphs.
 */
interface ReviewCapable {
    val request: StateFlow<HttpRequestModel>
    suspend fun send(): Result<HttpResponseModel>
}
