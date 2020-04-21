package org.dpppt.backend.sdk.data;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author bachmann
 * created on 21.04.20
 **/
public class EtagGeneratorTest {

    @Test
    public void testEtagZero() {
        EtagGenerator etagGenerator = new EtagGenerator();
        String etag = etagGenerator.getEtag(0);
        assertEquals("8321c313574929d220a72029837c1085", etag);
    }

    @Test
    public void testEtagOne() {
        EtagGenerator etagGenerator = new EtagGenerator();
        String etag = etagGenerator.getEtag(1);
        assertEquals("6bd26b412635ad2a7bdbe07b9f2f6e8b", etag);
    }

    @Test
    public void testEtagNotTheSame() {
        EtagGenerator etagGenerator = new EtagGenerator();
        Set<String> etags = new HashSet<>();
        int numberOfEtags = 10;
        for (int i = 0; i < numberOfEtags; i++) {
            etags.add(etagGenerator.getEtag(i));
        }
        assertEquals(numberOfEtags, etags.size());
    }
}