package com.sis.backend.service

import com.sis.backend.exception.AuthException
import io.ktor.http.HttpStatusCode
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.time.LocalDateTime
import java.util.Properties

fun interface TemporaryPasswordNotifier {
    fun send(email: String, fullName: String, password: String, expiresAt: LocalDateTime)
}

class SmtpTemporaryPasswordNotifier(
    private val environment: Map<String, String> = System.getenv()
) : TemporaryPasswordNotifier {
    override fun send(
        email: String,
        fullName: String,
        password: String,
        expiresAt: LocalDateTime
    ) {
        val host = environment["SMTP_HOST"]?.takeIf(String::isNotBlank)
            ?: throw AuthException(
                HttpStatusCode.ServiceUnavailable,
                "EMAIL_NOT_CONFIGURED",
                "Chưa cấu hình dịch vụ gửi email"
            )
        val from = environment["SMTP_FROM"]?.takeIf(String::isNotBlank)
            ?: throw AuthException(
                HttpStatusCode.ServiceUnavailable,
                "EMAIL_NOT_CONFIGURED",
                "Chưa cấu hình địa chỉ gửi email"
            )
        val port = environment["SMTP_PORT"]?.toIntOrNull() ?: 587
        val username = environment["SMTP_USERNAME"]?.takeIf(String::isNotBlank)
        val passwordFromEnvironment = environment["SMTP_PASSWORD"]

        val properties = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", (username != null).toString())
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
        }
        val session = if (username != null) {
            Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(username, passwordFromEnvironment.orEmpty())
            })
        } else {
            Session.getInstance(properties)
        }

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            subject = "Thông tin tài khoản Smart Inventory"
            setText(
                """
                Xin chào $fullName,

                Tài khoản Smart Inventory của bạn đã được tạo.
                Mật khẩu tạm thời: $password
                Vui lòng đăng nhập và đổi mật khẩu trước ${expiresAt}.

                Nếu bạn không yêu cầu tài khoản này, vui lòng liên hệ quản trị viên.
                """.trimIndent(),
                Charsets.UTF_8.name()
            )
        }
        Transport.send(message)
    }
}
