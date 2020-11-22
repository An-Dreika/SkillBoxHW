import java.util.concurrent.CopyOnWriteArrayList;

public class SiteMap {
    private volatile SiteMap parent;
    private volatile int depth;
    private String url;
    private volatile CopyOnWriteArrayList<SiteMap> children;

    public SiteMap(String url) {
        depth = 0;
        this.url = url;
        parent = null;
        children = new CopyOnWriteArrayList<>();
    }

    private int calculateDepth() {
        int result = 0;
        if (parent == null) {
            return result;
        }
        result = 1 + parent.calculateDepth();
        return result;
    }

    public synchronized void addChildren(SiteMap siteMap) {
        SiteMap root = getRootElement();
        if(!root.contains(siteMap.getUrl())) {
            siteMap.setParent(this);
            children.add(siteMap);
        }
    }

    public CopyOnWriteArrayList<SiteMap> getChildren() {
        return children;
    }

    public String getUrl() {
        return url;
    }

    private boolean contains(String url) {
        if (this.url.equals(url)) {
            return true;
        }
        for (SiteMap child : children) {
            if(child.contains(url))
                return true;
        }
        return false;
    }

    private void setParent(SiteMap sitemap) {
        synchronized (this) {
            this.parent = sitemap;
            this.depth = calculateDepth();
        }
    }

    public SiteMap getRootElement() {
        return parent == null ? this : parent.getRootElement();
    }
}