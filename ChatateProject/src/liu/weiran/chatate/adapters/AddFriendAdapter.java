package liu.weiran.chatate.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.service.CloudService;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;

public class AddFriendAdapter extends BaseListAdapter<AVUser> {
	public AddFriendAdapter(Context context, List<AVUser> list) {
		super(context, list);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View conView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (conView == null) {
			conView = inflater.inflate(R.layout.contact_add_friend_item, null);
		}
		final AVUser contact = items.get(position);
		TextView nameView = ViewHolder.findViewById(conView, R.id.name);
		ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
		Button addBtn = ViewHolder.findViewById(conView, R.id.add);
		String avatarUrl = User.getAvatarUrl(contact);
		UserService.displayAvatar(avatarUrl, avatarView);
		nameView.setText(contact.getUsername());
		addBtn.setText(R.string.add);
		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new NetworkAsyncTask(ctx) {
					@Override
					protected void doInBack() throws Exception {
						CloudService.tryCreateAddRequest(contact);
					}

					@Override
					protected void onPost(Exception e) {
						if (e != null) {
							Utils.toast(e.getMessage());
							CloudService.checkCloudCodeDeploy((AVException) e);
						} else {
							Utils.toast(R.string.sendRequestSucceed);
						}
					}
				}.execute();
			}
		});
		return conView;
	}

}
