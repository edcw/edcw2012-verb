package search;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class SearchTotal extends Search{

	private static Map<String, Double> cache;
	private String raw;
	private boolean useCache = true;

	public boolean isUseCache() {
		return useCache;
	}

	private double total;

	public double getTotalResults () {
		if (useCache) {
			return total;
		}
		return super.getTotalResults();
	}

	static public void setCache (int cacheSize) {
		cache = Collections.synchronizedMap(
				new HashMap<String, Double>(cacheSize));
	}

	/**
	 *
	 * @param cache Collections.synchronizedMap
	 */
	static public void setCahe (Map<String, Double> newCache) {
		cache = newCache;
	}

	public static void outCache(){
		DataOutputStream d = null;
		DataOutputStream s = null;

		try {
			s = new DataOutputStream(new FileOutputStream("cache.txt.log"));
			d = new DataOutputStream(new FileOutputStream("cache.value.log"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		for (Map.Entry<String, Double> m: cache.entrySet()){
			try {
				s.writeChars(m.getKey() + "\n");
				d.writeDouble(m.getValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			d.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public void inCache(){
		DataInputStream d = null;
		DataInputStream s = null;
		try {
			d = new DataInputStream(new FileInputStream("cache.value.log"));
			s = new DataInputStream(new FileInputStream("cache.txt.log"));
		} catch (FileNotFoundException e) {
			// 初回起動は必ず出る．無いなら無いでかまわない．
			System.err.println("Caution: cache.value.log or cache.txt.log dose not exist");
			return;
		}

		double tmp;
		try {
			while ((tmp = d.readDouble()) != -1){
				StringBuffer key = new StringBuffer();
				char c;
				while ((c = s.readChar()) != -1){
					if (c == '\n'){
						break;
					} else {
						key.append(c);
					}
				}
				synchronized (cache){
					cache.put(new String(key), tmp);
				}
			}
		} catch (EOFException e){
			//e.printStackTrace();
			System.err.println("read file");
		} catch (IOException e) {
			e.printStackTrace();
		}


		try {
			d.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	/**
	 * 検索結果数を取得する．
	 * @param total Collections.synchronizedMap
	 */
	public SearchTotal(String query) {
		super(query, 0, 1);
		raw = query;
	}

	/**
	 * 検索結果数を取得する．
	 * @param query 検索クエリ
	 * @param phrase 検索クエリをフレーズとして扱う
	 */
	public SearchTotal(String query, boolean phrase) {
		super(query, 0, 1, phrase);
		raw = query;
		useCache = true;
	}

	/**
	 *
	 * @param query 検索クエリ
	 * @param phrase フレーズ検索
	 * @param unuseCache 過去の検索結果を流用しない
	 */
	public SearchTotal(String query, boolean phrase, boolean useCache) {
		super(query, 0, 1, phrase);
		raw = query;
		this.useCache = useCache;
	}

	public void run(){
		//search();
		//synchronized(cache){
		//	cache.put(raw, Double.valueOf(getTotal()));
		//}

		synchronized (cache) {
			if (useCache &= cache.containsKey(raw)) {
				total = cache.get(raw);
				//System.out.println(raw + ": cache");//TODO 消す
			} /*else {
				search();
				cache.put(raw, Double.valueOf(getTotal()));
			}*/
		}


		if (!useCache) {
			search();
			synchronized(cache){
				cache.put(raw, Double.valueOf(getTotal()));
				//System.out.println(raw + ": search");//TODO 消す
			}
		}

	}

}
