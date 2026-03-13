package com.godofcodes.androidmouse.di

import com.godofcodes.androidmouse.data.repository.BluetoothRepositoryImpl
import com.godofcodes.androidmouse.data.repository.JigglerRepositoryImpl
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import com.godofcodes.androidmouse.domain.repository.JigglerRepository
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
    abstract fun bindBluetoothRepository(impl: BluetoothRepositoryImpl): BluetoothRepository

    @Binds
    @Singleton
    abstract fun bindJigglerRepository(impl: JigglerRepositoryImpl): JigglerRepository
}
