package com.microsoft.azure.webjobs.script.it;

import com.microsoft.azure.webjobs.script.it.functions.HttpFunction;
import com.microsoft.azure.webjobs.script.it.utils.RequestSpecificationProvider;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import static org.assertj.core.api.Assertions.*;

import org.junit.*;
import java.util.Arrays;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class HttpIT {
    private static RequestSpecification spec;

    @BeforeClass
    public static void initSpec(){
        spec = new RequestSpecBuilder()
                .addRequestSpecification(RequestSpecificationProvider.getDefault())
                .setContentType(ContentType.JSON)
                .build();
    }

    @Test
    public void http_echo_body() {
        String value = "value";

        given().spec(spec)
                .body(value)
                .when()
                .post("/httpEcho")
                .then()
                .assertThat().statusCode(202)
                .and().body(equalTo(value));
    }

    @Test
    public void http_echo_query_param() {
        String value = "value";

        given().spec(spec)
                .queryParam("name", value)
                .when()
                .get("/httpEcho")
                .then()
                .assertThat().body(equalTo("Hello " + value + "!"));
    }

    @Test
    public void http_handle_same_name() {
        String value = "value";

        given().spec(spec)
            .queryParam("req", value)
            .when()
            .get("/httpSameName")
            .then()
            .assertThat().body(equalTo(value));
    }

    @Test
    public void http_handle_string() {
        String value = "lorem ipsum et al";
        String expected = "HttpFunction string content \"" + value + "\"!";

        given().spec(spec)
                .body(value)
                .when()
                .post("/httpHandleString")
                .then()
                .assertThat().body(equalTo(expected)).and().statusCode(280);
    }

    @Test
    public void http_handle_int() {
        int value = 10;
        String expected = Integer.toString(value + 111);

        given().spec(spec)
            .body(value)
            .when()
            .post("/httpHandleInt")
            .then()
            .assertThat().body(equalTo(expected)).and().statusCode(281);
    }

    @Test
    public void http_handle_int_array() {
        int[] value = new int[] { 10, 11, 12, 13, 14, 15 };
        int[] expected = Arrays.stream(value).map(i -> i + 222).toArray();

        int[] actual = given().spec(spec)
                .body(value)
                .when()
                .post("/httpHandleIntArray")
                .then()
                .statusCode(282)
                .extract().body().as(int[].class);

        assertThat(actual).containsExactly(expected);
    }

    @Test
    public void http_handle_pojo() {
        HttpFunction.Point value = new HttpFunction.Point(55, 66);
        HttpFunction.Point expected = new HttpFunction.Point(value.getX() + 333, value.getY() + 333);

        HttpFunction.Point actual = given().spec(spec)
                .body(value)
                .when()
                .post("/httpHandlePojo")
                .then()
                .statusCode(283)
                .extract().body().as(HttpFunction.Point.class);

        assertThat(actual.getX()).isEqualTo(expected.getX());
        assertThat(actual.getY()).isEqualTo(expected.getY());
    }

    @Test
    public void http_handle_pojo_array() {
        HttpFunction.Point[] value = new HttpFunction.Point[] {
                new HttpFunction.Point(77, 88),
                new HttpFunction.Point(99, 100)
        };

        HttpFunction.Point[] expected = Arrays.stream(value).map(p ->
                new HttpFunction.Point(p.getX() + 444, p.getY() + 444)
            ).toArray(HttpFunction.Point[]::new);

        HttpFunction.Point[] actual = given().spec(spec)
                .body(value)
                .when()
                .post("/httpHandlePojoArray")
                .then()
                .statusCode(284)
                .extract().body().as(HttpFunction.Point[].class);

        assertThat(extractProperty("x").from(actual)).contains(expected[0].getX(), expected[1].getX());
        assertThat(extractProperty("y").from(actual)).contains(expected[0].getY(), expected[1].getY());
    }

    @Test
    public void http_handle_legacy() {
        String value = "Http Request String Body (Legacy)";

        given().spec(spec)
                .header("Content-Type", "text/plain")
                .body(value)
                .when()
                .post("/httpHandleLegacy")
                .then()
                .assertThat().contentType("")
                .and().statusCode(285)
                .and().body(equalTo(value));
    }

    @Test
    public void http_handle_headers() {
        given().spec(spec)
                .when()
                .body("")
                .get("/httpHandleHeaders")
                .then()
                .assertThat().statusCode(286)
                .and().header("test-header", equalTo("test response header value"))
                .and().body(equalTo("Check header value"));
    }
}
