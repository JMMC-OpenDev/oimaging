/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author grosje
 */
public abstract class ExpressionEvaluator {

    /** logger */
    private final static Logger _logger = Logger.getLogger(ExpressionEvaluator.class.getName());
    /** flag enabling Jython 2.7 support (false by default as this library is too heavy)  */
    private final static boolean ENABLE_JYTHON_SUPPORT = false;

    /** Factory instance */
    private static ExpressionEvaluator instance = null;

    /**
     * Return the expression evaluator singleton:
     * Jython in priority or JEL otherwise
     * @return expression evaluator singleton
     * @throws IllegalStateException if unable to create any ExpressionEvaluator
     */
    public final static ExpressionEvaluator getInstance() {
        if (instance == null) {
            // create an ExpressionEvaluator instance:
            ExpressionEvaluator eval = null;
            if (ENABLE_JYTHON_SUPPORT) {
                try {
                    _logger.fine("new JythonEval");
                    eval = new JythonEval();
                } catch (NoClassDefFoundError cnfe) {
                    _logger.info("JythonEval can not load Jython library.");
                } catch (Throwable th) {
                    _logger.log(Level.SEVERE, "JythonEval creation failure:", th);
                }
            }
            if (eval == null) {
                try {
                    _logger.fine("new JELEval");
                    eval = new JELEval();
                } catch (Throwable th) {
                    _logger.log(Level.SEVERE, "JELEval creation failure:", th);
                }
            }

            if (eval == null) {
                throw new IllegalStateException("unable to create an ExpressionEvaluator !");
            }

            instance = eval;
        }
        return instance;
    }

    /**
     * Evaluate an expression and return the result in a 2D array of double
     * @param oiData oiData table
     * @param colNameEval name of the derived column to evaluate
     * @param expression expression entered by the user
     * @param testOnly boolean which indicates if the method is used to check the expression (true) or to perform the script
     * @return 2D Array of double
     * @throws RuntimeException
     */
    public abstract double[][] eval(final OIData oiData, final String colNameEval, final String expression,
                                    final boolean testOnly) throws RuntimeException;
}
