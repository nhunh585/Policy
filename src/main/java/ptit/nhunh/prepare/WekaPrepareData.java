package ptit.nhunh.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;

import ptit.nhunh.context.Context;
import ptit.nhunh.dao.SQLDAO;
import ptit.nhunh.dao.SQLDAOFactory;
import ptit.nhunh.model.Comment;
import ptit.nhunh.model.Word;
import ptit.nhunh.utils.Utils;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class WekaPrepareData {
	private VietTokenizer vietToken;
	private SQLDAO cmtTestDao;
	private SQLDAO cmtDao;
	private SQLDAO wordDao;

	private ArrayList<Object> listTrainCmt;
	private ArrayList<Object> listTestCmt;

	private ArrayList<Word> listWord;

	private int labelCount = 2;

	public WekaPrepareData() throws SQLException, IOException {
		this.cmtTestDao = SQLDAOFactory.getDAO(SQLDAOFactory.COMMENTTEST);
		this.cmtDao = SQLDAOFactory.getDAO(SQLDAOFactory.COMMENT);
		this.wordDao = SQLDAOFactory.getDAO(SQLDAOFactory.WORD);
		// this.vietToken = new VietTokenizer();
		this.listWord = new ArrayList<>();

		String date = LocalDate.now().toString();
		String time = LocalTime.now().toString();

		String path = "src\\main\\resource\\data\\" + this.labelCount + "label\\" + date.replaceAll("-", "") + "\\"
				+ time.substring(0, 5).replace(":", "");
		BufferedWriter pathWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File("path.txt"))));
		pathWriter.write(path);
		pathWriter.close();

		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		this.listTestCmt = new ArrayList<>();
		this.listTrainCmt = new ArrayList<>();
		this.listTrainCmt = this.cmtTestDao
				.getData("select * from TblCommentTest where id <= " + Context.TRAINSIZE + " order by id");
		this.listTestCmt = this.cmtTestDao.getData("select * from TblCommentTest where id <= "
				+ (Context.TESTSIZE + Context.TRAINSIZE) + " and id > " + Context.TRAINSIZE + " order by id");
		// this.read();
		this.checkAcronymsWord(this.listTestCmt);
		this.checkAcronymsWord(this.listTrainCmt);
		this.wordDao.update("delete from TblWord");
		this.wordDao.update("DBCC CHECKIDENT ('TblWord', RESEED, 0)");
	}

	public static void main(String[] args)
			throws FileNotFoundException, SQLException, IOException, InterruptedException, ClassNotFoundException {
		new WekaPrepareData().process();
		// new Classifier().execute();
	}

	public void process() throws SQLException, IOException, InterruptedException {
		System.out.println("--- Start Processing ---");

		long t1 = System.currentTimeMillis();
		this.genTrainingDataFile(Context.TRAINSIZE);
		long t2 = System.currentTimeMillis();

		this.genTestingDataFile(Context.TRAINSIZE, Context.TESTSIZE + Context.TRAINSIZE);
		long t3 = System.currentTimeMillis();

		System.out.println("--- End Processing ---");

		System.out.println("Generate Training File   : " + (t2 - t1) / (float) 60000);
		System.out.println("Generate Testing File   : " + (t3 - t2) / (float) 60000);
	}

	/**
	 * Tạo các file train data (input.train, train).
	 * 
	 * @param train
	 * @param test
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void genTrainingDataFile(int train) throws SQLException, IOException, InterruptedException {
		System.out.println("\t+> Training file Generating...");

		this.collect(this.listWord, this.listTrainCmt);
		this.checkStopWord(this.listWord);
		this.write(this.listTrainCmt, Context.TRAINSIZE);
	}

	/**
	 * Tạo các file test data (input.test, test).
	 * 
	 * @param train
	 * @param test
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void genTestingDataFile(int train, int test) throws SQLException, IOException, InterruptedException {
		System.out.println("\t+> Testing file Generating...");

		this.collect(this.listWord, this.listTestCmt);
		this.checkStopWord(this.listWord);
		this.write(this.listTestCmt, Context.TRAINSIZE + Context.TESTSIZE);
	}

	/**
	 * Collect các từ trong <strong>rs</strong> mà chưa có trong
	 * <strong>listAllWord</strong>. Kết hợp tính DF của mỗi từ.
	 * 
	 * @param listWord
	 * @param rs
	 * @throws SQLException
	 */
	private void collect(ArrayList<Word> listWord, ArrayList<Object> listCmt) throws SQLException {
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);
		for (Object o : listCmt) {
			Comment c = (Comment) o;
			String segmentCmt = c.getCmt_segment();

			ArrayList<Word> aw = Utils.sentence2Words(segmentCmt);
			for (int i = 0; i < aw.size(); i++) {
				int pos = Utils.indexOf(listWord, aw.get(i));
				if (pos == -1) {
					aw.get(i).setId(listWord.size() + 1);
					aw.get(i).setDF(1);
					aw.get(i).setId(listWord.size() + 1);
					aw.get(i).setCmt_id(c.getId());
					listWord.add(aw.get(i));
				} else {
					listWord.get(pos).setDF(listWord.get(pos).getDF() + 1);
				}
			}
		}
	}

	/**
	 * <p>
	 * Chuyển các câu ở trong rs thành dạng vector và ghi ra file
	 * <strong>bw1</strong>, <strong>bw2</strong>.
	 * </p>
	 * 
	 * <strong>bw1</strong>: các câu có id ở đầu để map vào Database <br>
	 * <strong>bw2</strong>: các câu không có id nhưng map các dòng với bw1.
	 * 
	 * @param rs
	 * @param bw1
	 * @param bw2
	 * @throws SQLException
	 * @throws IOException
	 */
	public void write(ArrayList<Object> listCmt, int size) throws SQLException, IOException {
		this.clear(this.listWord);
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File("arff" + listCmt.size() + ".txt"))));
		bw.write("@relation policy\n");
		for (int i = 0; i < this.listWord.size(); i++) {
			bw.write("@attribute " + (i + 1) + " NUMERIC\n");
		}
		bw.write("@attribute label {1,2}\n\n");
		bw.write("@data\n");

		for (Object o : listCmt) {
			Comment c = (Comment) o;
			int sumWord = c.getCmt_segment().trim().split(" ").length;
			ArrayList<Word> words = Utils.sentence2Words(c.getCmt_segment());
			ArrayList<Word> listAll = SerializationUtils.clone(this.listWord);

			for (int i = 0; i < words.size(); i++) {
				int index = Utils.indexOf(listAll, words.get(i));
				if (listAll.get(index).getIsStopWord() != 1) {
					listAll.get(index).setTimesOccur(words.get(i).getTimesOccur());
				} else {

				}
			}

			for (int i = 0; i < listAll.size(); i++) {
				bw.write(String.valueOf(listAll.get(i).getTFIDF(size, sumWord)).substring(0, 3) + ",");
			}
			bw.write((c.getLabel() + "").toCharArray());
			bw.newLine();
		}
		bw.close();
	}

	private void checkStopWord(ArrayList<Word> listWord) throws IOException, SQLException {
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream("stopword.txt"), StandardCharsets.UTF_8));
		String s = "";
		while ((s = br.readLine()) != null) {
			for (Word word : listWord) {
				if (collator.equals(s, word.getWord())) {
					word.setIsStopWord(1);
				}
			}
		}
		br.close();
	}

	private void checkAcronymsWord(ArrayList<Object> listCmt) throws IOException, SQLException {
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.TERTIARY);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream("acronyms.txt"), StandardCharsets.UTF_8));
		String s = "";
		while ((s = br.readLine()) != null) {
			String acronyms = s.substring(0, s.indexOf(":"));
			String replaceWord = s.substring(s.indexOf(":") + 1).trim();
			for (Object obj : listCmt) {
				Comment cmt = (Comment) obj;
				String word = " " + acronyms + " ";
				if (cmt.getCmt_segment().length() > acronyms.length()) {
					if (cmt.getCmt_segment().indexOf(word) >= 0) {
						cmt.setCmt_segment(cmt.getCmt_segment().replace(word, " " + replaceWord + " "));
					}
					if (cmt.getCmt_segment().substring(0, acronyms.length() + 1).equals(acronyms + " ")) {
						cmt.setCmt_segment(replaceWord + cmt.getCmt_segment().substring(acronyms.length()));
					}
					if (cmt.getCmt_segment().substring(cmt.getCmt_segment().length() - acronyms.length() - 1).trim()
							.equals(" " + acronyms)) {
						cmt.setCmt_segment(
								cmt.getCmt_segment().substring(0, cmt.getCmt_segment().length() - acronyms.length())
										+ replaceWord);
					}
				}
			}
		}
		br.close();
	}

	private void read() throws NumberFormatException, IOException {
		BufferedReader br1 = null, br2 = null;
		if (Context.TYPEOFCOPYDATA2DATABASE == 1) {
			br1 = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File("src\\main\\resource\\data\\100\\1_150.txt")),
							StandardCharsets.UTF_8));
			br2 = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File("src\\main\\resource\\data\\100\\2_150.txt")),
							StandardCharsets.UTF_8));
		} else if (Context.TYPEOFCOPYDATA2DATABASE == 2) {
			br1 = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File("src\\main\\resource\\data\\100\\1_100.txt")),
							StandardCharsets.UTF_8));
			br2 = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File("src\\main\\resource\\data\\100\\2_100.txt")),
							StandardCharsets.UTF_8));
		}
		ArrayList<Comment> label1 = new ArrayList<>();
		ArrayList<Comment> label2 = new ArrayList<>();
		String line1 = "", line2 = "";
		while ((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
			Comment c1 = new Comment();
			c1.setLabel(Integer.parseInt(line1.substring(0, 1)));
			c1.setId(Integer.parseInt(line1.substring(2, 13).trim()));
			c1.setCmt_segment(line1.substring(14).trim());
			label1.add(c1);
			Comment c2 = new Comment();
			c2.setLabel(Integer.parseInt(line2.substring(0, 1)));
			c2.setId(Integer.parseInt(line2.substring(2, 13).trim()));
			c2.setCmt_segment(line2.substring(14).trim());
			label2.add(c2);
		}
		for (int i = 0; i < (Context.TRAINSIZE + Context.TESTSIZE) / 2; i++) {
			if (i < Context.TRAINSIZE / 2) {
				this.listTrainCmt.add(label1.get(i));
			} else {
				this.listTestCmt.add(label1.get(i));
			}
		}
		for (int i = 0; i < (Context.TRAINSIZE + Context.TESTSIZE) / 2; i++) {
			if (i < Context.TRAINSIZE / 2) {
				this.listTrainCmt.add(label2.get(i));
			} else {
				this.listTestCmt.add(label2.get(i));
			}
		}
	}

	public void clear(ArrayList<Word> listWord) {
		for (int i = 0; i < listWord.size(); i++) {
			listWord.get(i).setTimesOccur(0);
		}
	}
}