package com.softeq.crawler.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
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



    public Set<String> getLinks() {
        return new HashSet<>(links);
    }
    public Map<String, Integer> getResult() {
        return new HashMap<>(result);
    }
    public List<Pattern> getRules() {
        return new ArrayList<>(rules);
    }



    public void parse(String urlStr) throws IOException {
        for (Map.Entry<String, Integer> someCase : this.result.entrySet()) {
            someCase.setValue(0);
        }
        this.links.clear();

        Document doc = Jsoup.connect(urlStr).get();
        URL url = new URL(urlStr);

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
        Integer temp;
        String key;
        for (Pattern pt : rules) {
            Matcher matcher = pt.matcher(html);
            key = pt.pattern();
            while (matcher.find()) {
                temp = this.result.get(key);
                this.result.put(key, ++temp);
            }
        }

        Elements links = doc.getElementsByTag("a");
        String urlRes;
        for (Element link : links) {
            if ((urlRes = link.attr("href")) == null || urlRes.startsWith("#")) {
                continue;
            }
            String valUrlRes = urlRes.startsWith("http") ?
                    urlRes :
                    String.format("%s://%s%s", url.getProtocol(), url.getHost(), urlRes);
            this.links.add(valUrlRes);
        }
    }
}
