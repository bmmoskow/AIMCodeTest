package ben.math.distribution.resources;

import ben.math.distribution.resources.Models.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.WebApplicationException;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import io.restassured.specification.*;
import io.restassured.response.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;

public class APITests {
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test()
    public void test1() throws JsonProcessingException {
        Object a = get("https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus");//.then().body("lotto.lottoId", equalTo(5));
        baseURI = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus";
        RequestSpecification httpRequest = given();
        Response response = httpRequest.get("");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200);
        String body = response.getBody().asString();
        Item[] items = objectMapper.readValue(body, Item[].class);

        for (Integer i = 0; i < 20; i++) {
            Item item = items[i];
            response = httpRequest.get("/" + item.sku);
            statusCode = response.getStatusCode();
            Assert.assertEquals(statusCode, 200);
            body = response.getBody().asString();
            Sku sku = objectMapper.readValue(body, Sku.class);

            Assert.assertEquals(item.createdAt, sku.Item.createdAt);
            Assert.assertEquals(item.description, sku.Item.description);
            Assert.assertEquals(item.price, sku.Item.price);
            Assert.assertEquals(item.sku, sku.Item.sku);
            Assert.assertEquals(item.updatedAt, sku.Item.updatedAt);
        }
    }
}
