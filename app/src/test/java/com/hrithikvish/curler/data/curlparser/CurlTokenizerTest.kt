package com.hrithikvish.curler.data.curlparser

import org.junit.Assert.assertEquals
import org.junit.Test

class CurlTokenizerTest {

    @Test
    fun `splits on whitespace`() {
        assertEquals(listOf("curl", "https://api.example.com"), tokenizeCurl("curl https://api.example.com"))
    }

    @Test
    fun `keeps double-quoted segment with spaces as one token`() {
        assertEquals(
            listOf("-H", "Authorization: Bearer xxx"),
            tokenizeCurl("-H \"Authorization: Bearer xxx\""),
        )
    }

    @Test
    fun `keeps single-quoted segment with spaces as one token`() {
        assertEquals(
            listOf("-d", "{\"item\": \"sku_123\"}"),
            tokenizeCurl("-d '{\"item\": \"sku_123\"}'"),
        )
    }

    @Test
    fun `empty input produces no tokens`() {
        assertEquals(emptyList<String>(), tokenizeCurl(""))
    }

    @Test
    fun `handles mixed quoting in one line`() {
        assertEquals(
            listOf("curl", "-H", "a: b", "-d", "raw data", "https://x.com"),
            tokenizeCurl("curl -H \"a: b\" -d 'raw data' https://x.com"),
        )
    }
}
