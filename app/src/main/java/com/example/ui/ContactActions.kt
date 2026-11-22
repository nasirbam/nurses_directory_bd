package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.NurseEntity
import com.example.data.SecurityUtils

object ContactActions {
    fun dialPhone(context: Context, phone: String) {
        val cleaned = phone.trim()
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned"))
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "No phone dialer available", Toast.LENGTH_SHORT).show()
            }
    }

    fun sendSms(context: Context, phone: String, nurseName: String) {
        val cleaned = phone.trim()
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleaned")).apply {
            putExtra("sms_body", "Assalamu Alaikum $nurseName, contacting via Nurses BD E-Directory.")
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
            }
    }

    fun openWhatsApp(context: Context, whatsappNumber: String, nurseName: String) {
        val digits = SecurityUtils.formatWhatsAppDigits(whatsappNumber)
        val encodedText = Uri.encode("Assalamu Alaikum $nurseName, reaching out via Nurses BD Directory.")
        val uri = Uri.parse("https://wa.me/$digits?text=$encodedText")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
            }
    }

    fun sendEmail(context: Context, email: String, nurseName: String) {
        if (email.isBlank()) {
            Toast.makeText(context, "No email listed for this nurse", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${email.trim()}")).apply {
            putExtra(Intent.EXTRA_SUBJECT, "Nurses BD Directory Inquiry — $nurseName")
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "No email client available", Toast.LENGTH_SHORT).show()
            }
    }

    fun copyContact(context: Context, nurse: NurseEntity) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val summary = "${nurse.fullName} (${nurse.bnmcNumber}) — ${nurse.hospitalName}, ${nurse.district} | Tel: ${nurse.phone}"
        clipboard.setPrimaryClip(ClipData.newPlainText("Nurse Contact", summary))
        Toast.makeText(context, "Contact copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareNurseCard(context: Context, nurse: NurseEntity) {
        val shareBody = buildString {
            appendLine("Nurses BD E-Directory")
            appendLine("Name: ${nurse.fullName}")
            appendLine("BNMC Reg: ${nurse.bnmcNumber}")
            appendLine("Role: ${nurse.designation} (${nurse.specialty})")
            appendLine("Hospital: ${nurse.hospitalName}")
            appendLine("Location: ${nurse.district}, ${nurse.division} Division")
            appendLine("Phone: ${nurse.phone}")
            if (nurse.email.isNotBlank()) appendLine("Email: ${nurse.email}")
            append("Blood Group: ${nurse.bloodGroup}")
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Nurse Profile"))
    }

    fun openWebUrl(context: Context, url: String) {
        val cleanUrl = url.trim()
        if (!cleanUrl.startsWith("https://", ignoreCase = true)) {
            Toast.makeText(context, "Invalid or insecure update URL", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "No browser app found to open link", Toast.LENGTH_SHORT).show()
            }
    }
}
