package com.example.hacaton.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hacaton.retrofit.ExplanationRequest
import com.example.hacaton.retrofit.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

data class RecentFile(val uri: String, val name: String)

data class ExplanationItem(
    val id: String = UUID.randomUUID().toString(),
    val originalText: String,
    val title: String? = null,
    val explanation: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPinned: Boolean = false // Added pinned state
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()

    private val _fileContent = MutableStateFlow<String?>(null)
    val fileContent: StateFlow<String?> = _fileContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _explanations = MutableStateFlow<List<ExplanationItem>>(emptyList())
    val explanations: StateFlow<List<ExplanationItem>> = _explanations.asStateFlow()

    private val prefs = application.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var currentFileUri: String? = null

    init {
        loadRecentFiles()
        loadLastOpenedFile()
    }

    fun explainText(text: String) {
        val newItem = ExplanationItem(originalText = text)
        val currentList = _explanations.value.toMutableList()
        
        // Add new item after pinned items
        val pinnedCount = currentList.count { it.isPinned }
        currentList.add(pinnedCount, newItem) // Add at index = number of pinned items
        
        _explanations.value = currentList

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.explainText(ExplanationRequest(text))
                
                val updatedItem = newItem.copy(
                    isLoading = false,
                    title = response.title,
                    explanation = response.text
                )
                
                updateAndSaveExplanations(updatedItem)

            } catch (e: Exception) {
                val errorItem = newItem.copy(
                    isLoading = false,
                    error = "Error: ${e.localizedMessage}"
                )
                updateAndSaveExplanations(errorItem)
                e.printStackTrace()
            }
        }
    }
    
    private fun updateAndSaveExplanations(updatedItem: ExplanationItem) {
        val updatedList = _explanations.value.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
        _explanations.value = updatedList
        saveExplanationsForFile(currentFileUri, updatedList)
    }

    fun togglePin(id: String) {
        val currentList = _explanations.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = currentList[index]
            val newItem = item.copy(isPinned = !item.isPinned)
            
            // Remove old item and re-insert based on new pin status
            currentList.removeAt(index)
            
            // Logic: Pinned items first, then Unpinned items (keeping relative order if possible, or adding to top of section)
            // Re-sort: Pinned (descending), then original order? Or stable sort?
            // Simplest: If pinning -> move to index 0 (or end of pinned group).
            // If unpinning -> move to after pinned group.
            
            // Let's just re-sort everything: Pinned first, then keep existing order for others?
            // Better: Use sortedByDescending { it.isPinned } which is stable.
            
            // We insert the modified item back first
            currentList.add(index, newItem) 
            
            // Then sort
            val sortedList = currentList.sortedByDescending { it.isPinned }
            
            _explanations.value = sortedList
            saveExplanationsForFile(currentFileUri, sortedList)
        }
    }

    fun deleteExplanation(id: String) {
        val currentList = _explanations.value.toMutableList()
        currentList.removeIf { it.id == id }
        _explanations.value = currentList
        saveExplanationsForFile(currentFileUri, currentList)
    }

    fun openFile(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            currentFileUri = uri.toString()
            _explanations.value = loadExplanationsForFile(currentFileUri)
            
            try {
                val contentResolver = getApplication<Application>().contentResolver
                
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val text = withContext(Dispatchers.IO) {
                    readFileContent(uri)
                }
                _fileContent.value = text
                
                val name = getFileName(uri) ?: uri.lastPathSegment ?: "Unknown"
                val newFile = RecentFile(uri.toString(), name)
                val currentList = _recentFiles.value.toMutableList()
                currentList.removeIf { it.uri == newFile.uri }
                currentList.add(0, newFile)
                _recentFiles.value = currentList
                
                saveRecentFiles(currentList)
                saveLastOpenedFile(uri.toString())

            } catch (e: Exception) {
                _fileContent.value = "Error reading file: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun saveExplanationsForFile(fileUri: String?, explanations: List<ExplanationItem>) {
        if (fileUri == null) return
        val explanationsToSave = explanations.filter { !it.isLoading }
        val json = gson.toJson(explanationsToSave)
        prefs.edit().putString("explanations_$fileUri", json).apply()
    }

    private fun loadExplanationsForFile(fileUri: String?): List<ExplanationItem> {
        if (fileUri == null) return emptyList()
        val json = prefs.getString("explanations_$fileUri", null)
        return if (json != null) {
            val type = object : TypeToken<List<ExplanationItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun readFileContent(uri: Uri): String {
        val contentResolver = getApplication<Application>().contentResolver
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val mimeType = contentResolver.getType(uri)
                val isDocx = mimeType?.contains("wordprocessingml.document") == true ||
                        uri.toString().endsWith(".docx", ignoreCase = true)

                when {
                    isDocx -> readDocx(inputStream)
                    else -> readText(inputStream)
                }
            } ?: "Could not open file"
        } catch (e: Exception) {
            "Error parsing file: ${e.message}"
        }
    }

    private fun readText(inputStream: InputStream): String {
        return BufferedReader(InputStreamReader(inputStream)).readText()
    }

    private fun readDocx(inputStream: InputStream): String {
        return try {
            XWPFDocument(inputStream).use { doc ->
                XWPFWordExtractor(doc).text
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to read DOCX: ${e.message}", e)
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/')
        }
        return result
    }

    private fun saveRecentFiles(files: List<RecentFile>) {
        val data = files.joinToString(";") { "${it.uri}|${it.name}" }
        prefs.edit().putString("recent_files_list", data).apply()
    }

    private fun loadRecentFiles() {
        val data = prefs.getString("recent_files_list", null)
        if (data != null) {
            _recentFiles.value = data.split(";").mapNotNull {
                val parts = it.split("|")
                if (parts.size == 2) RecentFile(parts[0], parts[1]) else null
            }
        }
    }
    
    private fun saveLastOpenedFile(uriString: String) {
        prefs.edit().putString("last_opened_uri", uriString).apply()
    }
    
    private fun loadLastOpenedFile() {
        prefs.getString("last_opened_uri", null)?.let { 
            openFile(Uri.parse(it))
        }
    }
}
