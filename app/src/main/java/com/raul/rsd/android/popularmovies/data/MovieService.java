package com.raul.rsd.android.popularmovies.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.raul.rsd.android.popularmovies.domain.Movie;

import javax.inject.Inject;



public class MovieService {

    private static final String TAG = "MovieService";
    private Context mContext;

    @Inject
    public MovieService(Context context) {
        mContext = context;
    }

    // ------------------------ CRUD METHODS -------------------------
    public Movie findOne(long id){

        String idStr = String.valueOf(id);

        Uri uri = MoviesContract.CONTENT_URI.buildUpon().appendPath(idStr).build();
        Cursor response = mContext.getContentResolver().query(uri, null, null, null, null);

        if(response == null)
            return null;

        Log.e(TAG, "findOne: " + response.getCount());  // HELLO WORLD

        response.close();
        return null;
    }

    public Movie[] findAll(){
        return null;
    }

    public boolean delete(long id){
        return false;
    }

    public boolean update(Movie movie){
        return false;
    }
}
