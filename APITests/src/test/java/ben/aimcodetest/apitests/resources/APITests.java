package ben.aimcodetest.apitests.resources;

import ben.aimcodetest.apitests.resources.Models.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import io.restassured.specification.*;
import io.restassured.response.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;
import java.util.UUID;

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
            Item getResponseItem = executeGetTest(items[i]);
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
            Item getResponseItem = executeGetTest(postResponseItem);

            ItemBase updatedPostItem = new ItemBase();
            updatedPostItem.description = postItem.description;
            postItem.price = "1.24";
            updatedPostItem.sku = postItem.sku;

            Item updatedPostResponseItem = executePostTest(updatedPostItem);
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
        Assert.assertEquals(statusCode, 404);
    }

    private Item[] executeGetAllTest() throws JsonProcessingException {
        Response response = httpRequest.get("");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200);
        String body = response.getBody().asString();
        return objectMapper.readValue(body, Item[].class);
    }

    private Item executeGetTest(Item imputItem) throws JsonProcessingException {
        Response getResponse = httpRequest.get("/" + imputItem.sku);
        int statusCode = getResponse.getStatusCode();
        Assert.assertEquals(statusCode, 200);
        String getResponseBody = getResponse.getBody().asString();
        System.out.println(getResponseBody);
        Sku getResponseSku = objectMapper.readValue(getResponseBody, Sku.class);
        validateItem(getResponseSku.Item, imputItem);

        return getResponseSku.Item;
    }

    private Item executePostTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        httpRequest.body(objectMapper.writeValueAsString(postItem));
        Response response = httpRequest.post("");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200);
        String responseBody = response.getBody().asString();
        Item postResponseItem = objectMapper.readValue(responseBody, Item.class);
        validateItemBase(postResponseItem, postItem);

        return postResponseItem;
    }

    private void executeDeleteTest(ItemBase postItem) throws JsonProcessingException {
        httpRequest.header("Content-Type", "application/json");
        Response deleteResponse = httpRequest.delete("/" + postItem.sku.toString());
        int statusCode = deleteResponse.getStatusCode();
        Assert.assertEquals(statusCode, 200);
        String deleteResponseBody = deleteResponse.getBody().asString();
        System.out.println(deleteResponseBody);
        ItemBase deleteResponseItem = objectMapper.readValue(deleteResponseBody, ItemBase.class);
        validateItemBase(deleteResponseItem, postItem);
    }

    private void executeGetUnknownItemTest(String id) {
        Response getResponse = httpRequest.get("/" + id);
        int statusCode = getResponse.getStatusCode();
        Assert.assertEquals(statusCode, 404);
    }

    private void validateItem(Item actualItem, Item expectedItem) {
        Assert.assertEquals(actualItem.createdAt, expectedItem.createdAt);
        Assert.assertEquals(actualItem.updatedAt, expectedItem.updatedAt);
        validateItemBase(actualItem, expectedItem);
    }

    private void validateItemBase(ItemBase actualItem, ItemBase expectedItem) {
        Assert.assertEquals(actualItem.description, expectedItem.description);
        Assert.assertEquals(actualItem.price, expectedItem.price);
        Assert.assertEquals(actualItem.sku, expectedItem.sku);
    }
}