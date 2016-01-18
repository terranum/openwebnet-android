package com.github.openwebnet.component.module;

import android.content.Context;

import com.github.openwebnet.service.CommonService;
import com.github.openwebnet.service.PreferenceService;
import com.github.openwebnet.service.impl.CommonServiceImpl;
import com.github.openwebnet.service.impl.PreferenceServiceImpl;

import org.robolectric.RuntimeEnvironment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module
public class ApplicationContextModuleTest {

    @Provides
    @Singleton
    public Context applicationContext() {
        return RuntimeEnvironment.application.getApplicationContext();
    }

    @Provides
    @Singleton
    public PreferenceService providePreferenceService() {
        return mock(PreferenceService.class);
        //return new PreferenceServiceImpl();
    }

    @Provides
    @Singleton
    public CommonService provideCommonService() {
        return mock(CommonService.class);
        //return new CommonServiceImpl();
    }

}
