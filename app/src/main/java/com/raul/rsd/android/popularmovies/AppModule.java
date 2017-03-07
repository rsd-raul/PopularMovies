package com.raul.rsd.android.popularmovies;

import android.app.Application;
import android.content.Context;

import com.raul.rsd.android.popularmovies.domain.Movie;

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

}