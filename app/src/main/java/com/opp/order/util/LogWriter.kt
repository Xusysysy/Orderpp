package com.opp.order.util

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogWriter {
    private var logFile: File? = null

    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun init(dir: File) {
        logFile = File(dir, "Order_log.txt")
    }

    fun write(tag: String, msg: String) {
        try {
            val file = logFile ?: return
            val timestamp = dateFormat.format(Date())
            val line = "[$timestamp] [$tag] $msg\n"
            file.parentFile?.mkdirs()
            FileWriter(file, true).use { it.append(line) }
        } catch (_: Exception) {}
    }

    fun write(tag: String, msg: String, throwable: Throwable) {
        write(tag, "$msg | ${throwable.javaClass.simpleName}: ${throwable.message}")
    }

    fun read(): String {
        return try {
            val file = logFile
            if (file != null && file.exists()) file.readText() else "(日志为空)"
        } catch (e: Exception) {
            "读取日志失败: ${e.message}"
        }
    }
}
