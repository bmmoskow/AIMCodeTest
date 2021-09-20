package ben.aimcodetest.apitests;

import ben.aimcodetest.apitests.Models.Item;
import ben.aimcodetest.apitests.Models.ItemBase;
import ben.aimcodetest.apitests.Models.SkuMetadata;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static io.restassured.RestAssured.*;
import io.restassured.response.*;
import io.restassured.specification.*;
import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class APITests {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static RequestSpecification httpRequest;

    static {
        baseURI = "https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus";
        httpRequest = given();
    }

    /**
     * test GET API and then the first 10 skus
     * @throws JsonProcessingException
     */
    @Test()
    public void getTest() throws JsonProcessingException {
        Item[] items = executeGetAllTest();

        for (Integer i = 0; i < 10; i++) {
            executeGetTest(items[i]);
        }
    }

    /**
     * test the following
     * 1. POST
     * 2. GET for the just created sku
     * 3. POST for the same sku to update the data
     * 4. GET for the same sku to validate the updated data
     * 5. DELETE the sku
     * 6. GET to validate that the sku is gone
     * @throws JsonProcessingException
     */
    @Test()
    public void postGetDeleteTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "post test";
        postItem.price = "1.23";
        postItem.sku = randomUUID().toString();

        Long minCreatedAt = now().getEpochSecond(); // time right before post
        Item postResponseItem = executePostTest(postItem);
        Long maxCreatedAt = now().getEpochSecond(); // time right after post

        try {
            executeGetTestWithTimeChecks(postResponseItem, minCreatedAt, maxCreatedAt);

            ItemBase updatedPostItem = new ItemBase();
            updatedPostItem.description = postItem.description;
            postItem.price = "1.24";
            updatedPostItem.sku = postItem.sku;

            Long minUpdatedAt = now().getEpochSecond(); // time right before post
            Item updatedPostResponseItem = executePostTest(updatedPostItem);
            Long maxUpdatedAt = now().getEpochSecond(); // time right before post

            executeGetTestWithTimeChecks(updatedPostResponseItem, minCreatedAt, maxCreatedAt, minUpdatedAt, maxUpdatedAt);
        }
        finally {
            executeDeleteTest(postItem);
            executeGetUnknownItemTest(postItem.sku);
        }
    }

    /**
     * test GET for an unknown sku
     */
    @Test
    public void getUnknownItemTest() {
        executeGetUnknownItemTest(randomUUID().toString());
    }

    /**
     * test DELETE for an unknown sku
     */
    @Test
    public void deleteUnknownItemTest() {
        Response deleteResponse = httpRequest.delete("/" + randomUUID().toString());
        int statusCode = deleteResponse.getStatusCode();
        assertEquals(statusCode, 404, "should get 404 if the item to be deleted doesn't exist");
    }

    /**
     * test POST for an empty description
     * @throws JsonProcessingException
     */
    @Test()
    public void postEmptyDescriptionTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "";
        postItem.price = "1.23";
        postItem.sku = randomUUID().toString();

        executeNegativePostTest(postItem);
    }

    /**
     * test POST for an empty price
     * @throws JsonProcessingException
     */
    @Test()
    public void postEmptyPriceTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "post test";
        postItem.price = "";
        postItem.sku = randomUUID().toString();

        executeNegativePostTest(postItem);
    }

    /**
     * test POST for an empty sku
     * @throws JsonProcessingException
     */
    @Test()
    public void postEmptySkuTest() throws JsonProcessingException {
        ItemBase postItem = new ItemBase();
        postItem.description = "post test";
        postItem.price = "1.23";
        postItem.sku = "";

        executeNegativePostTest(postItem);
    }

    /**
     * execute the GET API that returns all the sku data
     * and run validation tests
     * @return all sku items resulting from the API
     * @throws JsonProcessingException
     */
    private Item[] executeGetAllTest() throws JsonProcessingException {
        Response response = httpRequest.get("");
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200, "expect 200 result");
        String body = response.getBody().asString();
        return objectMapper.readValue(body, Item[].class);
    }

    /**
     * run the GET API for a particular sku and validate the result
     * @param inputItem
     * @return Item object resulting from the GET-all API for a particular sku
     * @throws JsonProcessingException
     */
    private Item executeGetTest(Item inputItem) throws JsonProcessingException {
        Response getResponse = httpRequest.get("/" + inputItem.sku);
        int statusCode = getResponse.getStatusCode();
        assertEquals(statusCode, 200, "expect 200 result");
        String getResponseBody = getResponse.getBody().asString();
        SkuMetadata getResponseSku = objectMapper.readValue(getResponseBody, SkuMetadata.class);
        assertEquals(getResponseSku.ResponseMetadata.HTTPStatusCode, statusCode, "inconsistent status code");
        assertEquals(getResponseSku.ResponseMetadata.HTTPHeaders.content_type, "application/x-amz-json-1.0", "unexpected content-type");
        assertTrue(getResponseSku.ResponseMetadata.RetryAttempts >= 0, "retry attempts is non-negative");
        validateItem(getResponseSku.Item, inputItem);

        return getResponseSku.Item;
    }

    private void executeGetTestWithTimeChecks(Item postResponseItem, Long minCreatedAt, Long maxCreatedAt) throws JsonProcessingException {
        executeGetTestWithTimeChecks(postResponseItem, minCreatedAt,maxCreatedAt, minCreatedAt, maxCreatedAt);
    }

    /**
     * test the get functionality and also check that the timestamps are correct
     * @param postResponseItem data from the POST response for the particular sku
     * @throws JsonProcessingException
     */
    private void executeGetTestWithTimeChecks(Item postResponseItem, Long minCreatedAt, Long maxCreatedAt,
                                              Long minUpdatedAt, Long maxUpdatedAt) throws JsonProcessingException {
        Item getResponseItem = executeGetTest(postResponseItem);
        Long createdAt = Long.parseLong(getResponseItem.createdAt);
        Long updateAt = Long.parseLong(getResponseItem.updatedAt);
        assertTrue(createdAt >= minCreatedAt && createdAt <= maxCreatedAt,
                "test that 'createdAt' is in the right time interval");
        assertTrue(updateAt >= minUpdatedAt && updateAt <= maxUpdatedAt,
                "test that 'updateAt' is in the right time interval");

    }

    /**
     * execute the POST API and validate the result
     * @param postItem data for the POST body
     * @return output of the POST
     * @throws JsonProcessingException
     */
    private Item executePostTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        httpRequest.body(objectMapper.writeValueAsString(postItem));
        Response response = httpRequest.post("");
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200, "expect 200 result");
        String responseBody = response.getBody().asString();
        Item postResponseItem = objectMapper.readValue(responseBody, Item.class);
        validateItemBase(postResponseItem, postItem);

        return postResponseItem;
    }

    /**
     * execute the DELETE API and validate the result
     * @param postItem output of the DELETE
     * @throws JsonProcessingException
     */
    private void executeDeleteTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        Response deleteResponse = httpRequest.delete("/" + postItem.sku.toString());
        int statusCode = deleteResponse.getStatusCode();
        assertEquals(statusCode, 200, "expect 200 result");
        String deleteResponseBody = deleteResponse.getBody().asString();
        assertEquals(deleteResponseBody, "", "expect empty response body");
    }

    /**
     * test the POST API where the sku doesn't exist
     * @param sku sku ID
     */
    private void executeGetUnknownItemTest(String sku) {
        Response getResponse = httpRequest.get("/" + sku);
        int statusCode = getResponse.getStatusCode();
        assertEquals(statusCode, 404, "expect 404 result for a sku that doesn't exist");
    }

    /**
     * POST API test where certain fields are empty
     * @param postItem data for the POST body
     * @throws JsonProcessingException
     */
    private void executeNegativePostTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        httpRequest.body(objectMapper.writeValueAsString(postItem));
        Response response = httpRequest.post("");
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 400, "expect 400 for a malformed POST body");
    }

    /**
     * compare values between Item objects
     * @param actualItem
     * @param expectedItem
     */
    private void validateItem(Item actualItem, Item expectedItem) {
        assertEquals(actualItem.createdAt, expectedItem.createdAt, "test for 'createdAt' consistency");
        assertEquals(actualItem.updatedAt, expectedItem.updatedAt, "test for 'updatedAt' consistency");
        validateItemBase(actualItem, expectedItem);
    }

    /**
     * compare values ItemBase objects
     * @param actualItem
     * @param expectedItem
     */
    private void validateItemBase(ItemBase actualItem, ItemBase expectedItem) {
        assertEquals(actualItem.description, expectedItem.description, "test for 'description' consistency");
        assertEquals(actualItem.price, expectedItem.price, "test for 'price' consistency");
        assertEquals(actualItem.sku, expectedItem.sku, "test for 'sku' consistency");
    }
}