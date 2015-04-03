package liu.weiran.chatate.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import liu.weiran.chatate.R;
import liu.weiran.chatate.base.MyApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class Utils {

	public static final int BYTE_ARRAY_SIZE = 2048;

	public static InputStream getInputStreamFromUrl(String url)
			throws ClientProtocolException, IOException {
		HttpGet getRequest = new HttpGet(url);
		DefaultHttpClient mClient = new DefaultHttpClient();
		HttpResponse response = mClient.execute(getRequest);
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		return is;
	}

	/*
	 * Given a url, this method create a buffered reader which reads content get
	 * from this url.
	 */
	public static BufferedReader urlBufferedReader(String url)
			throws ClientProtocolException, IOException {
		InputStream is = getInputStreamFromUrl(url);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
		return br;
	}

	/*
	 * Given a string representing path of a file, this method return content of
	 * the file in terms of string
	 */
	public static String readFile(String path) throws IOException {
		InputStream is = new FileInputStream(new File(path));
		DataInputStream ds = new DataInputStream(is);
		// Estimated capacity, potential risk ???
		byte[] bytes = new byte[is.available()];
		ds.readFully(bytes);
		String text = new String(bytes);
		is.close();
		ds.close();
		return text;
	}

	public static Bitmap getBitmapFromUrl(String url)
			throws ClientProtocolException, IOException {
		return BitmapFactory.decodeStream(getInputStreamFromUrl(url));

	}

	public static Bitmap getBitmapFromFile(File f) throws FileNotFoundException {
		return BitmapFactory.decodeStream(new BufferedInputStream(
				new FileInputStream(f)));
	}

	public static void inputStreamToFileOutputStream(InputStream is,
			FileOutputStream os) throws IOException {
		byte[] buffer = new byte[BYTE_ARRAY_SIZE];
		int length = 0;
		while ((length = is.read(buffer)) != -1) {
			os.write(buffer, 0, length);
		}
		os.close();
		is.close();
	}

	public static byte[] readStreamToBytes(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] byteArray = new byte[BYTE_ARRAY_SIZE];
		int length = 0;
		while ((length = is.read(byteArray)) != -1) {
			bos.write(byteArray, 0, length);
		}
		bos.close();
		is.close();
		return bos.toByteArray();
	}

	public static Bitmap saveBitmapToLocal(String bitmapUrl, File bitmapFile)
			throws IOException {
		Bitmap bm;
		downloadUncachedFile(bitmapUrl, bitmapFile);
		bm = Utils.getBitmapFromFile(bitmapFile);
		return bm;
	}

	public static void downloadUncachedFile(String url, File targetFile)
			throws IOException {
		if (!targetFile.exists()) {
			downloadFileToLocal(url, targetFile);
		} else {
		}
	}

	public static void downloadFileToLocal(String url, File file)
			throws IOException {
		InputStream is = Utils.getInputStreamFromUrl(url);
		file.createNewFile();
		FileOutputStream os = new FileOutputStream(file);
		Utils.inputStreamToFileOutputStream(is, os);
	}

	public static void createFileFromBytes(byte[] bytes, File file)
			throws IOException {
		FileOutputStream os = new FileOutputStream(file);
		os.write(bytes);
		os.close();
	}

	public static void fixAsyncTaskBug() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				return null;
			}
		}.execute();
	}

	public static void hideSoftInputView(Activity activity) {
		if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			InputMethodManager im = (InputMethodManager) activity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (activity.getCurrentFocus() != null) {
				im.hideSoftInputFromWindow(activity.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	public static boolean isSDCardMounted() {
		return Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED);
	}

	// image size unit converter
	public static int dipToPixel(Context ctx, float dipValue) {
		final float densityScale = ctx.getResources().getDisplayMetrics().density;
		int pixelValue = (int) (dipValue * densityScale + 0.5f);
		return pixelValue;
	}

	public static String format(int cursorPosition) {
		return null;
	}

	public static void clearDirectory(String path) {
		File f = new File(path);
		File[] fileList = f.listFiles();
		for (File file : fileList) {
			file.delete();
		}
	}

	public static void fireAlertDialog(Activity aty, String msg) {
		new AlertDialog.Builder(aty).setMessage(msg).show();
	}

	public static void fireAlertDialog(Activity aty, int stringID) {
		new AlertDialog.Builder(aty).setMessage(aty.getString(stringID)).show();
	}

	public static int getWindowWidthInPixel(Activity aty) {
		return aty.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getWindowHeightInPixel(Activity aty) {
		return aty.getResources().getDisplayMetrics().heightPixels;
	}

	public static long getTimeInLong(String timeString) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SS");
		String base = "00:00:00.00";
		Date baseTime = format.parse(base);
		Date targetTime = format.parse(timeString);
		return targetTime.getTime() - baseTime.getTime();
	}

	public static long getDuration(long beginTime) {
		return System.currentTimeMillis() - beginTime;
	}

	public static long getDuration(String beginTime) throws ParseException {
		return System.currentTimeMillis() - getTimeInLong(beginTime);
	}

	// 265

	// 283
	public static void notify(Context context, String msg, String title,
			Class<?> toClz, int notifyId) {
		PendingIntent pend = PendingIntent.getActivity(context, 0, new Intent(
				context, toClz), 0);
		Notification.Builder builder = new Notification.Builder(context);
		int icon = context.getApplicationInfo().icon;
		builder.setContentIntent(pend).setSmallIcon(icon)
				.setWhen(System.currentTimeMillis()).setTicker(msg)
				.setContentTitle(title).setContentText(msg).setAutoCancel(true);

		NotificationManager man = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		man.notify(notifyId, builder.getNotification());
	}

	public static void cancelNotification(Context ctx, int notifyId) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nMgr = (NotificationManager) ctx
				.getSystemService(ns);
		nMgr.cancel(notifyId);
	}

	public static String getStringByFile(File f) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while ((line = br.readLine()) != null) {
			builder.append(line);
		}
		br.close();
		return builder.toString();
	}

	@SuppressWarnings("deprecation")
	public static String getShortUrl(String longUrl) throws IOException,
			JSONException {
		if (longUrl.startsWith("http") == false) {
			throw new IllegalArgumentException("longUrl must start with http");
		}
		String url = "https://api.weibo.com/2/short_url/shorten.json";
		HttpPost post = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("access_token",
				"2.00_hkjqBR1dbuCc632289355qerfeD"));
		params.add(new BasicNameValuePair("url_long", longUrl));
		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		HttpResponse res = new DefaultHttpClient().execute(post);
		if (res.getStatusLine().getStatusCode() == 200) {
			String str = EntityUtils.toString(res.getEntity());
			JSONObject json = new JSONObject(str);
			JSONArray arr = json.getJSONArray("urls");
			JSONObject urls = arr.getJSONObject(0);
			if (urls.getBoolean("result")) {
				return urls.getString("url_short");
			} else {
				return null;
			}
		}
		return null;
	}

	public static String getGb2312Encode(String s)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "gb2312");
	}

	public static void goActivity(Context cxt, Class<?> cls) {
		Intent intent = new Intent(cxt, cls);
		cxt.startActivity(intent);
	}

	/*
	 * public static void installApk(Context context, String path) { Intent
	 * intent1 = new Intent(); intent1.setAction(Intent.ACTION_VIEW); File file
	 * = new File(path); Log.i("lzw", file.getAbsolutePath());
	 * intent1.setDataAndType(Uri.fromFile(file),
	 * "application/vnd.android.package-archive");
	 * intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 * context.startActivity(intent1); }
	 */

	public static void showInfoDialog(Activity cxt, String msg, String title) {
		AlertDialog.Builder builder = getBaseDialogBuilder(cxt);
		builder.setMessage(msg)
				.setPositiveButton(cxt.getString(R.string.right), null)
				.setTitle(title).show();
	}

	public static Activity modifyDialogContext(Activity cxt) {
		Activity parent = cxt.getParent();
		if (parent != null) {
			return parent;
		} else {
			return cxt;
		}
	}

	public static AlertDialog.Builder getBaseDialogBuilder(Activity ctx) {
		return new AlertDialog.Builder(ctx).setTitle(R.string.tips).setIcon(
				R.drawable.icon_info_2);
	}

	// 381

	// 553
	public static void toast(Context ctx, String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	public static void toast(int id) {
		toast(MyApplication.mCtx, id);
	}

	public static void toast(String s) {
		toast(MyApplication.mCtx, s);
	}

	public static void toast(String s, String exceptionMsg) {
		if (MyApplication.isDebugModeOn) {
			s = s + exceptionMsg;
		}
		toast(s);
	}

	public static void toast(int resId, String exceptionMsg) {
		String s = MyApplication.mCtx.getString(resId);
		toast(s, exceptionMsg);
	}

	public static void toast(Context cxt, int id) {
		Toast.makeText(cxt, id, Toast.LENGTH_SHORT).show();
	}

	public static void toastLong(Context cxt, int id) {
		Toast.makeText(cxt, id, Toast.LENGTH_LONG).show();
	}

	// 580

	public static ProgressDialog showSpinnerDialog(Activity aty) {
		ProgressDialog dialog = new ProgressDialog(aty);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(true);
		dialog.setMessage("Loading");
		if (!aty.isFinishing()) {
			dialog.show();
		}
		return dialog;
	}

	// 611
	public static String uuid() {
		// return UUID.randomUUID().toString().substring(0, 24);
		return myUUID();
	}

	public static String myUUID() {
		StringBuilder sb = new StringBuilder();
		int start = 48, end = 58;
		appendChar(sb, start, end);
		appendChar(sb, 65, 90);
		appendChar(sb, 97, 123);
		String charSet = sb.toString();
		StringBuilder sb1 = new StringBuilder();
		for (int i = 0; i < 24; i++) {
			int len = charSet.length();
			int pos = new Random().nextInt(len);
			sb1.append(charSet.charAt(pos));
		}
		return sb1.toString();
	}

	public static void appendChar(StringBuilder sb, int start, int end) {
		int i;
		for (i = start; i < end; i++) {
			sb.append((char) i);
		}
	}

	// 639
	// cryptographic hash function
	public static String md5(String string) {
		byte[] hash = null;
		try {
			hash = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is not supported", e);
		}
		return computeMD5(hash);
	}

	public static String computeMD5(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input, 0, input.length);
			byte[] md5bytes = md.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < md5bytes.length; i++) {
				String hex = Integer.toHexString(0xff & md5bytes[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isCollectionNotEmpty(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			return true;
		}
		return false;
	}

	// 692
	public static int getColor(int resId) {
		return MyApplication.mCtx.getResources().getColor(resId);
	}

	// 708

	public static boolean isDoubleEqual(double a, double b) {
		return Math.abs(a - b) < 1E-8;
	}

	public static String getFormattedDistance(double distance) {
		if (distance < 1000) {
			int metres = (int) distance;
			return String.valueOf(metres)
					+ MyApplication.mCtx.getString(R.string.metres);
		} else {
			String num = String.format("%.1f", distance / 1000);
			return num + MyApplication.mCtx.getString(R.string.kilometres);
		}
	}

	// 722
	public static void printException(Exception e) {
		if (MyApplication.isDebugModeOn) {
			e.printStackTrace();
		}
	}

	// 728

	public static void alwaysShowMenuItem(MenuItem add) {
		add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	// 733
	public static byte[] getBytesFromBitmap(Bitmap bitmap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] bytes = bos.toByteArray();
		return bytes;
	}
}
