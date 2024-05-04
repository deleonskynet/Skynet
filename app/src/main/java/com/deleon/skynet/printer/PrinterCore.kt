package com.deleon.skynet.printer

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.deleon.skynet.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Printer Core.
 *
 * @param PRINTER [Class] input
 * @param STATUS [Class] progressive status
 * @param RESULT [Class] output
 * @constructor Create [PrinterCore]
 * @property context
 * @property scope [CoroutineScope]
 */
abstract class PrinterCore<PRINTER, STATUS, RESULT>(
    private val context: Context,
    protected open val scope: CoroutineScope
) {
    private lateinit var dialog: AlertDialog
    private val view: View = View.inflate(context, R.layout.async_dialog_view, null)
    private val title: TextView = view.findViewById(R.id.async_dialog_title)
    private val progressBar: ProgressBar = view.findViewById(R.id.async_dialog_progress)
    private val percentage: TextView = view.findViewById(R.id.async_dialog_percent)
    private val message: TextView = view.findViewById(R.id.async_dialog_message)

    /**
     * On Printing.
     *
     * @param printer [PRINTER]
     * @return [RESULT]
     */
    protected abstract suspend fun onPrinting(printer: PRINTER): RESULT

    /** On Prepare. */
    protected open fun onPrepare() {
        dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        dialog.show()
    }

    /**
     * On Printed.
     *
     * @param result [RESULT]
     */
    protected open fun onPrinted(result: RESULT) {
        if (dialog.isShowing) dialog.dismiss()
    }

    /**
     * On Status Update.
     *
     * @param status [STATUS]
     */
    protected open fun onStatusUpdate(status: STATUS) {}

    /**
     * Publish Status.
     *
     * @param status [STATUS]
     */
    protected suspend fun publishStatus(
        status: STATUS
    ) = withContext(Dispatchers.Main) {
        onStatusUpdate(status)
    }

    /**
     * Break Time.
     *
     * @param millis [Long]
     */
    protected suspend fun breakTime(
        millis: Long
    ) = withContext(Dispatchers.IO) {
        delay(millis)
    }
    protected fun String.asTitle() {
        if (dialog.isShowing) {
            this@PrinterCore.title.text = this
        }
    }
    protected fun Int.toProgress(max: Int) {
        if (dialog.isShowing) {
            this@PrinterCore.progressBar.progress = this
            this@PrinterCore.progressBar.max = max
            val percent = this.times(100f).div(max).roundToInt()
            this@PrinterCore.percentage.text = percent.toString()
        }
    }
    protected fun String.asMessage() {
        if (dialog.isShowing) {
            this@PrinterCore.message.text = this
        }
    }

    /**
     * Print.
     *
     * @param printer [PRINTER]
     */
    fun print(printer: PRINTER) {
        scope.launch {
            withContext(Dispatchers.Main) { onPrepare() }
            val result = async(Dispatchers.IO) { onPrinting(printer) }.await()
            withContext(Dispatchers.Main) { onPrinted(result) }
        }
    }
}