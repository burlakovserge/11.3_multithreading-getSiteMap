import monitor.Monitor;
import spider.Spider;

public class Main {
    public static void main(String[] args) {
        final String URL = "http://skillbox.ru/";
        Spider spider = new Spider(URL);
        spider.run();

        Monitor.watchAndSaveUrls(spider);
    }
}
