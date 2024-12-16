package api.basic;

import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.asserts.SoftAssert;
import com.github.javafaker.Faker;
import api.endpoint.BookEndPoints;
import api.payload.BookPayload;
import api.payload.UserPayload;

public class BaseTest {

	protected SoftAssert softAssert;
	protected Faker faker;
	protected UserPayload userPayload;
	protected static BookEndPoints bookEndPoints;
	protected BookPayload bookPayload;

	private static final Logger log = LogManager.getLogger(BaseTest.class);

	@BeforeClass
	public void setup() {
		softAssert = new SoftAssert();
		faker = new Faker();
		bookEndPoints = new BookEndPoints();
		bookPayload = new BookPayload();
	}

	public static String Url(String Endpoints) {
		ResourceBundle routes = ResourceBundle.getBundle("routes");
		return routes.getString("baseUrl") + routes.getString(Endpoints);
	}

	public static String BookUrl(String Endpoints) {
		ResourceBundle routes = ResourceBundle.getBundle("routes");
		return routes.getString("bookBaseUrl") + routes.getString(Endpoints);
	}

	protected static String getCredentials(String data) {
		ResourceBundle credentials = ResourceBundle.getBundle("credentials");
		return credentials.getString(data);
	}

}
