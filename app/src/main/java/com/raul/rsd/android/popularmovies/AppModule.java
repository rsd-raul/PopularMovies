package com.raul.rsd.android.popularmovies;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.raul.rsd.android.popularmovies.adapters.MovieItem;
import com.raul.rsd.android.popularmovies.data.MoviesAsyncHandler;
import com.raul.rsd.android.popularmovies.data.MoviesAsyncHandler.*;
import com.raul.rsd.android.popularmovies.data.MoviesDbHelper;

import javax.inject.Named;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Context contextProvider() {
        return app;
    }

    @Provides
    @Named("writable")
    SQLiteDatabase writableDatabaseProvider(MoviesDbHelper databaseHelper){
        return databaseHelper.getWritableDatabase();
    }

    @Provides
    @Named("readable")
    SQLiteDatabase readableDatabaseProvider(MoviesDbHelper databaseHelper){
        return databaseHelper.getReadableDatabase();
    }

    @Provides
    ContentResolver contentResolverProvider(){
        return app.getContentResolver();
    }

    @Provides
    MoviesAsyncQueryHandler moviesAsyncQueryHandlerProvider(MoviesAsyncHandler mah, ContentResolver cr){
        return mah.getHandler(cr);
    }

    @Provides
    FastItemAdapter<IItem> fastItemAdapterProvider(){
        return new FastItemAdapter<>();
    }

    @Provides
    FastItemAdapter<MovieItem> fastItemAdapterForMovieProvider(){
        return new FastItemAdapter<>();
    }
}