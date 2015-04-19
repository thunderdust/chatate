package liu.weiran.chatate.util.book;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liu.weiran.chatate.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class bookHtmlDownloader {

	private static bookHtmlDownloader downloaderInstance;
	private static String TAG = "bookDownloader";
	private tdHttpClient mHttpClient;
	private String[] bookHtmlPages;
	private int pageCount;
	private StringBuffer bookContentWhole;

	/* Script reference and meta information */

	private final String RANGY_REF1 = "<script src='rangy-core.js'></script>";
	private final String RANGY_REF2 = "<script src='rangy-serializer.js'></script>";
	private final String RANGY_REF3 = "<script src='rangy-textrange.js'></script>";
	private final String RANGY_REF4 = "<script src='rangy-highlighter.js'></script>";
	private final String RANGY_REF5 = "<script src='rangy-cssclassapplier.js'></script>";
	private final String RANGY_REF6 = "<script src='rangy-selectionsaverestore.js'></script>";
	private final String SCRIPT_ANNOTATION = "<script type=\"text/javascript\" src=\"annotation.js\"></script>";

	private final String RANGY_REF1_SRC = "rangy-core.js";
	private final String RANGY_REF2_SRC = "rangy-serializer.js";
	private final String RANGY_REF3_SRC = "rangy-textrange.js";
	private final String RANGY_REF4_SRC = "rangy-highlighter.js";
	private final String RANGY_REF5_SRC = "rangy-cssclassapplier.js";
	private final String RANGY_REF6_SRC = "rangy-selectionsaverestore.js";
	private final String SCRIPT_ANNOTATION_SRC = "annotation.js";

	// 'Synchronized' is to ensure there is only one instance
	public static synchronized bookHtmlDownloader getDownloaderInstance() {
		if (downloaderInstance == null) {
			downloaderInstance = new bookHtmlDownloader();
		}
		return downloaderInstance;
	}

	public void downloadBookHtml(Context context, String uid, String bookIndex,
			String bookName, String author, String access_token)
			throws JSONException, Exception {

		mHttpClient = tdHttpClient.getClientInstance();
		bookContentWhole = new StringBuffer();

		JSONObject bookObject = new JSONObject(mHttpClient.getBookDetails(
				bookIndex, access_token));
		// Log.d(TAG, "book detail:" + bookObject.toString());
		// String code = bookObject.getString("code");
		// String message = bookObject.getString("message");

		pageCount = Integer.parseInt(bookObject.getString("total_page"));
		bookHtmlPages = new String[pageCount];

		for (int i = 0; i < pageCount; i++) {
			// html page index starts with 1
			bookHtmlPages[i] = mHttpClient.getBookHtmlByPage(access_token,
					Integer.toString(i + 1), bookIndex);

			if (bookHtmlPages[i] != null) {
				Log.d(TAG, "Get page" + (i + 1) + "successfully");
				bookHtmlPages[i] = injectScriptReference(bookHtmlPages[i]);
				// Log.d(TAG,bookHtmlPages[i]);
			} else {
				Log.e(TAG, "Get page" + (i + 1) + "failed");
			}
		}
		boolean isBookAddedToReadingList = addBookToReadingList(bookIndex,
				access_token, uid);
		saveBookHtmlIntoDisk(context, bookIndex, bookName, author,
				bookHtmlPages);
	}

	/*
	 * This method inject reference to javascript files into downloaded html
	 * pages
	 */
	@SuppressLint("NewApi")
	private String injectScriptReference(String content)
			throws UnsupportedEncodingException, JSONException {

		Gson gson = new Gson();
		String htmlContent = gson.fromJson(content, String.class);
		Log.d(TAG, "html content converted with Gson:" + htmlContent);

		String finalContent = SCRIPT_ANNOTATION + htmlContent;
		Log.d(TAG, "parsed content:" + finalContent);

		/*
		 * Document doc = Jsoup.parse(htmlContent);
		 * doc.head().appendElement("script").attr("src", RANGY_REF1_SRC);
		 * doc.head().appendElement("script").attr("src", RANGY_REF2_SRC);
		 * doc.head().appendElement("script").attr("src", RANGY_REF3_SRC);
		 * doc.head().appendElement("script").attr("src", RANGY_REF4_SRC);
		 * doc.head().appendElement("script").attr("src", RANGY_REF5_SRC);
		 * doc.head().appendElement("script").attr("src", RANGY_REF6_SRC);
		 * doc.head().appendElement("script").attr("src",
		 * SCRIPT_ANNOTATION_SRC); String html = doc.html(); Log.d(TAG,
		 * "parsed content:" + html);
		 */
		return finalContent;
	}

	private static StringBuffer removeUTFCharacters(String data) {
		Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
		Matcher m = p.matcher(data);
		StringBuffer buf = new StringBuffer(data.length());
		while (m.find()) {
			String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
			m.appendReplacement(buf, Matcher.quoteReplacement(ch));
		}
		m.appendTail(buf);
		return buf;
	}

	private boolean addBookToReadingList(String bookID, String access_token,
			String userID) throws Exception {
		// add this book in reading list on server
		boolean isBookAdded = mHttpClient.addUserBook(bookID, access_token,
				userID);
		if (!isBookAdded) {
			Log.e(TAG, "New book adding on server failed");
		} else {
			Log.d(TAG, "New book adding on server succeeded");
		}
		return isBookAdded;
	}

	private void saveBookHtmlIntoDisk(Context context, String bookIndex,
			String bookName, String author, String[] contentHtmls)
			throws IOException {

		if (Utils.isSDCardMounted()) {
			String storagePath = Environment.getExternalStorageDirectory()
					.getPath();
			if (author.isEmpty() || author.length() == 0 || author == null) {
				author = "unknown";
			}
			String folderPath = storagePath + "/chatate/Books/" + bookIndex
					+ "-" + bookName + "-" + author;
			File folder = new File(folderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			saveBookInfoOnDisk(bookIndex, bookName, author, folderPath);
			// save html by page on disk
			for (int i = 0; i < contentHtmls.length; i++) {

				String htmlFileName = bookIndex + "-" + (i + 1) + ".html";
				File bookHtmlFile = new File(folderPath, htmlFileName);
				Writer htmlWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(bookHtmlFile), "UTF8"));
				htmlWriter.append(contentHtmls[i]);
				htmlWriter.flush();
				htmlWriter.close();
			}
		} else {
			Log.e(TAG, "SD not available");
		}
	}

	private void saveBookInfoOnDisk(String bookIndex, String bookName,
			String author, String folderPath) throws IOException {

		String bookInfoFileName = bookIndex + "-info.txt";
		String bookInfoContent = "book_name=" + bookName + "\n" + "author="
				+ author;
		File bookInfoFile = new File(folderPath, bookInfoFileName);
		FileWriter bookInfoWriter = new FileWriter(bookInfoFile);
		bookInfoWriter.append(bookInfoContent);
		bookInfoWriter.flush();
		bookInfoWriter.close();
	}
}
