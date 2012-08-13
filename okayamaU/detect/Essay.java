package detect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;

public class Essay {

	final public String fileName;
	public ArrayList<Sentence> sentences;

	/**
	 * SYSファイルを出力する．
	 */
	public void sysOut() {
		sysOut(null);
	}

	/**
	 * SYSファイルを出力
	 * @param dir 出力先ディレクトリ
	 */
	public void sysOut (String dir) {
		if (dir == null) {
			dir = "";
		} else {
			dir += "\\";
		}

		File file = new File(dir + fileName.replaceAll("\\..+$", "") + ".sys");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file, "EUC-JP");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		for (Sentence s : sentences) {
			pw.println(s.sysOut());
		}
		pw.close();
	}

	/**
	 * POSファイルから品詞タグ，チャンク情報を読み込む．
	 * @param dir
	 * @param fileName
	 */
	private void setPOSs(String dir, String fileName) {
		FileReader fr;
		try {
			fr = new FileReader(dir + "\\" + fileName + ".pos");
			BufferedReader br = new BufferedReader(fr);

			String line;
			int n = 0;

			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;

				while (n < sentences.size()) {
					//TODO EDCファイルの文と，POSファイルの文の対応付け
					if (sentences.get(n).raw.length() == 0 || sentences.get(n).raw.length() < line.length() * 0.1){
						n++;
						continue;
					} else {
						sentences.get(n).setPOS(line);
						n++;
						break;
					}
				}
			}

			br.close();
			fr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * EDCファイルから誤り検出対象となるファイルを読み込む．またPOSファイルから品詞タグ情報を読み込む．
	 * @param dir 対象ファイルのあるディレクトリの場所
	 * @param fileName 対象ファイル名
	 * @param v_agr trueの時はv_agr, falseの時はopen
	 */
	public Essay(String dir, String fileName, boolean v_agr) {
		this.fileName = fileName;

		try {
			// EDCファイルの読み込み
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(dir + "\\" + fileName), "EUC-JP"));
			sentences = new ArrayList<Sentence>();

			int l = 1;
			String line;
			while ((line = br.readLine()) != null) {
				if (v_agr) {
					sentences.add(new Sentence(line));
				} else {
					sentences.add(new SentenceSE(line));
				}
				sentences.get(sentences.size()-1).id = fileName + "-" + l;
				l++;
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// POSファイルの読み込み
		setPOSs(dir, fileName);
	}

	/**
	 * EDCファイルから対象の英文を読み込む．品詞タグはStanford Parserを用いて取得する．
	 * @param dir 対象ファイルのあるディレクトリの場所
	 * @param fileName 対象ファイル名
	 * @param v_agr trueの時はv_agr, falseの時はopen
	 * @param lp Stanford Parser
	 */
	public Essay(String dir, String fileName, boolean v_agr, LexicalizedParser lp) {
		this.fileName = fileName;

		try {
			//EDCファイルの読み込み
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(dir + "\\" + fileName), "EUC-JP"));
			sentences = new ArrayList<Sentence>();

			Pattern p = Pattern.compile("^\\s*$");

			int i = 1;
			String line;
			while ((line = br.readLine()) != null) {
				Sentence s = null;
				if (v_agr) { //
					s = new Sentence(line);
				} else {
					s = new SentenceSE(line);
				}
				s.id = fileName + "-" + i;

				Matcher m = p.matcher(line); // 空白行の検出
				if (!m.find()) {
					// Stanford Parserを用いて構文解析結果の取得
			        TokenizerFactory<CoreLabel> tokenizerFactory =
			        	PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			        List<CoreLabel> token =
			        	tokenizerFactory.getTokenizer(new StringReader(line)).tokenize();
			        Tree stp = lp.apply(token);

			        s.setPosTree(stp);
				}

				sentences.add(s);
				i++;
			}

			br.close();
			//fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
