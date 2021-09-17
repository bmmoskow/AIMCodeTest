package ben.math.distribution.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by valued on 3/1/18.
 */
public class DistributionResourceTests {
    @Test()
    public void test1() {
        Assert.assertEquals(1, 1);
    }
}
