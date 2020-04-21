package org.dpppt.backend.sdk.data;

import org.dpppt.backend.sdk.data.config.DPPPTDataServiceConfig;
import org.dpppt.backend.sdk.data.config.FlyWayConfig;
import org.dpppt.backend.sdk.data.config.StandaloneDataConfig;
import org.dpppt.backend.sdk.model.Exposee;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {StandaloneDataConfig.class,
        FlyWayConfig.class, DPPPTDataServiceConfig.class})
@ActiveProfiles("hsqldb")
public class DPPPTDataServiceTest {

    private static final String APP_SOURCE = "org.dpppt.demo";
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private DPPPTDataService dppptDataService;

    @Test
    public void testDataService() {
        DateTime now = DateTime.now();
        Exposee expected = createExposee(now,"key");

        dppptDataService.upsertExposee(expected, APP_SOURCE);

        List<Exposee> sortedExposedForDay = dppptDataService.getSortedExposedForDay(now);
        assertFalse(sortedExposedForDay.isEmpty());
        Exposee actual = sortedExposedForDay.get(0);
        assertExposee(expected, actual);

        dppptDataService.upsertExposee(expected, APP_SOURCE);
        dppptDataService.upsertExposee(expected, APP_SOURCE);

        sortedExposedForDay = dppptDataService.getSortedExposedForDay(now);
        assertEquals(1, sortedExposedForDay.size());
        actual = sortedExposedForDay.get(0);
        assertExposee(expected, actual);

        Exposee expected2 = createExposee(now, "key2");

        dppptDataService.upsertExposee(expected, APP_SOURCE);
        dppptDataService.upsertExposee(expected2, APP_SOURCE);

        sortedExposedForDay = dppptDataService.getSortedExposedForDay(now);
        assertEquals(2, sortedExposedForDay.size());
        actual = sortedExposedForDay.get(0);
        assertExposee(expected2, actual);
        sortedExposedForDay = dppptDataService.getSortedExposedForDay(now);
        actual = sortedExposedForDay.get(0);
        assertExposee(expected2, actual);

        Integer maxExposedIdForDay = dppptDataService.getMaxExposedIdForDay(now);
        assertEquals(2, maxExposedIdForDay);
    }

    private Exposee createExposee(DateTime now, String key) {
        Exposee expected = new Exposee();
        expected.setKey(key);
        expected.setOnset(fmt.print(now));
        return expected;
    }

    private void assertExposee(Exposee expected, Exposee actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getOnset(), actual.getOnset());
        assertNotNull(actual.getId());
    }


}