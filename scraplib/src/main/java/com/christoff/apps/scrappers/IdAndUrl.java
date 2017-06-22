package com.christoff.apps.scrappers;

/**
 * We will extract ob jects having an Id and an Url
 * Yes the id is in url but urls are not always clean rest urls
 * So the scrapper knows how to extract the id from the url and returns it
 * for future usage
 * Created by christophe on 14.06.17.
 */
public class IdAndUrl {

    private int id;
    private String url;

    public IdAndUrl(int id, String url) {
        this.id = id;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "IdAndUrl{" +
                "id=" + id +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdAndUrl idAndUrl = (IdAndUrl) o;

        if (id != idAndUrl.id) return false;
        return url.equals(idAndUrl.url);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + url.hashCode();
        return result;
    }
}
