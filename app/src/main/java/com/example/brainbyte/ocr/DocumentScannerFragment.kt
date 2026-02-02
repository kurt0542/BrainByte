package com.example.brainbyte.ocr

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.brainbyte.ImportFragment2
import com.example.brainbyte.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class DocumentScannerFragment : Fragment() {

    private lateinit var imageContainer: FrameLayout
    private lateinit var scannedImageView: ImageView
    private lateinit var textOverlayView: TextOverlayView
    private lateinit var progressBar: ProgressBar
    private lateinit var instructionText: TextView
    private lateinit var selectedTextPreview: TextView
    private lateinit var selectionModeGroup: MaterialButtonToggleGroup
    private lateinit var btnScanDocument: MaterialButton
    private lateinit var btnClearSelection: Button
    private lateinit var btnSelectAll: Button
    private lateinit var btnUseAsterm: Button
    private lateinit var btnUseAsDefinition: Button
    private lateinit var btnContinue: MaterialButton

    private lateinit var documentScanner: GmsDocumentScanner
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var currentBitmap: Bitmap? = null
    private var currentTextResult: Text? = null
    private var selectedTerm: String = ""
    private var selectedDefinition: String = ""
    private var allRecognizedText: String = ""
    private var flashcardSuggestions: List<SmartTextExtractor.FlashcardSuggestion> = emptyList()

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.firstOrNull()?.let { page ->
                processScannedPage(page.imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupDocumentScanner()
        setupListeners()
        updateUI()
    }

    private fun initViews(view: View) {
        imageContainer = view.findViewById(R.id.imageContainer)
        scannedImageView = view.findViewById(R.id.scannedImageView)
        textOverlayView = view.findViewById(R.id.textOverlayView)
        progressBar = view.findViewById(R.id.progressBar)
        instructionText = view.findViewById(R.id.instructionText)
        selectedTextPreview = view.findViewById(R.id.selectedTextPreview)
        selectionModeGroup = view.findViewById(R.id.selectionModeGroup)
        btnScanDocument = view.findViewById(R.id.btnScanDocument)
        btnClearSelection = view.findViewById(R.id.btnClearSelection)
        btnSelectAll = view.findViewById(R.id.btnSelectAll)
        btnUseAsterm = view.findViewById(R.id.btnUseAsTerm)
        btnUseAsDefinition = view.findViewById(R.id.btnUseAsDefinition)
        btnContinue = view.findViewById(R.id.btnContinue)
    }

    private fun setupDocumentScanner() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        documentScanner = GmsDocumentScanning.getClient(options)
    }

    private fun setupListeners() {
        btnScanDocument.setOnClickListener {
            launchDocumentScanner()
        }

        selectionModeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnWordMode -> textOverlayView.selectionMode = TextOverlayView.SelectionMode.WORD
                    R.id.btnLineMode -> textOverlayView.selectionMode = TextOverlayView.SelectionMode.LINE
                    R.id.btnBlockMode -> textOverlayView.selectionMode = TextOverlayView.SelectionMode.BLOCK
                }
            }
        }

        textOverlayView.onSelectionChangedListener = { selectedTexts ->
            val selectedText = selectedTexts.joinToString(" ")
            updateSelectedTextPreview(selectedText)
        }

        btnClearSelection.setOnClickListener {
            textOverlayView.clearSelection()
            updateSelectedTextPreview("")
        }

        btnSelectAll.setOnClickListener {
            textOverlayView.selectAll()
            updateSelectedTextPreview(textOverlayView.getSelectedText())
        }

        btnUseAsterm.setOnClickListener {
            val selected = textOverlayView.getSelectedText()
            if (selected.isNotEmpty()) {
                selectedTerm = selected
                textOverlayView.clearSelection()
                updateUI()
                Toast.makeText(context, "Term set: ${selectedTerm.take(30)}...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select text first", Toast.LENGTH_SHORT).show()
            }
        }

        btnUseAsDefinition.setOnClickListener {
            val selected = textOverlayView.getSelectedText()
            if (selected.isNotEmpty()) {
                selectedDefinition = selected
                textOverlayView.clearSelection()
                updateUI()
                Toast.makeText(context, "Definition set", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select text first", Toast.LENGTH_SHORT).show()
            }
        }

        btnContinue.setOnClickListener {
            navigateToNextScreen()
        }
    }

    private fun launchDocumentScanner() {
        documentScanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to start scanner: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun processScannedPage(imageUri: Uri) {
        showLoading(true)
        instructionText.text = "Processing image..."

        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            currentBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            currentBitmap?.let { bitmap ->
                scannedImageView.setImageBitmap(bitmap)
                scannedImageView.visibility = View.VISIBLE

                runTextRecognition(bitmap)
            } ?: run {
                showLoading(false)
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            showLoading(false)
            Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runTextRecognition(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                currentTextResult = text
                allRecognizedText = text.text

                textOverlayView.setTextResult(text, bitmap.width, bitmap.height)
                textOverlayView.visibility = View.VISIBLE

                flashcardSuggestions = if (SmartTextExtractor.detectTableStructure(text)) {
                    SmartTextExtractor.extractFromTwoColumnLayout(text)
                } else {
                    SmartTextExtractor.extractFlashcardSuggestions(text)
                }

                showLoading(false)
                instructionText.text = "Tap or swipe to select text"
                updateUI()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Text recognition failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun updateSelectedTextPreview(text: String) {
        selectedTextPreview.text = if (text.isEmpty()) {
            "No text selected"
        } else {
            "Selected: ${text.take(100)}${if (text.length > 100) "..." else ""}"
        }
    }

    private fun updateUI() {
        val hasImage = currentBitmap != null
        val hasText = currentTextResult != null

        imageContainer.visibility = if (hasImage) View.VISIBLE else View.GONE
        selectionModeGroup.visibility = if (hasText) View.VISIBLE else View.GONE

        btnContinue.isEnabled = allRecognizedText.isNotEmpty()

        val termStatus = if (selectedTerm.isNotEmpty()) "✓ Term set" else "No term"
        val defStatus = if (selectedDefinition.isNotEmpty()) "✓ Definition set" else "No definition"
        btnUseAsterm.text = termStatus
        btnUseAsDefinition.text = defStatus
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnScanDocument.isEnabled = !show
    }

    private fun navigateToNextScreen() {
        val textToPass = if (flashcardSuggestions.isNotEmpty()) {
            flashcardSuggestions.joinToString("\n\n") {
                "${it.term}: ${it.definition}"
            }
        } else {
            allRecognizedText
        }

        val fragment = ImportFragment2.newInstance(textToPass)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        textRecognizer.close()
        currentBitmap?.recycle()
    }

    companion object {
        fun newInstance(): DocumentScannerFragment {
            return DocumentScannerFragment()
        }
    }
}
