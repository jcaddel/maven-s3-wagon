package org.springframework.aws.maven;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 *
 */
public class Foo {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        try {
            PrintStream oldOut = System.out;

            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            PrintStream newOut = new PrintStream(bytesOut);
            System.setOut(newOut);
            System.out.print("foo");

            String s = bytesOut.toString() + " bar";
            System.setOut(oldOut);
            System.out.println(s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
