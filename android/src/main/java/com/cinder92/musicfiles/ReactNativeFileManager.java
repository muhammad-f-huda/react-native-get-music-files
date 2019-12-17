package com.cinder92.musicfiles;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

/**
 * Created by dantecervantes on 28/11/17.
 */

public class ReactNativeFileManager extends ReactNativeBlurImage{
    public String saveImageToStorageAndGetPath(long songId, Bitmap songImage) throws IOException {
        try{

            if (songImage != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                songImage.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                byte[] imageByte = Base64.decode(encodedImage, Base64.DEFAULT);

                if(byteArray != null) {
                    return saveToStorage(songId, imageByte);
                }
            }

        } catch (IOException e){
            Log.e("Error savingImageAfter",e.getMessage());
        }

        return("");

    }


    public String saveToStorage(long songId, byte[] imageBytes) throws IOException {
        FileOutputStream fos = null;
        try {
            String pathToImg = Environment.getExternalStorageDirectory() + "/MusicApp/cover/";
            File folderPath = new File(pathToImg);
            folderPath.mkdirs();
            pathToImg+= songId;
            File filePath = new File(pathToImg);
            fos = new FileOutputStream(filePath, true);
            fos.write(imageBytes);
            return("file://" + pathToImg);
        } catch (IOException e){
            Log.e("Error saving image => ", e.getMessage());
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }

        return("");

    }
}
