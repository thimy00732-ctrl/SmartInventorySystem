package com.sis.android.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class AuthValidationTest {
    @Test
    fun loginRequiresValidEmailAndPassword() {
        assertNull(AuthValidation.validateLogin("user@example.com", "password"))
        assertEquals(
            "Enter a valid email address.",
            AuthValidation.validateLogin("not-an-email", "password")
        )
        assertEquals("Password is required.", AuthValidation.validateLogin("a@b.com", ""))
    }

    @Test
    fun passwordChangeChecksLengthDifferenceAndConfirmation() {
        assertNull(AuthValidation.validatePasswordChange("oldPassword1", "newPassword1", "newPassword1"))
        assertEquals(
            "New password must be at least 8 characters.",
            AuthValidation.validatePasswordChange("oldPassword1", "short", "short")
        )
        assertEquals(
            "New password must be different from the current password.",
            AuthValidation.validatePasswordChange("samePassword", "samePassword", "samePassword")
        )
        assertEquals(
            "New passwords do not match.",
            AuthValidation.validatePasswordChange("oldPassword", "newPassword", "differentPassword")
        )
    }

    @Test
    fun userValidationRequiresNameAndValidEmail() {
        assertNull(AuthValidation.validateUser("Inventory staff", "staff@example.com"))
        assertEquals("Full name is required.", AuthValidation.validateUser(" ", "staff@example.com"))
    }

    @Test
    fun duplicateEmailApiMessageIsShownToTheUser() {
        val body = """{"message":"Email đã tồn tại trong hệ thống","code":"EMAIL_ALREADY_EXISTS"}"""
            .toResponseBody("application/json".toMediaType())
        val error = HttpException(Response.error<Any>(409, body))

        assertEquals(
            "Email đã tồn tại trong hệ thống",
            AuthRepository.errorMessage(error)
        )
    }
}
