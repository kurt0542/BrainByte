package com.example.brainbyte

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

class ImportFragment : Fragment() {

    private lateinit var recognizedTextView: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recognizedTextView = view.findViewById(R.id.recognizedTextView)
        val continueBtn = view.findViewById<Button>(R.id.continueBtn)

        continueBtn.setOnClickListener {
            val recognizedText = recognizedTextView.text.toString()

            val fragment = ImportFragment2.newInstance(recognizedText)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}