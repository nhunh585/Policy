package ptit.nhunh.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import ptit.nhunh.model.Article;
import ptit.nhunh.model.Comment;
import ptit.nhunh.model.Word;

public class Utils {
	public static Document getHtml(String url) throws IOException {
		return Jsoup.connect(url).ignoreContentType(true).timeout(15000).get();
	}

	public static String removeSymbol(String sentence) {
		for (int i = 33; i <= 47; i++) {
			sentence = sentence.replace(String.valueOf((char) i), " ");
		}
		for (int i = 58; i <= 62; i++) {
			sentence = sentence.replace(String.valueOf((char) i), " ");
		}

		while (sentence.indexOf("  ") >= 0) {
			sentence = sentence.replace("  ", " ");
		}

		return sentence;
	}

	/**
	 * Chuyển một sentence đã được phân đoạn thành 1 List các từ.
	 * 
	 * @param segmentSentence
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<Word> sentence2Words(String segmentSentence) throws SQLException {
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);

		ArrayList<Word> listWord = new ArrayList<>();
		String[] words = segmentSentence.split(" ");

		for (int i = 0; i < words.length; i++) {
			int index = Utils.indexOf(listWord, words[i]);
			if (index > -1) {
				listWord.get(index).setTimesOccur(listWord.get(index).getTimesOccur() + 1);
			} else {
				Word w = new Word(-1, words[i], 1, 0, false);
				listWord.add(w);
			}
		}

		return listWord;
	}

	/**
	 * Kiểm tra từ w có thuộc listWord hay không
	 * 
	 * @param listWord
	 * @param w
	 * @return
	 */
	public static int indexOf(ArrayList<Word> listWord, Word w) {
		Collator collator = Collator.getInstance();
		for (int i = 0; i < listWord.size(); i++) {
			if (collator.equals(listWord.get(i).getWord(), w.getWord().trim())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Kiểm tra String w có thuộc listWord hay không nếu có thì return lại vị trí từ
	 * chứa String đấy.
	 * 
	 * @param listWord
	 * @param w
	 * @return
	 */
	public static int indexOf(ArrayList<Word> listWord, String w) {
		Collator collator = Collator.getInstance();
		for (int i = 0; i < listWord.size(); i++) {
			if (collator.equals(listWord.get(i).getWord(), w.trim())) {
				return i;
			}
		}
		return -1;
	}

	public static String compound2Unicode(String str) {
		str = str.replaceAll("\u0065\u0309", "\u1EBB"); // ẻ
		str = str.replaceAll("\u0065\u0301", "\u00E9"); // é
		str = str.replaceAll("\u0065\u0300", "\u00E8"); // è
		str = str.replaceAll("\u0065\u0323", "\u1EB9"); // ẹ
		str = str.replaceAll("\u0065\u0303", "\u1EBD"); // ẽ
		str = str.replaceAll("\u00EA\u0309", "\u1EC3"); // ể
		str = str.replaceAll("\u00EA\u0301", "\u1EBF"); // ế
		str = str.replaceAll("\u00EA\u0300", "\u1EC1"); // ề
		str = str.replaceAll("\u00EA\u0323", "\u1EC7"); // ệ
		str = str.replaceAll("\u00EA\u0303", "\u1EC5"); // ễ
		str = str.replaceAll("\u0079\u0309", "\u1EF7"); // ỷ
		str = str.replaceAll("\u0079\u0301", "\u00FD"); // ý
		str = str.replaceAll("\u0079\u0300", "\u1EF3"); // ỳ
		str = str.replaceAll("\u0079\u0323", "\u1EF5"); // ỵ
		str = str.replaceAll("\u0079\u0303", "\u1EF9"); // ỹ
		str = str.replaceAll("\u0075\u0309", "\u1EE7"); // ủ
		str = str.replaceAll("\u0075\u0301", "\u00FA"); // ú
		str = str.replaceAll("\u0075\u0300", "\u00F9"); // ù
		str = str.replaceAll("\u0075\u0323", "\u1EE5"); // ụ
		str = str.replaceAll("\u0075\u0303", "\u0169"); // ũ
		str = str.replaceAll("\u01B0\u0309", "\u1EED"); // ử
		str = str.replaceAll("\u01B0\u0301", "\u1EE9"); // ứ
		str = str.replaceAll("\u01B0\u0300", "\u1EEB"); // ừ
		str = str.replaceAll("\u01B0\u0323", "\u1EF1"); // ự
		str = str.replaceAll("\u01B0\u0303", "\u1EEF"); // ữ
		str = str.replaceAll("\u0069\u0309", "\u1EC9"); // ỉ
		str = str.replaceAll("\u0069\u0301", "\u00ED"); // í
		str = str.replaceAll("\u0069\u0300", "\u00EC"); // ì
		str = str.replaceAll("\u0069\u0323", "\u1ECB"); // ị
		str = str.replaceAll("\u0069\u0303", "\u0129"); // ĩ
		str = str.replaceAll("\u006F\u0309", "\u1ECF"); // ỏ
		str = str.replaceAll("\u006F\u0301", "\u00F3"); // ó
		str = str.replaceAll("\u006F\u0300", "\u00F2"); // ò
		str = str.replaceAll("\u006F\u0323", "\u1ECD"); // ọ
		str = str.replaceAll("\u006F\u0303", "\u00F5"); // õ
		str = str.replaceAll("\u01A1\u0309", "\u1EDF"); // ở
		str = str.replaceAll("\u01A1\u0301", "\u1EDB"); // ớ
		str = str.replaceAll("\u01A1\u0300", "\u1EDD"); // ờ
		str = str.replaceAll("\u01A1\u0323", "\u1EE3"); // ợ
		str = str.replaceAll("\u01A1\u0303", "\u1EE1"); // ỡ
		str = str.replaceAll("\u00F4\u0309", "\u1ED5"); // ổ
		str = str.replaceAll("\u00F4\u0301", "\u1ED1"); // ố
		str = str.replaceAll("\u00F4\u0300", "\u1ED3"); // ồ
		str = str.replaceAll("\u00F4\u0323", "\u1ED9"); // ộ
		str = str.replaceAll("\u00F4\u0303", "\u1ED7"); // ỗ
		str = str.replaceAll("\u0061\u0309", "\u1EA3"); // ả
		str = str.replaceAll("\u0061\u0301", "\u00E1"); // á
		str = str.replaceAll("\u0061\u0300", "\u00E0"); // à
		str = str.replaceAll("\u0061\u0323", "\u1EA1"); // ạ
		str = str.replaceAll("\u0061\u0303", "\u00E3"); // ã
		str = str.replaceAll("\u0103\u0309", "\u1EB3"); // ẳ
		str = str.replaceAll("\u0103\u0301", "\u1EAF"); // ắ
		str = str.replaceAll("\u0103\u0300", "\u1EB1"); // ằ
		str = str.replaceAll("\u0103\u0323", "\u1EB7"); // ặ
		str = str.replaceAll("\u0103\u0303", "\u1EB5"); // ẵ
		str = str.replaceAll("\u00E2\u0309", "\u1EA9"); // ẩ
		str = str.replaceAll("\u00E2\u0301", "\u1EA5"); // ấ
		str = str.replaceAll("\u00E2\u0300", "\u1EA7"); // ầ
		str = str.replaceAll("\u00E2\u0323", "\u1EAD"); // ậ
		str = str.replaceAll("\u00E2\u0303", "\u1EAB"); // ẫ
		str = str.replaceAll("\u0045\u0309", "\u1EBA"); // Ẻ
		str = str.replaceAll("\u0045\u0301", "\u00C9"); // É
		str = str.replaceAll("\u0045\u0300", "\u00C8"); // È
		str = str.replaceAll("\u0045\u0323", "\u1EB8"); // Ẹ
		str = str.replaceAll("\u0045\u0303", "\u1EBC"); // Ẽ
		str = str.replaceAll("\u00CA\u0309", "\u1EC2"); // Ể
		str = str.replaceAll("\u00CA\u0301", "\u1EBE"); // Ế
		str = str.replaceAll("\u00CA\u0300", "\u1EC0"); // Ề
		str = str.replaceAll("\u00CA\u0323", "\u1EC6"); // Ệ
		str = str.replaceAll("\u00CA\u0303", "\u1EC4"); // Ễ
		str = str.replaceAll("\u0059\u0309", "\u1EF6"); // Ỷ
		str = str.replaceAll("\u0059\u0301", "\u00DD"); // Ý
		str = str.replaceAll("\u0059\u0300", "\u1EF2"); // Ỳ
		str = str.replaceAll("\u0059\u0323", "\u1EF4"); // Ỵ
		str = str.replaceAll("\u0059\u0303", "\u1EF8"); // Ỹ
		str = str.replaceAll("\u0055\u0309", "\u1EE6"); // Ủ
		str = str.replaceAll("\u0055\u0301", "\u00DA"); // Ú
		str = str.replaceAll("\u0055\u0300", "\u00D9"); // Ù
		str = str.replaceAll("\u0055\u0323", "\u1EE4"); // Ụ
		str = str.replaceAll("\u0055\u0303", "\u0168"); // Ũ
		str = str.replaceAll("\u01AF\u0309", "\u1EEC"); // Ử
		str = str.replaceAll("\u01AF\u0301", "\u1EE8"); // Ứ
		str = str.replaceAll("\u01AF\u0300", "\u1EEA"); // Ừ
		str = str.replaceAll("\u01AF\u0323", "\u1EF0"); // Ự
		str = str.replaceAll("\u01AF\u0303", "\u1EEE"); // Ữ
		str = str.replaceAll("\u0049\u0309", "\u1EC8"); // Ỉ
		str = str.replaceAll("\u0049\u0301", "\u00CD"); // Í
		str = str.replaceAll("\u0049\u0300", "\u00CC"); // Ì
		str = str.replaceAll("\u0049\u0323", "\u1ECA"); // Ị
		str = str.replaceAll("\u0049\u0303", "\u0128"); // Ĩ
		str = str.replaceAll("\u004F\u0309", "\u1ECE"); // Ỏ
		str = str.replaceAll("\u004F\u0301", "\u00D3"); // Ó
		str = str.replaceAll("\u004F\u0300", "\u00D2"); // Ò
		str = str.replaceAll("\u004F\u0323", "\u1ECC"); // Ọ
		str = str.replaceAll("\u004F\u0303", "\u00D5"); // Õ
		str = str.replaceAll("\u01A0\u0309", "\u1EDE"); // Ở
		str = str.replaceAll("\u01A0\u0301", "\u1EDA"); // Ớ
		str = str.replaceAll("\u01A0\u0300", "\u1EDC"); // Ờ
		str = str.replaceAll("\u01A0\u0323", "\u1EE2"); // Ợ
		str = str.replaceAll("\u01A0\u0303", "\u1EE0"); // Ỡ
		str = str.replaceAll("\u00D4\u0309", "\u1ED4"); // Ổ
		str = str.replaceAll("\u00D4\u0301", "\u1ED0"); // Ố
		str = str.replaceAll("\u00D4\u0300", "\u1ED2"); // Ồ
		str = str.replaceAll("\u00D4\u0323", "\u1ED8"); // Ộ
		str = str.replaceAll("\u00D4\u0303", "\u1ED6"); // Ỗ
		str = str.replaceAll("\u0041\u0309", "\u1EA2"); // Ả
		str = str.replaceAll("\u0041\u0301", "\u00C1"); // Á
		str = str.replaceAll("\u0041\u0300", "\u00C0"); // À
		str = str.replaceAll("\u0041\u0323", "\u1EA0"); // Ạ
		str = str.replaceAll("\u0041\u0303", "\u00C3"); // Ã
		str = str.replaceAll("\u0102\u0309", "\u1EB2"); // Ẳ
		str = str.replaceAll("\u0102\u0301", "\u1EAE"); // Ắ
		str = str.replaceAll("\u0102\u0300", "\u1EB0"); // Ằ
		str = str.replaceAll("\u0102\u0323", "\u1EB6"); // Ặ
		str = str.replaceAll("\u0102\u0303", "\u1EB4"); // Ẵ
		str = str.replaceAll("\u00C2\u0309", "\u1EA8"); // Ẩ
		str = str.replaceAll("\u00C2\u0301", "\u1EA4"); // Ấ
		str = str.replaceAll("\u00C2\u0300", "\u1EA6"); // Ầ
		str = str.replaceAll("\u00C2\u0323", "\u1EAC"); // Ậ
		str = str.replaceAll("\u00C2\u0303", "\u1EAA"); // Ẫ
		return str;
	}

	public static String getThanhNienPageId(String url) {
		try {
			return "tn" + url.substring(url.lastIndexOf("-") + 1, url.lastIndexOf("."));
		} catch (Exception e) {
			System.out.println("Loi pageID: " + url);
			return "-1";
		}
	}

	public static String getVnExpressPageId(String url) {
		try {
			return "ve" + url.substring(url.lastIndexOf("-") + 1, url.lastIndexOf("-") + 8);
		} catch (Exception e) {
			System.out.println(url);
			e.printStackTrace();
			return "fail";
		}
	}

	public static double round(double d) {
		return (int) (d * 100) / (double) 100;
	}

	public static int contain(ArrayList<Object> urls, Article url) {
		for (int i = 0; i < urls.size(); i++) {
			Article u = (Article) urls.get(i);
			if (u.getUrl().equals(url.getUrl())) {
				return i;
			}
		}
		return -1;
	}

	public static String getCurrentTime() {
		long currentTimeMillis = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("HH-mm");
		return sdf.format(new java.util.Date(currentTimeMillis));
	}

	public static String getCurrentDate() {
		long currentTimeMillis = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new java.util.Date(currentTimeMillis));
	}

	public static ArrayList<Comment> object2Comment(List<Object> objs) {
		ArrayList<Comment> listComment = new ArrayList<>();

		for (Object obj : objs) {
			listComment.add((Comment) obj);
		}

		return listComment;
	}

	public static ArrayList<Article> object2Article(List<Object> objs) {
		ArrayList<Article> listArticle = new ArrayList<>();

		for (Object obj : objs) {
			listArticle.add((Article) obj);
		}

		return listArticle;
	}

	public static ArrayList<Word> object2Word(List<Object> objs) {
		ArrayList<Word> listWord = new ArrayList<>();

		for (Object obj : objs) {
			listWord.add((Word) obj);
		}

		return listWord;
	}
}
