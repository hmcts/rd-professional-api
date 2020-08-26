package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.launchdarkly.sdk.server.LDClient;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeatureToggleServiceImplTest {

    LDClient ldClient = mock(LDClient.class);
    FeatureToggleServiceImpl flaFeatureToggleService = mock(FeatureToggleServiceImpl.class);

    @Test
    public void testIsFlagEnabled() {
        assertFalse(flaFeatureToggleService.isFlagEnabled("test", "test"));
        verify(flaFeatureToggleService, times(1))
            .isFlagEnabled("test", "test");
    }

    @Test
    public void mapServiceToFlagTest() {
        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        flaFeatureToggleService.mapServiceToFlag();
        assertTrue(flaFeatureToggleService.getLaunchDarklyMap().size() >= 1);
    }
}
