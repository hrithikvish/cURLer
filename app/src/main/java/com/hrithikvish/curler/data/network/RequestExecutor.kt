package com.hrithikvish.curler.data.network

import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel

interface RequestExecutor {
    suspend fun execute(request: HttpRequestModel): Result<HttpResponseModel>
}
