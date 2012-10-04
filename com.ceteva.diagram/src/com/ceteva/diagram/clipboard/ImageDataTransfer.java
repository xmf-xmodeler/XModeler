package com.ceteva.diagram.clipboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import com.ceteva.diagram.stubs.COM;

public class ImageDataTransfer extends ByteArrayTransfer {

      private static final ImageDataTransfer INSTANCE = new   ImageDataTransfer();
      private static final String CF_DIB = "CF_DIB";
      private static final int CF_DIBID = COM.CF_DIB;

      private ImageDataTransfer() {
              //Singleton
      }

      public static ImageDataTransfer getInstance() {
              return ImageDataTransfer.INSTANCE;
      }

      protected final int[] getTypeIds() {
              return new int[]{ImageDataTransfer.CF_DIBID};
      }

      protected final String[] getTypeNames() {
              return new String[]{ImageDataTransfer.CF_DIB};
      }

      protected Object nativeToJava(TransferData transferData) {
              final Object o = super.nativeToJava(transferData);
              final byte[] bytes = (byte[]) o;

              try {
                      final InputStream bis = new PrependWinBMPHeaderFilterInputStream(
                                      new UncompressDibFilterInputStream(new ByteArrayInputStream(
                                                      bytes)));
                      final ImageData[] data = new ImageLoader().load(bis);
                      if (data.length < 1) {
                              return null;
                      }
                      return data[0];
              } catch (IOException e) {
                      return null;
              }
      }
      protected void javaToNative(Object object, TransferData transferData) {
              final ImageData imgData = (ImageData) object;
              final ImageLoader loader = new ImageLoader();
              final ByteArrayOutputStream bos = new ByteArrayOutputStream();
              final byte[] bytes;

              loader.data = new ImageData[]{imgData};
              loader.save(new RemoveWinBMPHeaderFilterOutputStream(bos), SWT.IMAGE_BMP);
              bytes = bos.toByteArray();
              super.javaToNative(bytes, transferData);
      }
}
