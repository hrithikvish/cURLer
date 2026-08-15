package com.hrithikvish.curler.data.curlparser

import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.ParsedCurlRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class CurlParserTest {

    @Test
    fun `happy path POST with headers and body`() {
        val result = CurlParser.parse(
            """curl -X POST https://api.example.com/orders -H "Authorization: Bearer xxx" -H "Content-Type: application/json" -d '{"item":"sku_123","qty":2}'""",
        )
        require(result is ParsedCurlRequest.Success)
        assertEquals(HttpMethod.POST, result.method)
        assertEquals("https://api.example.com/orders", result.url)
        assertEquals(
            listOf("Authorization" to "Bearer xxx", "Content-Type" to "application/json"),
            result.headers,
        )
        assertEquals("""{"item":"sku_123","qty":2}""", result.body)
        assertTrue(result.bodyIsValidJson)
    }

    @Test
    fun `method defaults to GET without body`() {
        val result = CurlParser.parse("curl https://api.example.com/users/42")
        require(result is ParsedCurlRequest.Success)
        assertEquals(HttpMethod.GET, result.method)
    }

    @Test
    fun `method defaults to POST when body present`() {
        val result = CurlParser.parse("curl https://api.example.com/users -d '{}'")
        require(result is ParsedCurlRequest.Success)
        assertEquals(HttpMethod.POST, result.method)
    }

    @Test
    fun `multiple headers accumulate in order`() {
        val result = CurlParser.parse(
            "curl https://x.com -H \"A: 1\" -H \"B: 2\" -H \"C: 3\"",
        )
        require(result is ParsedCurlRequest.Success)
        assertEquals(listOf("A" to "1", "B" to "2", "C" to "3"), result.headers)
    }

    @Test
    fun `header missing colon produces exact error message`() {
        val result = CurlParser.parse("curl https://x.com -H \"NoColonHere\"")
        require(result is ParsedCurlRequest.Error)
        assertEquals("Header \"NoColonHere\" is missing a colon", result.message)
    }

    @Test
    fun `missing curl prefix errors`() {
        val result = CurlParser.parse("wget https://x.com")
        require(result is ParsedCurlRequest.Error)
        assertEquals("Command should start with \"curl\"", result.message)
    }

    @Test
    fun `no url errors`() {
        val result = CurlParser.parse("curl -X GET")
        require(result is ParsedCurlRequest.Error)
        assertEquals("No URL found in the command", result.message)
    }

    @Test
    fun `url missing scheme errors`() {
        val result = CurlParser.parse("curl api.example.com/orders")
        require(result is ParsedCurlRequest.Error)
        assertEquals("URL should start with http:// or https:// — got \"api.example.com/orders\"", result.message)
    }

    @Test
    fun `-u user pass produces correctly base64-encoded basic auth header`() {
        val result = CurlParser.parse("curl https://x.com -u user:pass")
        require(result is ParsedCurlRequest.Success)
        val expected = "Basic " + Base64.getEncoder().encodeToString("user:pass".toByteArray())
        assertEquals(listOf("Authorization" to expected), result.headers)
    }

    @Test
    fun `single and double quoted tokens with embedded spaces`() {
        val result = CurlParser.parse("curl https://x.com -H 'X-Custom: has spaces value'")
        require(result is ParsedCurlRequest.Success)
        assertEquals(listOf("X-Custom" to "has spaces value"), result.headers)
    }

    @Test
    fun `multi-line with backslash continuations`() {
        val result = CurlParser.parse(
            "curl -X POST https://x.com \\\n  -H \"A: 1\" \\\n  -d '{}'",
        )
        require(result is ParsedCurlRequest.Success)
        assertEquals(HttpMethod.POST, result.method)
        assertEquals(listOf("A" to "1"), result.headers)
    }

    @Test
    fun `invalid JSON body preserves raw body and flags bodyIsValidJson false`() {
        val result = CurlParser.parse("curl https://x.com -d '{item: sku_123}'")
        require(result is ParsedCurlRequest.Success)
        assertEquals("{item: sku_123}", result.body)
        assertFalse(result.bodyIsValidJson)
    }

    @Test
    fun `empty input errors`() {
        val result = CurlParser.parse("")
        require(result is ParsedCurlRequest.Error)
        assertEquals("Command should start with \"curl\"", result.message)
    }

    @Test
    fun `whitespace-only input errors`() {
        val result = CurlParser.parse("   \n  ")
        require(result is ParsedCurlRequest.Error)
        assertEquals("Command should start with \"curl\"", result.message)
    }

    @Test
    fun `data-raw and data-binary behave like -d`() {
        val raw = CurlParser.parse("curl https://x.com --data-raw 'abc'")
        val binary = CurlParser.parse("curl https://x.com --data-binary 'abc'")
        require(raw is ParsedCurlRequest.Success)
        require(binary is ParsedCurlRequest.Success)
        assertEquals("abc", raw.body)
        assertEquals("abc", binary.body)
    }

    @Test
    fun `repeated -d last one wins`() {
        val result = CurlParser.parse("curl https://x.com -d 'first' -d 'second'")
        require(result is ParsedCurlRequest.Success)
        assertEquals("second", result.body)
    }

    @Test
    fun `method is case-insensitive`() {
        val result = CurlParser.parse("curl -X post https://x.com")
        require(result is ParsedCurlRequest.Success)
        assertEquals(HttpMethod.POST, result.method)
        assertEquals("post", result.rawMethod)
    }
}
