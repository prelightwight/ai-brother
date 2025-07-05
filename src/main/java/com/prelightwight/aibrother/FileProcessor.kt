package com.prelightwight.aibrother

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

data class ProcessedFile(
    val id: String,
    val name: String,
    val originalName: String,
    val type: FileType,
    val size: Long,
    val localPath: String,
    val extractedText: String,
    val summary: String,
    val uploadTime: Long,
    val isAnalyzed: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)

enum class FileType {
    PDF, DOC, DOCX, TXT, XLS, XLSX, CSV, JPG, PNG, GIF, OTHER
}

class FileProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "FileProcessor"
        private const val FILES_FOLDER = "processed_files"
    }
    
    private fun getFilesDirectory(): File {
        val filesDir = File(context.getExternalFilesDir(null), FILES_FOLDER)
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        return filesDir
    }
    
    suspend fun processFileFromUri(uri: Uri): ProcessedFile? = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri)
            val fileType = getFileType(fileName)
            val fileId = UUID.randomUUID().toString()
            
            // Copy file to internal storage
            val localFile = File(getFilesDirectory(), "${fileId}_${fileName}")
            copyUriToFile(uri, localFile)
            
            // Extract text content
            val extractedText = extractTextFromFile(localFile, fileType)
            
            // Generate summary
            val summary = generateSummary(extractedText, fileType)
            
            // Get metadata
            val metadata = extractMetadata(uri, localFile, fileType)
            
            ProcessedFile(
                id = fileId,
                name = fileName,
                originalName = fileName,
                type = fileType,
                size = localFile.length(),
                localPath = localFile.absolutePath,
                extractedText = extractedText,
                summary = summary,
                uploadTime = System.currentTimeMillis(),
                isAnalyzed = true,
                metadata = metadata
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing file", e)
            null
        }
    }
    
    private fun getFileName(uri: Uri): String {
        var fileName = "unknown_file"
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
            }
        }
        
        return fileName
    }
    
    private fun getFileType(fileName: String): FileType {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> FileType.PDF
            "doc" -> FileType.DOC
            "docx" -> FileType.DOCX
            "txt" -> FileType.TXT
            "xls" -> FileType.XLS
            "xlsx" -> FileType.XLSX
            "csv" -> FileType.CSV
            "jpg", "jpeg" -> FileType.JPG
            "png" -> FileType.PNG
            "gif" -> FileType.GIF
            else -> FileType.OTHER
        }
    }
    
    private fun copyUriToFile(uri: Uri, targetFile: File) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    
    private fun extractTextFromFile(file: File, type: FileType): String {
        return try {
            when (type) {
                FileType.TXT -> extractTextFromTxt(file)
                FileType.DOC -> extractTextFromDoc(file)
                FileType.DOCX -> extractTextFromDocx(file)
                FileType.XLS -> extractTextFromXls(file)
                FileType.XLSX -> extractTextFromXlsx(file)
                FileType.CSV -> extractTextFromCsv(file)
                FileType.PDF -> extractTextFromPdf(file)
                FileType.JPG, FileType.PNG, FileType.GIF -> extractTextFromImage(file)
                else -> "Unable to extract text from this file type"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from ${type}", e)
            "Error extracting text: ${e.message}"
        }
    }
    
    private fun extractTextFromTxt(file: File): String {
        return file.readText()
    }
    
    private fun extractTextFromDoc(file: File): String {
        return try {
            file.inputStream().use { inputStream ->
                val document = HWPFDocument(inputStream)
                val extractor = WordExtractor(document)
                extractor.text
            }
        } catch (e: Exception) {
            "Error reading DOC file: ${e.message}"
        }
    }
    
    private fun extractTextFromDocx(file: File): String {
        return try {
            file.inputStream().use { inputStream ->
                val document = XWPFDocument(inputStream)
                val extractor = XWPFWordExtractor(document)
                extractor.text
            }
        } catch (e: Exception) {
            "Error reading DOCX file: ${e.message}"
        }
    }
    
    private fun extractTextFromXls(file: File): String {
        return try {
            file.inputStream().use { inputStream ->
                val workbook = HSSFWorkbook(inputStream)
                val sb = StringBuilder()
                
                for (i in 0 until workbook.numberOfSheets) {
                    val sheet = workbook.getSheetAt(i)
                    sb.append("Sheet: ${sheet.sheetName}\n")
                    
                    for (row in sheet) {
                        for (cell in row) {
                            sb.append("${cell} ")
                        }
                        sb.append("\n")
                    }
                    sb.append("\n")
                }
                
                workbook.close()
                sb.toString()
            }
        } catch (e: Exception) {
            "Error reading XLS file: ${e.message}"
        }
    }
    
    private fun extractTextFromXlsx(file: File): String {
        return try {
            file.inputStream().use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)
                val sb = StringBuilder()
                
                for (i in 0 until workbook.numberOfSheets) {
                    val sheet = workbook.getSheetAt(i)
                    sb.append("Sheet: ${sheet.sheetName}\n")
                    
                    for (row in sheet) {
                        for (cell in row) {
                            sb.append("${cell} ")
                        }
                        sb.append("\n")
                    }
                    sb.append("\n")
                }
                
                workbook.close()
                sb.toString()
            }
        } catch (e: Exception) {
            "Error reading XLSX file: ${e.message}"
        }
    }
    
    private fun extractTextFromCsv(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            "Error reading CSV file: ${e.message}"
        }
    }
    
    private fun extractTextFromPdf(file: File): String {
        // For now, return a placeholder. PDF text extraction with iText7 requires more complex setup
        return "PDF text extraction not yet implemented. File size: ${file.length()} bytes"
    }
    
    private fun extractTextFromImage(file: File): String {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                val width = bitmap.width
                val height = bitmap.height
                "Image analyzed: ${width}x${height} pixels. OCR text extraction not yet implemented."
            } else {
                "Unable to decode image file"
            }
        } catch (e: Exception) {
            "Error analyzing image: ${e.message}"
        }
    }
    
    private fun generateSummary(extractedText: String, type: FileType): String {
        val wordCount = extractedText.split("\\s+".toRegex()).size
        
        return when (type) {
            FileType.TXT -> "Text file with $wordCount words"
            FileType.DOC, FileType.DOCX -> "Document with $wordCount words"
            FileType.XLS, FileType.XLSX -> "Spreadsheet with data"
            FileType.CSV -> "CSV data file"
            FileType.PDF -> "PDF document"
            FileType.JPG, FileType.PNG, FileType.GIF -> "Image file"
            else -> "File processed successfully"
        }
    }
    
    private fun extractMetadata(uri: Uri, file: File, type: FileType): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        metadata["file_size"] = formatFileSize(file.length())
        metadata["file_type"] = type.name
        metadata["created_date"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Image-specific metadata
        if (type in listOf(FileType.JPG, FileType.PNG)) {
            try {
                val exif = ExifInterface(file.absolutePath)
                exif.getAttribute(ExifInterface.TAG_DATETIME)?.let {
                    metadata["image_datetime"] = it
                }
                exif.getAttribute(ExifInterface.TAG_MAKE)?.let {
                    metadata["camera_make"] = it
                }
                exif.getAttribute(ExifInterface.TAG_MODEL)?.let {
                    metadata["camera_model"] = it
                }
            } catch (e: IOException) {
                Log.w(TAG, "Could not read EXIF data", e)
            }
        }
        
        return metadata
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
    
    fun deleteFile(processedFile: ProcessedFile): Boolean {
        return try {
            val file = File(processedFile.localPath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            false
        }
    }
}