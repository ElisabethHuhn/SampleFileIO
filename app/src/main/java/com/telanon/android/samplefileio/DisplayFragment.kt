package com.telanon.android.samplefileio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.telanon.android.samplefileio.databinding.DisplayFragmentBinding
import com.telanon.android.samplefileio.fileio.*
import kotlinx.coroutines.*
import java.io.File

class DisplayFragment : Fragment() {


    /**
     * The view model is shared between the fragments
     */
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var _binding:  DisplayFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var myJob: Job? = null

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
        requestWritePublicFile(REQUEST_WRITE_TO_FILE)

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
     * create private Click Listener -
     *  o executes when create button is selected
     *  o triggers navigation to this Fragment
     */
    private val createPrivateOnClickListener = View.OnClickListener { itemView ->
        //Move the text from the UI EditText view to the shared view model
        var textToWrite = binding.fileioWritePrivateText.text.toString()
        if (textToWrite == null) {
            textToWrite = getString(R.string.write_private_text)
        }
        sharedViewModel.textToPrivateWrite = textToWrite

        //just write directly to the private file
        writeToPrivateFile(requireActivity() as AppCompatActivity, sharedViewModel, isCreate = true)

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
        writeToPrivateFile(requireActivity() as AppCompatActivity, sharedViewModel, isCreate = false)

        //cycle back to this fragment to display
        itemView.findNavController().navigate(navigateAction)
    }

    /**
     * Copy Click Listener -
     *  o executes when copy button is selected
     *  o triggers navigation to this Fragment
     */
    private val copyOnClickListener = View.OnClickListener { itemView ->
        sharedViewModel.lastActionStatus = getString(R.string.last_action_status)

        //If a background job is running, cancel it
        myJob?.cancel()
        //copy private file to public file
        requestWritePublicFile(REQUEST_COPY_FILE)
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

        //If a background job is running, candle it
        myJob?.cancel()
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
            fileioCreatePrivateButton.setOnClickListener(createPrivateOnClickListener)
            fileioWritePrivateButton.setOnClickListener(writePrivateOnClickListener)
            fileioCopyButton.setOnClickListener(copyOnClickListener)
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

    private fun requestWritePublicFile(commandCode: Int) {

        val titleString = getString(R.string.select_write_public_file)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_TITLE, titleString)

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker before your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        startActivityForResult(intent, commandCode )
    }

    @Deprecated("Deprecated in Java")
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
                    sharedViewModel.textToPublicWrite = "First Line of text written\n"

                    writeToPublicUri(
                        uri = fileUri,
                        activity = requireActivity() as AppCompatActivity,
                        sharedViewModel = sharedViewModel
                    )


                    //append to public file eludes me.
                    //I can only figure out how to append to private file

                    navigateToNextScreen()
                }
            }
        }

        else if (requestCode == REQUEST_COPY_FILE) {
            if (resultCode == AppCompatActivity.RESULT_OK && resultData != null) {
                val fileUri = resultData.data

                if (fileUri != null) {
                    sharedViewModel.textToPublicWrite = "First Line of text written\n"

                    copyFromPrivateFile(
                        uri = fileUri,
                        activity = requireActivity() as AppCompatActivity,
                        sharedViewModel = sharedViewModel
                    )

                    //to write more lines to the File, once per second
//                    myJob = startRepeatingJob(
//                        timeInterval = 1000L,
//                        fileUri = fileUri,
//                        sharedViewModel = sharedViewModel
//                    )

                    navigateToNextScreen()
                }
            }
        }
    }

    /**
     * start Job
     * val job = startRepeatingJob()
     * cancels the job and waits for its completion
     * job.cancelAndJoin()
     * Params
     * timeInterval: time milliSeconds
     */
    private fun startRepeatingJob(
        timeInterval: Long,
        fileUri: Uri,
        sharedViewModel : SharedViewModel
    ): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (NonCancellable.isActive) {
                delay(timeInterval)
                sharedViewModel.textToPublicWrite = "Another line to write to file\n"
                appendToPublicFile(fileUri, requireActivity() as AppCompatActivity, sharedViewModel)
             }
        }
    }


    private fun navigateToNextScreen() {
        view?.findNavController()?.navigate(navigateAction)
    }

    companion object {
        const val REQUEST_READ_FROM_FILE = 0
        const val REQUEST_WRITE_TO_FILE  = 1
        const val REQUEST_COPY_FILE  = 2
    }

}