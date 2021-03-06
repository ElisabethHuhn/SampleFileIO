package com.telanon.android.samplefileio.fileio

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.telanon.android.samplefileio.SharedViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*


const val PRIVATE_FILE_NAME = "privateFile"

fun readFromPublicUri(uri: Uri, activity : AppCompatActivity, sharedVM : SharedViewModel) {
    var inputBuffer = ""

    activity.contentResolver.openInputStream(uri)?.use { inputStream ->

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()

            while (line != null) {
                inputBuffer = inputBuffer + line + "\n"

                //prepare to read the next line from the file
                line = reader.readLine()
            }
        }
        sharedVM.textFromPublicRead = inputBuffer
        var status = "Public Read SUCCEEDED"
        if (inputBuffer == "") {
            status = "Public Read FAILED: No text found in file"
        }
        sharedVM.lastActionStatus = status
    }
}

fun readFromPrivateFile(sharedVM: SharedViewModel, activity: AppCompatActivity) {

    //set defaults for failure, success will override
    val textReadFromFile = readFromPrivateFile(PRIVATE_FILE_NAME, activity)
    val status = "Private Read SUCCEEDED"


    sharedVM.textFromPrivateRead = textReadFromFile
    sharedVM.lastActionStatus = status
}


fun copyFromPrivateFile(uri: Uri, activity: AppCompatActivity, sharedViewModel: SharedViewModel) {

    //set defaults for failure, success will override
    val textReadFromFile = readFromPrivateFile(PRIVATE_FILE_NAME, activity)
    val status = "Private Copy SUCCEEDED"

    sharedViewModel.textToPublicWrite = textReadFromFile
    writeToPublicUri(uri, activity, sharedViewModel)

}


@OptIn(DelicateCoroutinesApi::class)
fun writeToPublicUri(uri: Uri, activity : AppCompatActivity, sharedViewModel : SharedViewModel) {
    //Do the actual IO on a background thread
    GlobalScope.launch(Dispatchers.IO) {

        var status = "Public Write FAILED: Exception thrown trying to write to private file"
        val strBuilder = StringBuilder()

        //Build the String to output to file
//        val headerLine = "Text written to public file:\n"
//        strBuilder.append(headerLine)
        strBuilder.append(sharedViewModel.textToPublicWrite)

        activity.contentResolver.openOutputStream(uri)?.use { outputStream ->

            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                try {
                    writer.write(strBuilder.toString())
                    status = "Public Write SUCCEEDED"

                } catch (e: java.lang.Exception) {
                    status = status + "\n" + e.toString()
                }
            }
            sharedViewModel.lastActionStatus = status
        }

//        //create a new file
//        val fileOut = uri.toFile()
//        fileOut.createNewFile()
//
//        //append the header and a newline
//        val headerLine = "Text written to public file:\n"
//        fileOut.appendText(headerLine)
//        fileOut.appendText("\n")
//        sharedViewModel.textToPublicWrite?.let { fileOut.appendText(it) }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun appendToPublicFile(uri: Uri, activity : AppCompatActivity, sharedViewModel : SharedViewModel){
    //Do the actual IO on a background thread
    GlobalScope.launch(Dispatchers.IO) {
//        val fileOut = uri.toFile()
//        val lineToWrite = sharedViewModel.textToPrivateWrite
//        if (lineToWrite != null) {
//            fileOut.appendText(lineToWrite)
//        }

        var status = "Public Write FAILED: Exception thrown trying to write to private file"

        activity.contentResolver.openOutputStream(uri)?.use { outputStream ->

            val lineToWrite = sharedViewModel.textToPrivateWrite
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                try {
                    writer.write(lineToWrite)
                    status = "Public Write SUCCEEDED"

                } catch (e : java.lang.Exception) {
                    status = status + "\n" + e.toString()
                }
            }
            sharedViewModel.lastActionStatus = status
        }
    }
}

fun writeToPrivateFile(activity: AppCompatActivity, sharedViewModel: SharedViewModel, isCreate: Boolean) {
     val strBuilder = StringBuilder()

    val privateFile = File(PRIVATE_FILE_NAME)
    val fileExists = privateFile.exists()
    var status = "Private Write FAILED: Exception thrown, file exists = $fileExists"

    //Build the String to output to file
//    val headerLine = "Text written to private file:"
//    strBuilder.append(headerLine)
//    strBuilder.append("\n")
    strBuilder.append(sharedViewModel.textToPrivateWrite)
    strBuilder.append("\n")

    //File(TOTALS_FILE_NAME).writeText(strBuilder.toString())
    val dataToOutput = strBuilder.toString()
//    privateFile.writeText(dataToOutput)


    val outputData: ByteArray = dataToOutput.toByteArray()
    val mode = if (isCreate) Context.MODE_PRIVATE else Context.MODE_APPEND
    val fileOutputStream : FileOutputStream = activity.openFileOutput(PRIVATE_FILE_NAME, mode)

    try {
        fileOutputStream.write(outputData)
        status = "Private Write SUCCEEDED"
     }catch (e: Exception){
        //e.printStackTrace()
        sharedViewModel.lastActionStatus = status
        return
    }



    if (! testWriteToFile(dataToOutput, activity)) {
        status = "Private Write FAILED: String written and String re-read from file not equal"
    }
    sharedViewModel.lastActionStatus = status
}

fun readFromPrivateFile(fileName: String, activity: AppCompatActivity) : String{
    val inputStrBuilder = StringBuilder()
    val fileInputStream: FileInputStream? = activity.openFileInput(fileName)

    val inputStreamReader = InputStreamReader(fileInputStream)
    val bufferedReader = BufferedReader(inputStreamReader)

    var line: String? = bufferedReader.readLine()

    while (line != null) {
        inputStrBuilder.append(line)
        inputStrBuilder.append("\n")
        line = bufferedReader.readLine()
    }

    val fullFileContents = inputStrBuilder.toString()
    return (fullFileContents)

}

fun testWriteToFile(outputString: String, activity: AppCompatActivity) : Boolean {
    //    *****************************************************************
    // test code

    val fullFileContents = readFromPrivateFile(PRIVATE_FILE_NAME, activity)
    return (outputString == fullFileContents)

}

fun deleteOldPrivateText() : Boolean {
    val file = File(PRIVATE_FILE_NAME)
    val fileExists = file.exists()

    if (fileExists) {
        return file.delete()
    }
    return true
}





