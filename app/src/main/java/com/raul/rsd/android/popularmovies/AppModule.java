package com.raul.rsd.android.popularmovies;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.raul.rsd.android.popularmovies.data.MoviesDbHelper;
import com.raul.rsd.android.popularmovies.domain.Movie;

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
}