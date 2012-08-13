package detect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import search.SearchTotal;
import edu.stanford.nlp.process.Morphology;

public class Detection {

	private static final int N = 5;

	private static boolean isLog = false;

	public static void VALID_OUTPUT_LOGFILES(){
		isLog = true;
	}

	public static void INVALID_OUTPUT_LOGFILES(){
		isLog = false;;
	}

	/**
	 * 主語-動詞の数・人称の不一致に関する誤りの検出
	 * @param sentence 検出対象の文
	 */
	public void detectVerbError(Sentence sentence) {
		Tree tree = sentence.posTree;

		if (tree == null) {
			return;
		}

		Tree[] chunks = tree.getChildNodes();

		// 主語-動詞の検出
		for (int i = 1; i < chunks.length; i++) {
			if (chunks[i].node.equals("VP")) {
				if (chunks[i-1].node.equals("NP")) {
					// 主語-動詞の人称・数の一致に関する誤りの検出
					// NP + VP
					detectV_AgrError(chunks, i);
				} else if (chunks[i-1].node.equals("VP")) {
					// 未定義
				} else if (chunks[i-1].node.equals(",")) {
					// 未定義
				} else if (chunks[i-1].node.equals("ADVP")) {
					// 主語-動詞の人称・数の一致に関する誤りの検出
					// NP + ADVP + VP
					detectV_AgrError(chunks, i);
				} else if (chunks[i-1].node.equals("INTJ")) {
					// 未定義
				} else if (chunks[i-1].node.equals("PP")) {
					// 未定義
				} else if (chunks[i-1].node.equals("ADJP")) {
					// 未定義
				} else if (chunks[i-1].node.equals("CONJP")) {
					// ？
					detectV_AgrError(chunks, i);
				} else if (chunks[i-1].node.equals(".")) {
					// 未定義
				} else if (chunks[i-1].node.equals("SBAR")) {
					// 未定義
				} else if (chunks[i-1].node.equals("PRT")) {
					// 未定義
				} else if (chunks[i-1].node.equals("WHNP")) {
					// 主語-動詞の人称・数の一致に関する誤りの検出
					// Stanford Parserで品詞タグ付した場合に，この条件にあてはまることがある．
					// NP + WHNP + VP
					detectV_AgrError(chunks, i);
				} else {
					// ありえん
				}

			}
		}

	}

	/**
	 * 主語-動詞の人称・数の一致に関する誤りを検出する．
	 * @param chunks 対象となる英文のチャンク
	 * @param vpIndex 動詞の含まれているチャンク
	 * @return
	 */
	private boolean detectV_AgrError(Tree[] chunks, int vpIndex) {
		Tree wordt = chunks[vpIndex].getLeftmostLeaf();
		String word = wordt.node;

		if (isBe(word)) { // be動詞の場合
			return detectBeError(chunks, vpIndex);
		}

		Tree[] poss = chunks[vpIndex].getChildNodes();
		Tree pos = poss[0];

		// VPが動詞から始まる場合は，検出対象と見なす
		if (poss.length > 1 && pos.node.equals("RB")) { // VPがnotなどの副詞から始まる場合は次の単語を見る．
			if (poss[1].node.indexOf("VB") != 0) {
				pos = poss[1];
				return false;
			}
		} else if (pos.node.indexOf("VB") != 0) {
			return false;
		}

		boolean isPuralV; // 動詞の人称・数
		if (pos.node.equals("VB") || pos.node.equals("VBP")) { // 動詞原型
			isPuralV = true;
		} else if (pos.node.equals("VBZ")) { // 三人称単数現在
			isPuralV = false;
		} else { // それ以外（過去形，過去分詞形，現在分詞形など）の場合は何もしない
			return false;
		}

		if (isInterrogation(chunks, vpIndex)) { // 疑問文の場合
			if (isPuralV) { // 動詞が原形（複数形）の場合，何もしない
				return false;
			} else { 	// 動詞が三人称単数現在の場合，
				chunks[vpIndex].setError(true);
				return false;
			}
		}

		int tmp = isPuralSubject(chunks, vpIndex, isThere(chunks, vpIndex)); // 主語の人称・数の取得
		if (tmp == -1) { // 主語の人称・数がよくわからない場合は何もしない
			return false;
		}

		boolean isPuralN = tmp == 1? true: false; // 主語の人称・数

		if (isPuralN != isPuralV) { // 主語-動詞の人称・数が一致していない場合，誤りとして検出する．
			chunks[vpIndex].setError(true);
		}

		return false;
	}

