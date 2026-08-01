package com.openfinds.app.core.di

import com.openfinds.app.core.domain.repository.DeviceGroupRepository
import com.openfinds.app.core.domain.repository.DeviceGroupRepositoryImpl
import com.openfinds.app.core.domain.repository.DeviceHistoryRepository
import com.openfinds.app.core.domain.repository.DeviceHistoryRepositoryImpl
import com.openfinds.app.core.domain.repository.DeviceRepository
import com.openfinds.app.core.domain.repository.DeviceRepositoryImpl
import com.openfinds.app.core.domain.repository.DiscoveryRepository
import com.openfinds.app.core.domain.repository.DiscoveryRepositoryImpl
import com.openfinds.app.core.domain.repository.PairingRepository
import com.openfinds.app.core.domain.repository.PairingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository

    @Binds
    @Singleton
    abstract fun bindPairingRepository(impl: PairingRepositoryImpl): PairingRepository

    @Binds
    @Singleton
    abstract fun bindDeviceGroupRepository(impl: DeviceGroupRepositoryImpl): DeviceGroupRepository

    @Binds
    @Singleton
    abstract fun bindDeviceHistoryRepository(impl: DeviceHistoryRepositoryImpl): DeviceHistoryRepository
}
