package com.telanon.android.samplefileio.fileio

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.telanon.android.samplefileio.SharedViewModel
import java.io.*

const val PRIVATE_FILE_NAME = "privateFile"

fun readFromPublicUri(uri: Uri, activity : AppCompatActivity, sharedVM : SharedViewModel) {
    var inputBuffer = ""

    activity.contentResolver.openInputStream(uri)?.use { inputStream ->

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()

            while (line != null) {
                inputBuffer = inputBuffer + line

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
    var textReadFromFile = readFromPrivateFile(PRIVATE_FILE_NAME, activity)
    var status = "Private Read SUCCEEDED"


    sharedVM.textFromPrivateRead = textReadFromFile
    sharedVM.lastActionStatus = status
}

fun writeToPublicUri(uri: Uri, activity : AppCompatActivity, sharedVM : SharedViewModel) {
    val sharedViewModel: SharedViewModel = sharedVM
    var status = "Public Write FAILED: Exception thrown trying to write to private file"
    val strBuilder = StringBuilder()

    //Build the String to output to file
    val headerLine = "Text written to public file:"
    strBuilder.append(headerLine)
    strBuilder.append(sharedViewModel.textToPrivateWrite)

    activity.contentResolver.openOutputStream(uri)?.use { outputStream ->

        BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
            try {
                writer.write(strBuilder.toString())
                status = "Public Write SUCCEEDED"

            } catch (e : java.lang.Exception) {
                status = status + "\n" + e.toString()
            }
        }
        sharedViewModel.lastActionStatus = status
    }
}

fun writeToPrivateFile(activity: AppCompatActivity, sharedViewModel: SharedViewModel) {
     val strBuilder = StringBuilder()

    val privateFile = File(PRIVATE_FILE_NAME)
    val fileExists = privateFile.exists()
    var status = "Private Write FAILED: Exception thrown, file exists = $fileExists"

    //Build the String to output to file
    val headerLine = "Text written to private file:"
    strBuilder.append(headerLine)
    strBuilder.append("\n")
    strBuilder.append(sharedViewModel.textToPrivateWrite)
    strBuilder.append("\n")

    //File(TOTALS_FILE_NAME).writeText(strBuilder.toString())
    val dataToOutput = strBuilder.toString()
    val outputData: ByteArray = dataToOutput.toByteArray()
    val fileOutputStream : FileOutputStream = activity.openFileOutput(PRIVATE_FILE_NAME, Context.MODE_PRIVATE)

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
        inputStrBuilder.append(line + "\n")
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