	/**
	 * 主語-動詞の一致に関する誤りを検出する．ここでは，動詞がbe動詞の場合について扱う．
	 * @param chunks 対象となる英文のチャンク
	 * @param vpIndex 動詞の含まれているチャンク
	 * @return
	 */
	private boolean detectBeError(Tree[] chunks, int vpIndex) {
		Tree[] poss = chunks[vpIndex].getChildNodes();
		Tree pos = poss[0];

		//TODO 消すか？
		if (poss.length > 1 && pos.node.equals("RB")) {
			if (poss[1].node.indexOf("VB") != 0) {
				pos = poss[1];
				return false;
			}
		} else if (pos.node.indexOf("VB") != 0) {
			return false;
		}

		boolean isPuralV; // 動詞の人称・数
		if (pos.node.equals("VBP")) { // are, am
			isPuralV = true;
		} else if (pos.node.equals("VBZ")) { // is
			isPuralV = false;
		} else if (pos.node.equals("VBD")) { // was, were
			isPuralV = !pos.getChildNodes()[0].node.equals("was"); // wereならtrue

			if (chunks[vpIndex - 1].getLeftmostLeaf().node.equals("I")) { // I was の場合は例外
				chunks[vpIndex].setError(isPuralV);
				return false;
			}
		} else {
			return false;
		}

		int stat = -1;
		stat = isPuralSubject(chunks, vpIndex, isThere(chunks, vpIndex)); // 主語の人称・数の取得
		if (stat == -1) { // 主語の人称・数がよくわからない場合は何もしない
			return false;
		}

		boolean isPuralN = stat == 1? true: false; // 主語の人称・数

		if (isPuralN != isPuralV) { // 主語-動詞の人称・数が一致していない場合，誤りとして検出
			chunks[vpIndex].setError(true);
		}

		return false;
	}

	/**
	 * chunks[vpIndex]に含まれる動詞が疑問文に関係しているかどうか検出する．
	 * 具体的には，Do/Does + 主語 + 動詞のような文型であり，vpIndexの示す動詞が原形でなければならない場合，trueを返す．
	 * @param chunks 対象となる英文のチャンク
	 * @param vpIndex 動詞の含まれているチャンク
	 * @return 疑問文の場合true, それ以外の場合falseを返す．
	 */
	private boolean isInterrogation(Tree[] chunks, int vpIndex) {
		if (vpIndex < 2) { // vpIndex < 2の場合，疑問文の形をとれないためfalse
			return false;
		}

		int i = -1;
		boolean isNP = false;
		for (i = vpIndex - 1; i >= 0; i--) { // 主語部分を読み飛ばす
			if (isNP && (chunks[i].node.equals("VP") || chunks[i].node.equals("ADVP") || chunks[i].node.equals("SQ"))) {
				// VP/ADVP/SQ + NP + (PP + NP ...) + VPとなった場合，VP/ADVP/SQを次の処理へ
				break;
			} else if (!isNP && (chunks[i].node.equals("VP") || chunks[i].node.equals("ADVP"))) {
				return false; // 主語がうまく特定できなかった場合
			} else if (chunks[i].node.equals("NP")) {
				 // 主語の可能性があるNP
				 isNP = true;
				continue;
			} else if (chunks[i].node.equals("PP")) {
				// 主語が修飾されている場合 (ex. NP + PP + NP)
				isNP = false;
				continue;
			}
			return false; // それ以外の場合は扱わない
		}

		if (i < 0) {
			// vpIndexの示すVPの前に，疑問文を連想させるようなチャンクがなかった場合
			return false;
		}

		Tree[] pos = chunks[i].getChildNodes();
		if (pos.length == 1) {
			if (pos[0].node.equals("MD")) { // 助動詞があった
				return true;
			}

			String word = pos[0].getChildNodes()[0].node;
			if (word.equalsIgnoreCase("do") || word.equalsIgnoreCase("does") || word.equalsIgnoreCase("did")) { // doがあった
				return true;
			}
		} else {
			//TODO 後で考える
			return pos[0].node.equals("WRB") && pos[1].node.equals("MD");
		}

		return false;
	}

	/**
	 * There is/are 構文を検出する．
	 * @param chunks 対象となる英文のチャンク
	 * @param vpIndex 動詞の含まれているチャンク
	 * @return There is/are 構文である場合true, それ以外の場合falseを返す．
	 */
	private boolean isThere(Tree[] chunks, int vpIndex) {
		Tree[] poss = chunks[vpIndex-1].getChildNodes();
		if (poss[poss.length-1].node.equals("EX")) { // There/EXが検出された場合
			return true;
		}

		return false;
	}

