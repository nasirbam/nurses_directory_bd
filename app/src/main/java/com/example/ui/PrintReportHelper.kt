package com.example.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextUtils
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.ApprovalStatus
import com.example.data.NurseEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PrintColumn(val label: String, val defaultChecked: Boolean) {
    NAME("Full Name", true),
    FATHER_NAME("Father's Name", false),
    MOTHER_NAME("Mother's Name", false),
    CATEGORY("Category", true),
    DESIGNATION("Designation", true),
    REG_NO("BNMC / Student Reg.", true),
    GOVT_UID("Govt. UID", false),
    MOBILE("Mobile", true),
    WHATSAPP("WhatsApp", false),
    EMAIL("Email", false),
    WORKPLACE("Working Place / Institute", true),
    LOCATION("Division / District / Upazila", true),
    ADDRESS("Present Address", false),
    EDUCATION("Education / Degrees", false),
    SPECIALTY("Clinical Specialty", false),
    BLOOD_GROUP("Blood Group", false),
    EXPERIENCE("Experience (Yrs)", false),
    STATUS("Approval Status", false)
}

object PrintReportHelper {
    // Keep strong reference until WebView finishes loading and hands document adapter to PrintManager
    private var activePrintWebView: WebView? = null

    fun printNurseDirectoryPdf(
        context: Context,
        nurses: List<NurseEntity>,
        selectedColumns: List<PrintColumn>,
        reportTitle: String,
        scopeSubtitle: String,
        generatedBy: String,
        onStarted: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (nurses.isEmpty() || selectedColumns.isEmpty()) {
            onError("Select at least one nurse record and one column to print.")
            return
        }

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            onError("Android Print service is unavailable on this device.")
            return
        }

        try {
            val html = buildReportHtml(
                nurses = nurses,
                selectedColumns = selectedColumns,
                reportTitle = reportTitle,
                scopeSubtitle = scopeSubtitle,
                generatedBy = generatedBy
            )

            val webView = WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
            }
            activePrintWebView = webView

            val jobName = "NursesBD_Report_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}"
            val mediaSize = if (selectedColumns.size > 5) {
                PrintAttributes.MediaSize.ISO_A4.asLandscape()
            } else {
                PrintAttributes.MediaSize.ISO_A4.asPortrait()
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    val adapter = view.createPrintDocumentAdapter(jobName)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(mediaSize)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print(jobName, adapter, attributes)
                    activePrintWebView = null
                    onStarted()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        activePrintWebView = null
                        onError("Failed to prepare printable report.")
                    }
                }
            }

            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            activePrintWebView = null
            onError(e.localizedMessage ?: "Unable to launch print preview.")
        }
    }

    internal fun buildReportHtml(
        nurses: List<NurseEntity>,
        selectedColumns: List<PrintColumn>,
        reportTitle: String,
        scopeSubtitle: String,
        generatedBy: String
    ): String {
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
        val sb = StringBuilder()
        sb.append(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <style>
                @page { size: auto; margin: 12mm; }
                body { font-family: sans-serif; color: #162220; font-size: 11px; margin: 0; padding: 0; }
                .header { border-bottom: 2px solid #006B5E; padding-bottom: 8px; margin-bottom: 10px; }
                .title { font-size: 17px; font-weight: 700; color: #00584D; margin: 0 0 3px 0; }
                .meta { font-size: 10px; color: #4A5B58; margin: 0; line-height: 1.45; }
                table { width: 100%; border-collapse: collapse; margin-top: 8px; }
                th { background-color: #E6F3F1; color: #004D43; font-weight: 700; text-align: left;
                     border: 1px solid #B9D2CE; padding: 6px 5px; font-size: 10px; }
                td { border: 1px solid #D3E0DE; padding: 5px; vertical-align: top; font-size: 10px; word-break: break-word; }
                tr:nth-child(even) td { background-color: #F8FBFA; }
                tr { page-break-inside: avoid; }
                .verified { color: #0A7B3E; font-weight: 700; }
                .footer { margin-top: 10px; font-size: 9px; color: #617370; text-align: right; }
              </style>
            </head>
            <body>
              <div class="header">
                <p class="title">${escapeHtml(reportTitle)}</p>
                <p class="meta">
                  <strong>Scope / Filter:</strong> ${escapeHtml(scopeSubtitle)} &nbsp;|&nbsp;
                  <strong>Total Records:</strong> ${nurses.size}<br/>
                  <strong>Generated By:</strong> ${escapeHtml(generatedBy)} &nbsp;|&nbsp;
                  <strong>Date:</strong> ${escapeHtml(timestamp)}
                </p>
              </div>
              <table>
                <thead>
                  <tr>
                    <th style="width: 28px;">#</th>
            """.trimIndent()
        )

        selectedColumns.forEach { col ->
            sb.append("<th>").append(escapeHtml(col.label)).append("</th>")
        }
        sb.append("</tr></thead><tbody>")

        nurses.forEachIndexed { index, nurse ->
            sb.append("<tr><td>").append(index + 1).append("</td>")
            selectedColumns.forEach { col ->
                val cellHtml = formatCellHtml(nurse, col)
                sb.append("<td>").append(cellHtml).append("</td>")
            }
            sb.append("</tr>")
        }

        sb.append(
            """
                </tbody>
              </table>
              <div class="footer">Nurses BD E-Directory • Official Administrative Print Report</div>
            </body>
            </html>
            """.trimIndent()
        )
        return sb.toString()
    }

    private fun formatCellHtml(nurse: NurseEntity, column: PrintColumn): String {
        val value = when (column) {
            PrintColumn.NAME -> nurse.fullName
            PrintColumn.FATHER_NAME -> nurse.fatherName.ifBlank { "-" }
            PrintColumn.MOTHER_NAME -> nurse.motherName.ifBlank { "-" }
            PrintColumn.CATEGORY -> nurse.categoryEnum.label
            PrintColumn.DESIGNATION -> nurse.designation
            PrintColumn.REG_NO -> {
                val base = escapeHtml(nurse.bnmcNumber)
                return if (nurse.approvalEnum == ApprovalStatus.APPROVED) {
                    "$base <span class=\"verified\">&#10004; Verified</span>"
                } else {
                    base
                }
            }
            PrintColumn.GOVT_UID -> nurse.govtUid.ifBlank { "-" }
            PrintColumn.MOBILE -> nurse.phone
            PrintColumn.WHATSAPP -> nurse.whatsapp.ifBlank { nurse.phone }
            PrintColumn.EMAIL -> nurse.email.ifBlank { "-" }
            PrintColumn.WORKPLACE -> nurse.hospitalName.ifBlank { "Not specified" }
            PrintColumn.LOCATION -> listOfNotNull(
                nurse.upazila.takeIf { it.isNotBlank() },
                nurse.district.takeIf { it.isNotBlank() },
                nurse.division.takeIf { it.isNotBlank() }
            ).joinToString(", ")
            PrintColumn.ADDRESS -> nurse.address.ifBlank { "-" }
            PrintColumn.EDUCATION -> nurse.educationSummaryText().ifBlank { nurse.qualification }
            PrintColumn.SPECIALTY -> nurse.specialty
            PrintColumn.BLOOD_GROUP -> nurse.bloodGroup
            PrintColumn.EXPERIENCE -> "${nurse.experienceYears} yrs"
            PrintColumn.STATUS -> nurse.approvalEnum.name
        }
        return escapeHtml(value)
    }

    private fun escapeHtml(raw: String): String {
        return try {
            TextUtils.htmlEncode(raw)
        } catch (_: Throwable) {
            raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
        }
    }
}
