package ch.retomerz.recaptcha;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag("my-recaptcha")
public class ReCaptcha extends Component {

  private final String secretKey;

  private boolean valid;

  public ReCaptcha(String websiteKey, String secretKey) {
    this.secretKey = secretKey;

    Element div = new Element("div");
    div.setAttribute("class", "g-recaptcha");
    div.setAttribute("data-sitekey", websiteKey);
    div.setAttribute("data-callback", "myCallback"); // Note that myCallback must be declared in the global scope.
    getElement().appendChild(div);

    Element script = new Element("script");
    script.setAttribute("type", "text/javascript");
    script.setAttribute("src", "https://www.google.com/recaptcha/api.js?hl=en");
    getElement().appendChild(script);

    UI.getCurrent().getPage().executeJs("$0.init = function () {\n" +
            "    function myCallback(token) {\n" +
            "        $0.$server.callback(token);\n" +
            "    }\n" +
            "    window.myCallback = myCallback;" + // See myCallback comment above.
            "};\n" +
            "$0.init();\n", this);
  }

  public boolean isValid() {
    return valid;
  }

  @ClientCallable
  public void callback(String response) {
    try {
      valid = checkResponse(response);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean checkResponse(String response) throws IOException {
    String remoteAddr = getRemoteAddr(VaadinService.getCurrentRequest());

    String url = "https://www.google.com/recaptcha/api/siteverify";

    String postData = "secret=" + URLEncoder.encode(secretKey, "UTF-8") +
            "&remoteip=" + URLEncoder.encode(remoteAddr, "UTF-8") +
            "&response=" + URLEncoder.encode(response, "UTF-8");


    String result = doHttpPost(url, postData);

    System.out.println("Verify result:\n" + result);

    JsonObject parse = Json.parse(result);
    JsonValue jsonValue = parse.get("success");
    return jsonValue != null && jsonValue.asBoolean();
  }

  private static String getRemoteAddr(VaadinRequest request) {
    String ret = request.getHeader("x-forwarded-for");
    if (ret == null || ret.isEmpty()) {
      ret = request.getRemoteAddr();
    }
    return ret;
  }

  private static String doHttpPost(String urlStr, String postData) throws IOException {
    URL url = new URL(urlStr);
    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    try {

      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setReadTimeout(10_000);
      con.setConnectTimeout(10_000);
      con.setUseCaches(false);

      try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8)) {
        writer.write(postData);
      }

      StringBuilder response = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line).append("\n");
        }
      }

      return response.toString();
    } finally {
      con.disconnect();
    }
  }
}
