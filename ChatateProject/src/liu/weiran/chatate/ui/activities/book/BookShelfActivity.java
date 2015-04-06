package liu.weiran.chatate.ui.activities.book;

import liu.weiran.chatate.R;
import liu.weiran.chatate.ui.fragments.book.AllBooksFragment;
import liu.weiran.chatate.ui.fragments.book.MyBooksFragment;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class BookShelfActivity extends FragmentActivity implements
		ActionBar.TabListener {

	public static Context mCtx;
	private static String site_username;
	private static String avatar_url_link;
	private static String uid;
	private static String access_token;
	private static final String TAG = "bookshelf";

	private AllBooksFragment mAllBooksFragment;
	private MyBooksFragment mMyBooksFragment;

	private ViewPager viewPager;
	private BookShelfTabsPagerAdapter mAdapter;
	private ActionBar mBar;
	private android.support.v4.app.FragmentManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookshelf);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setTitle("Book Shelf");

		ActionBar.Tab tab1 = actionBar.newTab().setText("My Docs");
		ActionBar.Tab tab2 = actionBar.newTab().setText("Bookstore");

		mAllBooksFragment = new AllBooksFragment();
		mMyBooksFragment = new MyBooksFragment();
		mManager = getSupportFragmentManager();
		mAdapter = new BookShelfTabsPagerAdapter(mManager);
		viewPager = (ViewPager) findViewById(R.id.book_pager);
		if (viewPager != null) {
			viewPager.setAdapter(mAdapter);
		} else {
			Log.e("BOOK SHELF", "View pager is NULL");
		}

		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		tab1.setTabListener(this);
		tab2.setTabListener(this);

		actionBar.addTab(tab1);
		actionBar.addTab(tab2);
	}

	public class BookShelfTabsPagerAdapter extends FragmentPagerAdapter {

		public BookShelfTabsPagerAdapter(
				android.support.v4.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {

			switch (index) {
			case 0:
				return mMyBooksFragment;
			case 1:
				return mAllBooksFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			// get item count - equal to number of tabs
			return 2;
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}
