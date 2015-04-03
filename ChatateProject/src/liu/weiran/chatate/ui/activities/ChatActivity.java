package liu.weiran.chatate.ui.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.Session;
import liu.weiran.chatate.R;
import liu.weiran.chatate.adapters.ChatMessageAdapter;
import liu.weiran.chatate.adapters.EmotionGridAdapter;
import liu.weiran.chatate.adapters.EmoticonPagerAdapter;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.database.DatabaseHelper;
import liu.weiran.chatate.database.DatabaseMessage;
import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.MessageBuilder;
import liu.weiran.chatate.entity.RoomType;
import liu.weiran.chatate.entity.MessageSendCallback;
import liu.weiran.chatate.service.CacheService;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.MessageAgent;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.service.listener.MessageListener;
import liu.weiran.chatate.service.receiver.GroupMessageReceiver;
import liu.weiran.chatate.service.receiver.MessageReceiver;
import liu.weiran.chatate.ui.views.EmotionEditText;
import liu.weiran.chatate.ui.views.RecordButton;
import liu.weiran.chatate.ui.views.xlist.XListView;
import liu.weiran.chatate.util.*;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements OnClickListener,
		MessageListener, XListView.IXListViewListener {
	public static final int LOCATION_REQUEST = 1;
	private static final int TAKE_CAMERA_REQUEST = 2;
	public static final int PAGE_SIZE = 20;
	private static final int GALLERY_REQUEST = 0;
	private static final int GALLERY_KITKAT_REQUEST = 3;
	static String RETRY_ACTION = "liu.weiran.chatate..RETRY_CONNECT";

	private ChatMessageAdapter adapter;
	private List<Message> msgs = new ArrayList<Message>();
	AVUser curUser;
	DatabaseHelper dbHelper;
	public static ChatActivity ctx;

	View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;
	View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn, showAddBtn,
			addLocationBtn, showEmotionBtn;
	LinearLayout chatBottomLayout;
	ViewPager emotionPager;
	private EmotionEditText contentEdit;
	private XListView xListView;
	RecordButton recordBtn;
	private String localCameraPath = PathUtils.getTmpPath();
	private View addCameraBtn;
	int msgSize;

	public static RoomType roomType;
	public static final String CHAT_USER_ID = "chatUserId";
	public static final String GROUP_ID = "groupId";
	public static final String ROOM_TYPE = "roomType";
	AVUser chatUser;
	Group group;
	String audioId;
	MessageAgent MessageAgent;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.chat_layout);
		findView();
		initEmotionPager();
		initRecordBtn();
		setEditTextChangeListener();
		initListView();
		// set soft input keyboard mode
		setSoftInputMode(InputMethodManager.SHOW_IMPLICIT);
		initByIntent(getIntent());
	}

	private void initByIntent(Intent intent) {
		initData(intent);
		loadMsgsFromDB(true);
		ChatService.cancelNotification(ctx);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initByIntent(intent);
	}

	private void initListView() {
		adapter = new ChatMessageAdapter(ctx, msgs);
		adapter.setDatas(msgs);
		xListView.setAdapter(adapter);
		xListView.setPullRefreshEnable(true);
		xListView.setPullLoadEnable(false);
		xListView.setXListViewListener(this);
		xListView.setOnScrollListener(new PauseOnScrollListener(
				UserService.imageLoader, true, true));
	}

	private void initEmotionPager() {
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < 2; i++) {
			views.add(getEmotionGridView(i));
		}
		EmoticonPagerAdapter pagerAdapter = new EmoticonPagerAdapter(views);
		emotionPager.setAdapter(pagerAdapter);
	}

	private View getEmotionGridView(int pos) {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		View emotionView = inflater.inflate(R.layout.chat_emotion_gridview,
				null);
		GridView gridView = (GridView) emotionView.findViewById(R.id.gridview);
		final EmotionGridAdapter emotionGridAdapter = new EmotionGridAdapter(
				ctx);
		List<String> pageEmotions;
		if (pos == 0) {
			pageEmotions = EmoticonUtils.emotionTexts1;
		} else {
			pageEmotions = EmoticonUtils.emotionTexts2;
		}
		emotionGridAdapter.setDatas(pageEmotions);
		gridView.setAdapter(emotionGridAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String emotionText = (String) parent.getAdapter().getItem(
						position);
				int start = contentEdit.getSelectionStart();
				StringBuffer sb = new StringBuffer(contentEdit.getText());
				sb.replace(contentEdit.getSelectionStart(),
						contentEdit.getSelectionEnd(), emotionText);
				contentEdit.setText(sb.toString());
				CharSequence info = contentEdit.getText();
				if (info instanceof Spannable) {
					Spannable spannable = (Spannable) info;
					Selection.setSelection(spannable,
							start + emotionText.length());
				}
			}
		});
		return gridView;
	}

	public void initRecordBtn() {
		setNewRecordPath();
		recordBtn
				.setOnFinishedRecordListener(new RecordButton.RecordEventListener() {
					@Override
					public void onFinishedRecord(final String audioPath,
							int secs) {
						final String objectId = audioId;
						if (isStateFine()) {
							MessageAgent.createAndSendMsg(
									new MessageAgent.MessageBuilderHelper() {

										@Override
										public void specifyType(
												MessageBuilder msgBuilder) {
											msgBuilder.audio(objectId);
										}
									}, sendCallback);
						}
						setNewRecordPath();
					}

					@Override
					public void onStartRecord() {
					}
				});
	}

	public void setNewRecordPath() {
		audioId = Utils.uuid();
		String audioPath = PathUtils.getChatFilePath(audioId);
		recordBtn.setSavePath(audioPath);
	}

	public void setEditTextChangeListener() {
		contentEdit.addTextChangedListener(new SimpleTextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0) {
					sendBtn.setEnabled(true);
					showSendBtn();
				} else {
					sendBtn.setEnabled(false);
					showTurnToRecordBtn();
				}
				super.onTextChanged(s, start, before, count);
			}
		});
	}

	private void showTurnToRecordBtn() {
		sendBtn.setVisibility(View.GONE);
		turnToAudioBtn.setVisibility(View.VISIBLE);
	}

	private void showSendBtn() {
		sendBtn.setVisibility(View.VISIBLE);
		turnToAudioBtn.setVisibility(View.GONE);
	}

	void initActionBar() {
		String title;
		if (roomType == RoomType.Single) {
			title = chatUser.getUsername();
		} else {
			title = getChatGroup().getGroupDesc();
		}
		initActionBar(title);
	}

	private void findView() {
		xListView = (XListView) findViewById(R.id.listview);
		addImageBtn = findViewById(R.id.addImageBtn);

		contentEdit = (EmotionEditText) findViewById(R.id.textEdit);
		chatTextLayout = findViewById(R.id.chatTextLayout);
		chatAudioLayout = findViewById(R.id.chatRecordLayout);
		chatBottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
		turnToAudioBtn = findViewById(R.id.turnToAudioBtn);
		turnToTextBtn = findViewById(R.id.turnToTextBtn);
		recordBtn = (RecordButton) findViewById(R.id.recordBtn);
		chatTextLayout = findViewById(R.id.chatTextLayout);
		chatAddLayout = findViewById(R.id.chatAddLayout);
		addLocationBtn = findViewById(R.id.addAnnotationBtn);
		chatEmotionLayout = findViewById(R.id.chatEmotionLayout);
		showAddBtn = findViewById(R.id.showAddBtn);
		showEmotionBtn = findViewById(R.id.showEmotionBtn);
		sendBtn = findViewById(R.id.sendBtn);
		emotionPager = (ViewPager) findViewById(R.id.emotionPager);
		addCameraBtn = findViewById(R.id.addCameraBtn);

		sendBtn.setOnClickListener(this);
		contentEdit.setOnClickListener(this);
		addImageBtn.setOnClickListener(this);
		addLocationBtn.setOnClickListener(this);
		turnToAudioBtn.setOnClickListener(this);
		turnToTextBtn.setOnClickListener(this);
		showAddBtn.setOnClickListener(this);
		showEmotionBtn.setOnClickListener(this);
		addCameraBtn.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (roomType == RoomType.Single) {
			MessageReceiver.addMsgListener(this);
		} else {
			GroupMessageReceiver.addMsgListener(this);
		}
		initActionBar();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (roomType == RoomType.Single) {
			MessageReceiver.removeMsgListener(this);
		} else {
			GroupMessageReceiver.removeMsgListener(this);
		}
	}

	public void initData(Intent intent) {
		curUser = AVUser.getCurrentUser();
		dbHelper = new DatabaseHelper(ctx, MyApplication.DB_NAME,
				MyApplication.DB_VERSION);
		int roomTypeInt = intent.getIntExtra(ROOM_TYPE,
				RoomType.Single.getValue());
		roomType = RoomType.fromInt(roomTypeInt);
		msgSize = PAGE_SIZE;
		if (roomType == RoomType.Single) {
			String chatUserId = intent.getStringExtra(CHAT_USER_ID);
			chatUser = CacheService.lookupUser(chatUserId);
			ChatService.withUserToWatch(chatUser, true);
			MessageAgent = new MessageAgent(roomType, chatUser.getObjectId());
		} else {
			String groupId = intent.getStringExtra(GROUP_ID);
			Session session = ChatService.getSessionForCurrentUser();
			group = session.getGroup(groupId);
			ChatGroup chatGroup = CacheService.lookupChatGroup(groupId);
			CacheService.setCurrentChatGroup(chatGroup);
			MessageAgent = new MessageAgent(roomType, groupId);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_ativity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int menuId = item.getItemId();
		if (menuId == R.id.people) {
			if (roomType == RoomType.Single) {
				PersonInfoActivity.goPersonInfo(ctx, chatUser.getObjectId());
			} else {
				Utils.goActivity(ctx, GroupDetailActivity.class);
			}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public String currentChatId() {
		if (roomType == RoomType.Single) {
			return chatUser.getObjectId();
		} else {
			return getChatGroup().getObjectId();
		}
	}

	public void loadMsgsFromDB(boolean showDialog) {
		new GetDataTask(ctx, showDialog, true).execute();
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				msgSize += PAGE_SIZE;
				new GetDataTask(ctx, false, false).execute();
			}
		}, 1000);
	}

	@Override
	public void onLoadMore() {
	}

	@Override
	public boolean onMessageUpdate(String otherId) {
		if (otherId.equals(currentChatId())) {
			loadMsgsFromDB(false);
			return true;
		}
		return false;
	}

	public void resendMsg(final Message resendMsg) {
		MessageAgent.resendMsg(resendMsg, sendCallback);
	}

	class GetDataTask extends NetworkAsyncTask {
		List<Message> msgs;
		boolean scrollToLast = true;

		GetDataTask(Context cxt, boolean openDialog, boolean scrollToLast) {
			super(cxt, openDialog);
			this.scrollToLast = scrollToLast;
		}

		@Override
		protected void doInBack() throws Exception {
			String convid;
			if (roomType == RoomType.Single) {
				convid = ChatUtils.convid(ChatService.getPeerID(curUser),
						ChatService.getPeerID(chatUser));
			} else {
				convid = group.getGroupId();
			}
			msgs = DatabaseMessage.getMessages(dbHelper, convid, msgSize);
			DatabaseMessage.markMessagesAsHaveRead(msgs);
			ChatService.cacheUserOrChatGroup(msgs);
		}

		@Override
		protected void onPost(Exception e) {
			if (e == null) {
				ChatUtils.stopRefresh(xListView);
				addMsgsAndRefresh(msgs, scrollToLast);
			} else {
				Utils.toast(R.string.failedToGetData);
			}
		}
	}

	public void addMsgsAndRefresh(List<Message> msgs, boolean scrollToLast) {
		int lastN = adapter.getCount();
		int newN = msgs.size();
		this.msgs = msgs;
		adapter.setDatas(this.msgs);
		adapter.notifyDataSetChanged();
		if (scrollToLast) {
			scrollToLast();
		} else {
			xListView.setSelection(newN - lastN - 1);
			if (lastN == newN) {
				Utils.toast(R.string.loadMessagesFinish);
			}
		}
		if (newN < PAGE_SIZE) {
			xListView.setPullRefreshEnable(false);
		} else {
			xListView.setPullRefreshEnable(true);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sendBtn:
			sendText();
			break;
		case R.id.addImageBtn:
			selectImageFromLocal();
			break;
		case R.id.turnToAudioBtn:
			showAudioLayout();
			break;
		case R.id.turnToTextBtn:
			showTextLayout();
			break;
		case R.id.showAddBtn:
			toggleBottomAddLayout();
			break;
		case R.id.showEmotionBtn:
			toggleEmotionLayout();
			break;
		case R.id.addAnnotationBtn:
			selectLocationFromMap();
			break;
		case R.id.textEdit:
			hideBottomLayoutAndScrollToLast();
			break;
		case R.id.addCameraBtn:
			selectImageFromCamera();
			break;
		}
	}

	private void hideBottomLayoutAndScrollToLast() {
		hideBottomLayout();
		scrollToLast();
	}

	private void hideBottomLayout() {
		hideAddLayout();
		chatEmotionLayout.setVisibility(View.GONE);
	}

	private void selectLocationFromMap() {
		Intent intent = new Intent(this, AnnotationActivity.class);
		intent.putExtra("type", "select");
		startActivityForResult(intent, LOCATION_REQUEST);
	}

	private void toggleEmotionLayout() {
		if (chatEmotionLayout.getVisibility() == View.VISIBLE) {
			chatEmotionLayout.setVisibility(View.GONE);
		} else {
			chatEmotionLayout.setVisibility(View.VISIBLE);
			hideAddLayout();
			showTextLayout();
			hideSoftInputView();
		}
	}

	private void toggleBottomAddLayout() {
		if (chatAddLayout.getVisibility() == View.VISIBLE) {
			hideAddLayout();
		} else {
			chatEmotionLayout.setVisibility(View.GONE);
			hideSoftInputView();
			showAddLayout();
		}
	}

	private void hideAddLayout() {
		chatAddLayout.setVisibility(View.GONE);
	}

	private void showAddLayout() {
		chatAddLayout.setVisibility(View.VISIBLE);
	}

	private void showTextLayout() {
		chatTextLayout.setVisibility(View.VISIBLE);
		chatAudioLayout.setVisibility(View.GONE);
	}

	private void showAudioLayout() {
		chatTextLayout.setVisibility(View.GONE);
		chatAudioLayout.setVisibility(View.VISIBLE);
		chatEmotionLayout.setVisibility(View.GONE);
		hideSoftInputView();
	}

	public void selectImageFromLocal() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent,
							getResources().getString(R.string.select_picture)),
					GALLERY_REQUEST);
		} else {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			startActivityForResult(intent, GALLERY_KITKAT_REQUEST);
		}
	}

	public void selectImageFromCamera() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri imageUri = Uri.fromFile(new File(localCameraPath));
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(openCameraIntent, TAKE_CAMERA_REQUEST);
	}

	private void sendText() {
		final String content = contentEdit.getText().toString();
		if (TextUtils.isEmpty(content) == false) {
			if (isStateFine()) {
				MessageAgent.createAndSendMsg(
						new MessageAgent.MessageBuilderHelper() {

							@Override
							public void specifyType(MessageBuilder msgBuilder) {
								msgBuilder.text(content);
							}
						}, new DefaultSendCallback() {
							@Override
							public void onSuccess(Message msg) {
								super.onSuccess(msg);
								contentEdit.setText("");
							}
						});
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case GALLERY_REQUEST:
			case GALLERY_KITKAT_REQUEST:
				if (data == null) {
					return;
				}
				Uri uri;
				if (requestCode == GALLERY_REQUEST) {
					uri = data.getData();
				} else {
					uri = data.getData();
					final int takeFlags = data.getFlags()
							& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					getContentResolver().takePersistableUriPermission(uri,
							takeFlags);
				}
				String localSelectPath = ProviderPathUtils.getPath(ctx, uri);
				sendImageByPath(localSelectPath);
				break;
			case TAKE_CAMERA_REQUEST:
				sendImageByPath(localCameraPath);
				break;
			case LOCATION_REQUEST:
				sendLocationByReturnData(data);
				break;
			}
		}
		hideBottomLayout();
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void sendLocationByReturnData(Intent data) {
		final double latitude = data.getDoubleExtra("x", 0);
		final double longitude = data.getDoubleExtra("y", 0);
		final String address = data.getStringExtra("address");
		if (address != null && !address.equals("")) {
			if (isStateFine()) {
				MessageAgent.createAndSendMsg(
						new MessageAgent.MessageBuilderHelper() {
							@Override
							public void specifyType(MessageBuilder msgBuilder) {
								msgBuilder.annotation(address, latitude,
										longitude);
							}
						}, sendCallback);
			}
		} else {
			Utils.toast(ctx, R.string.cannotGetYourAddressInfo);
		}
	}

	class DefaultSendCallback implements MessageSendCallback {
		@Override
		public void onError(Exception e) {
			e.printStackTrace();
			Utils.toast(e.getMessage());
			loadMsgsFromDB(false);
		}

		@Override
		public void onStart(Message msg) {
			loadMsgsFromDB(false);
		}

		@Override
		public void onSuccess(Message msg) {
			loadMsgsFromDB(false);
		}
	}

	private MessageSendCallback sendCallback = new DefaultSendCallback();

	public boolean isStateFine() {
		if (Connectivity.isConnected(ctx) == false) {
			Utils.toast(ctx,
					MyApplication.mCtx.getString(R.string.pleaseCheckNetwork));
			return false;
		} else if (ChatService.isSessionPaused()) {
			ctx.sendBroadcast(new Intent(RETRY_ACTION));
			Utils.toast(ctx,
					MyApplication.mCtx.getString(R.string.sessionPausedTips));
			return false;
		} else {
			return true;
		}
	}

	private void sendImageByPath(String localSelectPath) {
		final String objectId = Utils.uuid();
		final String newPath = PathUtils.getChatFilePath(objectId);
		// ImageUtils.simpleCompressImage(localSelectPath,newPath);
		ImageUtils.compressImage(localSelectPath, newPath);
		if (isStateFine()) {
			MessageAgent.createAndSendMsg(
					new MessageAgent.MessageBuilderHelper() {
						@Override
						public void specifyType(MessageBuilder msgBuilder) {
							msgBuilder.image(objectId);
						}
					}, sendCallback);
		}
	}

	public void scrollToLast() {
		Logger.d("scrollToLast");
		// xListView.smoothScrollToPosition(xListView.getCount() - 1);
		xListView.setSelection(xListView.getCount() - 1);
	}

	@Override
	protected void onDestroy() {
		if (roomType == RoomType.Single) {
			ChatService.withUserToWatch(chatUser, false);
		} else {
			CacheService.setCurrentChatGroup(null);
		}
		ctx = null;
		super.onDestroy();
	}

	public static void goUserChat(Activity ctx, String userId) {
		Intent intent = getUserChatIntent(ctx, userId);
		ctx.startActivity(intent);
	}

	public static Intent getUserChatIntent(Context ctx, String userId) {
		Intent intent = new Intent(ctx, ChatActivity.class);
		intent.putExtra(CHAT_USER_ID, userId);
		intent.putExtra(ROOM_TYPE, RoomType.Single.getValue());
		return intent;
	}

	public static void goGroupChat(Activity ctx, String groupId) {
		Intent intent = getGroupChatIntent(ctx, groupId);
		ctx.startActivity(intent);
	}

	public static Intent getGroupChatIntent(Context ctx, String groupId) {
		Intent intent = new Intent(ctx, ChatActivity.class);
		intent.putExtra(GROUP_ID, groupId);
		intent.putExtra(ROOM_TYPE, RoomType.Group.getValue());
		return intent;
	}

	public ChatGroup getChatGroup() {
		return CacheService.getCurrentChatGroup();
	}
}
