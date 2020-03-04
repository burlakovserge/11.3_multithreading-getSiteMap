package helpers;

import spider.URL;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

//класс для записи найденых ссылок в файл, в виде требуемом по заданию - с отступами
public class Helpers {

	//метод сохранения ссылок в файл, принимает список из объектов класса URL
	// и путь к файлу в который записывать результат
	public static void saveUrls(Set<URL> data, String fileName) {
		Set<URL> items = new TreeSet<>(data);

		//2 строки ниже
		//PrintWriter для вывода результата в консоль, но ...
		//передавая newBufferedWriter с путем к файлу позволяет создать и записать результат в файл
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
		        Paths.get(fileName),StandardCharsets.UTF_8))) {

			//перебор всех ссылок из переданного в конструктор списка
			for (URL url : items) {

// ? url.getDepth() нашел что в Spider .run() в addUrl передан 0, но что это означает и какие и почему может принимать другие значениея не ясно
				// может быть это число увеличивается по мере "углубления" в дерево сайта. Изначально 0, рассматривая стартовую страницу,
				//перейдя по первой полученной ссылке глубина уже 1, переходя по полученным ссылкам на этом уровне будет 2...
				String s = indent('\t', url.getDepth()) + url.getUrl();
				//String s = url.getUrl();
				//запись ссылки с форматированием в файл
				pw.println(s);
			}

			//newBufferedWriter требует проброс IOException, в случае когда текст записываемый в файл
			// невозможно записать в указанной кодировке,  в таком случае вывести в консоль какое-то сообщение.
// ? Не разобрался что будет выводиться
		} catch (Exception e) {
			System.err.format("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
		}
	}

	//формируем отступ табами перед ссылок для построения дерева ссылок
	public static String indent(char c, int n) {
		//отступа нет если это нулевой уровень (главная страница)
		if (n <= 0) return "";
		//создать массив символов в количестве символов равном глубине
		char[] s = new char[n];
		for (int i = 0; i < n; i++) s[i] = c;
		//возвращается строка с табуляцией в кол-ве равной "глубине" в дереве сайта
		return new String(s);
	}

	public static void main(String[] args) throws IOException {
		;
    }
}
