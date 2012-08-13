package detect;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sentence {
	public String id;

	final public String raw; // 原文
	public String pos; 		// POSファイルから読み込んだ品詞タグ，チャンク情報
	public Tree posTree;	// 品詞タグ，チャンク情報を木構造に変換したもの

	static final private  String V_agr = "v_agr";
	//static final private  String V_tns = "v_tns";
	static final private  String Err = "gen";

	private Tree getPOSNode (String[] token, Tree parent, int index) {
		Tree pos, word;

		String[] temp = token[index].split("/");
		pos = new Tree(temp[1], parent);
		word = new Tree(temp[0], pos);
		pos.setChildNode(word);

		return pos;
	}

	private Tree getChunkNode(String[] token, Tree parent, int[] i) {

		Tree chunk = new Tree(token[i[0]++].substring(1), parent);
		ArrayList<Tree> subtree = new ArrayList<Tree>();

		while (token[i[0]].indexOf("]") != 0) {
			if (token[i[0]].indexOf("/") != -1) {
				subtree.add(getPOSNode(token, chunk,  i[0]));
			} else {
				System.err.println(token[i[0]]);
			}
			i[0]++;
		}

		chunk.setChildNodes((Tree[]) subtree.toArray(new Tree[subtree.size()]));

		return chunk;
	}

	private void setPosTree() {
		String[] token = pos.split(" ");

		posTree = new Tree("ROOT", null);
		ArrayList<Tree> subtree = new ArrayList<Tree>();

		for (int[] i = {0}; i[0] < token.length; i[0]++) {
			if (token[i[0]].indexOf("[") == 0) {
				subtree.add(getChunkNode(token, posTree, i));
			} else if (token[i[0]].indexOf("]") == 0) {
				continue;
			} else if (token[i[0]].indexOf("/") != -1) {
				subtree.add(getPOSNode(token, posTree, i[0]));
			} else {
				System.err.println(token[i[0]]);
			}
		}

		posTree.setChildNodes((Tree[]) subtree.toArray(new Tree[subtree.size()]));
	}

	/**
	 * Stanford Parserから得られた結果を，POSファイルから得られる形式に変換する．
	 * @param t
	 */
	public void setPosTree (edu.stanford.nlp.trees.Tree t) {
		if (t == null) {
			return;
		}

		posTree =  new Tree("ROOT");
		ArrayList<Tree> chunks = new ArrayList<Tree>();
		setPosTreeIter(t.flatten().firstChild(), posTree, chunks);
		for (int i = 0; i < chunks.size(); i++) {
			if (chunks.get(i).getNumOfChildNodes() == 0) {
				chunks.remove(i);
				i--;
			}
		}
		posTree.setChildNodes((Tree[])chunks.toArray(new Tree[0]));
	}

	private Tree setPosTreeIter (edu.stanford.nlp.trees.Tree st, Tree parent, ArrayList<Tree> chunks) {

		if (st.isLeaf()) {
			return new Tree(st.value(), parent);
		}

		if (st.isPreTerminal()) {
			Tree pos = new Tree(st.value(), parent);
			pos.setChildNode(setPosTreeIter(st.firstChild(), pos, chunks));
			return pos;
		}

		Tree newNode;
		if (st.value().equals("S")) {
			newNode = parent;
		} else {
			newNode = new Tree(st.value(), parent);
			chunks.add(newNode);
		}

		boolean isPhrase = false;
		for (edu.stanford.nlp.trees.Tree t: st.children()) {
			if (t.isPreTerminal() && !isPhrase){
				if (newNode.node.equals("ROOT")) {
					chunks.add(setPosTreeIter(t, newNode, chunks));
				} else {
					newNode.addChildNode(setPosTreeIter(t, newNode, chunks));
				}
			} else if (t.isPreTerminal() && isPhrase){
				if (!newNode.node.equals("ROOT")) {
					newNode = new Tree(newNode);
					newNode.setChildNodes(null);
					chunks.add(newNode);
					//chunks.add(setPosTreeIter2(t, newNode, chunks));
					newNode.addChildNode(setPosTreeIter(t, newNode, chunks));
					isPhrase = false;
				} else {
					chunks.add(setPosTreeIter(t, newNode, chunks));
				}
			} else {
				setPosTreeIter(t, parent, chunks);
				isPhrase = true;
			}
		}

		return newNode; 	// ダミー
	}

	/**
	 * エラータグを付与した英文を返す．
	 * @return
	 */
	public String sysOut () {
		if (raw == null) {
			return "";
		}

		if (raw.length() == 0 || posTree == null) {
			return raw;
		}

		StringBuilder out = new StringBuilder(raw);
		sysOutIter(out, posTree, countSpace(0, out, false), true, false);
		return out.toString();
	}

	/**
	 * 主にエラータグを付与する．
	 * @param out 出力用の文字列
	 * @param t 品詞タグ，チャンク情報を格納した木
	 * @param i インデックス
	 * @param vAgr trueの場合<v_agr>, falseの場合<gen>を付ける
	 * @param isSymbol 記号などの処理
	 * @return
	 */
	private int sysOutIter (StringBuilder out, Tree t, int i, boolean vAgr, boolean isSymbol) {
		if (t.isError()) {
			String tag = errTag(true, vAgr); // 開始タグ
			out.insert(i, tag);
			i += tag.length();
			i = sysOutIter2(out, t, i, vAgr, isSymbol);

			tag = errTag(false, vAgr); // 終了タグ
			out.insert(i-countSpace(i-1, out, true), tag);
			i += tag.length();
			return i;
		}

		return sysOutIter2(out, t, i, vAgr, isSymbol);
	}

	/**
	 * 主にiの値を進める．
	 * @param out 出力用の文字列
	 * @param t 品詞タグ，チャンク情報を格納した木
	 * @param i インデックス
	 * @param vAgr
	 * @param isSymbol 記号などの処理
	 * @return
	 */
	private int sysOutIter2 (StringBuilder out, Tree t, int i, boolean vAgr, boolean isSymbol) {

		if (t.isLeaf()) {
			if (isSymbol) {
				// 記号の場合の処理．Stanford Parserを使う場合，一部の記号が書き換えられているため
				Pattern p = Pattern.compile("(^[`'\"\\[\\]\\(\\)]+)");
				Matcher m = p.matcher(out.substring(i));

				if (m.find()) {
					int tmp = i + m.group(1).length();
					return tmp + countSpace(tmp, out, false);
				} else {
					System.err.println(" Fatal Error: sysOutIter2");
					System.err.println(t.node + " is not found.");
					System.err.println("raw: " + raw);
					posTree.print();
					return i;
				}

			} else {
				// 普通の単語の場合の処理
				i += t.node.length();
				return i + countSpace(i, out, false);
			}
		}

		if (t.hasLeaf()) {
			// Stanford Parserを使用した場合，書き換えられている記号を検出する．
			if (t.node.equals("``") || t.node.equals("''") || t.node.equals("-LRB-") || t.node.equals("-RRB-")) {
				return sysOutIter(out, t.getLeftmostLeaf(), i, vAgr, true);
			}
		}

		for (Tree c: t.getChildNodes()) {
			i = sysOutIter(out, c, i, vAgr, isSymbol);
		}

		return i;
	}

	protected String errTag (boolean startTag, boolean vAgr) {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		if (!startTag) {
			sb.append("/");
		}

		if (vAgr) {
			sb.append(V_agr);
		} else {
			sb.append(Err);
		}

		sb.append(">");

		return sb.toString();
	}

	/**
	 * indexの前(後)に連続するスペースを数える．
	 * @param index
	 * @param sb sb[index]を基準としてスペースを数える．
	 * @param backward trueの場合，indexから0方向に向かってスペースを数える．
	 * @return
	 */
	protected int countSpace (int index, StringBuilder sb, boolean backward) {
		if (backward) {
			if (sb.lastIndexOf(" ", index) == index && index > -1) {
				return 1 + countSpace(index-1,sb,backward);
			} else {
				return 0;
			}
		} else {
			if (sb.indexOf(" ", index) == index) {
				return 1 + countSpace(index+1,sb,backward);
			} else {
				return 0;
			}
		}
	}

	/**
	 * '(アポストロフィ)を考慮して単語を連結する．
	 * @param w1 前側の単語
	 * @param w2 後ろの単語
	 * @return w1とw2を連結した結果
	 */
	static public String catWords (String w1, String w2) {
		int apos = w2.indexOf('\'');
		if (apos == -1) {
			return w1 + ' ' + w2;
		} else if (apos == 0) {
			return w1 + w2;
		} else if (apos == 1 && w2.indexOf("n't") == 0) {
			return w1 + w2;
		} else {
			return w1 + ' ' + w2;
		}
	}

	/**
	 * '(アポストロフィ)を考慮して単語を連結する．
	 * @param words 連結する単語列
	 * @return wordsの単語を連結した結果
	 */
	static public String catWords (String[] words) {
		StringBuffer sb = new StringBuffer();

		for (String w: words) {
			int apos = w.indexOf('\'');
			if (apos == -1) {
				sb.append(' ');
				sb.append(w);
			} else if (apos == 0) {
				sb.append(w);
			} else if (apos == 1 && w.indexOf("n't") == 0) {
				sb.append(w);
			} else {
				sb.append(' ');
				sb.append(w);
			}
		}

		return sb.toString();
	}

	/**
	 * POSファイルから読み込んだ品詞タグ，チャンク情報を格納し，木構造に変換する．
	 * @param pos POSファイルから読み込んだだ品詞タグ，チャンク情報
	 */
	public void setPOS(String pos) {
		this.pos = pos;
		setPosTree();
	}

	public Sentence(String sentence) {
		raw = sentence;
	}

}
