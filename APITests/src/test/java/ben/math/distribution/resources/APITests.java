package ben.math.distribution.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by valued on 3/1/18.
 */
public class APITests {

    @BeforeClass
    public static void beforeClass() {

    }

    @Test()
    public void Test1() {
        Assert.assertEquals(1,1);
    }

    @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "expected \\[2\\] but found \\[1\\]")
    public void Test2() {
        Assert.assertEquals(1, 2);
    }
}
