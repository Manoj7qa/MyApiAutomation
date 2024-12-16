package api.test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.instanceOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import api.basic.BaseTest;
import api.endpoint.BookEndPoints;
import api.payload.BookPayload;
import api.payload.OrderBookPayload;
import api.payload.PatchOrderPayload;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class BookTest extends BaseTest {
	Faker faker;
	BookPayload bookPayload;
	OrderBookPayload orderBookPayload;
	PatchOrderPayload patchOrderPayload;
	String bookName, bookType, orderId, userToken;
	int bookId;
	public Logger logger; // for logs

	public String jsonSchema(String jsonSchemaPath) {
		String schemaPath = "./src/test/resources/Schemas/bookSchema.json";
		JsonNode rootnode = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			rootnode = mapper.readTree(new File(schemaPath));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rootnode.get(jsonSchemaPath).toString();
	}

	@BeforeTest
	public void setup() {
		faker = new Faker();
		bookPayload = new BookPayload();
		bookPayload.setClientName(faker.name().username());
		bookPayload.setClientEmail(faker.internet().safeEmailAddress());
		// logs
		logger = LogManager.getLogger(this.getClass());

		logger.debug("debugging.....");

	}

	@Test(priority = 1)
	public void testPostClient() {
		logger.info("********** Creating User  ***************");
		Response response = BookEndPoints.createClient(bookPayload);
		response.then().log().body().statusCode(201).contentType("application/json").header("Connection", "keep-alive")
				.body("accessToken", instanceOf(String.class)).body(matchesJsonSchema(jsonSchema("postClient")));
		JsonPath jsonData = response.jsonPath();
		userToken = jsonData.getString("accessToken");
		logger.info("**********User is creatged  ***************");
	}

	@Test(priority = 2)
	public void testGetBookStatus() {
		logger.info("********** Reading User Info ***************");
		Response response = bookEndPoints.getBookStatus(userToken);
		response.then().log().body().statusCode(200).statusLine("HTTP/1.1 200 OK").contentType("application/json")
				.header("Connection", "keep-alive").body("status", instanceOf(String.class))
				.body(matchesJsonSchema(jsonSchema("getBookStatus")));
		logger.info("**********User info  is displayed ***************");

	}

	@Test(priority = 3)
	public void testGetAllBooks() {
		logger.info("**********  Read All Books ***************");
		Response response = bookEndPoints.getAllBooks(userToken);
		response.then().log().all().statusCode(200).statusLine("HTTP/1.1 200 OK").contentType("application/json")
				.header("Connection", "keep-alive").body(matchesJsonSchema(jsonSchema("getAllBooks")));
		JsonPath jsonData = response.jsonPath();
		List<Map<String, Object>> books = jsonData.getList("");
		Assert.assertFalse(books.isEmpty(), "Book list is empty!");
		for (int i = 0; i < books.size(); i++) {
			response.then().body("[" + i + "]", instanceOf(Map.class)).body("[" + i + "].id", instanceOf(Integer.class))
					.body("[" + i + "].name", instanceOf(String.class))
					.body("[" + i + "].type", instanceOf(String.class))
					.body("[" + i + "].available", instanceOf(Boolean.class));
			if (jsonData.getBoolean("[" + i + "].available")) {
				bookId = jsonData.getInt("[" + i + "].id");
				bookName = jsonData.getString("[" + i + "].name");
				bookType = jsonData.getString("[" + i + "].type");
			}
		}
		logger.info("********** All Books Info Displayed ***************");

	}

	@Test(priority = 4)
	public void testGetSingleBooks() {
		logger.info("**********   Read Single Books  ***************");
		Response response = bookEndPoints.getSingleBooks(userToken, bookId);
		response.then().log().body().statusCode(200).statusLine("HTTP/1.1 200 OK").contentType("application/json")
				.header("Connection", "keep-alive").body(matchesJsonSchema(jsonSchema("getSingleBooks")))
				.body("id", instanceOf(Integer.class)).body("name", instanceOf(String.class))
				.body("author", instanceOf(String.class)).body("type", instanceOf(String.class))
				.body("price", instanceOf(Float.class)).body("current-stock", instanceOf(Integer.class))
				.body("available", instanceOf(Boolean.class)).body(matchesJsonSchema(jsonSchema("getSingleBooks")));
		JsonPath jsonData = response.jsonPath();
		assertEquals(jsonData.getInt("id"), bookId);
		assertEquals(jsonData.getString("name"), bookName);
		assertEquals(jsonData.getString("type"), bookType);
		assertTrue(jsonData.getBoolean("available"));
		assertTrue(jsonData.getInt("current-stock") != 0);
		logger.info("********** Books Info Displayed ***************");
	}

	@Test(priority = 5)
	public void testPostOrder() {
		logger.info("********** Order a Books ***************");
		orderBookPayload = new OrderBookPayload();
		orderBookPayload.setBookId(bookId);
		orderBookPayload.setCustomerName(faker.name().username());
		Response response = bookEndPoints.postOrder(orderBookPayload, userToken);
		response.then().log().body().statusCode(201).statusLine("HTTP/1.1 201 Created").contentType("application/json")
				.header("Connection", "keep-alive").body(matchesJsonSchema(jsonSchema("postOrder")))
				.body("created", instanceOf(Boolean.class)).body("orderId", instanceOf(String.class));
		JsonPath jsonData = response.jsonPath();
		assertTrue(jsonData.getBoolean("created"));
		orderId = jsonData.getString("orderId");
		logger.info("********** Order Placed  ***************");

	}

	@Test(priority = 6)
	public void testGetAllOrders() {
		logger.info("********** Read All Order Info ***************");
		Response response = bookEndPoints.getAllOrders(userToken);
		response.then().log().body().statusCode(200).statusLine("HTTP/1.1 200 OK").contentType("application/json")
				.header("Connection", "keep-alive").body(matchesJsonSchema(jsonSchema("getAllOrders")));
		JsonPath jsonData = response.jsonPath();
		List<Map<String, Object>> orders = jsonData.getList("");
		Assert.assertFalse(orders.isEmpty(), "Order list is empty!");
		for (int i = 0; i < orders.size(); i++) {
			response.then().body("[" + i + "]", instanceOf(Map.class)).body("[" + i + "].id", instanceOf(String.class))
					.body("[" + i + "].bookId", instanceOf(Integer.class))
					.body("[" + i + "].customerName", instanceOf(String.class))
					.body("[" + i + "].createdBy", instanceOf(String.class))
					.body("[" + i + "].quantity", instanceOf(Integer.class))
					.body("[" + i + "].timestamp", instanceOf(Long.class));
			if (i == orders.size() - 1) {
				assertEquals(jsonData.getString("[" + i + "].id"), orderId);
				assertEquals(jsonData.getInt("[" + i + "].bookId"), bookId);
				assertEquals(jsonData.getString("[" + i + "].customerName"), orderBookPayload.getCustomerName());
			}
		}
		logger.info("********** Order All Info Displayed ***************");

	}

	@Test(priority = 7)
	public void testGetSingleOrders() {
		logger.info("********** Read Single Order Info ***************");
		Response response = bookEndPoints.getSingleOrder(userToken, orderId);
		response.then().log().body().statusCode(200).statusLine("HTTP/1.1 200 OK").contentType("application/json")
				.header("Connection", "keep-alive").body(matchesJsonSchema(jsonSchema("getSingleOrder")))
				.body("id", instanceOf(String.class)).body("bookId", instanceOf(Integer.class))
				.body("customerName", instanceOf(String.class)).body("createdBy", instanceOf(String.class))
				.body("quantity", instanceOf(Integer.class)).body("timestamp", instanceOf(Long.class));
		JsonPath jsonData = response.jsonPath();
		assertEquals(jsonData.getString("id"), orderId);
		assertEquals(jsonData.getInt("bookId"), bookId);
		assertEquals(jsonData.getString("customerName"), orderBookPayload.getCustomerName());
		String responseBody = response.getBody().asString();
		assertTrue(responseBody.startsWith("{") && responseBody.endsWith("}"), "Response is not a JSON object");
		logger.info("********** Single Order Info Displayed ***************");
	}

	@Test(priority = 8)
	public void testPatchOrders() {
		logger.info("********** Update Order Info ***************");
		patchOrderPayload = new PatchOrderPayload();
		patchOrderPayload.setCustomerName(faker.name().username());
		Response response = bookEndPoints.patchOrder(userToken, patchOrderPayload, orderId);
		response.then().log().all().statusCode(204).statusLine("HTTP/1.1 204 No Content").header("Connection",
				"keep-alive");
		logger.info("********** Order Customer Name Updated ***************");
	}

	@Test(priority = 9)
	public void testGetSingleOrders1() {
		logger.info("********** Read Updated Order Info ***************");
		Response response = bookEndPoints.getSingleOrder(userToken, orderId);
		response.then().log().body().statusCode(200).statusLine("HTTP/1.1 200 OK").contentType("application/json")
				.header("Connection", "keep-alive").body(matchesJsonSchema(jsonSchema("getSingleOrder")))
				.body("id", instanceOf(String.class)).body("bookId", instanceOf(Integer.class))
				.body("customerName", instanceOf(String.class)).body("createdBy", instanceOf(String.class))
				.body("quantity", instanceOf(Integer.class)).body("timestamp", instanceOf(Long.class));
		JsonPath jsonData = response.jsonPath();
		assertEquals(jsonData.getString("id"), orderId);
		assertEquals(jsonData.getInt("bookId"), bookId);
		assertEquals(jsonData.getString("customerName"), patchOrderPayload.getCustomerName());
		String responseBody = response.getBody().asString();
		assertTrue(responseBody.startsWith("{") && responseBody.endsWith("}"), "Response is not a JSON object");
		logger.info("********** Updated Order Info Displayed ***************");
	}

	@Test(priority = 10)
	public void testDeleteOrder() {
		logger.info("********** Delete The Order ***************");
		Response response = bookEndPoints.deleteOrder(userToken, orderId);
		response.then().log().all().statusCode(204).statusLine("HTTP/1.1 204 No Content").header("Connection",
				"keep-alive");
		logger.info("********** Order Deleted ***************");
	}

}