	/**
	 * 主語の人称・数を判断する．
	 * @param chunks 対象となる英文のチャンク
	 * @param vpIndex 動詞の含まれているチャンク
	 * @param inversion There is 構文のように主語の倒置が起こっている場合はtrue
	 * @return pural:1, 3-rd sing present:0, error:-1
	 */
	private int isPuralSubject (Tree[] chunks, int vpIndex, boolean inversion){
		int npIndex = inversion? vpIndex + 1: vpIndex - 1;

		if (chunks[npIndex].node.equals("ADVP")) {
			npIndex += inversion? 1: -1;
		}

		if (npIndex < 0 || npIndex >= chunks.length) {
			return -1;
		}

		// NP PP NP の順でチャンクが並んでいる場合，最初のNPを主語とみなす．
		if (!inversion) {
			npIndex = getNPIndex(chunks, vpIndex, npIndex);
		}

		//TODO NP以外に主語になりうるチャンクがあれば，追加する．
		if (!chunks[npIndex].node.equals("NP")) {
			return -1;
		}

		// NPの直前に動名詞があれば，検索結果数により判断する．
		if (isVBG(chunks, npIndex)) {
			detectV_AgrError(chunks, npIndex, vpIndex);
			return -1;
		}


		// 名詞が列挙されているか調べる
		if (isNNEnumetation(chunks, npIndex, inversion)) {
			return 1;
		}

		if (isPuralIdiom(chunks, npIndex, inversion)) {
			return 1;
		}

		Tree[] pos = chunks[npIndex].getChildNodes();
		if (pos.length == 1) {
			// 代名詞の場合 (I/you/he/sheなど)
			if (pos[0].node.equals("PRP")) {
				return isPuralPronoun(pos[0].getChildNodes()[0].node)? 1: 0;
			}

			// 代名詞の場合 (These/Thoseなど)
			if (pos[0].node.equals("DT")) {
				return isPuralDT(pos[0].getChildNodes()[0].node)? 1: -1;
			}

			// 名詞の場合
			if (pos[0].node.equals("NN") || pos[0].node.equals("NNP")) {
				return 0;
			}
			if (pos[0].node.equals("NNS") || pos[0].node.equals("NNPS")) {
				return 1;
			}

			return -1;
		}

		// 動名詞が含まれる場合は，検索結果数で判断する．
		if (isVBG (pos)) {
			detectV_AgrError(chunks, npIndex, vpIndex);
			return -1;
		}

		// NPが複数の語からなる場合は，最後の名詞で判断する．
		if (pos[pos.length-1].node.equals("NNS") || pos[pos.length-1].node.equals("NNPS")) {
			return 1;
		}
		if (pos[pos.length-1].node.equals("NN") || pos[pos.length-1].node.equals("NNP")) {
			return 0;
		}
		if (pos[pos.length-1].node.equals("PRP")) {
			return isPuralPronoun(pos[pos.length-1].getChildNodes()[0].node)? 1: 0;
		}

		return -1;
	}

	/**
	 * chunks[vpIndex]の主語になるNPを探す．
	 * @param chunks
	 * @param vpIndex
	 * @param tmpNpIndex
	 * @return
	 */
	private int getNPIndex (Tree[] chunks, int vpIndex, int tmpNpIndex) {
		// 関係代名詞をスキップする
		if (chunks[tmpNpIndex].node.equals("WHNP")) {
			if (tmpNpIndex -1 >= 0 && chunks[tmpNpIndex-1].node.equals("NP")) {
				tmpNpIndex -= 1;
			}
		}

		// 前置詞による修飾をスキップする
		for (int i = tmpNpIndex; i > 1; i--) {
			if (chunks[i-1].node.equals("PP") && chunks[i-2].node.equals("NP")) {
				tmpNpIndex = isPuralIdiom(chunks, i, false)? tmpNpIndex: i-2;
				i--;
			} else {
				break;
			}
		}

		return tmpNpIndex;
	}

	/**
	 * 代名詞の人称・数を判定する．
	 * 厳密には，三人称単数でない代名詞が与えられた場合，trueを返す．
	 * @param prp 検査したい単語
	 * @return
	 */
	private boolean isPuralPronoun (String prp) {
		// 動詞に三単現のsを付ける必要がある場合はfalseを返す。代名詞かどうかの判断は別途する必要がある。
		return !(prp.equalsIgnoreCase("she") || prp.equalsIgnoreCase("he") || prp.equalsIgnoreCase("it") || prp.equalsIgnoreCase("one"));
	}

	/**
	 * 代名詞の人称・数を判定する．ただし，ここでは品詞タグがDTのものを扱う．
	 * @param dt 検査したい単語
	 * @return
	 */
	private boolean isPuralDT (String dt) {
		// These, Thoseの場合trueを返す。
		return (dt.equalsIgnoreCase("these") || dt.equalsIgnoreCase("those"));
	}

