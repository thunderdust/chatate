package liu.weiran.chatate.ui.activities;

import liu.weiran.chatate.R;
import liu.weiran.chatate.service.CacheService;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.UpdateService;
import liu.weiran.chatate.service.receiver.FinishReceiver;
import liu.weiran.chatate.ui.fragments.ContactFragment;
import liu.weiran.chatate.ui.fragments.ConversationFragment;
import liu.weiran.chatate.ui.fragments.MySpaceFragment;
import liu.weiran.chatate.ui.fragments.book.AllBooksFragment;
import liu.weiran.chatate.ui.fragments.book.AllBooksFragment.OnNewBookDownloadedListener;
import liu.weiran.chatate.ui.fragments.book.MyBooksFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.avos.avoscloud.AVUser;

public class MainActivity extends BaseActivity implements
		OnNewBookDownloadedListener {

	Button conversationBtn, contactBtn, bookStoreBtn, myBookBtn, mySpaceBtn;
	View fragmentContainer;
	ContactFragment contactFragment;
	ConversationFragment conversationFragment;
	AllBooksFragment mBookStoreFragment;
	MyBooksFragment mMyBooksFragment;
	MySpaceFragment mySpaceFragment;

	public static final int FRAGMENT_N = 4;
	Button[] tabs;
	public static final int[] tabsNormalBackIds = new int[] {
			R.drawable.tabbar_chat, R.drawable.tabbar_contacts,
			R.drawable.tabbar_my_books, R.drawable.tabbar_book_store,
			R.drawable.tabbar_me };
	public static final int[] tabsActiveBackIds = new int[] {
			R.drawable.tabbar_chat_active, R.drawable.tabbar_contacts_active,
			R.drawable.tabbar_my_books, R.drawable.tabbar_book_store,
			R.drawable.tabbar_me_active };

	View recentTips, contactTips;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findView();
		init();

		// set current focus
		// mySpaceBtn.performClick();
		// contactBtn.performClick();
		conversationBtn.performClick();
		// discoverBtn.performClick();

		// UpdateService.createUpdateInfoInBackground();
		UpdateService updateService = UpdateService.getInstance(this);
		updateService.checkUpdate();
		CacheService.registerUserCache(AVUser.getCurrentUser());
		FinishReceiver.broadcastFinish(this);
		if (AVUser.getCurrentUser() != null) {
			ChatService.openSession();
		}

		// App.initTables();
	}

	public static void goMainActivity(Activity activity) {
		Intent intent = new Intent(activity, MainActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void init() {
		tabs = new Button[] { conversationBtn, contactBtn, myBookBtn,
				bookStoreBtn, mySpaceBtn };
	}

	private void findView() {
		conversationBtn = (Button) findViewById(R.id.btn_message);
		contactBtn = (Button) findViewById(R.id.btn_contact);
		myBookBtn = (Button) findViewById(R.id.btn_my_book);
		bookStoreBtn = (Button) findViewById(R.id.btn_all_books);
		mySpaceBtn = (Button) findViewById(R.id.btn_my_space);

		fragmentContainer = findViewById(R.id.fragment_container);
		recentTips = findViewById(R.id.iv_recent_tips);
		contactTips = findViewById(R.id.iv_contact_tips);
	}

	public void onTabSelect(View v) {
		int id = v.getId();
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		hideFragments(transaction);
		setNormalBackgrounds();
		if (id == R.id.btn_message) {
			if (conversationFragment == null) {
				conversationFragment = new ConversationFragment();
				transaction.add(R.id.fragment_container, conversationFragment);
			}
			transaction.show(conversationFragment);
		} else if (id == R.id.btn_contact) {
			if (contactFragment == null) {
				contactFragment = new ContactFragment();
				transaction.add(R.id.fragment_container, contactFragment);
			}
			transaction.show(contactFragment);
		} else if (id == R.id.btn_my_book) {
			if (mMyBooksFragment == null) {
				mMyBooksFragment = new MyBooksFragment();
				transaction.add(R.id.fragment_container, mMyBooksFragment);
			}
			transaction.show(mMyBooksFragment);
		} else if (id == R.id.btn_all_books) {
			if (mBookStoreFragment == null) {
				mBookStoreFragment = new AllBooksFragment();
				transaction.add(R.id.fragment_container, mBookStoreFragment);
			}
			transaction.show(mBookStoreFragment);
		} else if (id == R.id.btn_my_space) {
			if (mySpaceFragment == null) {
				mySpaceFragment = new MySpaceFragment();
				transaction.add(R.id.fragment_container, mySpaceFragment);
			}
			transaction.show(mySpaceFragment);
		}
		int pos;
		for (pos = 0; pos < FRAGMENT_N; pos++) {
			if (tabs[pos] == v) {
				break;
			}
		}
		transaction.commit();
		setTopDrawable(tabs[pos], tabsActiveBackIds[pos]);
	}

	private void setNormalBackgrounds() {
		for (int i = 0; i < tabs.length; i++) {
			Button v = tabs[i];
			setTopDrawable(v, tabsNormalBackIds[i]);
		}
	}

	private void setTopDrawable(Button v, int resId) {
		v.setCompoundDrawablesWithIntrinsicBounds(null, mCtx.getResources()
				.getDrawable(resId), null, null);
	}

	private void hideFragments(FragmentTransaction transaction) {
		Fragment[] fragments = new Fragment[] { conversationFragment,
				contactFragment, mMyBooksFragment, mBookStoreFragment,
				mySpaceFragment };
		for (Fragment f : fragments) {
			if (f != null) {
				transaction.hide(f);
			}
		}
	}

	@Override
	public void notifyNewBookDownloaded() {
		// TODO Auto-generated method stub
		if (mMyBooksFragment != null) {
			mMyBooksFragment.updateLocalBooks();
		}

	}
}
