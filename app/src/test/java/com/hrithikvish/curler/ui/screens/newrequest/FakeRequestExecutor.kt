package com.hrithikvish.curler.ui.screens.newrequest

import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import com.hrithikvish.curler.data.network.RequestExecutor

class FakeRequestExecutor(
    private val result: Result<HttpResponseModel> = Result.success(
        HttpResponseModel(
            statusCode = 200,
            statusMessage = "OK",
            headers = emptyList(),
            body = "{}",
            isBodyJson = true,
            durationMs = 10,
            sizeBytes = 2,
        ),
    ),
) : RequestExecutor {
    var lastRequest: HttpRequestModel? = null
        private set

    override suspend fun execute(request: HttpRequestModel): Result<HttpResponseModel> {
        lastRequest = request
        return result
    }
}
