package spider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jsoup.Connection.*;

public class Spider {

    private String rootUrl;
    private String hostName;

    private final Logger LOG = LogManager.getLogger("spider");

    private Set<URL> urls;
    private Set<String> urlsDone;
    private Set<String> urlsPending;


    private AtomicBoolean isDone = new AtomicBoolean();

    private ExecutorService executor;
    private Instant startTime;
    private int numThreads;

    //конструктор принимает ссылуна вход (главная страница сайта), вызывает конструктор куда передает ту же ссылку и число (кол-во процессов)
    public Spider(String url) {
        // as we deal with IO-bound operations, we can safely double the number of threads
        this(url, 2 * Runtime.getRuntime().availableProcessors());
    }

    public Spider(String url, int numThreads) {
        //Переменная rootUrl равна входящей ссылке если та заканчивается слешем, иначе слеш добавляется к ссылке
        this.rootUrl = url.endsWith("/") ? url : url + "/";
        this.numThreads = numThreads;

        //Создаем executor с фиксированным числом потоков (потоков может быть от 1 до указанного на входе в метод)
        executor = Executors.newFixedThreadPool(numThreads);

        //Три коллекции инициализируем как синхронизированные.
        urls = Collections.synchronizedSet(new HashSet<>());
        urlsDone = Collections.synchronizedSet(new HashSet<>());
        urlsPending = Collections.synchronizedSet(new HashSet<>());

        //Атомарную переменную  объявляем false
        //"Запоминаем" время старта вызова метода
        isDone.set(false);
        startTime = Instant.now();
    }

    //в этом методе инициализируем
    public void run() {
        try {
            Response response = Jsoup.connect(rootUrl).followRedirects(true).execute();
// ? зачем если ранее rootUrl и hostName в Spider инициализирована?
            setRootUrl(response.url().toString());
            setBaseUrlHostName(response.url().getHost());
            //вызов метода с передачей ему 2 параметров
            addUrl(getRootUrl(), 0);
        } catch (IOException e) {
            LOG.debug("Can't connect to the URL: {}", rootUrl);
        }
    }

    //получив на вход ссылку и глубину вложенности, создается новое задание с параметрами текущего класса, ссылкой и глубиной вложенности
    void addUrl(String url, int depth) {
            urlsPending.add(url);
            LOG.debug("Added URL: {}, @depth={}.", url, depth);

            executor.submit(new Task(this, url, depth));
    }

    void removeInvalidUrl(String url) {
        urlsPending.remove(url);
    }

    //пометить ссылку выполненной
    void markUrlDone(String url, int depth) {
        if (urlsPending.contains(url)) {
            urlsPending.remove(url);
            urlsDone.add(url);
            urls.add(new URL(url, depth));
            LOG.debug("URL '{}' marked as done.", url);
        } else {
            LOG.debug("URL '{}' was removed as invalid or wasn't processed.", url);
        }
    }

    //дальше геттеры сеттеры. комментариев ниже нет.
    public ExecutorService getExecutor() {
        return executor;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int nThreads) {
        this.numThreads = nThreads;
    }

    public Set<String> getUrlsDone() {
        return urlsDone;
    }

    public Set<String> getUrlsPending() {
        return urlsPending;
    }

    public Set<URL> getSortedUrls() {
        return new TreeSet<>(urls);
    }

    public State getState() {
        return new State(urlsDone.size(), urlsPending.size(), startTime);
    }

    public boolean isDone() {
        return urlsPending.isEmpty();
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getBaseUrlHostName() {
        return hostName;
    }

    public void setBaseUrlHostName(String hostName) {
        this.hostName = hostName;
    }

    public Logger getLogger() {
        return LOG;
    }

    @Override
    public String toString() {
        return "Spider{" +
                "root='" + rootUrl + '\'' +
                '}';
    }
}
