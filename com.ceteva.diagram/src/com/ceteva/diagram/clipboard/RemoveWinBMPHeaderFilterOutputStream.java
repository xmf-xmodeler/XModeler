package com.ceteva.diagram.clipboard;

import java.io.IOException;
import java.io.OutputStream;

class RemoveWinBMPHeaderFilterOutputStream extends OutputStream {
        private final OutputStream out;
        private int counter = 0;

        public RemoveWinBMPHeaderFilterOutputStream(OutputStream out) {
                this.out = out;
        }
        public void write(int b) throws IOException {
                //ignore the bmp file header
                if (this.counter < PrependWinBMPHeaderFilterInputStream.BITMAPFILEHEADER_SIZEOF) {
                        this.counter++;
                } else {
                        this.out.write(b);
                }
        }
}