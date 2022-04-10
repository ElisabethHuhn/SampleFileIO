package com.telanon.android.samplefileio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.telanon.android.samplefileio.databinding.DisplayFragmentBinding
import com.telanon.android.samplefileio.fileio.readFromPrivateFile
import com.telanon.android.samplefileio.fileio.readFromPublicUri
import com.telanon.android.samplefileio.fileio.writeToPrivateFile
import com.telanon.android.samplefileio.fileio.writeToPublicUri

class DisplayFragment : Fragment() {


    /**
     * The view model is shared between the fragments
     */
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var _binding:  DisplayFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    /**
     * Read Public Click Listener -
     *  o executes when read button is selected
     *  o triggers navigation to this Fragment
     */
    private val navigateAction = DisplayFragmentDirections.actionDisplayFragmentSelf()
    private val readPublicOnClickListener = View.OnClickListener { itemView ->
        //open the Public file
        requestReadPublicFile() //Will read in the file when activity result returns

        //Navigation to redisplay this fragment is in onActivityReceive()
     }

    /**
     * write Public Click Listener -
     *  o executes when write button is selected
     *  o triggers navigation to this Fragment
     */
     private val writePublicOnClickListener = View.OnClickListener { itemView ->
        //Move the text from the UI EditText view to the shared view model
        //We could do this with a TextWatcher on the EditText,
        // That would do the transfer with every keystroke by the user
        // but this works too, as this is the only place we read what the user input
        var textToWrite = binding.fileioWritePublicText.text.toString()
        if (textToWrite == null) {
            textToWrite = getString(R.string.write_public_text)
        }
        sharedViewModel.textToPublicWrite = textToWrite

        //open the public file for write
        requestWritePublicFile()

        //Navigation to redisplay this fragment is in onActivityReceive()
    }

    /**
     * Read Private Click Listener -
     *  o executes when read button is selected
     *  o triggers navigation to this Fragment
     */
    private val readPrivateOnClickListener = View.OnClickListener { itemView ->
        //read the private file
        readFromPrivateFile(sharedViewModel, requireActivity() as AppCompatActivity)  //Will read in the file when activity result returns

        //cycle back to this fragment to display
        itemView.findNavController().navigate(navigateAction)
    }

    /**
     * write private Click Listener -
     *  o executes when write button is selected
     *  o triggers navigation to this Fragment
     */
    private val writePrivateOnClickListener = View.OnClickListener { itemView ->
        //Move the text from the UI EditText view to the shared view model
        var textToWrite = binding.fileioWritePrivateText.text.toString()
        if (textToWrite == null) {
            textToWrite = getString(R.string.write_private_text)
        }
        sharedViewModel.textToPrivateWrite = textToWrite

        //just write directly to the private file
        writeToPrivateFile(requireActivity() as AppCompatActivity, sharedViewModel)

        //cycle back to this fragment to display
        itemView.findNavController().navigate(navigateAction)
    }

    /**
     * Redisplay Click Listener -
     *  o executes when redisplay button is selected
     *  o triggers navigation to this Fragment
     */
    private val redisplayOnClickListener = View.OnClickListener { itemView ->
        sharedViewModel.lastActionStatus = getString(R.string.last_action_status)
        //cycle back to this fragment to display
        itemView.findNavController().navigate(navigateAction)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DisplayFragmentBinding.inflate(inflater, container, false)
        binding.apply {
            //Initialize the screen
            if (sharedViewModel.textFromPublicRead == null) {
                sharedViewModel.textFromPublicRead = getString(R.string.read_public_text)
            }
            if (sharedViewModel.textToPublicWrite == null) {
                sharedViewModel.textToPublicWrite = getString(R.string.write_public_text)
            }
            if (sharedViewModel.textFromPrivateRead == null) {
                sharedViewModel.textFromPrivateRead = getString(R.string.read_private_text)
            }
            if (sharedViewModel.textToPrivateWrite == null) {
                sharedViewModel.textToPrivateWrite = getString(R.string.write_private_text)
            }
            //initialize text to the display
            fileioReadPublicText.text   = sharedViewModel.textFromPublicRead
            fileioWritePublicText.text  = sharedViewModel.textToPublicWrite!!.toEditable()
            fileioReadPrivateText.text  = sharedViewModel.textFromPrivateRead
            fileioWritePrivateText.text = sharedViewModel.textToPrivateWrite!!.toEditable()

            fileioLastActionStatus.text = sharedViewModel.lastActionStatus

            //add click listeners
            fileioReadPublicButton.setOnClickListener(readPublicOnClickListener)
            fileioWritePublicButton.setOnClickListener(writePublicOnClickListener)
            fileioReadPrivateButton.setOnClickListener(readPrivateOnClickListener)
            fileioWritePrivateButton.setOnClickListener(writePrivateOnClickListener)
            fileioRedisplayButton.setOnClickListener(redisplayOnClickListener)

        }
        return binding.root
    }

    //reads from a public file
    private fun requestReadPublicFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/csv"
        val requestCode = REQUEST_READ_FROM_FILE
        val titleString = getString(R.string.select_read_public_file)
        intent.putExtra(Intent.EXTRA_TITLE, titleString)
        startActivityForResult(Intent.createChooser(intent, titleString), requestCode)
    }

    private fun requestWritePublicFile() {

        val titleString = getString(R.string.select_write_public_file)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_TITLE, titleString)

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker before your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        startActivityForResult(intent, REQUEST_WRITE_TO_FILE )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == REQUEST_READ_FROM_FILE) {
            //Toast.makeText(this, "Request granted: read data file", Toast.LENGTH_SHORT).show();

            if (resultCode == AppCompatActivity.RESULT_OK && resultData != null) {
                val fileUri = resultData.data

                if (fileUri != null) {
                    readFromPublicUri(fileUri, activity as AppCompatActivity, sharedViewModel)
                }
                //cycle back to this fragment to display
                navigateToNextScreen()
            }

        }
        else if (requestCode == REQUEST_WRITE_TO_FILE) {
            if (resultCode == AppCompatActivity.RESULT_OK && resultData != null) {
                val fileUri = resultData.data

                if (fileUri != null) {
                    writeToPublicUri(fileUri,
                        requireActivity() as AppCompatActivity,
                        sharedViewModel)

                    navigateToNextScreen()
                }
            }
        }
    }




    private fun navigateToNextScreen() {
        //Once the ballot is loaded, navigate to next fragment
        view?.findNavController()?.navigate(navigateAction)
    }

    companion object {
        const val REQUEST_READ_FROM_FILE = 0
        const val REQUEST_WRITE_TO_FILE  = 1
    }

}