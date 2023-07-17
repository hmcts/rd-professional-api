package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceImplTest {

    LDClient ldClient = mock(LDClient.class);
    FeatureToggleServiceImpl flaFeatureToggleService = mock(FeatureToggleServiceImpl.class);

    @Test
    void testIsFlagEnabled() {
        when(flaFeatureToggleService.isFlagEnabled("test1", "test1")).thenReturn(true);
        assertTrue(flaFeatureToggleService.isFlagEnabled("test1", "test1"));


        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        assertFalse(flaFeatureToggleService.isFlagEnabled("test1", "test1"));
    }

    @Test
    void testIsFlagEnabledFalse() {
        when(flaFeatureToggleService.isFlagEnabled("test1", "test1")).thenReturn(false);
        assertFalse(flaFeatureToggleService.isFlagEnabled("test1", "test1"));

        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd1");
        assertFalse(flaFeatureToggleService.isFlagEnabled("test1", "test1"));
    }

    @Test
    void mapServiceToFlagTest() {
        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        flaFeatureToggleService.mapServiceToFlag();
        assertTrue(flaFeatureToggleService.getLaunchDarklyMap().size() >= 1);
    }
}
