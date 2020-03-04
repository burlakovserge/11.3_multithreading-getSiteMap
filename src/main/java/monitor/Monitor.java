package monitor;

import helpers.Helpers;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spider.Spider;
import spider.State;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//класс который создает потоки и вызывает helpers для записи результата в файл
public class Monitor {

    private static final String FILE_PATH = "src/main/resources/result.txt";

    private static final int MONITOR_INITIAL_DELAY = 0;
    private static final int MONITOR_DELAY = 1000;

    private static final Logger LOG = LogManager.getLogger("monit");

    public static void watchAndSaveUrls(Spider spider) {
        if (spider == null) return;
        //однопоточный "исполнитель"
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

        //выполнение кода  ниже с начальной задержкой 0 и повторением через каждую секунду
        monitor.scheduleAtFixedRate(() -> {
            State state = spider.getState();

            //вызов метода, который в консоль напишет информацию о общем кол-ве, ожидающих и выполненных ссылок для обработки
            showStateMonitor(state);

            //если кол-во ожидающих ссылок 0
            if (spider.isDone()) {
 // ? 2 строки ниже наверное остановка потоков, для освобождения памяти?
                spider.getExecutor().shutdownNow();
                monitor.shutdownNow();

                Helpers.saveUrls(spider.getSortedUrls(), FILE_PATH);

                LOG.debug("Done!");
            }

        }, MONITOR_INITIAL_DELAY, MONITOR_DELAY, TimeUnit.MILLISECONDS);
    }

    //построчно расписать showStateMonitor не могу, тут уровни логгирования, которые мне не мало понятны впринципе
    //то что они могут быть разными знаю, а в чем разница нет. Почему выбран debug а не другой?

    private static void showStateMonitor(State state) {
        final String INFO_LINE = "[MONITOR] Total: %4d, Pending: %4d, Done: %4d, ET: %4ds";
        int totalUrls = state.getUrlsDoneCount() + state.getUrlsPendingCount();

        if (LOG.getLevel() != Level.DEBUG) {
            System.out.printf("\r" + INFO_LINE,
                    totalUrls,
                    state.getUrlsPendingCount(),
                    state.getUrlsDoneCount(),
                    (int) state.getElapsed() / 1000);

            return;
        }

        LOG.debug(String.format(INFO_LINE,
                totalUrls,
                state.getUrlsPendingCount(),
                state.getUrlsDoneCount(),
                (int) state.getElapsed() / 1000));
    }
}
