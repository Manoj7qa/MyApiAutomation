package api.endpoint;

import static io.restassured.RestAssured.given;
import api.basic.BaseTest;
import api.payload.BookPayload;
import api.payload.OrderBookPayload;
import api.payload.PatchOrderPayload;
import api.payload.UserPayload;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class BookEndPoints {
	public static Response createClient(BookPayload payload) {
		return given().contentType(ContentType.JSON).accept(ContentType.JSON).body(payload).when()
				.post(BaseTest.BookUrl("postClient"));
	}

	public static Response getBookStatus(String token) {
		return given().header("Authorization", "Bearer " + token).when().get(BaseTest.BookUrl("getBookStatus"));
	}

	public static Response getAllBooks(String token) {
		return given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).accept(ContentType.JSON)
				.when().get(BaseTest.BookUrl("getAllBooks"));

	}

	public static Response getSingleBooks(String token, int bookId) {
		return given().header("Authorization", "Bearer " + token).pathParam("bookId", bookId).when()
				.get(BaseTest.BookUrl("getSingleBooks"));

	}

	public static Response postOrder(OrderBookPayload payload, String token) {
		return given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON)
				.accept(ContentType.JSON).body(payload).when().post(BaseTest.BookUrl("postOrder"));
	}
	public static Response getAllOrders(String token) {
		return given().header("Authorization", "Bearer " + token).when()
				.get(BaseTest.BookUrl("getAllOrders"));

	}
	public static Response getSingleOrder(String token, String orderId) {
		return given().header("Authorization", "Bearer " + token).pathParam("orderId", orderId).when()
				.get(BaseTest.BookUrl("getSingleOrder"));
	}
	public static Response patchOrder(String token, PatchOrderPayload payload, String orderId) {
		return given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON)
				.accept(ContentType.JSON).pathParam("orderId", orderId).body(payload).when()
				.patch(BaseTest.BookUrl("patchOrder"));

	}
	public static Response deleteOrder(String token, String orderId) {
		return given().header("Authorization", "Bearer " + token).pathParam("orderId", orderId).when()
				.delete(BaseTest.BookUrl("deleteOrder"));
	}

}
