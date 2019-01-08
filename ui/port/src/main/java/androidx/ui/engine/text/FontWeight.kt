/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.ui.engine.text

import androidx.ui.clamp
import androidx.ui.lerpInt
import kotlin.math.roundToInt

// The thickness of the glyphs used to draw the text
// TODO(Migration/siyamed): Check if index can be removed or converted into a function since the
// same thing can be calculated using weight
class FontWeight private constructor(internal val index: Int, internal val weight: Int) :
    Comparable<FontWeight> {

    // TODO(Migration/siyamed): This is weird since it should actually be possible to create a font
    // weight that is not one of the items here. This decision changes the lerp behavior.
    companion object {
        // Thin, the least thick
        val w100 = FontWeight(0, 100)
        // Extra-light
        val w200 = FontWeight(1, 200)
        // Light
        val w300 = FontWeight(2, 300)
        // Normal / regular / plain
        val w400 = FontWeight(3, 400)
        // Medium
        val w500 = FontWeight(4, 500)
        // Semi-bold
        val w600 = FontWeight(5, 600)
        // Bold
        val w700 = FontWeight(6, 700)
        // Extra-bold
        val w800 = FontWeight(7, 800)
        // Black, the most thick
        val w900 = FontWeight(8, 900)
        // The default font weight.
        val normal = w400
        // A commonly used font weight that is heavier than normal.
        val bold = w700

        // A list of all the font weights.
        val values: List<FontWeight> = listOf(w100, w200, w300, w400, w500, w600, w700, w800, w900)

        // Linearly interpolates between two font weights.
        //
        // Rather than using fractional weights, the interpolation rounds to the
        // nearest weight.
        //
        // Any null values for `a` or `b` are interpreted as equivalent to [normal]
        // (also known as [w400]).
        //
        // The `t` argument represents position on the timeline, with 0.0 meaning
        // that the interpolation has not started, returning `a` (or something
        // equivalent to `a`), 1.0 meaning that the interpolation has finished,
        // returning `b` (or something equivalent to `b`), and values in between
        // meaning that the interpolation is at the relevant point on the timeline
        // between `a` and `b`. The interpolation can be extrapolated beyond 0.0 and
        // 1.0, so negative values and values greater than 1.0 are valid (and can
        // easily be generated by curves such as [Curves.elasticInOut]). The result
        // is clamped to the range [w100]–[w900].
        //
        // Values for `t` are usually obtained from an [Animation<Float>], such as
        // an [AnimationController].
        // TODO(Migration/siyamed): I did not like the variable naming.
        fun lerp(a: FontWeight?, b: FontWeight?, t: Float): FontWeight {
            return values[lerpInt(
                a?.index ?: normal.index,
                b?.index ?: normal.index,
                t
            ).roundToInt().clamp(0, 8)]
        }
    }

    override fun compareTo(other: FontWeight): Int {
        return weight.compareTo(other.weight)
    }

    override fun toString(): String {
        return when (index) {
            0 -> "FontWeight.w100"
            1 -> "FontWeight.w200"
            2 -> "FontWeight.w300"
            3 -> "FontWeight.w400"
            4 -> "FontWeight.w500"
            5 -> "FontWeight.w600"
            6 -> "FontWeight.w700"
            7 -> "FontWeight.w800"
            8 -> "FontWeight.w900"
            else -> "FontWeight.unknown"
        }
    }
}
