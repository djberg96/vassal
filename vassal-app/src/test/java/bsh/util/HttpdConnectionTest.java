package bsh.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class HttpdConnectionTest {
  @Test
  public void emptyRequestReturnsBadRequestAndClosesClient() {
    final TestSocket client = new TestSocket("");

    new HttpdConnection(client).run();

    assertThat(client.output(), containsString("Empty Request"));
    assertThat(client.isClosed(), is(true));
  }

  @Test
  public void httpOneRequestWithMissingHeaderTerminatorDoesNotThrow() {
    final TestSocket client = new TestSocket("POST /missing HTTP/1.0\r\n");

    new HttpdConnection(client).run();

    assertThat(client.output(), containsString("Bad Request"));
    assertThat(client.isClosed(), is(true));
  }

  private static class TestSocket extends Socket {
    private final ByteArrayInputStream input;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private boolean closed;

    TestSocket(String request) {
      input = new ByteArrayInputStream(request.getBytes(UTF_8));
    }

    @Override
    public InputStream getInputStream() {
      return input;
    }

    @Override
    public OutputStream getOutputStream() {
      return output;
    }

    @Override
    public synchronized void close() throws IOException {
      closed = true;
    }

    @Override
    public boolean isClosed() {
      return closed;
    }

    String output() {
      return output.toString(UTF_8);
    }
  }
}
