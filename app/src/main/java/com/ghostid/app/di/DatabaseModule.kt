package com.ghostid.app.di

import android.content.Context
import androidx.room.Room
import com.ghostid.app.data.db.AliasDao
import com.ghostid.app.data.db.GhostIDDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GhostIDDatabase =
        Room.databaseBuilder(context, GhostIDDatabase::class.java, "ghostid.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAliasDao(db: GhostIDDatabase): AliasDao = db.aliasDao()
}
