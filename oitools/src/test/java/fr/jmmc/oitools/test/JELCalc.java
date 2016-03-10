package fr.jmmc.oitools.test;

/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
/**
 *
 * @author grosje
 */
import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import java.util.Arrays;

public class JELCalc {

    public static void main(String[] args) throws CompilationException {
        String expr = "0.0 + x + y + z";

        final int len = 1000000;
        double[] tab1 = new double[len];
        double[] tab2 = new double[len];
        double[] tabRes = new double[len];

        for (int i = 0; i < len; i++) {
            tab1[i] = i;
            tab2[i] = 5 * i;
            tabRes[i] = Double.NaN;
        }

        Class[] staticLib = new Class[1];
        try {
            staticLib[0] = Class.forName("java.lang.Math");
        } catch (ClassNotFoundException e) {
        }

        final VariableProvider dataProvider = new VariableProvider();

        final Class[] dynamicLib = new Class[1];
        final Object[] context = new Object[1];

        dynamicLib[0] = dataProvider.getClass();
        context[0] = dataProvider;

        final Library lib = new Library(staticLib, dynamicLib, null, dataProvider, null);

        // Math.random():
        lib.markStateDependent("random", null);

        // Compile
        CompiledExpression expr_c = null;
        try {
            System.out.println("Compilation ...");
            expr_c = Evaluator.compile(expr, lib);
        } catch (CompilationException ce) {
            System.err.println("COMPILATION ERROR:");
            System.err.println(ce.getMessage());
            System.err.println("Expression:");
            System.err.println(expr);
            int column = ce.getColumn(); // Column, where error was found
            for (int i = 0; i < column - 1; i++) {
                System.err.print(' ');
            }
            System.err.println('^');
        }

        if (expr_c != null) {
            System.out.println("Evaluation ...");

            double tot = 0.0;
            // Evaluate (Can do it now any number of times FAST !!!)
            /*try {
             for (int n = 0; n < 100; n++) {
             final long startTime = System.nanoTime();

             for (int i = 0; i <= 1000000; i++) {
             variables.x = i;      // <- Value of the variable

             result = expr_c.evaluate_double(context);
             //System.out.println("result = " + result);

             tot += result;
             }
             System.out.println("duration = " + 1e-6d * (System.nanoTime() - startTime));
             System.out.println("result: " + result);
             System.out.println("tot: " + tot);
             }

             }*/
            try {
                for (int n = 0; n < 100; n++) {
                    final long startTime = System.nanoTime();

                    for (int i = 0; i < tab1.length; i++) {
                        dataProvider.x = tab1[i];      // <- Value of the variable
                        dataProvider.y = tab2[i];      // <- Value of the variable

                        tabRes[i] = expr_c.evaluate_double(context);
                    }
                    System.out.println("duration = " + 1e-6d * (System.nanoTime() - startTime));
                }
                System.out.println("result: " + Arrays.toString(tabRes));

            } catch (Throwable e) {
                System.err.println("Exception emerged from JEL compiled"
                        + " code (IT'S OK) :");
                System.err.print(e);
            }
        }
    }

    public static class VariableProvider extends DVMap {

        double x;
        double y;

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        @Override
        public String getTypeName(String name) {
            System.out.println("getTypeName: " + name);

            // return null if unknown variable
            return "Double";
        }

        public double getDoubleProperty(String name) {
//            System.out.println("getDoubleProperty: " + name);
            if ("x".equals(name)) {
                return x;
            }
            if ("y".equals(name)) {
                return y;
            }
            return Double.NaN;
        }

// --- performance: use integer mapping --
        @Override
        public Object translate(String name) {
            System.out.println("translate: " + name);
            /*
             if ("x".equals(name)) {
             return Integer.valueOf(0);
             }
             if ("y".equals(name)) {
             return Integer.valueOf(1);
             }
             */
            return super.translate(name);
        }

        public double getDoubleProperty(int index) {
//            System.out.println("getDoubleProperty: " + index);
            switch (index) {
                case 0:
                    return x;
                case 1:
                    return y;
                default:
            }
            return Double.NaN;
        }

    }

}
