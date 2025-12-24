package com.auditory.trackoccupancy.di

import android.content.Context
import android.content.SharedPreferences
import com.auditory.trackoccupancy.BuildConfig
import com.auditory.trackoccupancy.data.api.ApiConfig
import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.repository.AuthRepository
import com.auditory.trackoccupancy.data.repository.AuthRepositoryImpl
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import com.auditory.trackoccupancy.data.repository.OccupancyRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("track_occupancy_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        preferences: SharedPreferences
    ): Retrofit {
        val baseUrl = BuildConfig.BASE_URL
        val authToken = runBlocking {
            preferences.getString("auth_token", null)
        }
        return ApiConfig.createRetrofit(baseUrl, authToken)
    }

    @Provides
    @Singleton
    fun provideTrackOccupancyApi(retrofit: Retrofit): TrackOccupancyApi {
        return ApiConfig.createApiService(retrofit)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: TrackOccupancyApi,
        preferences: SharedPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(api, preferences)
    }

    @Provides
    @Singleton
    fun provideOccupancyRepository(api: TrackOccupancyApi): OccupancyRepository {
        return OccupancyRepositoryImpl(api)
    }
}
