package aero.testcompany.internetstat.domain

import android.content.Context
import java.io.File
import java.io.FileWriter

class MyFileWriter(mcoContext: Context, sFileName: String) {

    private val file = File(mcoContext.filesDir, "mydir")
    private val writer: FileWriter

    init {
        if (!file.exists()) {
            file.mkdir()
        }
        writer = FileWriter(File(file, sFileName))
    }

    fun add(body: String) {
        try {
            writer.append(body)
            writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            writer.close()
        }
    }

    fun close() {
        try {
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}