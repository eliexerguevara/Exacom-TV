package com.exacomtv.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.exacomtv.app.BuildConfig
import com.exacomtv.data.local.ExacomTVDatabase
import com.exacomtv.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DEBUG_SLOW_QUERY_THRESHOLD_MS = 100L

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExacomTVDatabase =
        Room.databaseBuilder(
            context,
            ExacomTVDatabase::class.java,
            "exacomtv.db"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .openHelperFactory(
                if (BuildConfig.DEBUG) {
                    SlowQueryLoggingOpenHelperFactory(
                        delegate = FrameworkSQLiteOpenHelperFactory(),
                        slowQueryThresholdMs = DEBUG_SLOW_QUERY_THRESHOLD_MS
                    )
                } else {
                    FrameworkSQLiteOpenHelperFactory()
                }
            )
            .addMigrations(
                ExacomTVDatabase.MIGRATION_1_2,
                ExacomTVDatabase.MIGRATION_2_3,
                ExacomTVDatabase.MIGRATION_3_4,
                ExacomTVDatabase.MIGRATION_4_5,
                ExacomTVDatabase.MIGRATION_5_6,
                ExacomTVDatabase.MIGRATION_6_7,
                ExacomTVDatabase.MIGRATION_7_8,
                ExacomTVDatabase.MIGRATION_8_9,
                ExacomTVDatabase.MIGRATION_9_10,
                ExacomTVDatabase.MIGRATION_10_11,
                ExacomTVDatabase.MIGRATION_11_12,
                ExacomTVDatabase.MIGRATION_12_13,
                ExacomTVDatabase.MIGRATION_13_14,
                ExacomTVDatabase.MIGRATION_14_15,
                ExacomTVDatabase.MIGRATION_15_16,
                ExacomTVDatabase.MIGRATION_16_17,
                ExacomTVDatabase.MIGRATION_17_18,
                ExacomTVDatabase.MIGRATION_18_19,
                ExacomTVDatabase.MIGRATION_19_20,
                ExacomTVDatabase.MIGRATION_20_21,
                ExacomTVDatabase.MIGRATION_21_22,
                ExacomTVDatabase.MIGRATION_22_23,
                ExacomTVDatabase.MIGRATION_23_24,
                ExacomTVDatabase.MIGRATION_24_25,
                ExacomTVDatabase.MIGRATION_25_26,
                ExacomTVDatabase.MIGRATION_26_27,
                ExacomTVDatabase.MIGRATION_27_28,
                ExacomTVDatabase.MIGRATION_28_29,
                ExacomTVDatabase.MIGRATION_29_30,
                ExacomTVDatabase.MIGRATION_30_31,
                ExacomTVDatabase.MIGRATION_31_32,
                ExacomTVDatabase.MIGRATION_32_33,
                ExacomTVDatabase.MIGRATION_33_34,
                ExacomTVDatabase.MIGRATION_34_35,
                ExacomTVDatabase.MIGRATION_35_36,
                ExacomTVDatabase.MIGRATION_36_37,
                ExacomTVDatabase.MIGRATION_37_38,
                ExacomTVDatabase.MIGRATION_38_39,
                ExacomTVDatabase.MIGRATION_39_40,
                ExacomTVDatabase.MIGRATION_40_41,
                ExacomTVDatabase.MIGRATION_41_42,
                ExacomTVDatabase.MIGRATION_42_43,
                ExacomTVDatabase.MIGRATION_43_44,
                ExacomTVDatabase.MIGRATION_44_45,
                ExacomTVDatabase.MIGRATION_45_46,
                ExacomTVDatabase.MIGRATION_46_47,
                ExacomTVDatabase.MIGRATION_47_48,
                ExacomTVDatabase.MIGRATION_48_49,
                ExacomTVDatabase.MIGRATION_49_50,
                ExacomTVDatabase.MIGRATION_50_51,
                ExacomTVDatabase.MIGRATION_51_52,
                ExacomTVDatabase.MIGRATION_52_53,
                ExacomTVDatabase.MIGRATION_53_54,
                ExacomTVDatabase.MIGRATION_54_55,
                ExacomTVDatabase.MIGRATION_55_56,
                ExacomTVDatabase.MIGRATION_56_57,
                ExacomTVDatabase.MIGRATION_57_58,
                ExacomTVDatabase.MIGRATION_58_59,
                ExacomTVDatabase.MIGRATION_59_60
            )
            // NOTE: fallbackToDestructiveMigration() intentionally removed.
            // All future schema changes MUST add a corresponding Migration in ExacomTVDatabase.
            .build()

    @Provides fun provideProviderDao(db: ExacomTVDatabase): ProviderDao = db.providerDao()
    @Provides fun provideChannelDao(db: ExacomTVDatabase): ChannelDao = db.channelDao()
    @Provides fun provideChannelPreferenceDao(db: ExacomTVDatabase): ChannelPreferenceDao = db.channelPreferenceDao()
    @Provides fun provideMovieDao(db: ExacomTVDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: ExacomTVDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideEpisodeDao(db: ExacomTVDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideCategoryDao(db: ExacomTVDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideCatalogSyncDao(db: ExacomTVDatabase): CatalogSyncDao = db.catalogSyncDao()
    @Provides fun provideProgramDao(db: ExacomTVDatabase): ProgramDao = db.programDao()
    @Provides fun provideFavoriteDao(db: ExacomTVDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideVirtualGroupDao(db: ExacomTVDatabase): VirtualGroupDao = db.virtualGroupDao()
    @Provides fun providePlaybackHistoryDao(db: ExacomTVDatabase): PlaybackHistoryDao = db.playbackHistoryDao()
    @Provides fun provideTmdbIdentityDao(db: ExacomTVDatabase): TmdbIdentityDao = db.tmdbIdentityDao()
    @Provides fun provideSearchHistoryDao(db: ExacomTVDatabase): SearchHistoryDao = db.searchHistoryDao()
    @Provides fun provideSearchDao(db: ExacomTVDatabase): SearchDao = db.searchDao()
    @Provides fun provideSyncMetadataDao(db: ExacomTVDatabase): SyncMetadataDao = db.syncMetadataDao()
    @Provides fun provideMovieCategoryHydrationDao(db: ExacomTVDatabase): MovieCategoryHydrationDao = db.movieCategoryHydrationDao()
    @Provides fun provideSeriesCategoryHydrationDao(db: ExacomTVDatabase): SeriesCategoryHydrationDao = db.seriesCategoryHydrationDao()
    @Provides fun provideEpgSourceDao(db: ExacomTVDatabase): EpgSourceDao = db.epgSourceDao()
    @Provides fun provideProviderEpgSourceDao(db: ExacomTVDatabase): ProviderEpgSourceDao = db.providerEpgSourceDao()
    @Provides fun provideEpgChannelDao(db: ExacomTVDatabase): EpgChannelDao = db.epgChannelDao()
    @Provides fun provideEpgProgrammeDao(db: ExacomTVDatabase): EpgProgrammeDao = db.epgProgrammeDao()
    @Provides fun provideChannelEpgMappingDao(db: ExacomTVDatabase): ChannelEpgMappingDao = db.channelEpgMappingDao()
    @Provides fun provideCombinedM3uProfileDao(db: ExacomTVDatabase): CombinedM3uProfileDao = db.combinedM3uProfileDao()
    @Provides fun provideCombinedM3uProfileMemberDao(db: ExacomTVDatabase): CombinedM3uProfileMemberDao = db.combinedM3uProfileMemberDao()
    @Provides fun provideRecordingScheduleDao(db: ExacomTVDatabase): RecordingScheduleDao = db.recordingScheduleDao()
    @Provides fun provideRecordingRunDao(db: ExacomTVDatabase): RecordingRunDao = db.recordingRunDao()
    @Provides fun provideProgramReminderDao(db: ExacomTVDatabase): ProgramReminderDao = db.programReminderDao()
    @Provides fun provideRecordingStorageDao(db: ExacomTVDatabase): RecordingStorageDao = db.recordingStorageDao()
    @Provides fun providePlaybackCompatibilityDao(db: ExacomTVDatabase): PlaybackCompatibilityDao = db.playbackCompatibilityDao()
    @Provides fun provideXtreamContentIndexDao(db: ExacomTVDatabase): XtreamContentIndexDao = db.xtreamContentIndexDao()
    @Provides fun provideXtreamIndexJobDao(db: ExacomTVDatabase): XtreamIndexJobDao = db.xtreamIndexJobDao()
    @Provides fun provideXtreamLiveOnboardingDao(db: ExacomTVDatabase): XtreamLiveOnboardingDao = db.xtreamLiveOnboardingDao()
    @Provides fun provideDownloadDao(db: ExacomTVDatabase): DownloadDao = db.downloadDao()
}
