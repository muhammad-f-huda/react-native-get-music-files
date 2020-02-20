package com.cinder92.musicfiles;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

/**
 * Created by dantecervantes on 28/11/17.
 */

public class ReactNativeFileManager extends Application {

    public void saveImageToStorage(String path, Bitmap songImage) throws IOException {
        try {

            if(songImage!=null) {
                ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                songImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray= byteArrayOutputStream.toByteArray();
                if(byteArray!=null) saveToStorage(path, byteArray);
            }

        } catch(IOException e){
            Log.e("Error savingImageAfter", e.getMessage());
        }
    }


    public void saveToStorage(String path, byte[] imageBytes) throws IOException {
        FileOutputStream fileOutputStream= null;
        try {

            File filePath= new File(path);
            File basepath= filePath.getParentFile();
            basepath.mkdirs();
            fileOutputStream= new FileOutputStream(filePath, true);
            fileOutputStream.write(imageBytes);

        } catch(IOException e) {
            Log.e("Error saving image => ", e.getMessage());
        } finally {
            if(fileOutputStream!=null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
    }

}
