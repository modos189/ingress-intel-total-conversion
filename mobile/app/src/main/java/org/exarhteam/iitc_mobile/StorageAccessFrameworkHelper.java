package org.exarhteam.iitc_mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class StorageAccessFrameworkHelper {
    private static final int REQUEST_CODE = 42;

    private final Activity activity;

    public StorageAccessFrameworkHelper(Activity activity) {
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void requestAccessToFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean createFile(Context context, Uri pickedDir) {
        DocumentFile folder = DocumentFile.fromTreeUri(context, pickedDir);
        if (folder == null) {
            return false;
        }

        String mimeType = "text/plain";
        String fileName = "test.txt";
        String fileContent = "This is a test file.";

        DocumentFile file = folder.createFile(mimeType, fileName);
        if (file == null) {
            return false;
        }

        OutputStream outputStream;
        try {
            outputStream = context.getContentResolver().openOutputStream(file.getUri());
            outputStream.write(fileContent.getBytes());
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getFirstFileContentInFolder(Context context, Uri folderUri) {

        DocumentFile folder = DocumentFile.fromTreeUri(context, folderUri);
        if (folder == null) {
            return null;
        }

        DocumentFile[] files = folder.listFiles();
        if (files.length == 0) {
            return null;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(files[0].getUri());
            if (inputStream == null) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
