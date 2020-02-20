
package com.cinder92.musicfiles;

import org.apache.commons.io.FilenameUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

// import wseemann.media.FFmpegMediaMetadataRetriever;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.io.File;

import com.cinder92.musicfiles.ReactNativeFileManager;

// import org.farng.mp3.MP3File;


public class RNReactNativeGetMusicFilesModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private int minimumSongDuration= 0;
    private int version= Build.VERSION.SDK_INT;
    private String artworkBasepath= "/MusicApp/artwork/";
    private String[] STAR= {"*"};

    public RNReactNativeGetMusicFilesModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext= reactContext;
    }

    @Override
    public String getName() {
        return "RNReactNativeGetMusicFiles";
    }

    @ReactMethod
    public void getAll(ReadableMap options, final Callback successCallback, final Callback errorCallback) {

        // if (options.hasKey("batchNumber")) {
        //     songsPerIteration = options.getInt("batchNumber");
        // }

        if(options.hasKey("minimumSongDuration") && options.getInt("minimumSongDuration")>0) {
            minimumSongDuration= options.getInt("minimumSongDuration");
        }
        if(options.hasKey("artworkBasepath")) {
            artworkBasepath= options.getString("artworkBasepath")+"/";
        }

        if(version<20) {
            getSongs(successCallback, errorCallback);
        } else {
            Thread bgThread= new Thread(null, new Runnable() {
                    @Override
                    public void run() {
                        getSongs(successCallback, errorCallback);
                    }
                }, "asyncTask", 1024);
            bgThread.start();
        }
    }

    private void getSongs(final Callback successCallback, final Callback errorCallback){
        ContentResolver musicResolver= getCurrentActivity().getContentResolver();
        Uri musicUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection= MediaStore.Audio.Media.IS_MUSIC+" != 0";

        if(minimumSongDuration>0){
            selection+= " AND "+MediaStore.Audio.Media.DURATION+" >= "+minimumSongDuration;
        }

        String sortOrder= MediaStore.Audio.Media.TITLE+" ASC";
        Cursor musicCursor= musicResolver.query(musicUri, STAR, selection, null, sortOrder);

        if(musicCursor==null) {
            Log.i("com.tests", "Something get wrong with musicCursor");
            errorCallback.invoke("Something get wrong with musicCursor");
        }
        if(musicCursor.getCount()<1) {
            Log.i("com.tests", "Error, you dont' have any songs");
            successCallback.invoke("Error, you dont' have any songs");
        }

        MediaMetadataRetriever mediaMetadataRetriever= new MediaMetadataRetriever();
        WritableArray data= new WritableNativeArray();

        try {
            while(musicCursor.moveToNext()) {
                try{

                    WritableMap item= new WritableNativeMap();

                    int idColumn= musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    String id= String.valueOf(musicCursor.getLong(idColumn));
                    item.putString("id", id);

                    int pathIndex= musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    String path= musicCursor.getString(pathIndex);
                    item.putString("url", "file://"+path);

                    String type= FilenameUtils.getExtension(path);
                    item.putString("type", type);

                    String name= FilenameUtils.getBaseName(path);
                    item.putString("name", name);

                    int sizeIndex= musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
                    item.putString("size", musicCursor.getString(sizeIndex));

                    mediaMetadataRetriever.setDataSource(path);

                    String duration= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_DURATION);
                    item.putString("duration", duration);

                    String artist= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_ARTIST);
                    item.putString("artist", (artist==null) ? "Unknown" : artist);

                    String title= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_TITLE);
                    item.putString("title", (title==null) ? "Unknown" : title);

                    String album= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_ALBUM);
                    item.putString("album", (album==null) ? "Unknown" : album);

                    String genre= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_GENRE);
                    item.putString("genre", (genre==null) ? "Unknown" : genre);

                    String bitrate= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_BITRATE);
                    item.putString("bitrate", (bitrate==null) ? "-" : bitrate);

                    String year= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_YEAR);
                    item.putString("year", (year==null) ? "-" : year);

                    String tracks= mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
                    item.putString("tracks", (tracks==null) ? "-" : tracks);

                    String artworkPath= Environment.getExternalStorageDirectory()+artworkBasepath+name;
                    File artworkFile= new File(artworkPath);
                    if(artworkFile.exists()) {
                        item.putString("artwork", "file://"+artworkPath);
                    } else {
                        byte[] artworkData= mediaMetadataRetriever.getEmbeddedPicture();
                        if(artworkData!=null) {
                            ReactNativeFileManager writeImage= new ReactNativeFileManager();
                            Bitmap artworkDataBitmap= BitmapFactory.decodeByteArray(artworkData, 0, artworkData.length);
                            writeImage.saveImageToStorage(artworkPath, artworkDataBitmap);
                            item.putString("artwork", "file://"+artworkPath);
                        }
                    }

                    data.pushMap(item);

                } catch(Exception e) {
                    continue;
                }
            }
            successCallback.invoke(data);
        } catch(RuntimeException e) {
            errorCallback.invoke(e.toString());
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        } finally {
            mediaMetadataRetriever.release();
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

}
