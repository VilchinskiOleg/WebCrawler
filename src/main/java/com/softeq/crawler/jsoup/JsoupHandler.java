package com.softeq.crawler.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class JsoupHandler {
    private Set<String> links = new HashSet<>();
    private List<Pattern> rules = new ArrayList<>();
    private Map<String, Integer> result;

    public JsoupHandler(Pattern rule) {
        this.rules.add(rule);
        this.result = new HashMap<>(1, 1.1F);
        this.result.put(rule.pattern(), 0);
    }

    public JsoupHandler(String ... regexp) {
        this.result = new HashMap<>(regexp.length, 1.1F);
        for (String rule : regexp) {
            this.rules.add(Pattern.compile(rule));
            this.result.put(rule, 0);
        }
    }

    public JsoupHandler() { }

    public Set<String> getLinks() {
        return new HashSet<>(links);
    }
    public Map<String, Integer> getResult() {
        return new HashMap<>(result);
    }
    public List<Pattern> getRules() {
        return new ArrayList<>(rules);
    }



    public void parse(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

//        List<Node> allNodes = doc.childNodes();
//        for (Node item : allNodes) {
//            List<Node> nodes = item.childNodes();
//        }

//        Element titleEl = doc.getElementsByTag("title").first();
//        String name = titleEl.nodeName();
//        String content = titleEl.data(); - some meta information
//        String content = titleEl.text(); - text-content
//        Attributes attributes = titleEl.attributes();
//        for (Attribute attr : attributes) {
//            System.out.printf("key : %s / value : %s", attr.getKey(), attr.getValue());
//        }

//        Element bodyEl = doc.select("body").first();
//        String text = bodyEl.text();
//        for (Element element : bodyEl.children()) {
//            String name = element.nodeName();
//            String content = element.text();
//            String aClass = element.attr("class");
//            System.out.println();
//        }

        String html = doc.getElementsByTag("html").text();
        Element titleEl = doc.getElementsByTag("title").first();
        Element bodyEl = doc.select("body").first();

        Elements links = doc.getElementsByTag("a");
    }
}
