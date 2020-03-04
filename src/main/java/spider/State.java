package spider;

import java.time.Instant;

// ? Класс для отображения состояния работающего потока? Сколько обработано ссылок, сколько осталось ... ?
public class State {

    private final int urlsDoneCount;
    private final int urlsPendingCount;
    private final long startTime;
    private final long elapsed;

    //конструктор принимающий число обработанных ссылок, число ожидающих обработки ссылок, время начала
    public State(int urlsDoneCount, int urlsPendingCount, Instant startTime) {
        this.urlsDoneCount = urlsDoneCount;
        this.urlsPendingCount = urlsPendingCount;
        this.startTime = startTime.toEpochMilli();
        this.elapsed = Instant.now().toEpochMilli() - this.startTime;
    }

    //геттеры сеттеры на поля класса
    public int getUrlsDoneCount() {
        return urlsDoneCount;
    }

    public int getUrlsPendingCount() {
        return urlsPendingCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsed() {
        return elapsed;
    }

    @Override
    public String toString() {
        return "State{" +
                "urlsDoneCount=" + urlsDoneCount +
                ", urlsPendingCount=" + urlsPendingCount +
                ", elapsed=" + elapsed +
                '}';
    }
}
