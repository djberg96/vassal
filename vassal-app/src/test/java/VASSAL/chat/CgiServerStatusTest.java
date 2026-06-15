package VASSAL.chat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class CgiServerStatusTest {
  @Test
  public void getStatusSkipsMalformedRows() throws IOException {
    final HttpRequestWrapper request = mock(HttpRequestWrapper.class);
    when(request.doGet(eq("getCurrentConnections"), any(Properties.class))).thenReturn(List.of( // NON-NLS
      "Module B\tRoom\tBeth", // NON-NLS
      "Malformed", // NON-NLS
      "Module A\tRoom\tAlice" // NON-NLS
    ));

    final ServerStatus.ModuleSummary[] status = new CgiServerStatus(request).getStatus();

    assertThat(status, arrayWithSize(2));
    assertThat(status[0].getModuleName(), is(equalTo("Module A")));
    assertThat(status[1].getModuleName(), is(equalTo("Module B")));
  }

  @Test
  public void getHistoryReturnsEmptyStatusWhenFetchFails() throws IOException {
    final HttpRequestWrapper request = mock(HttpRequestWrapper.class);
    when(request.doGet(eq("getConnectionHistory"), any(Properties.class)))
      .thenThrow(new IOException("offline"));

    final CgiServerStatus status = new CgiServerStatus(request);
    final ServerStatus.ModuleSummary[] history = status.getHistory(status.getSupportedTimeRanges()[0]);

    assertThat(history, arrayWithSize(0));
  }
}
