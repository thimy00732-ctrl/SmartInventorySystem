package com.sis.backend.serializer

import java.math.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Chuyển đổi BigDecimal thành chuỗi khi đọc và ghi JSON.
 *
 * Việc sử dụng chuỗi giúp hạn chế sai số khi truyền dữ liệu tiền tệ.
 */
object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    /**
     * Chuyển giá trị BigDecimal thành chuỗi JSON.
     */
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    /**
     * Chuyển chuỗi JSON thành giá trị BigDecimal.
     */
    override fun deserialize(decoder: Decoder): BigDecimal {
        val value = decoder.decodeString()

        return try {
            BigDecimal(value)
        } catch (e: NumberFormatException) {
            throw SerializationException("Giá trị BigDecimal không hợp lệ: $value", e)
        }
    }
}