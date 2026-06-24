package VASSAL.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.stream.Stream;

import VASSAL.Info;
import VASSAL.build.GameModule;
import VASSAL.tools.http.HttpClientService;
import VASSAL.tools.http.HttpResponseData;

public class BugUtils {
  private static final URI BUG_REPORT_URI = URI.create("https://vassalengine.org/util/abr"); //NON-NLS
  private static final HttpClientService HTTP =
    HttpClientService.createDefault(Duration.ofSeconds(60));

  private BugUtils() {}

  public static void sendBugReport(String email,
                                   String description,
                                   String errorLog,
                                   Throwable t) throws IOException {
    sendBugReport(email, description, errorLog, t, BUG_REPORT_URI);
  }

  static void sendBugReport(String email,
                            String description,
                            String errorLog,
                            Throwable t,
                            URI uri) throws IOException {
    final MultipartBody body = buildBugReportBody(email, description, errorLog, t);
    final HttpRequest request = HttpRequest.newBuilder(uri)
      .POST(HttpRequest.BodyPublishers.ofByteArray(body.body()))
      .header("Content-Type", body.contentType()) //NON-NLS
      .build();

    final HttpResponseData response = HTTP.send(request);
    final int status = response.status();
    // GitHub documents 201 as the expected success code, but starting in
    // May 2026 we observed that 200 is sometimes returned, contra the
    // documentation, so we check for both.
    if (status != 200 && status != 201) {
      throw new IOException("Bug report failed: " + response.status() + ": " + response.body()); //NON-NLS
    }
  }

  static MultipartBody buildBugReportBody(String email,
                                          String description,
                                          String errorLog,
                                          Throwable t) throws IOException {
    final String boundary = "----VASSAL-BugReport-" + System.nanoTime(); //NON-NLS
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    writeTextPart(out, boundary, "version", Info.getReportableVersion()); //NON-NLS
    writeTextPart(out, boundary, "email", email); //NON-NLS
    writeTextPart(out, boundary, "summary", getSummary(t)); //NON-NLS
    writeTextPart(out, boundary, "description", getDescription(description, errorLog)); //NON-NLS
    writeFilePart(
      out,
      boundary,
      "log", //NON-NLS
      Info.getErrorLogPath().getName(),
      errorLog.getBytes(StandardCharsets.UTF_8)
    );
    out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8)); //NON-NLS

    return new MultipartBody(
      "multipart/form-data; boundary=" + boundary, //NON-NLS
      out.toByteArray()
    );
  }

  private static void writeTextPart(
    ByteArrayOutputStream out,
    String boundary,
    String name,
    String value
  ) throws IOException {
    out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8)); //NON-NLS
    out.write(("Content-Disposition: form-data; name=\"" + escapeQuoted(name) + "\"\r\n").getBytes(StandardCharsets.UTF_8)); //NON-NLS
    out.write("Content-Type: text/plain; charset=UTF-8\r\n\r\n".getBytes(StandardCharsets.UTF_8)); //NON-NLS
    out.write(value.getBytes(StandardCharsets.UTF_8));
    out.write("\r\n".getBytes(StandardCharsets.UTF_8)); //NON-NLS
  }

  private static void writeFilePart(
    ByteArrayOutputStream out,
    String boundary,
    String name,
    String filename,
    byte[] body
  ) throws IOException {
    out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8)); //NON-NLS
    out.write(("Content-Disposition: form-data; name=\"" + escapeQuoted(name)
      + "\"; filename=\"" + escapeQuoted(filename) + "\"\r\n").getBytes(StandardCharsets.UTF_8)); //NON-NLS
    out.write("Content-Type: text/plain\r\n\r\n".getBytes(StandardCharsets.UTF_8)); //NON-NLS
    out.write(body);
    out.write("\r\n".getBytes(StandardCharsets.UTF_8)); //NON-NLS
  }

  private static String escapeQuoted(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\""); //NON-NLS
  }

  private static String getDescription(String description, String errorLog) {
    final GameModule g = GameModule.getGameModule();
    return
      description + "\n\n" +
      (g == null ? "" : g.getGameName() + " v" + g.getGameVersion() + " ") +
      Info.getVersion() + "\n\n" + //NON-NLS
      getStackTraceSummary(errorLog);
  }

  private static String getStackTraceSummary(String errorLog) {
    final StringBuilder summary = new StringBuilder();
    final Stream<String> log = errorLog.substring(errorLog.lastIndexOf("ERROR VASSAL.tools.ErrorDialog")).lines(); //NON-NLS
    log.skip(1).limit(5).forEach(l -> summary.append(l.replace('\t', ' ')).append('\n'));
    return summary.toString();
  }

  private static String getSummary(Throwable t) {
    final GameModule g = GameModule.getGameModule();
    String summary = g == null ? "" : "[" + g.getGameName() + "] ";
    if (t == null) {
      summary += "Automated Bug Report"; //NON-NLS
    }
    else {
      final String tc = t.getClass().getName();
      summary += tc.substring(tc.lastIndexOf('.') + 1);

      if (t.getMessage() != null) {
        summary += ": " + t.getMessage();
      }
    }
    return summary;
  }

// REFACTOR: Move this to the error-log utility code.
  public static String getErrorLog() {
    final File f = Info.getErrorLogPath();
    try {
      return Files.readString(f.toPath(),  StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      // Don't bother logging this---if we can't read the errorLog,
      // then we probably can't write to it either.
      return null;
    }
  }

  record MultipartBody(String contentType, byte[] body) {
  }
}
