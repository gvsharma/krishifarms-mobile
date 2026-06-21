package com.krishifarms.mobile.core.security.di

import com.krishifarms.mobile.core.security.rbac.DynamicMenuProvider
import com.krishifarms.mobile.core.security.rbac.NavigationGuard
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.core.security.rbac.PermissionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindPermissionManager(impl: PermissionManagerImpl): PermissionManager

    companion object {
        @Provides
        @Singleton
        fun provideNavigationGuard(permissionManager: PermissionManager): NavigationGuard =
            NavigationGuard(permissionManager)

        @Provides
        @Singleton
        fun provideDynamicMenuProvider(): DynamicMenuProvider = DynamicMenuProvider()
    }
}
