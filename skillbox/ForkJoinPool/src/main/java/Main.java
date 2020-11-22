import java.io.FileOutputStream;
import java.util.Collections;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static final String URL = "https://lenta.ru/";

    public static void main(String[] args) throws Exception {

        SiteMap sitemapRoot = new SiteMap(URL);
        new ForkJoinPool().invoke(new SiteMapRecursiveAction(sitemapRoot));

        FileOutputStream stream = new FileOutputStream("data/siteMap.txt");
        String result = createSiteMap(sitemapRoot, 0); // 0 - сайт начинается с корня без отступов
        stream.write(result.getBytes());
        stream.flush();
        stream.close();
    }

    public static String createSiteMap(SiteMap node, int depth) {
        String tabs = String.join("", Collections.nCopies(depth, "\t")); // без дополнительных знаков, только табуляция
        StringBuilder result = new StringBuilder(tabs + node.getUrl());
        node.getChildren().forEach(child -> result.append("\n").append(createSiteMap(child, depth + 1)));
        return result.toString();
    }
}