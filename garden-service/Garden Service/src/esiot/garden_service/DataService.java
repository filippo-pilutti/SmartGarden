package esiot.garden_service;

import io.vertx.core.AbstractVerticle;





import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.LinkedList;

/*
 * Data Service as a vertx event-loop 
 */
public class DataService extends AbstractVerticle {

	CommChannel scc;

	private int port;
	private static final int MAX_SIZE = 10;
	private static final String SERIAL_PORT = "COM7";
	private static final int BAUD_RATE = 9600;
	private LinkedList<DataPoint> values;
	private JsonObject currentStatus;
	
	public DataService(int port) {
		values = new LinkedList<>();		
		this.port = port;
		currentStatus = new JsonObject();
	}

	@Override
	public void start() throws Exception {
		try {
            scc = new SerialCommChannel(SERIAL_PORT, BAUD_RATE);
            log("Serial communication channel initialized successfully.");
        } catch (Exception e) {
            log("Error initializing serial communication: " + e.getMessage());
            throw e; // Throw exception to handle initialization failure
        }
		
		vertx.setPeriodic(100, id -> {
			if (scc.isMsgAvailable()) {
				String msg = "";
				try {
					msg = scc.receiveMsg();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				updateStatus(msg);
			}
		});
		
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.post("/api/data").handler(this::handleAddNewData);
		router.get("/api/data").handler(this::handleGetData);
		router.get("/api/status").handler(context -> {
			try {
				handleGetStatus(context);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		vertx
			.createHttpServer()
			.requestHandler(router)
			.listen(port);

		log("Service ready.");
	}
	
	private void handleAddNewData(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		// log("new msg "+routingContext.getBodyAsString());
		JsonObject res = routingContext.getBodyAsJson();
		if (res == null) {
			sendError(400, response);
		} else {
			int value = res.getInteger("value");
			String name = res.getString("name");
			int range = res.getInteger("range");
			//long time = System.currentTimeMillis();
			
			//Invia il range sulla seriale
			if (name.equals("Temp")) {
				scc.sendMsg("T" + range);
			} else if (name.equals("Light")) {
				scc.sendMsg("L" + range);
			}
			
			values.addFirst(new DataPoint(name, value, range));
			if (values.size() > MAX_SIZE) {
				values.removeLast();
			}
			
			log("New data. Name: " + name + ", value: " + value + ", range: " + range + ".");
			response.setStatusCode(200).end();
		}
	}
	
	private void handleGetData(RoutingContext routingContext) {
		JsonArray arr = new JsonArray();
		for (DataPoint p: values) {
			JsonObject data = new JsonObject();
			data.put("name", p.getName());
			data.put("value", p.getValue());
			data.put("range", p.getRange());
			arr.add(data);
		}
		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(arr.encodePrettily());
	}
	
	private void handleGetStatus(RoutingContext routingContext) throws InterruptedException {
		
		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(currentStatus.encodePrettily());
	}
	
	private void updateStatus(String status) {
		currentStatus.put("status", status);
		log("Updated status from Arduino: " + status);
	}
	
	private void sendError(int statusCode, HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}

	private void log(String msg) {
		System.out.println("[DATA SERVICE] " + msg);
	}

}