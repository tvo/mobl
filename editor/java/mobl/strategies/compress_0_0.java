package mobl.strategies;

import java.io.StringReader;
import java.io.StringWriter;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Example Java strategy implementation.
 *
 * This strategy can be used by editor services and can be called in Stratego
 * modules by declaring it as an external strategy as follows:
 *
 * <code>
 *  external java-strategy(|)
 * </code>
 *
 * @see InteropRegisterer This class registers java_strategy_0_0 for use.
 */
public class compress_0_0 extends Strategy {

    public static compress_0_0 instance = new compress_0_0();

    @Override
    public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        String code = getStringFromTerm(current.getSubterm(1));
        ITermFactory factory = context.getFactory();
        StringReader in = new StringReader(code);
        StringWriter out = new StringWriter();
        try {
            JavaScriptCompressor compressor = new JavaScriptCompressor(in,
                    new ErrorReporter() {

                        public void warning(String message, String sourceName,
                                int line, String lineSource, int lineOffset) {
                            if (line < 0) {
                                System.err.println("\n[WARNING] " + message);
                            } else {
                                System.err.println("\n[WARNING] " + line + ':'
                                        + lineOffset + ':' + message);
                            }
                        }

                        public void error(String message, String sourceName,
                                int line, String lineSource, int lineOffset) {
                            if (line < 0) {
                                System.err.println("\n[ERROR] " + message);
                            } else {
                                System.err.println("\n[ERROR] " + line + ':'
                                        + lineOffset + ':' + message);
                            }
                        }

                        public EvaluatorException runtimeError(String message,
                                String sourceName, int line, String lineSource,
                                int lineOffset) {
                            error(message, sourceName, line, lineSource,
                                    lineOffset);
                            return new EvaluatorException(message);
                        }
                    });

            // Close the input stream first, and then open the output stream,
            // in case the output file should override the input file.
            in.close();
            in = null;

            compressor.compress(out, -1, true, false,
                    true, false);
            return factory.makeString(out.toString());
        } catch (Exception e) {
            context.getIOAgent().printError(e.toString());
            context.getIOAgent().printError(e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    public static String getStringFromTerm(IStrategoTerm current) {
        Class<?> cls = current.getClass();
        try {
            return (String) cls.getMethod("stringValue").invoke(current);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}