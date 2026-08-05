@file:OptIn(kotlin.ExperimentalUnsignedTypes::class)

package www.xdyl.hygge.shared

/**
 * 纯 Kotlin 实现（无 JVM API）的 MD5 与 SHA-256 哈希。
 * 用于下载后的文件校验，必须可移植到 Kotlin/Native。
 * 注意：UInt 的 shr 已经是逻辑右移（无符号）。
 */
object Hash {

    private val HEX = "0123456789abcdef"

    // ============================ MD5 (RFC 1321) ============================

    // K[i] = floor(abs(sin(i + 1)) * 2^32)
    private val MD5_K = uintArrayOf(
        0xd76aa478u, 0xe8c7b756u, 0x242070dbu, 0xc1bdceeeu,
        0xf57c0fafu, 0x4787c62au, 0xa8304613u, 0xfd469501u,
        0x698098d8u, 0x8b44f7afu, 0xffff5bb1u, 0x895cd7beu,
        0x6b901122u, 0xfd987193u, 0xa679438eu, 0x49b40821u,
        0xf61e2562u, 0xc040b340u, 0x265e5a51u, 0xe9b6c7aau,
        0xd62f105du, 0x02441453u, 0xd8a1e681u, 0xe7d3fbc8u,
        0x21e1cde6u, 0xc33707d6u, 0xf4d50d87u, 0x455a14edu,
        0xa9e3e905u, 0xfcefa3f8u, 0x676f02d9u, 0x8d2a4c8au,
        0xfffa3942u, 0x8771f681u, 0x6d9d6122u, 0xfde5380cu,
        0xa4beea44u, 0x4bdecfa9u, 0xf6bb4b60u, 0xbebfbc70u,
        0x289b7ec6u, 0xeaa127fau, 0xd4ef3085u, 0x04881d05u,
        0xd9d4d039u, 0xe6db99e5u, 0x1fa27cf8u, 0xc4ac5665u,
        0xf4292244u, 0x432aff97u, 0xab9423a7u, 0xfc93a039u,
        0x655b59c3u, 0x8f0ccc92u, 0xffeff47du, 0x85845dd1u,
        0x6fa87e4fu, 0xfe2ce6e0u, 0xa3014314u, 0x4e0811a1u,
        0xf7537e82u, 0xbd3af235u, 0x2ad7d2bbu, 0xeb86d391u
    )

