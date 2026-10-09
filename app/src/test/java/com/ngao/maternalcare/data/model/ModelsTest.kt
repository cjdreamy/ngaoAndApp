package com.ngao.maternalcare.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelsTest {
    @Test
    fun `profile accepts legacy null full_name values`() {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val profiles = json.decodeFromString<List<Profile>>(
            """[
                {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "full_name": null,
                  "role": "mother",
                  "phone": "null"
                }
            ]"""
        )

        assertEquals(1, profiles.size)
        assertNull(profiles[0].fullName)
        assertEquals("null", profiles[0].phone)
    }
}