	/**
	 * 複数表現のイディオムがあるかどうか判定する．
	 * 現在はa lot ofにのみ対応している．
	 * @param chunks
	 * @param npIndex
	 * @param inversion
	 * @return
	 */
	private boolean isPuralIdiom (Tree[] chunks, int npIndex, boolean inversion) {

		Tree[] words = null;
		if (!inversion && npIndex > 1) {
			Tree[] tmp1 = chunks[npIndex - 2].getLeaves();
			Tree[] tmp2 = chunks[npIndex - 1].getLeaves();
			words = new Tree[tmp1.length + tmp2.length];
			System.arraycopy(tmp1, 0, words, 0, tmp1.length);
			System.arraycopy(tmp2, 0, words, tmp1.length, tmp2.length);
		} else if (inversion && chunks.length - npIndex > 2) {
			Tree[] tmp1 = chunks[npIndex].getLeaves();
			Tree[] tmp2 = chunks[npIndex + 1].getLeaves();
			words = new Tree[tmp1.length + tmp2.length];
			System.arraycopy(tmp1, 0, words, 0, tmp1.length);
			System.arraycopy(tmp2, 0, words, tmp1.length, tmp2.length);
		} else {
			return false;
		}

		// a lot of の検出
		if (words.length > 2) {
			boolean isALotOf = false;
			isALotOf = words[words.length - 3].node.equalsIgnoreCase("a");
			isALotOf &= words[words.length - 2].node.equals("lot");
			isALotOf &= words[words.length - 1].node.equals("of");
			if (isALotOf) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 名詞が列挙されている場合，trueを返す．
	 * @param chunks
	 * @param npIndex
	 * @param inversion
	 * @return
	 */
	private boolean isNNEnumetation (Tree[] chunks, int npIndex, boolean inversion) {

		// チャンクラベルがNP, NP, NPと並んでいた場合は名詞が列挙されていると見なす
		if (inversion? chunks.length - npIndex > 4: npIndex > 3) {
			if (inversion
					&& chunks[npIndex+1].node.equals(",")
					&& chunks[npIndex+2].node.equals("NP")
					&& chunks[npIndex+3].node.equals(",")
					&& chunks[npIndex+4].node.equals("NP")){
				return true;
			}

			if (!inversion
					&& chunks[npIndex-1].node.equals(",")
					&& chunks[npIndex-2].node.equals("NP")
					&& chunks[npIndex-3].node.equals(",")
					&& chunks[npIndex-4].node.equals("NP")){
				return true;
			}
		}

		// チャンクラベルがNP, NPと並んでおり，最後のNPが複数の名詞を含んでおり，それらの名詞がandで分割されている場合
		if (inversion? chunks.length - npIndex > 3: npIndex > 2) {
			if (inversion && chunks[npIndex+1].node.equals(",") && chunks[npIndex+2].node.equals("NP") && isNNEnumetation(chunks[npIndex+2])) {
				return true;
			}

			if (!inversion && chunks[npIndex-1].node.equals(",") && chunks[npIndex-2].node.equals("NP") && isNNEnumetation(chunks[npIndex])) {
				return true;
			}
		}

		// NPが複数の名詞を含んでおり，それらの名詞がandで分割されている場合
		return isNNEnumetation(chunks[npIndex]);
	}

	// とりあえず動く、
	private boolean isNNEnumetation (Tree np) {
		boolean isNN = false;
		boolean isAnd = false;

		Tree[] pos = np.getChildNodes();
		for (int i = 0; i < pos.length; i++) {
			if (pos[i].node.indexOf("NN") == 0 || pos[i].node.indexOf("PRP") == 0)
				isNN = true;

			if (pos[i].node.equals("CC") && isNN)
				if (pos[i].getLeftmostLeaf().node.equalsIgnoreCase("and"))
					isNN = !(isAnd = true);
		}

		return isNN && isAnd;
	}

	/**
	 * 動名詞っぽい単語が含まれているか調べる．
	 * @param tree posノード
	 * @return
	 */
	private boolean isVBG (Tree[] pos) {
		return pos[0].getLeftmostLeaf().node.endsWith("ing");
	}

	/**
	 * chunks[npIndex - 1]が動詞+ingで終わっている場合trueを返す．
	 * すなわち，chunks[npIndex - 1]が[VP *\/VBG]と等しい場合trueを返す．
	 * @param chunks phraseノード
	 * @param npIndex
	 * @return
	 */
	private boolean isVBG (Tree[] chunks, int npIndex) {
		if (npIndex < 1) {
			return false;
		}
		if (chunks[npIndex-1].node.equals("VP")){
			Tree[] t = chunks[npIndex-1].getChildNodes();
			return t.length == 1 && t[0].node.equals("VBG");
		}

		return false;
	}

	/**
	 * 検索結果数に基づき，主語-動詞の数・人称の一致に関する誤りを検出する．
	 * @param chunks
	 * @param npIndex
	 * @param vpIndex
	 * @return
	 */
	private boolean detectV_AgrError(Tree[] chunks, int npIndex, int vpIndex) {
		Tree pos = chunks[vpIndex].getChildNodes()[0];
		if (pos.node.indexOf("VB") != 0) {
			return false;
		}

		String word = pos.getChildNodes()[0].node;
		if (isBe(word)) {
			return detectBeError(chunks, npIndex, vpIndex);
		}

		String np = null;
		String vp = null;

		String raw = null;	// 原文から生成した検索クエリ
		String sug = null;	// 修正候補を含む検索クエリ

		np = expandNP(chunks, npIndex); //chunks[npIndex].getLeaves();

		if (pos.node.equals("VB") || pos.node.equals("VBP")) {
			String vbz = toVBZ(word);
			vp = chunks[vpIndex].getLeavesString();

			raw = np + ' ' + vp;
			sug = np + ' ' + vbz + vp.substring(word.length());
		}

		if (pos.node.equals("VBZ")) {
			Morphology m = new Morphology();
			String stem = m.lemma(word, "VBZ");
			vp = chunks[vpIndex].getLeavesString();

			raw = np + ' ' + vp;
			sug = np + ' ' + stem + vp.substring(word.length());
		}

		if (raw != null) {
			if (!isCorrectExp(new String[]{raw, sug})) {
				chunks[vpIndex].setError(true);
			}
		}


		return false;
	}

	private String expandNP (Tree[] chunks, int npIndex) {
		String np = chunks[npIndex].getLeavesString();

		if (npIndex < 1) {
			return np;
		}

		if (chunks[npIndex-1].node.equals("VP")) {
			Tree[] pos = chunks[npIndex-1].getChildNodes();
			if (pos.length == 1 && pos[0].node.equals("VBG")) {
				return chunks[npIndex-1].getLeavesString() + ' ' + np;
			}
		}


		return np;
	}

	private boolean detectBeError (Tree[] chunks, int npIndex, int vpIndex) {
		Tree pos = chunks[vpIndex].getChildNodes()[0];
		String word = pos.getChildNodes()[0].node;

		Tree[] tmp = chunks[npIndex].getChildNodes();

		if (tmp.length == 1 && tmp[0].node.equals("PRP")) {
			return false;
		}

		String np = expandNP(chunks, npIndex); //chunks[npIndex].getLeaves();
		String vp = chunks[vpIndex].getLeavesString();


		String be = null;

		if (pos.node.equals("VBP")) {
			be = "is";
		} else if (pos.node.equals("VBZ")) {
			be = "are";
		} else if (pos.node.equals("VBD")) {
			be = pos.getChildNodes()[0].node.equals("was")? "were": "was";
		} else {
			return false;
		}

		String raw = null;	// 元の文から作った検索クエリ
		String sug = null;	// 修正候補を含む検索クエリ

		if (tmp.length == 1 && tmp[0].node.equals("EX")) {
			//There is/are の場合
			if (vpIndex < chunks.length-1) {
				np = chunks[vpIndex+1].getLeavesString();
				raw = Sentence.catWords(vp, np);
				sug = Sentence.catWords(be + vp.substring(word.length()), np);
			} else {
				return false;
			}

		} else {
			raw = Sentence.catWords(np, vp);
			sug = np + ' ' + be + vp.substring(word.length());
		}

		if (!isCorrectExp(new String[]{raw, sug})) {
			chunks[vpIndex].setError(true);
		}

		return false;
	}

	/**
	 * 各クエリを用いて検索結果数を取得し，比較することによって，正しい表現であるか調べる．
	 * 最初のクエリによる検索結果数を基準として，それより大きい検索結果数が得られた場合，正しい表現ではないと判断する．
	 * @param queries
	 * @return 先頭のクエリが正しい表現の場合true，そうでない場合falseを返す．
	 */
	private boolean isCorrectExp (String[] queries) {
		if (queries == null) {
			return true;
		}

		SearchTotal[] st = new SearchTotal[queries.length];

		for (int i = 0; i < queries.length; i++) {
			st[i] = new SearchTotal(queries[i], true);
			st[i].start();
		}

		for (int i = 0; i < queries.length; i++) {
			try {
				st[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		double max = st[0].getTotalResults();

		for (int i = 0; i < queries.length; i++) {
			System.out.printf("%sの検索結果数：%10.0f件%n", queries[i], st[i].getTotalResults());
		}

		for (int i = 1; i < queries.length; i++) {
			if (max < st[i].getTotalResults()) {
				return false;
			}
		}

		return true;
	}

	/*
	private boolean detectV_TnsError() {
		return false;
	}
	*/

	/**
	 * be動詞の検出
	 * @param vb 検査したい単語
	 * @return vbがbe動詞の場合trueを返す
	 */
	private boolean isBe (String vb) {
		String lvb = vb.toLowerCase();
		return lvb.equals("be") || lvb.equals("am") || lvb.equals("are") || lvb.equals("is") || lvb.equals("was")
				|| lvb.equals("were") || lvb.equals("been") || lvb.equals("'m")  || lvb.equals("'s") || lvb.equals("'re");
	}

	/**
	 * 与えられた動詞の三人称単数現在形を得る
	 * @param vb 動詞原形
	 * @return vbの三人称単数現在形
	 */
	private String toVBZ (String vb) {
		if (vb.equals("have")) {
			return "has";
		}

		if (vb.endsWith("s") || vb.endsWith("sh")|| vb.endsWith("ch")|| vb.endsWith("x") || vb.endsWith("z")) {
			return vb + "es";
		}

		if (vb.endsWith("o") && !(vb.endsWith("ao") || vb.endsWith("eo")|| vb.endsWith("io")|| vb.endsWith("oo")|| vb.endsWith("uo"))) {
			return vb + "es";
		}

		if (vb.endsWith("y") && !(vb.endsWith("ay") || vb.endsWith("ey")|| vb.endsWith("iy")|| vb.endsWith("oy")|| vb.endsWith("uy"))) {
			return vb.substring(0, vb.length()-1) + "ies";
		}

		return vb + "s";
	}

	/**
	 * あらゆる誤りの検出
	 * @param sentence
	 */
	public void detectAllError(SentenceSE sentence) {
		// 検索結果数を取得する．
		searchTotalNumber(sentence);

		// 英文が誤りを含んでいるかどうかを判断する
		String feature = makeFeatureForSentence(sentence);
		String file = sentence.id + ".f1";
		String rfile = file + ".r1";

		File featureFile = writeFile(file, feature);

		String[] results = callSVM(file, isLog? rfile: null, "model1");

		if (!isLog) {
			featureFile.delete();
		}

		if (!hasError (results)) { // 英文が誤りを含むかどうかの判定
			return;
		}

		// 英文中の誤っている単語を検出する．
		feature = makeFeatureForWords(sentence);
		file = sentence.id+".f2";
		rfile = file + ".r2";

		featureFile = writeFile(file, feature);
		results = callSVM(file, isLog? rfile: null, "model2");

		detectIncorrectWord(sentence, results);

		if (!isLog) {
			featureFile.delete();
		}
	}

	// ファイルに書き込み
	private File writeFile (String name, String body) {
		File file = new File(name);
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.print(body);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	private String[] callSVM (String file, String outFile, String model) {
		Runtime rt = Runtime.getRuntime();
		String result;
		ArrayList<String> resultsList = new ArrayList<String>();

		try {
			// SVMを用いて，英文が誤りを含むか判定
			String call = "svm_classify -V \"" + file + "\" " + model;
			//String call = "svm_classify -V \"" + file + "\" modelsR";
			//System.out.println(call);
			Process p = rt.exec(call);

			InputStream is = p.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    StringBuilder r = new StringBuilder();

		    System.out.println();
		    System.out.println(model.equals("model1")? "SVMによる誤り文検出結果": "SVMによる誤り単語検出結果");
			while ((result = br.readLine()) != null) { // SVMの結果受け取り
				resultsList.add(result);
				if (outFile != null) {
					r.append(result);
					r.append("\n");
				}

				if (result.charAt(0) == '1') {
					System.out.println(result);
				}
		    }
			System.out.println();

			if (outFile != null) {
				result = new String(r);
				writeFile(outFile, result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (String[]) resultsList.toArray(new String[0]);
	}

	/**
	 * SVMの結果より英文が誤りを含むかどうか判定する
	 * @param results SVMの出力
	 * @return
	 */
	private boolean hasError (String[] results) {
		Pattern p = Pattern.compile("^[\\+-]?1\\s+-");
		for (String line: results) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * SVMの結果より英文中の誤っている単語を検出する．
	 * @param sentence
	 * @param results SVMの出力
	 */
	private void detectIncorrectWord(SentenceSE sentence, String[] results) {
		Pattern p = Pattern.compile("^[\\+-]?1\\s+-");
		for (int i = 0; i < sentence.length; i++) {
			Matcher m = p.matcher(results[i]);

			if (m.find()) {
				sentence.setIsError(true, i);
			} else {
				sentence.setIsError(false, i);
			}
		}
	}

	/**
	 * 英文が誤りを含むかどうか判定するための素性を生成する．
	 * @param sentence
	 * @return
	 */
	private String makeFeatureForSentence(SentenceSE sentence) {
		return makeFeatureForSentence(sentence, false);
	}

	/**
	 * 英文が誤りを含むかどうか判定するための素性を生成する．
	 * @param sentence
	 * @param isTraining trueのときはトレーニングデータを作成する．
	 * @return
	 */
	public String makeFeatureForSentence(SentenceSE sentence, boolean isTraining) {
		int $l= sentence.length;
		//int $N = 4;
	    $l -= 2;
		//int $ll = $l + 1;
		StringBuilder feature = new StringBuilder();

	    for (int $i = 0; $i < $l; $i++) {
	        int $id    = 1;
	        double $elem = 0;

	        if (isTraining) {
	        	boolean isE = false;
	        	for (int $j = $i; $j < $i+4 && $j < $l; $j++) {
	        		isE |= sentence.getIsError($j);
	        	}
	        	feature.append(isE? "-1": "1");
	        } else {
	        	feature.append("1");
	        }

			$elem = calcMIScoreN(3, $i, sentence);
			feature.append(printFeature ($elem, $id++));

			$elem = calcMIScore2( 4, $i, 1, sentence );
			feature.append(printFeature ($elem, $id++));

			$elem = calcMIScore2( 4, $i, 3, sentence );
			feature.append(printFeature ($elem, $id++));

			feature.append("\n");
	    }
		return new String(feature);
	}

	/**
	 * 英文中の誤っている単語の検出に用いる素性を生成する．
	 * @param sentence
	 * @return
	 */
	private String makeFeatureForWords(SentenceSE sentence) {
		return makeFeatureForWords(sentence, false);
	}

	/**
	 * 英文中の誤っている単語の検出に用いる素性を生成する．
	 * @param sentence
	 * @param isTraining trueのときトレーニングデータを生成する．
	 * @return
	 */
	public String makeFeatureForWords(SentenceSE sentence, boolean isTraining) {
		int $l = sentence.length;
		StringBuilder feature = new StringBuilder();

	    for (int $i = 0; $i < $l; $i++) {
	        int $id    = 1;

	        if (isTraining) {
	        	feature.append(sentence.getIsError($i)? "-1": "1");
	        } else {
	        	feature.append("1");
	        }

	        // SVMに与える素性
	        double $elem = calcMIScoreN( 2, $i, sentence );    // 1
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 2, $i - 1, sentence );    // 2
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 3, $i, sentence );        // 3
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 3, $i - 1, sentence );    // 4
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 3, $i - 2, sentence );    // 5
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 3, $i, 1, sentence );				// 6
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 3, $i - 2, 2, sentence );	// 7
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 3, $i - 1, 1, sentence ); 			// 8
			feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 3, $i - 1, 2, sentence );			// 9
	        printFeature( $elem, $id++ );

	        $elem = calcMIScoreN( 4, $i, sentence );        // 10
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 4, $i - 1, sentence );    // 11
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 4, $i - 2, sentence );    // 12
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 4, $i - 3, sentence );    // 13
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 4, $i, 1, sentence );     // 14
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 4, $i - 3, 3, sentence );	// 15
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2(4, $i - 1, 1, sentence);          // 16
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 4, $i - 2, 3, sentence );       // 17
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore3( 4, $i - 1, 1, sentence );      // 18
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2(4, $i - 1, 2, sentence);  // 19
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2(4, $i - 2, 2, sentence);  // 20
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore3(4, $i - 2, 2, sentence); 	// 21
			feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 5, $i, sentence );        // 22
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 5, $i - 1, sentence );    // 23
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 5, $i - 2, sentence );    // 24
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 5, $i - 3, sentence );    // 25
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScoreN( 5, $i - 4, sentence );    // 26
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 5, $i, 1, sentence );     // 27
	        feature.append(printFeature( $elem, $id++ ));

	        $elem = calcMIScore2( 5, $i - 4, 4, sentence );	// 28
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 5, $i - 1, 1, sentence ); 			// 29
			feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 5, $i - 3, 4, sentence ); 			// 30
			feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore3(5, $i - 1, 1, sentence);          // 31
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore3(5, $i - 2, 2, sentence);          // 32
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore3(5, $i - 3, 3, sentence);          // 33
	        feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 5, $i - 1, 2, sentence ); 	// 34
			feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 5, $i - 3, 3, sentence ); 	// 35
			feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 5, $i - 2, 3, sentence ); 	// 36
			feature.append(printFeature( $elem, $id++ ));

			$elem = calcMIScore2( 5, $i - 2, 2, sentence ); 	// 37
			feature.append(printFeature( $elem, $id++ ));

	        feature.append("\n");
	    }

		return new String(feature);
	}

	/**
	 * SVMの形式で素性の要素を返す．
	 * <feature>:<value>
	 * @param $value
	 * @param $id
	 * @return
	 */
	private String printFeature (double $value, int $id) {
		if (Double.isNaN($value)) {
			return "";
		}

		// printf WFH "\t%d:%f", $id, $value;
		return "\t" + $id + ":" + $value;
	}

	/**
	 * log10(x)の値を返す．ただし，xが1未満の場合0を返す．
	 * @param num
	 * @return
	 */
	private double log10p1 (double num) {
		return Math.log10((num < 1)? 1: num);
	}

	/**
	 * 検討する英文の$index番目単語を先頭に持つn-gramのMIスコアを計算する．
	 * 期待値はuni-gramの積で見積もる．
	 * @param $n n-gram
	 * @param $index
	 * @param s
	 * @return
	 */
	private double calcMIScoreN (int $n, int $index, SentenceSE s) {
	    int $length = s.length;

	    $length -= $n - 1;

	    if ( $index < 0 || $index > $length - 1 ) { // 文頭，文末処理
	        return Double.NaN;
	    }

		double[] $uniGram = s.getNGramResults(1);
		double[] $nGram = s.getNGramResults($n);
	    double $v = log10p1( $nGram[$index] ); // 分子
	    for ( int $i = 0 ; $i < $n ; $i++ ) { // 分母
	        $v -= log10p1( $uniGram[ $index + $i ] );
	    }

	    return $v;
	}

	/**
	 * 検討する英文の$head番目単語を先頭に持つn-gramのMIスコアを計算する．
	 * 期待値はn-gram中の$sp番目までの単語列と，それ以降の単語列の積で見積もる．
	 * @param $n n-gram
	 * @param $head 検討したいn-gramが元の英文中で出現している位置
	 * @param $sp n-gramを区切る位置．$headからの相対位置
	 * @param s
	 * @return
	 */
	private double calcMIScore2 (int $n, int $head, int $sp, SentenceSE s) {
	    int $length = s.length;


		if ( $head < 0 || $head + $n > $length) { // 文頭，文末処理
			return Double.NaN;
		}

		// エラー処理，もういらないのでそのうち消す
		if ( $sp > $n - 1 ) {
			System.err.println("error: MI::mi2");
		}

		int $seg1 = $head;
		int $seg2 = $head + $sp;
		int $n_1 = $sp;
		int $n_2 = $n - $sp;

		double[] $nGram = s.getNGramResults($n);
		double[] $n_1Gram = s.getNGramResults($n_1);
		double[] $n_2Gram = s.getNGramResults($n_2);

		double $v = log10p1($nGram[$head]); // 分子
		$v -= log10p1($n_1Gram[$seg1]); // 分母
		$v -= log10p1($n_2Gram[$seg2]); // 分母

		return $v;
	}

	/**
	 * 検討する英文の$head番目単語を先頭に持つn-gramのMIスコアを計算する．
	 * 期待値はn-gram中の$sp番目までの単語列，$sp+1番目の単語とそれ以降の単語列の積で見積もる．
	 * @param $n n-gram
	 * @param $head 検討したいn-gramが元の英文中で出現している位置
	 * @param $sp
	 * @param s
	 * @return
	 */
	private double calcMIScore3 (int $n, int $head, int $sp, SentenceSE s) { // N, head, split point from head
		int $length = s.length;

		if ( $head < 0 || $head + $n > $length) { // 文頭，文末処理
			return Double.NaN;
		}

		// エラー処理，そのうち消す
		if ( $sp > $n - 1 ) {
			System.err.println("error: MI::mi3");
		}

		int $seg1 = $head;
		int $seg2 = $head + $sp;
		int $seg3 = $seg2 + 1;
		int $n_1 = $n - ($n - $sp);
		int $n_2 = 1;
		int $n_3 = $n - $n_1 - $n_2;

		double[] $nGram = s.getNGramResults($n);
		double[] $n_1Gram = s.getNGramResults($n_1);
		double[] $n_2Gram = s.getNGramResults($n_2);
		double[] $n_3Gram = s.getNGramResults($n_3);

		double $v = log10p1($nGram[$head]); // 分子
		$v -= log10p1($n_1Gram[$seg1]); // 分母
		$v -= log10p1($n_2Gram[$seg2]); // 分母
		$v -= log10p1($n_3Gram[$seg3]); // 分母

		return $v;
	}

	/**
	 * n-gramまでの検索結果数の取得．
	 * @param sentence
	 */
	public void searchTotalNumber(SentenceSE sentence) {
		ArrayList<String> nGram;
		for (int i = 1; i < N + 1; i++) {
			nGram = sentence.getNGram(i);


			double[] preNGramTotal = null;
			if (i > 1) {
				preNGramTotal = sentence.getNGramResults(i - 1);
			}

			int l = nGram.size();
			SearchTotal[] st = new SearchTotal[l];
			for (int j = 0; j < l; j++) {
				st[j] = new SearchTotal(nGram.get(j), true);
				st[j].run();
			}

			for (int j = 0; j < l; j++) {
				try {
					st[j].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				double r = st[j].getTotalResults();
				if (preNGramTotal != null) {
					if (preNGramTotal[j] < r){
						//research(sentence, i - 1, j, r, 0);
					}
					if (preNGramTotal[j+1] < r) {
						//research(sentence, i - 1, j + 1, r, 0);
					}
					/*
					if (r < 1 && preNGramTotal[j] > 1 && preNGramTotal[j+1] > 1) {
						research(sentence, i, j, 1, 0);
					}
					*/
				}
				sentence.setNGramTotal(i, r, j);
			}

			double[] nGramTotal = sentence.getNGramResults(i);
			for (int j = 0; j < l; j++) {
				System.out.printf("%-30sの検索結果数：%10.0f件%n", nGram.get(j), nGramTotal[j]);
			}
		}
	}

	/*
	// 検索結果数が気に入らない時に使う
	private void research (SentenceSE sentence, int n, int i, double lowest, int cnt) {
		if (cnt > 2) {
			System.err.print("Ignored: Search Results Error\n");
			return;
		}

		String query = sentence.getNGram(n).get(i);
		SearchTotal s = new SearchTotal(query, false);
		s.run();
		try {
			s.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		double r =s.getTotalResults();

		if (r < lowest) {
			try {
				System.err.print("Search again: Please Wait 1sec\n");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			research(sentence, n, i, lowest, cnt+1);
		} else {
		    sentence.setNGramTotal(n, r, i);
		}
	}
	*/
}
