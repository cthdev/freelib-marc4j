
package org.marc4j.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.marc4j.ErrorHandler;
import org.marc4j.MarcException;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

public class PermissiveReaderExample {

    private PermissiveReaderExample() {
    }

    /**
     * This test program demonstrates the use of the MarcPermissiveStreamReader to read Marc records, with the
     * permissive setting turned on. It also demonstrates the capability of printing out the error messages that are
     * generated when the MarcPermissiveStreamReader encounters records with structural error or encoding errors.
     * <p>
     * When run in verbose mode, (by passing -v as the first parameter) the program will display the entire record
     * highlighting the lines in the record that have errors that the permissive reader was able to detect and make an
     * attempt at correcting. Following that the program will list all of the errors that it found in the record.
     * </p>
     * <p>
     * When run in verbose mode as described above, the program is useful for validating records.
     * </p>
     * <p>
     * Shown below is the output generated when the program is run on the file error.mrc found in the resources
     * sub-directory in the samples directory:
     * </p>
     * <pre>
     *  Fatal Exception: error parsing data field for tag: 250 with data:    a1st ed.
     *  Typo         : Record terminator character not found at end of record length --- [ n/a : n/a ]
     *  Typo         : Record terminator appears after stated record length, reading extra bytes --- [ n/a : n/a ]
     *  Minor Error  : Field length found in record different from length stated in the directory. --- [ n/a : n/a ]
     *     LEADER 00715cam a2200205 a 4500
     *     001 12883376
     *     005 20030616111422.0
     *     008 020805s2002    nyu    j      000 1 eng
     *     020   $a0786808772
     *     020   $a0786816155 (pbk.)
     *     040   $aDLC$cDLC$dDLC
     *     100 1 $aChabon, Michael.
     *     245 10$aSummerland /$cMichael Chabon.
     *     250   $a1st ed.
     *     260   $aNew York :$bMiramax Books/Hyperion Books for Children,$cc2002.
     *     300   $a500 p. ;$c22 cm.
     *     520   $aEthan Feld finds himself recruited by a 100-year-old scout to help a band of fairies.
     *     650  1$aFantasy.
     *     650  1$aBaseball$vFiction.
     *     650  1$aMagic$vFiction.
     * </pre>
     */
    public static void main(final String[] aArgsArray) {
        final PrintStream out = System.out;
        final String[] args;

        boolean verbose = Boolean.parseBoolean(System.getProperty("marc.verbose"));
        boolean veryverbose = Boolean.parseBoolean(System.getProperty("marc.verbose"));

        if (aArgsArray != null && aArgsArray.length > 0) {
            verbose = true;
            final String newArgs[] = new String[aArgsArray.length - 1];
            System.arraycopy(aArgsArray, 1, newArgs, 0, aArgsArray.length - 1);
            args = newArgs;

            if (args[0].equals("-vv")) {
                veryverbose = true;
            } else if (args[0].equals("-v")) {
                verbose = true;
            }
        } else {
            args = aArgsArray;
        }

        final File file = new File("src/test/resources/summerland.mrc");
        final ErrorHandler errorHandler = new ErrorHandler();
        final boolean to_utf_8 = true;
        final InputStream inNorm;
        final InputStream inPerm;

        MarcReader readerNormal = null;
        MarcReader readerPermissive = null;
        OutputStream patchedRecStream = null;
        MarcWriter patchedRecs = null;

        try {
            inNorm = new FileInputStream(file);
            readerNormal = new MarcPermissiveStreamReader(inNorm, false, to_utf_8);
            inPerm = new FileInputStream(file);
            readerPermissive = new MarcPermissiveStreamReader(inPerm, errorHandler, to_utf_8, "BESTGUESS");
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (args != null && args.length > 1) {
            try {
                patchedRecStream = new FileOutputStream(new File(args[1]));
                patchedRecs = new MarcStreamWriter(patchedRecStream);
            } catch (final FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        while (readerNormal.hasNext() && readerPermissive.hasNext()) {
            final Record recNorm;
            final Record recPerm = readerPermissive.next();
            final String strPerm = recPerm.toString();

            try {
                recNorm = readerNormal.next();
            } catch (final MarcException me) {
                if (verbose) {
                    out.println("Fatal Exception: " + me.getMessage());
                    dumpErrors(out, errorHandler);
                    showDiffs(out, null, strPerm);
                    out.println("----------------------------------------------------------------------------------");
                }

                continue;
            }

            final String strNorm = recNorm.toString();

            if (!strNorm.equals(strPerm)) {
                if (verbose) {
                    dumpErrors(out, errorHandler);
                    showDiffs(out, strNorm, strPerm);
                    out.println("----------------------------------------------------------------------------------");

                }

                if (patchedRecs != null) {
                    patchedRecs.write(recPerm);
                }
            } else if (errorHandler.hasErrors()) {
                if (verbose) {
                    out.println("Results identical, but errors reported");
                    dumpErrors(out, errorHandler);
                    showDiffs(out, strNorm, strPerm);
                    out.println("----------------------------------------------------------------------------------");
                }

                if (patchedRecs != null) {
                    patchedRecs.write(recPerm);
                }
            } else if (veryverbose) {
                showDiffs(out, strNorm, strPerm);
            }
        }
    }

    /**
     * Normalizes strings to be able to show diffs.
     *
     * @param out The output diff stream
     * @param strNorm The normalized string
     * @param strPerm The perm string
     */
    public static void showDiffs(final PrintStream out, final String strNorm, final String strPerm) {
        if (strNorm != null) {
            final String normLines[] = strNorm.split("\n");
            final String permLines[] = strPerm.split("\n");

            if (normLines.length == permLines.length) {
                for (int i = 0; i < normLines.length; i++) {
                    if (normLines[i].equals(permLines[i])) {
                        out.println("   " + normLines[i]);
                    } else {
                        out.println(" < " + normLines[i]);
                        out.println(" > " + permLines[i]);
                    }
                }
            }
        } else {
            final String permLines[] = strPerm.split("\n");

            for (int i = 0; i < permLines.length; i++) {
                out.println("   " + permLines[i]);
            }
        }
    }

    /**
     * Dumps errors from the supplied error handler to the supplied print stream.
     *
     * @param out The stream to which to write errors
     * @param errorHandler The error handler with the errors to be written to the out stream
     */
    @SuppressWarnings("unchecked")
    public static void dumpErrors(final PrintStream out, final ErrorHandler errorHandler) {
        final List<Object> errors = errorHandler.getErrors();

        if (errors != null) {
            final Iterator<Object> iter = errors.iterator();

            while (iter.hasNext()) {
                final Object error = iter.next();
                out.println(error.toString());
            }
        }
    }
}
