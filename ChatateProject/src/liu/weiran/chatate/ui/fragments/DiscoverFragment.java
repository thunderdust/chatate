package liu.weiran.chatate.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.InjectView;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.adapters.NearPeopleAdapter;
import liu.weiran.chatate.service.PreferenceMap;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.ui.activities.PersonInfoActivity;
import liu.weiran.chatate.ui.views.BaseListView;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends BaseFragment {

	@InjectView(R.id.list_near)
	BaseListView<AVUser> listView;

	NearPeopleAdapter adapter;
	List<AVUser> nears = new ArrayList<AVUser>();
	int orderType;
	PreferenceMap preferenceMap;

	private final SortDialogListener distanceListener = new SortDialogListener(
			UserService.ORDER_DISTANCE);
	private final SortDialogListener updatedAtListener = new SortDialogListener(
			UserService.ORDER_UPDATED_AT);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.discover_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		preferenceMap = PreferenceMap.getCurUserPreference(getActivity());
		orderType = preferenceMap.getNearbyOrder();
		headerLayout.showTitle(R.string.discover);
		headerLayout.showRightImageButton(R.drawable.nearby_order,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setTitle(R.string.sort)
								.setPositiveButton(R.string.loginTime,
										updatedAtListener)
								.setNegativeButton(R.string.distance,
										distanceListener).show();
					}
				});
		initXListView();
		listView.onRefresh();
	}

	public class SortDialogListener implements DialogInterface.OnClickListener {
		int orderType;

		public SortDialogListener(int orderType) {
			this.orderType = orderType;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			DiscoverFragment.this.orderType = orderType;
			listView.onRefresh();
		}
	}

	private void initXListView() {
		adapter = new NearPeopleAdapter(ctx, nears);
		listView = (BaseListView<AVUser>) getView()
				.findViewById(R.id.list_near);
		listView.init(new BaseListView.DataInterface<AVUser>() {
			@Override
			public List<AVUser> getDatas(int skip, int limit,
					List<AVUser> currentDatas) throws Exception {
				return UserService.findNearbyPeople(orderType, skip, limit);
			}

			@Override
			public void onItemSelected(AVUser item) {
				PersonInfoActivity.goPersonInfo(ctx, item.getObjectId());
			}
		}, adapter);

		PauseOnScrollListener listener = new PauseOnScrollListener(
				UserService.imageLoader, true, true);
		listView.setOnScrollListener(listener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		preferenceMap.setNearbyOrder(orderType);
	}
}
