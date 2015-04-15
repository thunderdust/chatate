package liu.weiran.chatate.ui.fragments.book;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import liu.weiran.chatate.R;
import liu.weiran.chatate.ui.activities.book.ReadBookHtmlActivity;
import liu.weiran.chatate.ui.fragments.BaseFragment;
import liu.weiran.chatate.util.book.tdHttpClient;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyBooksFragment extends BaseFragment {

	private View view;// Cache page view
	private GridView bookShelf;
	private String uid;
	private String access_token;
	private final String TAG = "my book fragment";

	// private static String myBookInfo;
	private ArrayList<String> bookNames;
	private ArrayList<String> bookIDs;
	private ArrayList<String> authorNames;
	private ArrayList<Drawable> bookCovers;
	boolean isUserBookDeletedOnServer = false;
	// boolean isServerBookDeletionTaskFinished = false;

	private static int myBookNum = 0;
	private ProgressDialog dialog = null;
	private ShelfAdapter mAdapter;
	private tdHttpClient mHttpClient;
	

	private final float BOOK_TITLE_FONT_SIZE_BIG = 15;
	private final float BOOK_TITLE_FONT_SIZE_MEDIUM = 12;
	private final float BOOK_TITLE_FONT_SIZE_SMALL = 10;
	private final int BOOK_MANAGEMENT_DIALOG_WIDTH = 600;
	private final float BOOK_MANAGEMENT_DIALOG_ALPHA = 0.9f;
	private final float BOOK_MANAGEMENT_DIALOG_DIM = 0.8f;

	private final String GET_READING_LIST_FROM_SERVER_FAIL = "Fail to update user reading list!";
	private final int HANDLER_GET_READING_LIST_INFO_DONE = 2;
	private final int HANDLER_GET_READING_LIST_INFO_FAIL = 3;
	private final int LOADING_DIALOG_TIME_OUT = 4;
	private final long loading_dialog_time_out_duration = 3000l;

	/* Ensure adapter setting is after book data loading finish */
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				dialog.dismiss();
				mAdapter = new ShelfAdapter(getActivity(), bookNames);
				bookShelf.setAdapter(mAdapter);
				break;
			case LOADING_DIALOG_TIME_OUT:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			default:
				break;
			}
		}
	};

	// newInstance constructor for creating fragment
	public static MyBooksFragment newInstance() {
		MyBooksFragment f = new MyBooksFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHttpClient = tdHttpClient.getClientInstance();
		Log.d(TAG, "on create");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Log.d(TAG, "on create view");
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_mybook, container, false);
		}
		// Remove parent if any
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}
		// Dialog ask user to wait
		dialog = ProgressDialog.show(getActivity(), "Loading",
				"Loading contents for you");
		Message timeOutMsg = new Message();
		timeOutMsg.what = LOADING_DIALOG_TIME_OUT;
		handler.sendMessageAtTime(timeOutMsg, loading_dialog_time_out_duration);

		if (getArguments() != null) {
			uid = getArguments().getString("uid");
			Log.i(TAG, uid);
			access_token = getArguments().getString("access_token");
			Log.i(TAG, access_token);
		} else {
			Log.i(TAG, "arguments is null");
		}

		bookNames = new ArrayList<String>();
		bookCovers = new ArrayList<Drawable>();
		bookIDs = new ArrayList<String>();
		authorNames = new ArrayList<String>();
		// Get local & remote user reading books
		updateLocalBooks();
		bookShelf = (GridView) view.findViewById(R.id.book_shelf);
		bookShelf.setLongClickable(true);

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		double diagonalPixels = Math.sqrt((Math.pow(dm.widthPixels, 2) + Math
				.pow(dm.heightPixels, 2)));
		double screenSize = diagonalPixels / (160 * dm.density);
		Log.d(TAG, "getScreenSize() physical size:" + screenSize);

		if (screenSize > 6) {
			// tablet
			Log.d(TAG, "Tablet size");
			bookShelf.setNumColumns(4);
		} else {
			// phone
			Log.d(TAG, "Phone size");
			bookShelf.setNumColumns(3);
		}

		// Click item -> open book
		bookShelf.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				String bookID = bookIDs.get(index);
				String bookTitle = bookNames.get(index);

				if (index >= myBookNum) {

				} else {
					// Intent openBookIntent = new
					// Intent(getActivity(),ReadBookActivity.class);
					Intent openBookHtmlIntent = new Intent(getActivity(),
							ReadBookHtmlActivity.class);
					openBookHtmlIntent.putExtra("bid", bookID);
					openBookHtmlIntent.putExtra("uid", uid);
					openBookHtmlIntent.putExtra("access_token", access_token);
					openBookHtmlIntent.putExtra("book name", bookTitle);
					startActivity(openBookHtmlIntent);
				}
			}
		});

		// Long press item -> delete/favorite dialog
		bookShelf.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int index, long arg3) {

				final String bookID = bookIDs.get(index);
				final String bookTitle = bookNames.get(index);
				Dialog bookManageDialog = new Dialog(getActivity());
				bookManageDialog.setTitle("Manage this book");
				bookManageDialog.setCanceledOnTouchOutside(true);
				bookManageDialog.setContentView(R.layout.dialog_book_manage);

				Window mWindow = bookManageDialog.getWindow();
				WindowManager.LayoutParams lp = mWindow.getAttributes();
				lp.alpha = BOOK_MANAGEMENT_DIALOG_ALPHA;
				lp.dimAmount = BOOK_MANAGEMENT_DIALOG_DIM;
				lp.width = BOOK_MANAGEMENT_DIALOG_WIDTH;
				mWindow.setAttributes(lp);

				Button deleteBtn = (Button) bookManageDialog
						.findViewById(R.id.btn_delete);
				Drawable iconDelete = getResources().getDrawable(
						R.drawable.icon_delete);
				deleteBtn.setCompoundDrawablesWithIntrinsicBounds(iconDelete,
						null, null, null);
				deleteBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						boolean isDeleted = false;
						try {
							isDeleted = deleteALocalBook(bookTitle, bookID);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (isDeleted) {
							bookNames.remove(index);
							bookIDs.remove(index);
							bookCovers.remove(index);
							mAdapter.notifyDataSetChanged();
							Toast.makeText(getActivity(), "Deletion completed",
									Toast.LENGTH_SHORT).show();
						} else {
							Log.e(TAG, "Book deletion failed!");
						}
					}
				});
				Button favoriteBtn = (Button) bookManageDialog
						.findViewById(R.id.btn_add_to_favorite);
				Drawable iconFavorite = getResources().getDrawable(
						R.drawable.icon_favorite);
				favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(
						iconFavorite, null, null, null);
				favoriteBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

					}
				});
				bookManageDialog.show();
				return true;
			}
		});
		return view;
	}

	// Public for use in bookshelf activity
	public void updateLocalBooks() {

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				updateLocalBookInfoByScanningDisk();
				getBookCovers(myBookNum);
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		}, 1500L);
	}

	private void updateLocalBookInfoByScanningDisk() {

		/* Reset data for reloading */
		bookNames.clear();
		bookIDs.clear();
		authorNames.clear();
		bookCovers.clear();
		Log.d(TAG, "Updating");
		File[] fileList = getLocalBookFiles();
		if (fileList != null) {
			myBookNum = fileList.length;
			Log.d(TAG, "book number:" + myBookNum);
		} else {
			myBookNum = 0;
		}

		for (int i = 0; i < myBookNum; i++) {
			// book index is in between "- -" of the file name
			String fileName = fileList[i].getName();
			// Log.d(TAG,"fileName:"+fileName);
			String bookIndex = fileName.split("-")[0];
			String bookName = fileName.split("-")[1];
			String authorName = fileName.split("-")[2];
			if (authorName == null) {
				authorName = "Unknown";
			}
			bookNames.add(bookName);
			bookIDs.add(bookIndex);
			authorNames.add(authorName);
		}
	}

	private class deleteUserBookOnServerTask extends
			AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... bookID) {
			try {
				isUserBookDeletedOnServer = mHttpClient.deleteUserBook(
						bookID[0], access_token, uid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// Delete a book from local storage -> update user's library on server side
	private boolean deleteALocalBook(String bookName, final String bookID)
			throws TimeoutException, InterruptedException, ExecutionException {

		// delete from server side first
		// use global boolean <isUserBookDeletedOnServer> to indicate if
		// deletion is successful
		deleteUserBookOnServerTask newDeletionTask = new deleteUserBookOnServerTask();
		newDeletionTask.execute(bookID);
		newDeletionTask.get(1000, TimeUnit.MILLISECONDS);
		if (isUserBookDeletedOnServer) {
			Log.d(TAG, "book on server is deleted");
			if (Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				String storagePath = Environment.getExternalStorageDirectory()
						.getPath() + "/chatate/Books/";
				String bookPath = storagePath + bookID + "-" + bookName;
				File bookFile = new File(bookPath);
				boolean isDeleted = bookFile.delete();
				return isDeleted;

			} else {
				Log.e(TAG, "No SD card found");
				Toast.makeText(getActivity(), "Error: No SD card found",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} else {
			return false;
		}
	}

	private File[] getLocalBookFiles() {
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			String storagePath = Environment.getExternalStorageDirectory()
					.getPath() + "/chatate/Books/";
			File folder = new File(storagePath);
			File[] fileList = folder.listFiles();
			return fileList;
		} else {
			return null;
		}
	}

	class ShelfAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<String> bookNames;

		public ShelfAdapter(Context context, ArrayList<String> bookNames) {
			this.context = context;
			this.bookNames = bookNames;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return bookNames.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {
			// TODO Auto-generated method stub

			contentView = LayoutInflater.from(
					getActivity().getApplicationContext()).inflate(
					R.layout.gridview_item_book, null);

			TextView tView = (TextView) contentView
					.findViewById(R.id.bookCoverTextView);
			ImageView iView = (ImageView) contentView
					.findViewById(R.id.bookCoverImageView);
			tView.setTextColor(Color.WHITE);

			// Log.i(TAG, "position:" + Integer.toString(position));
			if (bookNames.size() > position) {
				String bookTitle = bookNames.get(position);
				setTitleTextSize(tView, bookTitle);

				if (bookTitle != null) {
					tView.setText(bookTitle);
				} else {
					tView.setText("Loading");
				}
				if (bookCovers.get(position) != null) {
					iView.setBackgroundDrawable(bookCovers.get(position));
				} else {
					iView.setBackgroundResource(R.drawable.default_cover);
				}
			} else {
				contentView.setClickable(false);
				contentView.setVisibility(View.INVISIBLE);
			}
			return contentView;
		}
	}

	private void setTitleTextSize(TextView titleTextView, String title) {

		if (title.length() < 30) {
			titleTextView.setTextSize(BOOK_TITLE_FONT_SIZE_BIG);
		} else if (title.length() < 50) {
			titleTextView.setTextSize(BOOK_TITLE_FONT_SIZE_MEDIUM);
		} else {
			titleTextView.setTextSize(BOOK_TITLE_FONT_SIZE_SMALL);
		}
	}

	private void getBookCovers(int number) {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String rootPath = Environment.getExternalStorageDirectory()
					.getPath();
			String applicationFolderPath = rootPath + "/chatate";
			String pictureFilePath = applicationFolderPath + "/Cache/Pictures/";
			for (int i = 0; i < number; i++) {
				String bookCoverImagePath = pictureFilePath + bookIDs.get(i)
						+ "-cover.jpg";
				bookCovers.add(Drawable.createFromPath(bookCoverImagePath));
			}
		}
	}
}
