package com.github.openwebnet.repository;

import com.github.openwebnet.BuildConfig;
import com.github.openwebnet.component.ApplicationComponentTest;
import com.github.openwebnet.component.DaggerApplicationComponentTest;
import com.github.openwebnet.component.Injector;
import com.github.openwebnet.component.module.ApplicationContextModuleTest;
import com.github.openwebnet.component.module.DatabaseModuleTest;
import com.github.openwebnet.component.module.DomoticModuleTest;
import com.github.openwebnet.component.module.RepositoryModuleTest;
import com.github.openwebnet.database.DatabaseRealm;
import com.github.openwebnet.model.DomoticModel;
import com.github.openwebnet.model.LightModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.observers.TestSubscriber;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PrepareForTest({Injector.class})
public class LightRepositoryTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Inject
    LightRepository lightRepository;

    @Inject
    DatabaseRealm databaseRealm;

    @Before
    public void setupDagger() {
        ApplicationComponentTest applicationComponentTest = DaggerApplicationComponentTest.builder()
            .applicationContextModuleTest(new ApplicationContextModuleTest())
            .databaseModuleTest(new DatabaseModuleTest())
            .repositoryModuleTest(new RepositoryModuleTest(false))
            .domoticModuleTest(new DomoticModuleTest())
            .build();

        PowerMockito.mockStatic(Injector.class);
        PowerMockito.when(Injector.getApplicationComponent()).thenReturn(applicationComponentTest);

        ((ApplicationComponentTest) Injector.getApplicationComponent()).inject(this);
    }

    @Test
    public void lightRepository_findByEnvironment() {
        Integer ENVIRONMENT = 108;

        LightModel light1 = LightModel.updateBuilder("uuid1")
            .environment(ENVIRONMENT)
            .gateway("gateway")
            .name("light1")
            .where("10")
            .build();

        LightModel light2 = LightModel.updateBuilder("uuid2")
            .environment(ENVIRONMENT)
            .gateway("gateway")
            .name("light2")
            .where("11")
            .build();

        List<LightModel> lights = Arrays.asList(light1, light2);

        when(databaseRealm.findCopyWhere(LightModel.class, DomoticModel.FIELD_ENVIRONMENT_ID,
            ENVIRONMENT, DomoticModel.FIELD_NAME)).thenReturn(lights);

        TestSubscriber<List<LightModel>> tester = new TestSubscriber<>();
        lightRepository.findByEnvironment(ENVIRONMENT).subscribe(tester);

        verify(databaseRealm).findCopyWhere(LightModel.class, DomoticModel.FIELD_ENVIRONMENT_ID,
            ENVIRONMENT, DomoticModel.FIELD_NAME);

        tester.assertValue(lights);
        tester.assertCompleted();
        tester.assertNoErrors();
    }

    @Test
    public void lightRepository_findFavourites() {
        LightModel light = LightModel.updateBuilder("uuid2")
            .environment(108)
            .gateway("gateway")
            .name("light2")
            .where("11")
            .favourite(true)
            .build();

        List<LightModel> lights = Arrays.asList(light);

        when(databaseRealm.findCopyWhere(LightModel.class, DomoticModel.FIELD_FAVOURITE,
            true, DomoticModel.FIELD_NAME)).thenReturn(lights);

        TestSubscriber<List<LightModel>> tester = new TestSubscriber<>();
        lightRepository.findFavourites().subscribe(tester);

        verify(databaseRealm).findCopyWhere(LightModel.class, DomoticModel.FIELD_FAVOURITE,
            true, DomoticModel.FIELD_NAME);

        tester.assertValue(lights);
        tester.assertCompleted();
        tester.assertNoErrors();
    }

}
