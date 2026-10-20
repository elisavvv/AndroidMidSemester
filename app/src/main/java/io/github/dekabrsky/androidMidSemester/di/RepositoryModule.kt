package io.github.dekabrsky.androidMidSemester.di

import io.github.dekabrsky.androidMidSemester.data.cache.MatchesMemoryCache
import io.github.dekabrsky.androidMidSemester.data.repository.CountriesRepositoryImpl
import io.github.dekabrsky.androidMidSemester.data.repository.InMemoryTeamSelectionRepository
import io.github.dekabrsky.androidMidSemester.data.repository.SharedPrefTeamSelectionRepository
import org.koin.android.ext.koin.androidContext
import io.github.dekabrsky.androidMidSemester.data.repository.MatchRepositoryImpl
import io.github.dekabrsky.androidMidSemester.data.repository.TeamsRepositoryImpl
import io.github.dekabrsky.androidMidSemester.data.source.EspnMatchesRemoteDataSource
import io.github.dekabrsky.androidMidSemester.data.source.MatchesRemoteDataSource
import io.github.dekabrsky.androidMidSemester.data.source.SStatsMatchesRemoteDataSource
import io.github.dekabrsky.androidMidSemester.domain.repository.CountriesRepository
import io.github.dekabrsky.androidMidSemester.domain.repository.MatchRepository
import io.github.dekabrsky.androidMidSemester.domain.repository.TeamSelectionRepository
import io.github.dekabrsky.androidMidSemester.domain.repository.TeamsRepository
import org.koin.dsl.module

private const val ESPN_LEAGUE = "all"

val repositoryModule = module {
    single<CountriesRepository> { CountriesRepositoryImpl(get()) }
    single { SStatsMatchesRemoteDataSource(get()) }
    single { EspnMatchesRemoteDataSource(api = get(), league = ESPN_LEAGUE) }
    single<MatchesRemoteDataSource> { get<EspnMatchesRemoteDataSource>() }
    single<MatchesRemoteDataSource> { get<SStatsMatchesRemoteDataSource>() }
    single { MatchesMemoryCache() }
    single<MatchRepository> { MatchRepositoryImpl(get(), get()) }
    single<TeamsRepository> { TeamsRepositoryImpl(get()) }
    single<TeamSelectionRepository> { SharedPrefTeamSelectionRepository(androidContext()) }
}