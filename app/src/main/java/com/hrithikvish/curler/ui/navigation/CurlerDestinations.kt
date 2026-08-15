package com.hrithikvish.curler.ui.navigation

import kotlinx.serialization.Serializable

sealed interface CurlerRoute {

    @Serializable
    data object Home : CurlerRoute

    // requestFlow graph: NewRequest (start) -> Review -> Response,
    // all sharing one RequestFlowViewModel scoped to this graph.
    @Serializable
    data object RequestFlowGraph : CurlerRoute

    @Serializable
    data object NewRequest : CurlerRoute

    @Serializable
    data object RequestReview : CurlerRoute

    @Serializable
    data object RequestResponse : CurlerRoute

    // historyFlow graph: Review -> Response only, entered directly from a
    // Home history-row tap. Scoped to a lighter HistoryReviewViewModel.
    @Serializable
    data class HistoryFlowGraph(val historyId: Long) : CurlerRoute

    @Serializable
    data object HistoryReview : CurlerRoute

    @Serializable
    data object HistoryResponse : CurlerRoute
}
