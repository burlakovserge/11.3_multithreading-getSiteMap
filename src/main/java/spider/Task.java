package spider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Task implements Runnable {

    private Spider spider;
    private String url;
    private String baseUrl;
    private int depth;
    private long startTime;
    private final Logger LOG = LogManager.getLogger("task");

    public Task(Spider spider, String url, int depth) {
        this.spider = spider;
        this.url = url;
        this.depth = depth;
        startTime = Instant.now().toEpochMilli();
        baseUrl = spider.getBaseUrlHostName();
    }

    @Override
    public void run() {
        LOG.debug("Started processing URL: {}, @depth={}.", url, depth);
// ? задержка потока на время, но зачем? В monitor же задержку 1 сек написали
        randomWait(500);

        //формируется список ссылок со страницы, которая передана в конструктор
        Set<String> urls = getUrls(url);
        urls.removeAll(spider.getUrlsDone());
        urls.removeAll(spider.getUrlsPending());

        urls.stream()
                .filter(this::urlIsNotProcessedYet)
                .forEach(url -> spider.addUrl(url, depth + 1));

        spider.markUrlDone(url, depth);

        long elapsed = Instant.now().toEpochMilli() - startTime;
        LOG.debug("Finished processing URL: {} in {} ms.", url, elapsed);
    }

    //метод для поиска всех ссылок на странице переданной как параметр метода
    private Set<String> getUrls(String url) {
        Set<String> urlsFound;
        Document document = null;

        try {
            document = Jsoup.connect(url)
                    .followRedirects(true)
                    .timeout(60 * 1000)
                    .maxBodySize(0)
                    .get();

        } catch (Exception e) {
            LOG.debug("Skipped URL '{}'. Reason: {}: {}.", url, e.getClass().getSimpleName(), e.getMessage());
            spider.removeInvalidUrl(url);
        }

        if (document != null ) {
            urlsFound = document.select("a[href]").stream()
                    .map(link -> link.attr("abs:href"))
                    .filter(link -> !link.isEmpty())
                    .filter(this::sameAsRootUrl)
                    .filter(link -> !link.equals(url))
                    .map(link -> cleanUrl(link))
                    .collect(Collectors.toSet());

        } else {
            urlsFound = new HashSet<>();
        }

        LOG.debug("Got {} new urls.", urlsFound.size());
        return urlsFound;
    }

    private void randomWait(int delay) {
        try {
            int d = (int) (delay * Math.random());
            LOG.debug("Waiting {} ms...", d);
            Thread.sleep(d);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //очистка ссылки от символа # или ?
    private String cleanUrl(String url) {
        // URI = scheme:[//authority]path[?query][#fragment]
        // Clear out ?query and #fragment parts from the url.
        String cleanedUrl = url.replaceAll("[#?].*", "");

        LOG.debug("Url '{}' cleaned to '{}'.", url, cleanedUrl);

        return cleanedUrl;
    }

    //метод для проверки ссылки, что она начинается так же как главная страница, а не является ссылкой на другой ресурс
    private boolean sameAsRootUrl(String url) {
        final String BASE_URL = baseUrl + "/";
        return url.startsWith("http://" + BASE_URL) || url.startsWith("https://"  + BASE_URL);
    }

// ? вопрос по строке 115
    private boolean urlIsNotProcessedYet(String url) {
        final String URL_PATTERN = "https?://(.*)";
// ? замена на $1 ? Что будет после замены тогда?
        final String URL_SUBSTITUTION = "$1";
        final String URL = url.replaceAll(URL_PATTERN, URL_SUBSTITUTION);
        final String HTTP_URL = "http://" + URL;
        final String HTTPS_URL = "https://" + URL;

        if ((spider.getUrlsDone().contains(url) ||
                spider.getUrlsPending().contains(url) ||
                spider.getUrlsDone().contains(HTTP_URL) ||
                spider.getUrlsDone().contains(HTTPS_URL) ||
                spider.getUrlsPending().contains(HTTP_URL) ||
                spider.getUrlsPending().contains(HTTPS_URL)
        )) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Task{" +
                "url='" + url + '\'' +
                '}';
    }
}
