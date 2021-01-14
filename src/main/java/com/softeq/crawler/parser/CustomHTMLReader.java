package com.softeq.crawler.parser;

import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomHTMLReader implements XMLReader {

    private boolean documentTypeIsChecked = false;
    private ContentHandler htmlHandler;
    private String uri;
    private String localName;
    private String qName;
    private Attributes attributes;

    /**
     * For check <...> :
    * */
    private List<String> specialTags = new ArrayList<>() {
        {
            this.add("input");
            this.add("!DOCTYPE html");
        }
    };



    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {

    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {

    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.htmlHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return this.htmlHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {

    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public void parse(InputSource input) {
        this.uri = input.getSystemId();
        if (this.htmlHandler == null || this.uri == null) {
            throw new RuntimeException();
        }
        Reader reader = input.getCharacterStream();

        parseRun(reader);
        clearFieldValue();
    }

    @Override
    public void parse(String systemId) throws IOException {
        this.uri = systemId;
        if (this.htmlHandler == null || this.uri == null) {
            throw new RuntimeException();
        }
        URL url = new URL(this.uri);
        InputStream inStr = url.openStream();
        InputStreamReader reader = new InputStreamReader(inStr);

        parseRun(reader);
        clearFieldValue();
    }

    private void parseRun(Reader reader) {
        boolean processNode = false;
        boolean inOpeningTag = false;
        boolean inClosingTag = false;
        boolean isAttributeValue = false;
        boolean isHtmlComment = false;

        int currentSymbol;
        List<Character> tagData = new LinkedList<>();
        List<Character> contentData = new LinkedList<>();

        try (BufferedReader inBf = new BufferedReader(reader, 4)) {
            while ((currentSymbol = inBf.read()) != -1) {
                switch (currentSymbol) {
                    case '<':
                        if (!isHtmlComment && !isAttributeValue) {
                            inBf.mark(4);
                            char[] buf = new char[3];
                            inBf.read(buf);
                            if (buf[0] == '/') {
                                inOpeningTag = false;
                                inClosingTag = true;
                                processNode = true;
                                htmlHandler.characters(charListToArrayHandler(contentData), 0, contentData.size());
                                contentData.clear();
                                tagData.clear();
                                inBf.reset();
                            } else if (new String(buf, 0, buf.length).equals("!--")) {
                                isHtmlComment = true;
                            } else {
                                inOpeningTag = true;
                                processNode = true;
                                contentData.clear();
                                inBf.reset();
                            }
                        } else if (isAttributeValue) {
                            tagData.add((char) currentSymbol);
                        } else if (isHtmlComment) {
                            // ...
                        }
                        break;
                    case '>':
                        if (processNode && inOpeningTag && !isAttributeValue && !isHtmlComment) {
                            tagDataHandler(tagData);
                            htmlHandler.startElement(uri, localName, qName, attributes);
                            tagData.clear();
                            if (specialTags.contains(qName)) {
                                htmlHandler.endElement(uri, localName, qName); //*
                            }
                            inOpeningTag = false;
                        } else if (processNode && inClosingTag && !isAttributeValue && !isHtmlComment) {
                            tagDataHandler(tagData);
                            htmlHandler.endElement(uri, localName, qName);
                            tagData.clear();
                            processNode = false;
                            inClosingTag = false;
                        } else if (isAttributeValue) {
                            tagData.add((char) currentSymbol);
                        } else if (isHtmlComment) {
                            //...
                        }
                        break;
                    case '/':
                        if (processNode && inOpeningTag && !isAttributeValue && !isHtmlComment) {
                            inBf.mark(2);
                            char[] buf = new char[1];
                            inBf.read(buf);
                            if (buf[0] == '>') {
                                inOpeningTag = false;
                                inClosingTag = false;
                                processNode = false;
                                tagDataHandler(tagData);
                                htmlHandler.startElement(uri, localName, qName, attributes);
                                htmlHandler.endElement(uri, localName, qName);
                                tagData.clear();
                            } else {
                                throw new RuntimeException("/ in attribute key!");
                            }
                        } else if (processNode && inClosingTag && !isAttributeValue && !isHtmlComment) {
                            //...
                        } else if (isAttributeValue) {
                            tagData.add((char) currentSymbol);
                        } else if (isHtmlComment) {
                            //...
                        }
                        break;
                    case '\"':
                        if (processNode && !isAttributeValue && !isHtmlComment && (inOpeningTag || inClosingTag)) {
                            tagData.add((char) currentSymbol);
                            isAttributeValue = true;
                        } else if (processNode && !isAttributeValue && !isHtmlComment && !(inOpeningTag || inClosingTag)) {
                            contentData.add((char) currentSymbol);
                        } else if (isAttributeValue) {
                            tagData.add((char) currentSymbol);
                            isAttributeValue = false;
                        } else if (isHtmlComment) {

                        }
                        break;
                    case '-':
                        if (isHtmlComment) {
                            inBf.mark(3);
                            char[] buf = new char[2];
                            inBf.read(buf);
                            if (new String(buf, 0, buf.length).equals("->")) {
                                isHtmlComment = false;
                            } else {

                            }
                        } else if (processNode && (inOpeningTag || inClosingTag)) {
                            tagData.add((char) currentSymbol);
                        } else if (processNode && !(inOpeningTag || inClosingTag)) {
                            contentData.add((char) currentSymbol);
                        }
                        break;
                    default:
                        if (processNode && (inOpeningTag || inClosingTag) && !isHtmlComment) {
                            tagData.add((char) currentSymbol);
                        } else if (processNode && !(inOpeningTag || inClosingTag) && !isHtmlComment) {
                            contentData.add((char) currentSymbol);
                        } else if (isHtmlComment) {

                        }
                        break;
                }
            }
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private void tagDataHandler(List<Character> tagData) {
        Map<String, String> data = processSomeTag(tagData);

        String tagName = data.get("name");
        if (!documentTypeIsChecked) {
            if (tagName.equals("!DOCTYPE html")) {
                documentTypeIsChecked = true;
                this.localName = tagName;
                this.qName = tagName;
                this.attributes = null;
                return;
            } else {
                throw new RuntimeException("This file is not HTML document!");
            }
        }

        String tagAttr;
        this.localName = tagName;
        this.qName = tagName;
        this.attributes =
                (tagAttr = data.get("attr")) == null ?
                null : generateAttributes(tagAttr, uri);
    }



    /**
     * Util methods:...
     */
    private char[] charListToArrayHandler(List<Character> textData) {
        char[] content = new char[textData.size()];
        char item;
        for (int i = 0; i < textData.size(); i++) {
            content[i] = (item = textData.get(i)) == '\n' ? ' ' : item;
        }
        return content;
    }

    private Map<String, String> processSomeTag(List<Character> tagData) {
        Map<String, String> data = new HashMap<>();

        char[] charSequence = charListToArrayHandler(tagData);
        String str = new String(charSequence, 0, charSequence.length).trim();

        int index;
        if (str.equals("!DOCTYPE html") || (index = str.indexOf(" ")) == - 1) {
            data.put("name", str);
        } else {
            data.put("name", str.substring(0, index).trim());
            data.put("attr", str.substring(index).trim());

        }
        return data;
    }

    private Attributes generateAttributes(String attr, String uri) {
        AttributesImpl attributes = new AttributesImpl();

        int firstCharCurrentKeys = 0;
        String currentDirtyValue = null;
        String value = null;
        String keys = null;
        String key = null;

        Pattern pt = Pattern.compile("=\".*?\"");
        Matcher mch = pt.matcher(attr);

        while (mch.find()) {
            currentDirtyValue = mch.group();
            value = currentDirtyValue.length() == 3 ?
                    null :
                    currentDirtyValue.substring(2, currentDirtyValue.length() - 1);
            keys = attr.substring(firstCharCurrentKeys, mch.start()).trim();
            firstCharCurrentKeys = mch.end();

            String[] s = keys.split(" +");
            if (s.length == 1) {
                key = keys;
                attributes.addAttribute(uri, key, key, "text", value);
            } else {
                for (int i = 0; i < s.length - 1; i++) {
                    key = s[i];
                    attributes.addAttribute(uri, key, key, "text", null);
                }
                key = s[s.length - 1];
                attributes.addAttribute(uri, key, key, "text", value);
            }
        }

        // ... Depreciated :...
        if (firstCharCurrentKeys == 0) {
            attributes.addAttribute(uri, attr, attr, "text", null);
        } else if (firstCharCurrentKeys != attr.length()) {
            key = attr.substring(firstCharCurrentKeys).trim();
            attributes.addAttribute(uri, key, key, "text", null);
        }
        return attributes;
    }

    private void clearFieldValue() {
        documentTypeIsChecked = false;
        uri = null;
        localName = null;
        qName = null;
        attributes = null;
    }
}
