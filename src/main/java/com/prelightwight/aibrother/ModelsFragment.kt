package com.prelightwight.aibrother

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.prelightwight.aibrother.llm.ModelDownloader
import com.prelightwight.aibrother.llm.ModelInfo
import com.prelightwight.aibrother.llm.LlamaInterface
import kotlinx.coroutines.launch

class ModelsFragment : Fragment() {
    
    private lateinit var modelDownloader: ModelDownloader
    private lateinit var llamaInterface: LlamaInterface
    private lateinit var modelsListView: ListView
    private lateinit var storageInfoText: TextView
    private lateinit var refreshButton: Button
    private var modelAdapter: ModelAdapter? = null
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_models, container, false)
        
        modelDownloader = ModelDownloader(requireContext())
        llamaInterface = LlamaInterface.getInstance()
        
        initializeViews(view)
        setupUI()
        refreshModelList()
        
        return view
    }
    
    private fun initializeViews(view: View) {
        modelsListView = view.findViewById(R.id.modelsListView)
        storageInfoText = view.findViewById(R.id.storageInfoText)
        refreshButton = view.findViewById(R.id.refreshButton)
    }
    
    private fun setupUI() {
        refreshButton.setOnClickListener {
            refreshModelList()
        }
    }
    
    private fun refreshModelList() {
        val availableModels = modelDownloader.getAvailableModels()
        
        // Update storage info
        val storageInfo = modelDownloader.getModelStorageInfo()
        storageInfoText.text = "Storage: ${storageInfo.totalUsedMB} MB used • " +
                "${storageInfo.downloadedModelsCount} models downloaded • " +
                "${storageInfo.availableMB} MB available"
        
        // Update models list
        modelAdapter = ModelAdapter(availableModels)
        modelsListView.adapter = modelAdapter
    }
    
    private inner class ModelAdapter(private val models: List<ModelInfo>) : BaseAdapter() {
        
        override fun getCount(): Int = models.size
        override fun getItem(position: Int): ModelInfo = models[position]
        override fun getItemId(position: Int): Long = position.toLong()
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_model, parent, false)
            val model = getItem(position)
            
            setupModelView(view, model)
            return view
        }
        
        private fun setupModelView(view: View, model: ModelInfo) {
            val nameText = view.findViewById<TextView>(R.id.modelName)
            val descriptionText = view.findViewById<TextView>(R.id.modelDescription)
            val detailsText = view.findViewById<TextView>(R.id.modelDetails)
            val actionButton = view.findViewById<Button>(R.id.modelActionButton)
            val progressBar = view.findViewById<ProgressBar>(R.id.downloadProgress)
            val progressText = view.findViewById<TextView>(R.id.progressText)
            val recommendedBadge = view.findViewById<TextView>(R.id.recommendedBadge)
            
            // Set model info
            nameText.text = model.displayName
            descriptionText.text = model.description
            detailsText.text = "${model.parameters} • ${model.quantization} • ~${model.estimatedSizeMB} MB"
            
            // Show recommended badge
            recommendedBadge.visibility = if (model.isRecommended) View.VISIBLE else View.GONE
            
            // Hide progress initially
            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE
            
            // Set up action button based on model state
            val isDownloaded = modelDownloader.isModelDownloaded(model)
            val currentlyLoadedPath = llamaInterface.getLoadedModelPath()
            val modelPath = modelDownloader.getModelFilePath(model).absolutePath
            val isCurrentlyLoaded = currentlyLoadedPath == modelPath
            
            when {
                isCurrentlyLoaded -> {
                    actionButton.text = "Loaded ✓"
                    actionButton.isEnabled = false
                    actionButton.setBackgroundResource(android.R.drawable.button_onoff_indicator_on)
                }
                isDownloaded -> {
                    actionButton.text = "Load"
                    actionButton.isEnabled = true
                    actionButton.setBackgroundResource(android.R.drawable.btn_default)
                    actionButton.setOnClickListener {
                        loadModel(model, actionButton)
                    }
                }
                else -> {
                    actionButton.text = "Download"
                    actionButton.isEnabled = true
                    actionButton.setBackgroundResource(android.R.drawable.btn_default)
                    actionButton.setOnClickListener {
                        downloadModel(model, actionButton, progressBar, progressText)
                    }
                }
            }
            
            // Add long press for delete option on downloaded models
            if (isDownloaded && !isCurrentlyLoaded) {
                view.setOnLongClickListener {
                    showDeleteDialog(model)
                    true
                }
            }
        }
        
        private fun loadModel(model: ModelInfo, actionButton: Button) {
            actionButton.isEnabled = false
            actionButton.text = "Loading..."
            
            lifecycleScope.launch {
                try {
                    val modelPath = modelDownloader.getModelFilePath(model).absolutePath
                    val success = llamaInterface.loadModel(modelPath)
                    
                    if (success) {
                        Toast.makeText(context, "Model loaded: ${model.displayName}", Toast.LENGTH_SHORT).show()
                        activity?.findViewById<TextView>(R.id.status_text)?.text = "AI Ready"
                        refreshModelList()
                    } else {
                        actionButton.isEnabled = true
                        actionButton.text = "Load"
                        Toast.makeText(context, "Failed to load model", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    actionButton.isEnabled = true
                    actionButton.text = "Load"
                    Toast.makeText(context, "Error loading model: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        
        private fun downloadModel(
            model: ModelInfo, 
            actionButton: Button, 
            progressBar: ProgressBar, 
            progressText: TextView
        ) {
            actionButton.isEnabled = false
            actionButton.text = "Downloading..."
            progressBar.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                modelDownloader.downloadModel(model).collect { progress ->
                    when {
                        progress.error != null -> {
                            // Download failed
                            actionButton.isEnabled = true
                            actionButton.text = "Download"
                            progressBar.visibility = View.GONE
                            progressText.visibility = View.GONE
                            Toast.makeText(context, "Download failed: ${progress.error}", Toast.LENGTH_LONG).show()
                        }
                        progress.isComplete -> {
                            // Download completed
                            actionButton.isEnabled = true
                            actionButton.text = "Load"
                            actionButton.setOnClickListener {
                                loadModel(model, actionButton)
                            }
                            progressBar.visibility = View.GONE
                            progressText.visibility = View.GONE
                            Toast.makeText(context, "Download completed: ${model.displayName}", Toast.LENGTH_SHORT).show()
                            refreshModelList()
                        }
                        else -> {
                            // Download in progress
                            progressBar.progress = progress.percentageComplete
                            
                            val downloadedMB = progress.bytesDownloaded / 1024 / 1024
                            val totalMB = progress.totalBytes / 1024 / 1024
                            val speedMBps = progress.downloadSpeedKBps / 1024.0
                            
                            progressText.text = "${progress.percentageComplete}% • " +
                                    "$downloadedMB/$totalMB MB • " +
                                    "${String.format("%.1f", speedMBps)} MB/s"
                        }
                    }
                }
            }
        }
        
        private fun showDeleteDialog(model: ModelInfo) {
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Delete Model")
            builder.setMessage("Are you sure you want to delete ${model.displayName}?")
            builder.setPositiveButton("Delete") { _, _ ->
                deleteModel(model)
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }
        
        private fun deleteModel(model: ModelInfo) {
            lifecycleScope.launch {
                val success = modelDownloader.deleteModel(model)
                
                if (success) {
                    Toast.makeText(context, "Model deleted: ${model.displayName}", Toast.LENGTH_SHORT).show()
                    refreshModelList()
                } else {
                    Toast.makeText(context, "Failed to delete model", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}