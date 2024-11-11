package com.example.mircoservices.order.integration;

import lombok.extern.slf4j.Slf4j;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

import com.example.microservices.order.OrderApplication;
import com.example.microservices.order.event.OrderPlacedEvent;
import com.example.mircoservices.order.stub.InventoryClientStub;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.restassured.RestAssured;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = OrderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@DirtiesContext
@EmbeddedKafka(controlledShutdown = true, topics = { "order-placed" })
class OrderIntegrationTests {

	@ServiceConnection
	static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0");
	@LocalServerPort
	private Integer port;

	private final static String orderPlacedTopic = "order-placed";

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	private KafkaListenerEndpointRegistry registry;

	@Value("${spring.embedded.kafka.brokers}")
	private String brokerAddresses;

	@BeforeEach
	void setup() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		// Wait until the partitions are assigned.
		registry.getListenerContainers().stream().forEach(container -> ContainerTestUtils.waitForAssignment(container,
				embeddedKafkaBroker.getPartitionsPerTopic()));

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.resetAllScenarios();
		WireMock.resetToDefault();

		registerSchema(1, orderPlacedTopic, OrderPlacedEvent.getClassSchema().toString());
	}

	/**
	 * Register the schema derived from the avro generated class for the given
	 * topic.
	 *
	 * @param schemaId the schema id to use
	 * @param topic    the topic name for the message schema to register
	 * @param schema   the schema JSON string
	 */
	private void registerSchema(int schemaId, String topic, String schema) throws Exception {
		// Stub for the POST of the subject, to return the associated schemaId.
		// (The Avro schema, obtained by the serializer by reflection, will be in the
		// body POSTed).
		// This is used by the Producer when serializing.
		stubFor(post(urlPathMatching("/subjects/" + topic + "-value"))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
						.withBody("{\"id\":" + schemaId + "}")));

		// Stub for the GET registered schema call for the given schema Id, returning
		// the schema.
		// This is used by the Consumer when deserializing.
		// /schemas/ids/1?fetchMaxId=false
		final SchemaString schemaString = new SchemaString(schema);
		stubFor(get(urlPathMatching("/schemas/ids/" + schemaId))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
						.withBody(schemaString.toJson())));
	}

	static {
		mySQLContainer.start();
	}

	@Test
	void shouldSubmitOrder() {
		String submitOrderJson = """
				{
				     "skuCode": "iphone_15",
				     "price": 1000,
				     "quantity": 1,
					 "userDetails":
						{
							"email": "joe@gmail.com",
							"firstName": "joe",
							"lastName": "bloggs"
						}
				}
				""";
		InventoryClientStub.stubInventoryCall("iphone_15", 1);

		var responseBodyString = RestAssured.given()
				.contentType("application/json")
				.body(submitOrderJson)
				.when()
				.post("/api/order")
				.then()
				.log().all()
				.statusCode(201)
				.extract()
				.body().asString();

		assertThat(responseBodyString, Matchers.is("Order Placed Successfully"));
	}

	@Test
	void shouldFailOrderWhenProductIsNotInStock() {
		String submitOrderJson = """
				{
				     "skuCode": "iphone_15",
				     "price": 1000,
				     "quantity": 1000
				}
				""";
		InventoryClientStub.stubInventoryCall("iphone_15", 1000);

		RestAssured.given()
				.contentType("application/json")
				.body(submitOrderJson)
				.when()
				.post("api/order")
				.then()
				.log().all()
				.statusCode(500);
	}
}
