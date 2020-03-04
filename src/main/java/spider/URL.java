package spider;

//класс для работы с ссылками не просто как со строкой, а как объектами имеющими
// строкое поле- сама ссылка и параметр "глубина" - т.е. уровень вложенности ее в общем списке
//с этим классом мне все понятно
public class URL implements Comparable<URL> {
    private String url;
    private int depth;

    public URL(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    //компатароры и как они сравнивают переданные данные между собой мне еще слабо понятен сам механизм,
    //давно отметил себе что с компараторами надо подробнее познакомитсья
    //но смысл запиши ниже понятен - перезаписываем метод сравнения, для того что бы экземпляры класса URL
    //при работе в упорядоченном списке, правильно были упорядочены :)
    @Override
    public int compareTo(URL other) {
        return this.url.compareTo(other.url);
    }

    @Override
    public String toString() {
        return "Link{" +
                "url='" + url + '\'' +
                ", depth=" + depth +
                '}';
    }
}
