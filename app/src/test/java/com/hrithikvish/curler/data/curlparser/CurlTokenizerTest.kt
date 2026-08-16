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

    @Test
    fun `keeps ansi-c dollar-single-quoted segment with spaces as one token`() {
        assertEquals(
            listOf("--data", "{\"name\": \"Jane Doe\"}"),
            tokenizeCurl("--data \$'{\"name\": \"Jane Doe\"}'"),
        )
    }

    @Test
    fun `ansi-c quoting decodes backslash escapes`() {
        assertEquals(
            listOf("line1\nline2\ttabbed"),
            tokenizeCurl("\$'line1\\nline2\\ttabbed'"),
        )
    }

    @Test
    fun `double-quoted segment honors backslash escapes for quote and backslash`() {
        assertEquals(
            listOf("say \"hi\" \\ done"),
            tokenizeCurl("\"say \\\"hi\\\" \\\\ done\""),
        )
    }

    @Test
    fun `quotes can open mid-word and glue onto surrounding text`() {
        assertEquals(
            listOf("foobar bazqux"),
            tokenizeCurl("foo'bar baz'qux"),
        )
    }

    @Test
    fun `unquoted backslash escapes the next character`() {
        assertEquals(
            listOf("foo bar"),
            tokenizeCurl("foo\\ bar"),
        )
    }
}
