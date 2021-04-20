package javaparser;

import java.util.function.Function;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;


public class PrettyPrinter implements Printer {

    private PrinterConfiguration configuration;

    private Function<PrettyPrinterConfiguration, VoidVisitor<Void>> visitorFactory;

    public PrettyPrinter() {
        this(new PrettyPrinterConfiguration());
    }

    public PrettyPrinter(PrettyPrinterConfiguration configuration) {
        this(configuration, PrettyPrintVisitor::new);
    }

    public PrettyPrinter(PrettyPrinterConfiguration configuration, Function<PrettyPrinterConfiguration, VoidVisitor<Void>> visitorFactory) {
        this.configuration = configuration;
        this.visitorFactory = visitorFactory;
    }

    /*
     * Returns the PrettyPrinter configuration
     */
    public PrinterConfiguration getConfiguration() {
        return configuration;
    }

    /*
     * set or update the PrettyPrinter configuration
     */
    public Printer setConfiguration(PrinterConfiguration configuration) {
        if (!(configuration instanceof PrettyPrinterConfiguration))
            throw new IllegalArgumentException("PrettyPrinter must be configured with a PrettyPrinterConfiguration class");
        this.configuration = configuration;
        return this;
    }

    @Override
    public String print(Node node) {
        final VoidVisitor<Void> visitor = visitorFactory.apply((PrettyPrinterConfiguration)configuration);
        node.accept(visitor, null);
        return visitor.toString();
    }

}
