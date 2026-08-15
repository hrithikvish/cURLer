package com.hrithikvish.curler.data.json

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonPrettyPrinterTest {

    @Test
    fun `pretty prints a compact JSON object`() {
        val result = JsonPrettyPrinter.prettyPrintOrNull("""{"item":"sku_123","qty":2}""")
        assertEquals(
            "{\n  \"item\": \"sku_123\",\n  \"qty\": 2\n}",
            result,
        )
    }

    @Test
    fun `invalid JSON returns null from prettyPrintOrNull`() {
        assertNull(JsonPrettyPrinter.prettyPrintOrNull("not json"))
    }

    @Test
    fun `prettyPrint falls back to raw text when invalid`() {
        assertEquals("not json", JsonPrettyPrinter.prettyPrint("not json"))
    }
}
