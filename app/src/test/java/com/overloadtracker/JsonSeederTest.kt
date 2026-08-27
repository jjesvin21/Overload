/**
 * Unit test verifying exercises.json parsing without Android runtime.
 */
package com.overloadtracker

import com.overloadtracker.util.JsonSeeder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonSeederTest {

    @Test
    fun parseSampleExercise_mapsFields() {
        val raw = """
            [{
              "id": "0001",
              "name": "3/4 sit-up",
              "category": "waist",
              "body_part": "waist",
              "equipment": "body weight",
              "target": "abs",
              "muscle_group": "abs",
              "secondary_muscles": ["hip flexors"],
              "instructions": { "en": "Lie flat and curl up." },
              "instruction_steps": { "en": ["Lie flat", "Curl up"] },
              "image": "images/0001-x.jpg",
              "gif_url": "videos/0001-x.gif"
            }]
        """.trimIndent()
        val list = JsonSeeder.parseExercisesJson(raw)
        assertEquals(1, list.size)
        val ex = list.first()
        assertEquals("0001", ex.id)
        assertEquals("3/4 sit-up", ex.name)
        assertEquals("waist", ex.category)
        assertEquals("body weight", ex.equipment)
        assertTrue(ex.instructions.contains("Lie flat"))
        assertEquals("images/0001-x.jpg", ex.imagePath)
        assertEquals("videos/0001-x.gif", ex.gifPath)
    }
}
