package org.kuali.maven.wagon;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Very simple, throw away class for parsing the mime-type mappings out of the web.xml file that comes with Tomcat and
 * then converting the mime-types in the format needed by jets3t.
 */
public class MimeTypeParser {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        try {
            String filename = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/conf/web.xml";
            String contents = IOUtils.toString(new FileInputStream(filename));
            String tomcatMimetypes = StringUtils.substringBetween(contents,
                    "<!-- deployment descriptor.                                               -->",
                    "<!-- ==================== Default Welcome File List ===================== -->");
            String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(tomcatMimetypes, "<mime-mapping>");
            System.out.println(tokens.length);
            List<Mapping> mappings = new ArrayList<Mapping>();
            int count = 0;
            for (String token : tokens) {
                count++;
                if (count == 1) {
                    continue;
                }
                Mapping m = new Mapping();
                String extension = StringUtils.substringBetween(token, "<extension>", "</extension>");
                String type = StringUtils.substringBetween(token, "<mime-type>", "</mime-type>");
                m.setExtension(extension);
                m.setType(type);
                // System.out.println("'" + token + "'" + " " + type + " " + extension);
                mappings.add(m);
            }
            Map<String, String> mimeTypes = new TreeMap<String, String>();
            for (Mapping mapping : mappings) {
                String extension = mimeTypes.get(mapping.getType());
                if (extension == null) {
                    extension = mapping.getExtension();
                } else {
                    extension += " " + mapping.getExtension();
                }
                mimeTypes.put(mapping.getType(), extension);
            }
            System.out.println(mappings.size() + " " + mimeTypes.size());
            System.out.println(getString(mimeTypes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String getString(final Map<String, String> mimeTypes) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> pair : mimeTypes.entrySet()) {
            sb.append(pair.getKey() + "\t" + pair.getValue() + "\n");
        }
        return sb.toString();
    }
}
