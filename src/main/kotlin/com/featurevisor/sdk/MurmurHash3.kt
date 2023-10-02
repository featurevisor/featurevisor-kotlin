package com.featurevisor.sdk

/**
 * Taken from: https://github.com/goncalossilva/kotlinx-murmurhash
 *
 * (Copied to make it work, we can resort to using it as a package later)
 *
 * ---
 *
 * MIT License
 *
 * Copyright (c) 2021-2022 Gonçalo Silva
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class MurmurHash3(private val seed: UInt = 1.toUInt()) {
    public fun hash32x86(key: ByteArray): UInt {
        var h = seed
        val len = key.size
        val nblocks = len / 4

        for (i in 0 until nblocks * 4 step 4) {
            val k = key.getLittleEndianUInt(i)

            h = h xor k.mix(R1_32, C1_32, C2_32)
            h = h.rotateLeft(R2_32)
            h = h * M_32 + N_32
        }

        val index = nblocks * 4
        val rem = len - index
        var k = 0u
        if (rem == 3) {
            k = k xor (key.getUInt(index + 2) shl 16)
        }
        if (rem >= 2) {
            k = k xor (key.getUInt(index + 1) shl 8)
        }
        if (rem >= 1) {
            k = k xor key.getUInt(index)
            h = h xor k.mix(R1_32, C1_32, C2_32)
        }

        h = h xor len.toUInt()

        h = h.fmix()

        return h
    }

    private fun ByteArray.getLittleEndianUInt(index: Int): UInt {
        return this.getUInt(index) or
            (this.getUInt(index + 1) shl 8) or
            (this.getUInt(index + 2) shl 16) or
            (this.getUInt(index + 3) shl 24)
    }

    private fun ByteArray.getLittleEndianLong(index: Int): ULong {
        return this.getULong(index) or
            (this.getULong(index + 1) shl 8) or
            (this.getULong(index + 2) shl 16) or
            (this.getULong(index + 3) shl 24) or
            (this.getULong(index + 4) shl 32) or
            (this.getULong(index + 5) shl 40) or
            (this.getULong(index + 6) shl 48) or
            (this.getULong(index + 7) shl 56)
    }

    private fun UInt.mix(r: Int, c1: UInt, c2: UInt): UInt {
        var k = this
        k *= c1
        k = k.rotateLeft(r)
        k *= c2
        return k
    }

    private fun ULong.mix(r: Int, c1: ULong, c2: ULong): ULong {
        var k = this
        k *= c1
        k = k.rotateLeft(r)
        k *= c2
        return k
    }

    private fun UInt.fmix(): UInt {
        var h = this
        h = h xor (h shr 16)
        h *= 0x85ebca6bu
        h = h xor (h shr 13)
        h *= 0xc2b2ae35u
        h = h xor (h shr 16)
        return h
    }

    private fun ULong.fmix(): ULong {
        var h = this
        h = h xor (h shr 33)
        h *= 0xff51afd7ed558ccduL
        h = h xor (h shr 33)
        h *= 0xc4ceb9fe1a85ec53uL
        h = h xor (h shr 33)
        return h
    }

    private fun ByteArray.getUInt(index: Int) = get(index).toUByte().toUInt()

    private fun ByteArray.getULong(index: Int) = get(index).toUByte().toULong()

    private companion object {
        private const val C1_32: UInt = 0xcc9e2d51u
        private const val C2_32: UInt = 0x1b873593u

        private const val R1_32: Int = 15
        private const val R2_32: Int = 13

        private const val M_32: UInt = 5u
        private const val N_32: UInt = 0xe6546b64u

        private const val C1_128x86: UInt = 0x239b961bu
        private const val C2_128x86: UInt = 0xab0e9789u
        private const val C3_128x86: UInt = 0x38b34ae5u
        private const val C4_128x86: UInt = 0xa1e38b93u

        private const val R1_128x86: Int = 15
        private const val R2_128x86: Int = 16
        private const val R3_128x86: Int = 17
        private const val R4_128x86: Int = 18
        private const val R5_128x86: Int = 19
        private const val R6_128x86: Int = 13

        private const val M_128x86: UInt = 5u
        private const val N1_128x86: UInt = 0x561ccd1bu
        private const val N2_128x86: UInt = 0x0bcaa747u
        private const val N3_128x86: UInt = 0x96cd1c35u
        private const val N4_128x86: UInt = 0x32ac3b17u

        private const val C1_128x64: ULong = 0x87c37b91114253d5uL
        private const val C2_128x64: ULong = 0x4cf5ad432745937fuL

        private const val R1_128x64: Int = 31
        private const val R2_128x64: Int = 27
        private const val R3_128x64: Int = 33

        private const val M_128x64: ULong = 5u
        private const val N1_128x64: ULong = 0x52dce729u
        private const val N2_128x64: ULong = 0x38495ab5u
    }
}
