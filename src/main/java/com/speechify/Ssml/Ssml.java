package com.speechify.Ssml;

import java.util.List;

/**
 * SSML (Speech Synthesis Markup Language) is a subset of XML specifically
 * designed for controlling synthesis. You can see examples of how the SSML
 * should be parsed in com.speechify.SSMLTest in `src/test/java/SSMLTest.java`.
 *
 * You may:
 *  - Read online guides to supplement information given in com.speechify.SSMLTest to understand SSML syntax.
 * You must not:
 *  - Use XML parsing libraries or the DocumentBuilderFactory. The task should be solved only using string manipulation.
 *  - Read guides about how to code an XML or SSML parser.
 */
public class Ssml {

    // Parses SSML to a SSMLNode, throwing on invalid SSML
    public static SSMLNode parseSSML(String ssml) {
        ssml = ssml.trim();
        if(!ssml.startsWith("<")){
            return new SSMLText(unescapeXMLChars(ssml)); //It's an pure text node.
        }

        //Exract the regex for opening tag.
        Pattern tagPattern = Pattern.compile("^<([a-zA-Z0-9]+)>");
        Matcher matcher = tagPattern.matcher(ssml);

        if(!matcher.find()){
            throw new IllegalArugmentException("Invalid ssml : "+ssml);
        }
        String tagName = matcher.group();
        String closingTag = "</" + tagName + ">";

        if(!ssml.endsWith(closingTag)){
            throw new IllegalArugmentException("Unmatched tag : "+tagName);
        }
        String inner = ssml.substring(matcher.end(), ssml.length() - closingTag.length());

        List<SSMLNode> child = new ArrayList<>();
        int i = 0;
        while(i < inner.length()){
            if(inner.charAt(i) == '<'){ //Find the matching closing tag
                int depth = 0, j = i;
                for(; j<inner.length(); j++){
                    if(inner.charAt(j) == '<' && inner.startsWith("</", j)){
                        if(depth == 0){break}; 
                        depth--;
                    }else if(inner.charAt(j) == '<'){
                        depth++;                    
                }
            }
                String childTag = inner.substring(i, j + inner.substring(j).indexOf(">")+1);
                child.add(parseSSML(childTag));
                i = j + inner.substring(j).indexOf(">")+1;
        }else{
                int j = i;
                while(j < inner.length() && inner.charAt(j) != '<'){
                    j++;
                }
                child.add(new SSMLText(unescapeXMLChars(inner.substring(i, j))));
                i = j;
        }
         return new SSMLElement(tagName, child);   
    }

    // Recursively converts SSML node to string and unescapes XML chars
    public static String ssmlNodeToText(SSMLNode node) {
        if(node instanceOf SSMLText text){
            return text.valueOf();
        }else if(node instanceOf SSMLElement element){
            StringBuilder sb = new StringBuilder();
            for(SSMLNode child : element.children()){
                sb.append(ssmlNodeToText(child));
            }
            return sb.toString();
        }
        return "";
    }

    public sealed interface SSMLNode permits SSMLElement, SSMLText {}

    public record SSMLElement(String name, List<SSMLAttribute> attributes, List<SSMLNode> children) implements SSMLNode {}

    public record SSMLAttribute(String name, String value) {}

    public record SSMLText(String text) implements SSMLNode {}
}
