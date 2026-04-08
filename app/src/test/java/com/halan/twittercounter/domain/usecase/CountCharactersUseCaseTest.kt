package com.halan.twittercounter.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CountCharactersUseCaseTest {

    private lateinit var useCase: CountCharactersUseCase

    @Before
    fun setUp() {
        useCase = CountCharactersUseCase()
    }

    @Test
    fun `empty string returns zero typed and 280 remaining`() {
        val result = useCase("")
        assertEquals(0, result.typed)
        assertEquals(280, result.remaining)
        assertFalse(result.isOverLimit)
    }

    @Test
    fun `plain text counts each character`() {
        val result = useCase("Hello")
        assertEquals(5, result.typed)
        assertEquals(275, result.remaining)
    }

    @Test
    fun `exactly 280 characters is not over limit`() {
        val result = useCase("a".repeat(280))
        assertEquals(280, result.typed)
        assertEquals(0, result.remaining)
        assertFalse(result.isOverLimit)
    }

    @Test
    fun `281 characters is over limit`() {
        val result = useCase("a".repeat(281))
        assertEquals(281, result.typed)
        assertEquals(-1, result.remaining)
        assertTrue(result.isOverLimit)
    }

    @Test
    fun `http URL is counted as 23 characters`() {
        val result = useCase("https://example.com/long/path/here")
        assertEquals(23, result.typed)
        assertEquals(257, result.remaining)
    }

    @Test
    fun `two URLs counted as 23 each plus space`() {
        val result = useCase("https://a.com https://b.com")
        assertEquals(47, result.typed)
    }

    @Test
    fun `text with URL counted correctly`() {
        val result = useCase("Hello https://example.com")
        assertEquals(29, result.typed)
    }

    @Test
    fun `emoji counted as two characters`() {
        val result = useCase("😀")
        assertEquals(2, result.typed)
        assertEquals(278, result.remaining)
    }

    @Test
    fun `text with emoji counts correctly`() {
        val result = useCase("Hi 😀")
        assertEquals(5, result.typed)
        assertEquals(275, result.remaining)
    }

    @Test
    fun `maxTweetLength property is 280`() {
        assertEquals(280, useCase.maxTweetLength)
    }
}