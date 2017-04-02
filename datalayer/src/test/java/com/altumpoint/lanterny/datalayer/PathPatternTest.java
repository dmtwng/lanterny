package com.altumpoint.lanterny.datalayer;


import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for {@link PathPattern}.
 *
 * @author Dmytro Patserkovskyi
 * @see PathPattern
 */
public class PathPatternTest {

    @Test
    public void testMatch() {
        PathPattern pathPattern = new PathPattern("/domain/${id}");
        Assert.assertNotNull(pathPattern);
        Assert.assertTrue("Path matching failed", pathPattern.matches("/domain/34"));
        Assert.assertTrue("Path matching failed", pathPattern.matches("/domain/3b"));
        Assert.assertFalse("Path matching failed", pathPattern.matches("/domain/3b/"));
        Assert.assertFalse("Path matching failed", pathPattern.matches("/domain/"));
        Assert.assertFalse("Path matching failed", pathPattern.matches("/domain1/34"));
        Assert.assertFalse("Path matching failed", pathPattern.matches("domain/34"));

        Assert.assertTrue("Pattern matching failed", pathPattern.equalsPattern("/domain/${id}"));
    }

}
