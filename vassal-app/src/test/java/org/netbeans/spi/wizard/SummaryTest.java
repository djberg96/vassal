package org.netbeans.spi.wizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

public class SummaryTest {
    @Test
    public void textSummaryCanBeCreatedOffEdt() {
        final Summary summary = Summary.create("Done", "result");

        assertInstanceOf(JScrollPane.class, summary.getSummaryComponent());
        assertEquals("result", summary.getResult());
    }

    @Test
    public void listSummaryCanBeCreatedOffEdt() {
        final Summary summary = Summary.create(new String[] {"One", "Two"}, "result");

        assertInstanceOf(JScrollPane.class, summary.getSummaryComponent());
        assertEquals("result", summary.getResult());
    }

    @Test
    public void textSummaryCanBeCreatedOnEdt() throws Exception {
        final AtomicReference<Summary> summaryRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> summaryRef.set(Summary.create("Done", "result")));

        assertInstanceOf(JScrollPane.class, summaryRef.get().getSummaryComponent());
        assertEquals("result", summaryRef.get().getResult());
    }

    @Test
    public void customComponentSummaryUsesProvidedComponent() {
        final JScrollPane component = new JScrollPane();
        final Summary summary = Summary.create(component, "result");

        assertSame(component, summary.getSummaryComponent());
        assertEquals("result", summary.getResult());
    }
}
