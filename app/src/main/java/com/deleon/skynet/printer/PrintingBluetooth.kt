package com.deleon.skynet.printer

import android.content.Context
import kotlinx.coroutines.CoroutineScope

class PrintingBluetooth(
    context: Context,
    scope: CoroutineScope,
    listener: OnResultListener? = null
) : Printing(context, scope, listener) {
    constructor(context: Context, scope: CoroutineScope, listener: (success: Boolean) -> Unit) : this(context, scope, object : OnResultListener {
        override fun onPrinted(success: Boolean) {
            listener(success)
        }
    })
    override suspend fun onPrinting(printer: Printer): Result {
        if (printer.printerConnection == null) {
            return Result(printer, false)
        } else {
            try {
                printer.printerConnection.connect()
            } catch (e: Exception) {
                //
            }
        }
        return super.onPrinting(printer)
    }
}