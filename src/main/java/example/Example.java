package example;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Example {

  private static final OkHttpClient client = new OkHttpClient();
  private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(8080);

  static {
    mockServer.when(
        HttpRequest.request()
          .withPath("/test")
        )
        .respond(
            HttpResponse.response()
              .withBody("OK")
              .withDelay(new Delay(TimeUnit.SECONDS, 10))
        );

    client.setConnectTimeout(15, TimeUnit.SECONDS);
    client.setWriteTimeout(15, TimeUnit.SECONDS);
    client.setReadTimeout(15, TimeUnit.SECONDS);
  }

  private static void print(String message) {
    System.out.println(message + ": " + DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.sss"));
  }

  private static void doRequest() {
    print("Starting doRequest method");

    Request request = new Request.Builder()
      .url("http://localhost:8080/test")
      .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Request request, IOException e) {
        e.printStackTrace();
        stopMockServer();
      }

      @Override
      public void onResponse(Response response) throws IOException {
        print("Response [" + response.body().string() + "]");
        stopMockServer();
      }

      private void stopMockServer() {
        print("Stopping MockServer");
        if (mockServer != null) {
          mockServer.stop();
        }
      }
    });

    print("Ending doRequest method");
  }

  public static void main(String[] args) {
    print("Starting main method");
    doRequest();
    print("Ending main method");
  }
}
