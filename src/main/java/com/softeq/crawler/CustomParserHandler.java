package com.softeq.crawler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomParserHandler extends DefaultHandler {

    private Deque<String> domElementStack = new LinkedList<>();
    private Set<String> links = new HashSet<>();
    private List<Pattern> rules = new ArrayList<>();
    private Map<String, Integer> result;



    public CustomParserHandler(Pattern rule) {
        this.rules.add(rule);
        this.result = new HashMap<>(1, 1.1F);
        this.result.put(rule.pattern(), 0);
    }

    public CustomParserHandler(String ... regexp) {
        this.result = new HashMap<>(regexp.length, 1.1F);
        for (String rule : regexp) {
            this.rules.add(Pattern.compile(rule));
            this.result.put(rule, 0);
        }
    }



    public Set<String> getLinks() {
        return new HashSet<>(links);
    }
    public Map<String, Integer> getResult() {
        return new HashMap<>(result);
    }
    public List<Pattern> getRules() {
        return new ArrayList<>(rules);
    }



    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        domElementStack.push(qName);
        String value;
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URI not fund!");
        }
        if (qName.equals("a")) {
            String link = attributes != null ? attributes.getValue("href") : null;
            if (link != null && !link.startsWith("#")) {
                value = link.startsWith("http") ? link : String.format("%s://%s%s", url.getProtocol(), url.getHost(), link);
                links.add(value);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (domElementStack.peekFirst().equals(qName)) {
            domElementStack.pop();
        } else {
            throw new RuntimeException("Error! Html structure is bed!");
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String content = new String(ch, start, length).trim();
        for (Pattern rule : rules) {
            Matcher matcher = rule.matcher(content);
            while (matcher.find()) {
                int value = result.get(rule.pattern());
                result.put(rule.pattern(), ++value);
            }
        }
    }

    public void clear() {
        links.clear();
        for (Map.Entry<String, Integer> item : result.entrySet()) {
            item.setValue(0);
        }
    }
}
