package com.example.yukti.createbusiness

import android.content.Context
import android.content.Intent


import android.os.Environment
import android.util.Log
import android.widget.Toast

import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson


import java.io.FileWriter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

class ExportChatData {
    fun exportChatData(
        context: Context,
        businessId: String,
        businessName: String,
        currentUserUid: String
    ) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("chats").child("businessChats").child(businessId).child(businessName).child(currentUserUid)

        // Fetch chat data
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val chatData = dataSnapshot.value
            exportPdfData(context, chatData.toString())

//            if (chatData != null) {
//                try {
//                    // Convert data to JSON format (or other format)
//                    val jsonData = Gson().toJson(chatData)
//
//                    // Create a file
//                    val fileName = "chat_data_${System.currentTimeMillis()}.json"
//                    val file = File(
//                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
//                        fileName
//                    )
//
//                    // Write data to the file
//                    val writer = FileWriter(file)
//                    writer.write(jsonData)
//                    writer.close()
//
//                    // Share the file
////                    shareFile(file, context)
//
//
//                    Toast.makeText(context, "Data exported successfully!", Toast.LENGTH_SHORT).show()
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Error exporting data: ${e.message}", Toast.LENGTH_SHORT).show()
//                    Log.d("ExportChatData", "Error exporting data: ${e.message}")
//                    e.printStackTrace()
//                }
//            } else {
//                Toast.makeText(context, "No chat data found to export!", Toast.LENGTH_SHORT).show()
//            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to fetch chat data: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun shareFile(file: File, context: Context) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Chat Data"))
    }

    fun generateDynamicPdfFile(context: Context, fileName: String, data: String): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        // Set up page dimensions (A4 size: 595x842 points)
        val pageWidth = 595
        val pageHeight = 842
        val lineHeight = 15f
        val margin = 20f
        val contentWidth = pageWidth - 2 * margin
        val contentHeight = pageHeight - 2 * margin
        val linesPerPage = (contentHeight / lineHeight).toInt()

        // Split data into lines
        val lines = data.split("\n")
        var currentLine = 0

        while (currentLine < lines.size) {
            // Create a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
            val page = pdfDocument.startPage(pageInfo)

            // Draw content on the page
            val canvas: Canvas = page.canvas
            var y = margin

            while (currentLine < lines.size && y + lineHeight <= contentHeight + margin) {
                canvas.drawText(lines[currentLine], margin, y, paint)
                y += lineHeight
                currentLine++
            }

            pdfDocument.finishPage(page)
        }

        // Save the PDF to a file
        val file = File(context.getExternalFilesDir(null), "$fileName.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }


    fun exportPdfData(context: Context, data: String) {
        val file = generateDynamicPdfFile(context, "chat_data", data)
        if (file != null) {
            sharePdfFile(context, file)
        } else {
            Log.e("ExportPDF", "Error generating PDF")
        }
    }

    fun sharePdfFile(context: Context, file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share PDF File"))
    }



}