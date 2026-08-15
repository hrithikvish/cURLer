package com.hrithikvish.curler.data.query

import org.junit.Assert.assertEquals
import org.junit.Test

class QueryParamsParserTest {

    @Test
    fun `url without query string returns empty list`() {
        assertEquals(emptyList<Pair<String, String>>(), QueryParamsParser.parse("https://api.example.com/orders"))
    }

    @Test
    fun `parses single query param`() {
        assertEquals(
            listOf("page" to "2"),
            QueryParamsParser.parse("https://api.example.com/orders?page=2"),
        )
    }

    @Test
    fun `parses multiple query params in order`() {
        assertEquals(
            listOf("page" to "2", "limit" to "10"),
            QueryParamsParser.parse("https://api.example.com/orders?page=2&limit=10"),
        )
    }

    @Test
    fun `param without value becomes empty string`() {
        assertEquals(
            listOf("flag" to ""),
            QueryParamsParser.parse("https://api.example.com/orders?flag"),
        )
    }

    @Test
    fun `url-decodes keys and values`() {
        assertEquals(
            listOf("q" to "hello world"),
            QueryParamsParser.parse("https://api.example.com/search?q=hello%20world"),
        )
    }

    @Test
    fun `ignores fragment after hash`() {
        assertEquals(
            listOf("page" to "2"),
            QueryParamsParser.parse("https://api.example.com/orders?page=2#section"),
        )
    }

    @Test
    fun `trailing question mark with no params returns empty list`() {
        assertEquals(emptyList<Pair<String, String>>(), QueryParamsParser.parse("https://api.example.com/orders?"))
    }
}