    // 每轮循环左移位数
    private val MD5_S = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    )

    /** MD5（RFC 1321），返回小写 hex。 */
    fun md5(data: ByteArray): String {
        val byteLen = data.size
        val bitLen = byteLen.toLong() * 8L
        val paddedLen = ((byteLen + 8) / 64 + 1) * 64
        val padded = ByteArray(paddedLen)
        data.copyInto(padded, 0, 0, byteLen)
        padded[byteLen] = 0x80.toByte()
        // 附加 64 位比特长度，小端序
        for (i in 0 until 8) {
            padded[paddedLen - 8 + i] = ((bitLen shr (8 * i)) and 0xffL).toByte()
        }

        var a0 = 0x67452301u
        var b0 = 0xefcdab89u
        var c0 = 0x98badcfeu
        var d0 = 0x10325476u

        var off = 0
        while (off < paddedLen) {
            val m = UIntArray(16) { i -> readLE32(padded, off + i * 4) }
            var a = a0
            var b = b0
            var c = c0
            var d = d0
            for (i in 0 until 64) {
                val f: UInt
                val g: Int
                when (i / 16) {
                    0 -> { f = (b and c) or (b.inv() and d); g = i }
                    1 -> { f = (d and b) or (d.inv() and c); g = (5 * i + 1) and 15 }
                    2 -> { f = b xor c xor d; g = (3 * i + 5) and 15 }
                    else -> { f = c xor (b or d.inv()); g = (7 * i) and 15 }
                }
                val sum = f + a + MD5_K[i] + m[g]
                val sh = MD5_S[i]
                val rot = (sum shl sh) or (sum shr (32 - sh))
                val newA = d
                val newD = c
                val newC = b
                val newB = b + rot
                a = newA; b = newB; c = newC; d = newD
            }
            a0 += a; b0 += b; c0 += c; d0 += d
            off += 64
        }

        return uintToHexLE(a0) + uintToHexLE(b0) + uintToHexLE(c0) + uintToHexLE(d0)
    }

    // ============================ SHA-256 (FIPS 180-4) ============================

    // 前 8 个素数平方根小数部分的前 32 位
    private val SHA256_H = uintArrayOf(
        0x6a09e667u, 0xbb67ae85u, 0x3c6ef372u, 0xa54ff53au,
        0x510e527fu, 0x9b05688cu, 0x1f83d9abu, 0x5be0cd19u
    )

    // 前 64 个素数立方根小数部分的前 32 位
    private val SHA256_K = uintArrayOf(
        0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u, 0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
        0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u, 0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
        0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu, 0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
        0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u, 0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
        0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u, 0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
        0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u, 0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
        0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u, 0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
        0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u, 0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u
    )

    /** SHA-256（FIPS 180-4），返回小写 hex。 */
    fun sha256(data: ByteArray): String {
        val byteLen = data.size
        val bitLen = byteLen.toLong() * 8L
        val paddedLen = ((byteLen + 8) / 64 + 1) * 64
        val padded = ByteArray(paddedLen)
        data.copyInto(padded, 0, 0, byteLen)
        padded[byteLen] = 0x80.toByte()
        // 附加 64 位比特长度，大端序
        for (i in 0 until 8) {
            padded[paddedLen - 8 + i] = ((bitLen shr (8 * (7 - i))) and 0xffL).toByte()
        }

        var h0 = SHA256_H[0]; var h1 = SHA256_H[1]; var h2 = SHA256_H[2]; var h3 = SHA256_H[3]
        var h4 = SHA256_H[4]; var h5 = SHA256_H[5]; var h6 = SHA256_H[6]; var h7 = SHA256_H[7]

        var off = 0
        while (off < paddedLen) {
            val w = UIntArray(64)
            for (i in 0 until 16) w[i] = readBE32(padded, off + i * 4)
            for (i in 16 until 64) {
                val s0 = rotr(w[i - 15], 7) xor rotr(w[i - 15], 18) xor (w[i - 15] shr 3)
                val s1 = rotr(w[i - 2], 17) xor rotr(w[i - 2], 19) xor (w[i - 2] shr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }

            var a = h0; var b = h1; var c = h2; var d = h3
            var e = h4; var f = h5; var g = h6; var h = h7
            for (i in 0 until 64) {
                val s1 = rotr(e, 6) xor rotr(e, 11) xor rotr(e, 25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + SHA256_K[i] + w[i]
                val s0 = rotr(a, 2) xor rotr(a, 13) xor rotr(a, 22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj
                h = g; g = f; f = e
                e = d + temp1
                d = c; c = b; b = a
                a = temp1 + temp2
            }
            h0 += a; h1 += b; h2 += c; h3 += d
            h4 += e; h5 += f; h6 += g; h7 += h
            off += 64
        }

        return uintToHexBE(h0) + uintToHexBE(h1) + uintToHexBE(h2) + uintToHexBE(h3) +
                uintToHexBE(h4) + uintToHexBE(h5) + uintToHexBE(h6) + uintToHexBE(h7)
    }

    // ============================ 工具 ============================

    private fun rotr(x: UInt, n: Int): UInt = (x shr n) or (x shl (32 - n))

    private fun readLE32(b: ByteArray, off: Int): UInt =
        (b[off].toUByte().toUInt()) or
                (b[off + 1].toUByte().toUInt() shl 8) or
                (b[off + 2].toUByte().toUInt() shl 16) or
                (b[off + 3].toUByte().toUInt() shl 24)

    private fun readBE32(b: ByteArray, off: Int): UInt =
        (b[off].toUByte().toUInt() shl 24) or
                (b[off + 1].toUByte().toUInt() shl 16) or
                (b[off + 2].toUByte().toUInt() shl 8) or
                (b[off + 3].toUByte().toUInt())

    /** 小端输出（MD5 结果顺序）。 */
    private fun uintToHexLE(v: UInt): String {
        val sb = StringBuilder(8)
        for (i in 0 until 4) {
            val byte = (v shr (8 * i)) and 0xffu
            sb.append(HEX[byte.toInt() shr 4])
            sb.append(HEX[byte.toInt() and 0xf])
        }
        return sb.toString()
    }

    /** 大端输出（SHA-256 结果顺序）。 */
    private fun uintToHexBE(v: UInt): String {
        val sb = StringBuilder(8)
        for (i in 0 until 4) {
            val byte = (v shr (8 * (3 - i))) and 0xffu
            sb.append(HEX[byte.toInt() shr 4])
            sb.append(HEX[byte.toInt() and 0xf])
        }
        return sb.toString()
    }
}
