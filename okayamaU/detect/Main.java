package detect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;
import search.Search;
import search.SearchTotal;

public class Main {

	public static String[] dryRunDir = {
		"40-42",
		"42-44",
		"43-45",
		"44-46",
		"47-49",
		"48-50",
		"49-51",
		"50-52",
		"52-54",
		"54-56",
		"55-57",
		"56-58",
		"58-60",
		"59-61",
		"61-63",
		"62-64",
		"64-66",
	};

	public static String[][] formalRunData = {
		{"18-18\\1", "18-18-1"},
		{"18-18\\10", "18-18-10"},
		{"18-18\\2", "18-18-2"},
		{"18-18\\3", "18-18-3"},
		{"18-18\\4", "18-18-4"},
		{"18-18\\5", "18-18-5"},
		{"18-18\\6", "18-18-6"},
		{"18-18\\7", "18-18-7"},
		{"18-18\\8", "18-18-8"},
		{"18-18\\9", "18-18-9"},
		{"41-43\\1", "41-43-1"},
		{"41-43\\10", "41-43-10"},
		{"41-43\\2", "41-43-2"},
		{"41-43\\3", "41-43-3"},
		{"41-43\\4", "41-43-4"},
		{"41-43\\5", "41-43-5"},
		{"41-43\\6", "41-43-6"},
		{"41-43\\7", "41-43-7"},
		{"41-43\\8", "41-43-8"},
		{"41-43\\9", "41-43-9"},
		{"46-48\\1", "46-48-1"},
		{"46-48\\10", "46-48-10"},
		{"46-48\\2", "46-48-2"},
		{"46-48\\3", "46-48-3"},
		{"46-48\\4", "46-48-4"},
		{"46-48\\5", "46-48-5"},
		{"46-48\\6", "46-48-6"},
		{"46-48\\7", "46-48-7"},
		{"46-48\\8", "46-48-8"},
		{"46-48\\9", "46-48-9"},
		{"51-53\\1", "51-53-1"},
		{"51-53\\2", "51-53-2"},
		{"53-55\\1", "53-55-1"},
		{"53-55\\10", "53-55-10"},
		{"53-55\\2", "53-55-2"},
		{"53-55\\3", "53-55-3"},
		{"53-55\\4", "53-55-4"},
		{"53-55\\5", "53-55-5"},
		{"53-55\\6", "53-55-6"},
		{"53-55\\7", "53-55-7"},
		{"53-55\\8", "53-55-8"},
		{"53-55\\9", "53-55-9"},
		{"57-59\\1", "57-59-1"},
		{"57-59\\2", "57-59-2"},
		{"57-59\\3", "57-59-3"},
		{"57-59\\4", "57-59-4"},
		{"57-59\\5", "57-59-5"},
		{"63-65\\1", "63-65-1"},
		{"63-65\\2", "63-65-2"},
		{"63-65\\3", "63-65-3"},
		{"63-65\\4", "63-65-4"},
		{"63-65\\5", "63-65-5"},
		{"63-65\\6", "63-65-6"},
		{"65-67\\1", "65-67-1"},
		{"65-67\\10", "65-67-10"},
		{"65-67\\2", "65-67-2"},
		{"65-67\\3", "65-67-3"},
		{"65-67\\4", "65-67-4"},
		{"65-67\\5", "65-67-5"},
		{"65-67\\6", "65-67-6"},
		{"65-67\\7", "65-67-7"},
		{"65-67\\8", "65-67-8"},
		{"65-67\\9", "65-67-9"},
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Detection d = new Detection();

	    LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		SearchTotal.setCache(100000);

		//dryRun(d, lp, true);
		//dryRun(d, lp, false);
		//dryRun(d, null, false, null);
		//formalRun(d, lp, true);
		//formalRun(d, null, true);
		//formalRun(d, lp, false);

		boolean isCache = false;
		boolean isDryRun = false;
		boolean isFormalRun = false;
		boolean isLog = false;
		String inDir = null;
		String outDir = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].charAt(0) == '-'){
				switch (args[i].charAt(1)) {
				case 'i':
					inDir = args[++i];
					break;

				case 'o':
					outDir = args[++i];
					File dirs = new File(outDir);
					if (!dirs.exists()) {
						dirs.mkdirs();
					}
					break;

				case 'd':
					isDryRun = true;
					break;

				case 'f':
					isFormalRun = true;
					break;

				case 'l':
					isLog = isCache = true;
					Search.VAILD_OUTPUT_XMLFILES();
					Detection.VALID_OUTPUT_LOGFILES();
					break;

				case 'c':
					isCache = true;
					SearchTotal.inCache();
					break;

				default:
					break;
				}
			}

		}

		if (isDryRun) {
			dryRun(d, lp, true, outDir);
		}

		if (isFormalRun) {
			formalRun(d, lp, true, outDir);
		}

		if (inDir == null && !isDryRun && !isFormalRun) {
			commandLine(d, lp);
		}

		if (inDir != null) {
			detectErrorFromDir(inDir, outDir, d, lp, true);
		}

		if (isCache || isLog) {
			SearchTotal.outCache();
		}

	}

	private static void commandLine (Detection d, LexicalizedParser lp) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Pattern p = Pattern.compile("^\\s*$");
		while (true) {
			try {
				System.out.print("> ");
				String line = br.readLine();
				if (line.equals("quit") || line.equals("exit")) {
					return;
				}

				Matcher m = p.matcher(line); // 空白行の検出
				if (m.find()) {
					continue;
				}

				Sentence s = new Sentence(line);
				TokenizerFactory<CoreLabel> tokenizerFactory =
						PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
				List<CoreLabel> token =
						tokenizerFactory.getTokenizer(new StringReader(line)).tokenize();
				Tree stp = lp.apply(token);

				s.setPosTree(stp);

				d.detectVerbError(s);

				System.out.println(s.sysOut());

			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private static void detectErrorFromFile (String file, String inDir, String outDir, Detection d, LexicalizedParser lp, boolean v_agr) {
		Essay cd = (lp == null)? new Essay(inDir, file, v_agr): new Essay(inDir, file, v_agr, lp);
		System.out.println();
		System.out.println(cd.fileName);
		System.out.println();
		//cd.fprint();
		for (Sentence s: cd.sentences) {
			System.out.println(s.raw);
			if (v_agr) {
				d.detectVerbError(s);
			} else {
				d.detectAllError((SentenceSE) s);
			}
			System.out.println();
			System.out.println(s.sysOut());
			System.out.println();
		}
		cd.sysOut(outDir);
	}

	private static void detectErrorFromDir (String inDir, String outDir, Detection d, LexicalizedParser lp, boolean v_agr) {
		File dir = new File(inDir);

		if (!dir.exists()) {
			return;
		}

		File[] files = dir.listFiles();

		for (File f: files) {
			if (f.isFile() && f.getName().endsWith(".edc")) {
				detectErrorFromFile(f.getName(), inDir, outDir, d, lp, v_agr);
			}

			if (f.isDirectory()) {
				detectErrorFromDir(inDir + "\\" + f.getName(), outDir, d, lp, v_agr);
			}
		}

	}

	private static void dryRun (Detection d, LexicalizedParser lp, boolean v_agr, String outDir) {
		for (String dir: dryRunDir) {
			for (int subDir = 1; subDir < 11; subDir++) {
				detectErrorFromFile (dir+"-"+subDir + ".edc", ".\\KJCorpus\\corpus_data\\"+dir+"\\"+subDir, null, d, lp, v_agr);
			}
		}
	}

	private static void formalRun (Detection d, LexicalizedParser lp, boolean v_agr, String outDir) {
		for (String names[]: formalRunData) {
			detectErrorFromFile(names[1] + ".edc", "corpus_data\\" + names[0], null, d, lp, v_agr);
		}
	}
}
