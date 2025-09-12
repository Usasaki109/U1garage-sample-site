import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.net.http.*;
import java.net.URI;
import java.util.regex.*;

public class LoanerUpdater {
  public static void main(String[] args) throws Exception {
    String url = getenvOrThrow("LOANER_URL");
    String contactPath = System.getenv().getOrDefault("CONTACT_PATH", "contact.html");

    // ---- 1) 取得 & 検証 ----
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest req = HttpRequest.newBuilder(URI.create(url))
      .GET().header("User-Agent", "LoanerUpdater/1.0").build();
    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() != 200) fail("Fetch failed: HTTP " + res.statusCode());

    String body = res.body().trim();
    if (body.isEmpty()) fail("Empty response");
    char ch = body.charAt(0);
    if (ch < '0' || ch > '9') fail("Invalid first char: " + ch);
    int n = ch - '0';

    // ---- 2) data/loaner.json を上書き ----
    Path dataDir = Paths.get("data");
    Files.createDirectories(dataDir);
    String date = LocalDate.now(ZoneId.of("Asia/Tokyo")).toString();
    String json = String.format("{\"count\":%d,\"date\":\"%s\"}%n", n, date);
    Files.writeString(dataDir.resolve("loaner.json"), json, StandardCharsets.UTF_8);

    // ---- 3) contact.html の <span data-loaner-count> を差し替え ----
    Path contact = Paths.get(contactPath);
    if (Files.exists(contact)) {
      String html = Files.readString(contact, StandardCharsets.UTF_8);
      Pattern p = Pattern.compile("(<span[^>]*data-loaner-count[^>]*>)(.*?)(</span>)",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
      String replaced = p.matcher(html).replaceAll("$1" + n + "$3");
      if (!replaced.equals(html)) {
        Files.writeString(contact, replaced, StandardCharsets.UTF_8);
        System.out.println("Updated " + contactPath + " with count " + n);
      } else {
        System.out.println(contactPath + " already up-to-date.");
      }
    } else {
      System.out.println(contactPath + " not found; skipped HTML update.");
    }

    System.out.println("Wrote data/loaner.json with count " + n + " and date " + date);
  }

  private static String getenvOrThrow(String key) {
    String v = System.getenv(key);
    if (v == null || v.isBlank()) fail(key + " not set");
    return v;
  }
  private static void fail(String msg) {
    System.err.println(msg);
    System.exit(1);
  }
}
