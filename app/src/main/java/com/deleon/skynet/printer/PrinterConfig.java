package com.deleon.skynet.printer;

import androidx.annotation.NonNull;

import com.dantsu.escposprinter.EscPosPrinterSize;
import com.dantsu.escposprinter.connection.DeviceConnection;

public class PrinterConfig extends EscPosPrinterSize {
    /** @noinspection FieldMayBeFinal*/
    private DeviceConnection printerConnection;
    private String[] texts = new String[0];
    public PrinterConfig(DeviceConnection printerConnection, int printerDpi, float printerWidthMM, int printerNbrCharactersPerLine) {
        super(printerDpi, printerWidthMM, printerNbrCharactersPerLine);
        this.printerConnection = printerConnection;
    }

    public DeviceConnection getPrinterConnection() {
        return printerConnection;
    }

    public PrinterConfig setTexts(String[] texts) {
        this.texts = texts;
        return this;
    }

    @NonNull
    public PrinterConfig addText(String text) {
        String[] tmp = new String[texts.length + 1];
        System.arraycopy(texts, 0, tmp, 0, texts.length);
        tmp[texts.length] = text;
        texts = tmp;
        return this;
    }

    public String[] getTexts() {
        return texts;
    }
}
