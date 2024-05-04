package com.deleon.skynet.printer

import android.content.Context
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import kotlinx.coroutines.CoroutineScope

abstract class Printing(
    context: Context,
    scope: CoroutineScope,
    private val onResultListener: OnResultListener? = null
) : PrinterCore<Printing.Printer, Printing.Status, Printing.Result>(context, scope) {
    override suspend fun onPrinting(printer: Printer): Result {
        val texts: Array<String> = printer.texts
        val connection = printer.printerConnection
        val progressMax = texts.size.plus(4)
        publishStatus(Status(1, progressMax))
        if (connection == null) {
            return Result(printer, false)
        }
        try {
            val pos = EscPosPrinter(
                connection,
                printer.printerDpi,
                printer.printerWidthMM,
                printer.printerNbrCharactersPerLine,
                EscPosCharsetEncoding("windows-1252", 16)
            )
            publishStatus(Status(2, progressMax))
            breakTime(100)
            texts.forEachIndexed { index, text ->
                publishStatus(Status(index.plus(3), progressMax))
                pos.printFormattedText(text, 5f)
                breakTime(500)
            }
            publishStatus(Status(progressMax, progressMax))
            breakTime(100)
        } catch (e: Exception) {
            return Result(printer, false)
        }
        return Result(printer, true)
    }

    override fun onPrepare() {
        super.onPrepare()
        "Print Card".asTitle()
        1.toProgress(1000)
        "Preparing....".asMessage()
    }

    override fun onStatusUpdate(status: Status) {
        status.state.toProgress(status.finish)
        when (status.state) {
            1 -> "Connecting...."
            2 -> "Connected"
            status.finish -> "Printed"
            else -> "Printing: ${status.state.minus(3)}/${status.finish.minus(4)}"
        }.asMessage()
    }

    override fun onPrinted(result: Result) {
        super.onPrinted(result)
        onResultListener?.onPrinted(result.success)
    }

    class Printer(
        printerConnection: DeviceConnection?,
        printerDpi: Int,
        printerWidthMM: Float,
        printerNbrCharactersPerLine: Int
    ) : PrinterConfig(printerConnection, printerDpi, printerWidthMM, printerNbrCharactersPerLine) {
        override fun addText(text: String?): Printer {
            super.addText(text)
            return this
        }
    }
    data class Result(
        val printer: Printer,
        val success: Boolean
    )
    data class Status(
        val state: Int,
        val finish: Int
    )
    interface OnResultListener {
        fun onPrinted(success: Boolean)
    }
}