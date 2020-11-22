import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class SiteMapRecursiveAction extends RecursiveAction
{
    private SiteMap siteMap;

    public SiteMapRecursiveAction (SiteMap siteMap) {
        this.siteMap = siteMap;
    }

    @Override
    protected void compute() {
        try {
            sleep(100);
            Document document = Jsoup.connect(siteMap.getUrl())
                    .userAgent("Chrome/86.0.4240.111")
                    .referrer("http://www.google.com")
                    .timeout(1500)
                    .get();
            Elements elements = document.select("body").select("a");
            elements.stream().map(a -> a.absUrl("href")).filter(this::isCorrectUrl).map(SiteMapRecursiveAction::stripParams).forEach(childrenUrl -> {
                System.out.println(childrenUrl); // вывод на экран самого процесса
                siteMap.addChildren(new SiteMap(childrenUrl));
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        siteMap.getChildren().stream().map(SiteMapRecursiveAction::new).forEachOrdered(SiteMapRecursiveAction::compute);
    }

    private static String stripParams(String url) {
        return url.replaceAll("\\?.+","");
    }

    private boolean isCorrectUrl(String url) {
        Pattern root = Pattern.compile("^" + siteMap.getUrl());
        Pattern notFile = Pattern.compile("([^\\s]+(\\.(?i)(jpg|bmp|gif|png|pdf))$)");
        Pattern notAnchor = Pattern.compile("#([\\w\\-]+)?$");
        return root.matcher(url).lookingAt()
                && !notFile.matcher(url).find()
                && !notAnchor.matcher(url).find();
    }
}