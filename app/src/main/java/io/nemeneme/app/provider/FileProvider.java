package io.nemeneme.app.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;

public class FileProvider extends ContentProvider {
    public static final String BASE_CONTENT_URI = "content://io.nemeneme.provider/";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = uri.getQueryParameter("mimeType");
        return (mimeType != null && !mimeType.isEmpty()) ? mimeType : "application/octet-stream";
    }

    private String safePath(Uri uri) throws FileNotFoundException {
        String rawPath = uri.getPath();
        if (rawPath == null) rawPath = "";
        if (!rawPath.startsWith("/")) rawPath = "/" + rawPath;

        File file = new File(rawPath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found: " + uri);
        }
        return file.getAbsolutePath();
    }

    private String getName(Uri uri) {
        String name = uri.getQueryParameter("name");
        if (name != null && !name.isEmpty()) {
            return name;
        }
        String path = uri.getPath();
        return (path != null) ? new File(path).getName() : "unknown";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (!uri.toString().startsWith(BASE_CONTENT_URI)) {
            throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
        if (!"r".equalsIgnoreCase(mode)) {
            throw new UnsupportedOperationException("Only read mode is supported");
        }

        File file = new File(safePath(uri));
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder
    ) {
        try {
            File file = new File(safePath(uri));
            String[] proj = (projection != null)
                    ? projection
                    : new String[]{"_display_name", "_size", "_data", "mime_type"};

            Object[] row = new Object[proj.length];
            String name = getName(uri);
            String type = getType(uri);

            for (int i = 0; i < proj.length; i++) {
                switch (proj[i]) {
                    case "_display_name":
                        row[i] = name;
                        break;
                    case "_size":
                        row[i] = file.length();
                        break;
                    case "_data":
                        row[i] = file.getAbsolutePath();
                        break;
                    case "mime_type":
                        row[i] = type;
                        break;
                    default:
                        row[i] = null;
                        break;
                }
            }

            MatrixCursor cursor = new MatrixCursor(proj);
            cursor.addRow(row);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }
}