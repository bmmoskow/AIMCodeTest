package ben.aimcodetest.apitests;

import ben.aimcodetest.apitests.Models.Item;
import ben.aimcodetest.apitests.Models.ItemBase;
import ben.aimcodetest.apitests.Models.SkuMetadata;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static io.restassured.RestAssured.*;
import io.restassured.response.*;
import io.restassured.specification.*;
import java.time.Instant;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

public class APITests {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static RequestSpecification httpRequest;

    static {
        baseURI = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus";
        httpRequest = given();
    }

    @Test()
    public void getTest() throws JsonProcessingException {
        Item[] items = executeGetAllTest();

        for (Integer i = 0; i < 2; i++) {
            executeGetTest(items[i]);
        }
    }

    @Test()
    public void postGetDeleteTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "post test";
        postItem.price = "1.23";
        postItem.sku = UUID.randomUUID().toString();

        Item postResponseItem = executePostTest(postItem);

        try {
            executeGetTestWithTimeChecks(postResponseItem);

            ItemBase updatedPostItem = new ItemBase();

            updatedPostItem.description = postItem.description;
            postItem.price = "1.24";
            updatedPostItem.sku = postItem.sku;

            Item updatedPostResponseItem = executePostTest(updatedPostItem);
            executeGetTestWithTimeChecks(updatedPostResponseItem);
        }
        finally {
            executeDeleteTest(postItem);
            executeGetUnknownItemTest(postItem.sku);
        }
    }

    @Test
    public void getUnknownItemTest() {
        executeGetUnknownItemTest(UUID.randomUUID().toString());
    }

    @Test
    public void deleteUnknownItemTest() {
        Response deleteResponse = httpRequest.delete("/" + UUID.randomUUID().toString());
        int statusCode = deleteResponse.getStatusCode();
        Assert.assertEquals(statusCode, 404, "should get 404 if the item to be deleted doesn't exist");
    }

    @Test()
    public void postEmptyDescriptionTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "";
        postItem.price = "1.23";
        postItem.sku = UUID.randomUUID().toString();

        executeNegativePostTest(postItem);
    }

    @Test()
    public void postEmptyPriceTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "post test";
        postItem.price = "";
        postItem.sku = UUID.randomUUID().toString();

        executeNegativePostTest(postItem);
    }

    @Test()
    public void postEmptySkuTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "post test";
        postItem.price = "1.23";
        postItem.sku = "";

        executeNegativePostTest(postItem);
    }

    private Item[] executeGetAllTest() throws JsonProcessingException {
        Response response = httpRequest.get("");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200, "expect 200 result");
        String body = response.getBody().asString();
        return objectMapper.readValue(body, Item[].class);
    }

    private Item executeGetTest(Item imputItem) throws JsonProcessingException {
        Response getResponse = httpRequest.get("/" + imputItem.sku);
        int statusCode = getResponse.getStatusCode();
        Assert.assertEquals(statusCode, 200, "expect 200 result");
        String getResponseBody = getResponse.getBody().asString();
        SkuMetadata getResponseSku = objectMapper.readValue(getResponseBody, SkuMetadata.class);
        Assert.assertEquals(getResponseSku.ResponseMetadata.HTTPStatusCode, statusCode, "inconsistent status code");
        Assert.assertEquals(getResponseSku.ResponseMetadata.HTTPHeaders.content_type, "application/x-amz-json-1.0", "unexpected content-type");
        Assert.assertTrue(getResponseSku.ResponseMetadata.RetryAttempts >= 0, "retry attempts is non-negative");
        validateItem(getResponseSku.Item, imputItem);

        return getResponseSku.Item;
    }

    private void executeGetTestWithTimeChecks(Item postResponseItem) throws JsonProcessingException {
        Long timeBeforeGet = Instant.now().getEpochSecond();
        Item getResponseItem = executeGetTest(postResponseItem);
        Long timeAfterGet = Instant.now().getEpochSecond();
        Long createdAt = Long.parseLong(getResponseItem.createdAt);
        Assert.assertTrue(createdAt >= timeBeforeGet && createdAt <= timeAfterGet,
                "test that 'createdAt' is in the right time interval");
    }

    private Item executePostTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        httpRequest.body(objectMapper.writeValueAsString(postItem));
        Response response = httpRequest.post("");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200, "expect 200 result");
        String responseBody = response.getBody().asString();
        Item postResponseItem = objectMapper.readValue(responseBody, Item.class);
        validateItemBase(postResponseItem, postItem);

        return postResponseItem;
    }

    private void executeDeleteTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        Response deleteResponse = httpRequest.delete("/" + postItem.sku.toString());
        int statusCode = deleteResponse.getStatusCode();
        Assert.assertEquals(statusCode, 200, "expect 200 result");
        String deleteResponseBody = deleteResponse.getBody().asString();
        Assert.assertEquals(deleteResponseBody, "", "expect empty response body");
    }

    private void executeGetUnknownItemTest(String sku) {
        Response getResponse = httpRequest.get("/" + sku);
        int statusCode = getResponse.getStatusCode();
        Assert.assertEquals(statusCode, 404, "expect 404 result for a sku that doesn't exist");
    }

    private void executeNegativePostTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        httpRequest.body(objectMapper.writeValueAsString(postItem));
        Response response = httpRequest.post("");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 400, "expect 400 for a malformed POST body");
    }

    private void validateItem(Item actualItem, Item expectedItem) {
        Assert.assertEquals(actualItem.createdAt, expectedItem.createdAt, "test for 'createdAt' consistency");
        Assert.assertEquals(actualItem.updatedAt, expectedItem.updatedAt, "test for 'updatedAt' consistency");
        validateItemBase(actualItem, expectedItem);
    }

    private void validateItemBase(ItemBase actualItem, ItemBase expectedItem) {
        Assert.assertEquals(actualItem.description, expectedItem.description, "test for 'description' consistency");
        Assert.assertEquals(actualItem.price, expectedItem.price, "test for 'price' consistency");
        Assert.assertEquals(actualItem.sku, expectedItem.sku, "test for 'sku' consistency");
    }
}