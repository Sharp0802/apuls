package com.apulsetech.apuls2.platform

data class MailMessage(
    val from: String,
    val to: String,
    val subject: String,
    val body: String
)

interface MailSender {
    suspend fun send(message: MailMessage)
}
