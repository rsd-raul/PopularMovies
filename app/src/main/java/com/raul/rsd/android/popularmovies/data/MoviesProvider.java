package com.raul.rsd.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.raul.rsd.android.popularmovies.App;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.raul.rsd.android.popularmovies.data.MoviesContract.*;

public class MoviesProvider extends ContentProvider {

    private final String TAG = "MoviesProvider";

    // --------------------------- VALUES ----------------------------

    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_ID = 101;

    // ------------------------- ATTRIBUTES --------------------------

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    @Inject @Named("writable") Provider<SQLiteDatabase> wDB;
    @Inject @Named("readable") Provider<SQLiteDatabase> rDB;
    @Inject Context context;

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    public boolean onCreate() {
        return true;
    }

    /** Method to defer Dagger's injection as onCreate documentation reads:
     *
     *  "You should defer nontrivial initialization (such as opening, upgrading, and scanning
     *  databases) until the content provider is used."
     *
     * Aaaand because it breaks the injection in onCreate, mostly that.
     */
    private void deferInit(){
        App app = (App) getContext();
        if(app != null)
            app.getComponent().inject(this);
    }

    // -------------------------- AUXILIARY --------------------------

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIE, MOVIE);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        return uriMatcher;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) throws UnsupportedOperationException{
        String type;

        switch (sUriMatcher.match(uri)) {
            case MOVIE:         // directory
                type = "vnd.android.cursor.dir";
                break;
            case MOVIE_WITH_ID: // single item type
                type = "vnd.android.cursor.item";
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return type + "/" + MoviesContract.AUTHORITY + "/" + MoviesContract.PATH_MOVIE;
    }

    // ------------------------ CRUD METHODS -------------------------

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                                                String[] selectionArgs, String sortOrder)
                                                throws UnsupportedOperationException, SQLException{
        if(context == null)
            deferInit();

        Cursor response;

        switch(sUriMatcher.match(uri)){
            case MOVIE:
                response = rDB.get().query(MoviesEntry.TABLE_NAME, projection,
                                                                        selection, selectionArgs,
                                                                        null, null, sortOrder);
                break;
            case MOVIE_WITH_ID:
                // Retrieve the id, knowing the Uri has 2 paths "/tasks/id" we get the index
                String id = uri.getPathSegments().get(1);

                // Use selection and selection args to filter
                String idSelection = MoviesEntry._ID + "=?";
                String[] idSelectionArgs = new String[]{id};

                // Construct a query as usual, but passing in the selection/args
                response =  rDB.get().query(MoviesEntry.TABLE_NAME, projection,
                                                                    idSelection, idSelectionArgs,
                                                                    null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("query: Not implemented for uri: " + uri);
        }

        if(response == null)
            throw new SQLException("query: Failed to find any movie with uri: " + uri);

        response.setNotificationUri(context.getContentResolver(), uri);
        return response;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
                                                throws SQLException, UnsupportedOperationException {
        if(context == null)
            deferInit();

        if(sUriMatcher.match(uri) != MOVIE)
            throw new UnsupportedOperationException("insert: Unknown uri: " + uri);

        long id = wDB.get().insert(MoviesEntry.TABLE_NAME, null, values);
        if(id == -1)
            throw new SQLException("insert: Failed to insert row into: " + uri);

        // Notify the change so the resolver can update the database and any associate UI
        context.getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(MoviesContract.CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs)
                                                throws UnsupportedOperationException {
        if(context == null)
            deferInit();

        int itemsDeleted;

        switch (sUriMatcher.match(uri)){
            case MOVIE:
                if (selection == null)
                    selection = "1";

                itemsDeleted =  wDB.get().delete(MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);

                // Selection is the _ID column = ?, and the Selection args = the row ID from the URI
                String idSelection = MoviesEntry._ID + "=?";
                String[] idSelectionArgs = new String[]{id};

                // Construct a query as you would normally, passing in the selection/args
                itemsDeleted =  wDB.get().delete(MoviesEntry.TABLE_NAME,
                                                                    idSelection, idSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("delete: Unknown uri: " + uri);
        }

        // Notify the change so the resolver can update the database and any associate UI
        if(itemsDeleted < 1)
            Log.e(TAG, "delete: " + itemsDeleted + " items deleted with uri: " + uri);

        context.getContentResolver().notifyChange(uri, null);
        return itemsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selArgs)
                                                            throws UnsupportedOperationException{
        if(context == null)
            deferInit();

        if(sUriMatcher.match(uri) != MOVIE_WITH_ID)
            throw new UnsupportedOperationException("update: Unknown uri: " + uri);

        String id = uri.getPathSegments().get(1);

        // Selection is the _ID column = ?, and the Selection args = the row ID from the URI
        String idSelection = MoviesEntry._ID + "=?";
        String[] idSelectionArgs = new String[]{id};

        int itemsUpdated = wDB.get().update(MoviesEntry.TABLE_NAME, values,
                                                                    idSelection, idSelectionArgs);

        // Notify the change so the resolver can update the database and any associate UI
        if(itemsUpdated == 0)
            Log.e(TAG, "update: No items updated with uri: " + uri + " and id: " + id);

        context.getContentResolver().notifyChange(uri, null);
        return itemsUpdated;
    }
}
