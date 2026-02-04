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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.ImportFragment2
import com.example.brainbyte.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var btnUseAsTerm: Button
    private lateinit var btnUseAsDefinition: Button
    private lateinit var btnContinue: MaterialButton
    private lateinit var termEditText: TextInputEditText
    private lateinit var definitionEditText: TextInputEditText
    private lateinit var btnAddToList: MaterialButton
    private lateinit var btnClearEntry: MaterialButton
    private lateinit var flashcardListRecyclerView: RecyclerView
    private lateinit var flashcardCountText: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var btnClearAll: MaterialButton
    private lateinit var documentScanner: GmsDocumentScanner
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var currentBitmap: Bitmap? = null
    private var currentTextResult: Text? = null
    private var allRecognizedText: String = ""
    private var flashcardSuggestions: List<SmartTextExtractor.FlashcardSuggestion> = emptyList()
    private val flashcardsList = mutableListOf<FlashcardPair>()
    private lateinit var flashcardAdapter: ScannedFlashcardAdapter

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
        btnUseAsTerm = view.findViewById(R.id.btnUseAsTerm)
        btnUseAsDefinition = view.findViewById(R.id.btnUseAsDefinition)
        btnContinue = view.findViewById(R.id.btnContinue)
        termEditText = view.findViewById(R.id.termEditText)
        definitionEditText = view.findViewById(R.id.definitionEditText)
        btnAddToList = view.findViewById(R.id.btnAddToList)
        btnClearEntry = view.findViewById(R.id.btnClearEntry)
        flashcardListRecyclerView = view.findViewById(R.id.flashcardListRecyclerView)
        flashcardCountText = view.findViewById(R.id.flashcardCountText)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        btnClearAll = view.findViewById(R.id.btnClearAll)
        flashcardAdapter = ScannedFlashcardAdapter(
            flashcardsList,
            onDeleteClick = { position -> deleteFlashcard(position) },
            onFlashcardChanged = { updateFlashcardCount() }
        )
        flashcardListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = flashcardAdapter
        }
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

        btnUseAsTerm.setOnClickListener {
            val selected = textOverlayView.getSelectedText()
            if (selected.isNotEmpty()) {
                val currentText = termEditText.text.toString()
                val newText = if (currentText.isEmpty()) {
                    selected
                } else {
                    "$currentText $selected"
                }
                termEditText.setText(newText)
                textOverlayView.clearSelection()
                Toast.makeText(context, "Added to term", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select text first", Toast.LENGTH_SHORT).show()
            }
        }

        btnUseAsDefinition.setOnClickListener {
            val selected = textOverlayView.getSelectedText()
            if (selected.isNotEmpty()) {
                val currentText = definitionEditText.text.toString()
                val newText = if (currentText.isEmpty()) {
                    selected
                } else {
                    "$currentText $selected"
                }
                definitionEditText.setText(newText)
                textOverlayView.clearSelection()
                Toast.makeText(context, "Added to definition", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select text first", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddToList.setOnClickListener {
            addFlashcardToList()
        }

        btnClearEntry.setOnClickListener {
            clearCurrentEntry()
        }

        btnClearAll.setOnClickListener {
            clearAllFlashcards()
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

        updateFlashcardCount()
    }

    private fun addFlashcardToList() {
        val term = termEditText.text.toString().trim()
        val definition = definitionEditText.text.toString().trim()

        if (term.isEmpty() && definition.isEmpty()) {
            Toast.makeText(context, "Please add a term or definition", Toast.LENGTH_SHORT).show()
            return
        }

        if (term.isEmpty()) {
            Toast.makeText(context, "Term is empty. Please add a term.", Toast.LENGTH_SHORT).show()
            return
        }

        if (definition.isEmpty()) {
            Toast.makeText(context, "Definition is empty. Please add a definition.", Toast.LENGTH_SHORT).show()
            return
        }

        flashcardAdapter.addFlashcard(FlashcardPair(term, definition))
        clearCurrentEntry()
        updateFlashcardCount()

        Toast.makeText(context, "Flashcard added!", Toast.LENGTH_SHORT).show()
    }

    private fun clearCurrentEntry() {
        termEditText.setText("")
        definitionEditText.setText("")
    }

    private fun deleteFlashcard(position: Int) {
        flashcardAdapter.removeFlashcard(position)
        updateFlashcardCount()
        Toast.makeText(context, "Flashcard deleted", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllFlashcards() {
        if (flashcardsList.isEmpty()) return

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear All Flashcards?")
            .setMessage("This will delete all ${flashcardsList.size} flashcards. This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                flashcardAdapter.clearAll()
                updateFlashcardCount()
                Toast.makeText(context, "All flashcards cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateFlashcardCount() {
        val count = flashcardsList.size
        flashcardCountText.text = "$count card${if (count != 1) "s" else ""}"

        if (count == 0) {
            emptyStateContainer.visibility = View.VISIBLE
            flashcardListRecyclerView.visibility = View.GONE
            btnClearAll.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            flashcardListRecyclerView.visibility = View.VISIBLE
            btnClearAll.visibility = View.VISIBLE
        }

        btnContinue.isEnabled = count > 0
        btnContinue.text = if (count > 0) {
            "Continue with $count Card${if (count != 1) "s" else ""}"
        } else {
            "Add flashcards to continue"
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnScanDocument.isEnabled = !show
    }

    private fun navigateToNextScreen() {
        if (flashcardsList.isEmpty()) {
            Toast.makeText(context, "Please add at least one flashcard", Toast.LENGTH_SHORT).show()
            return
        }

        val finalFlashcards = flashcardAdapter.getFlashcards()

        val textToPass = finalFlashcards.joinToString("\n\n") {
            "${it.term}: ${it.definition}"
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
