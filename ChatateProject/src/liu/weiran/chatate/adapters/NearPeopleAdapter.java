package liu.weiran.chatate.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.service.PreferenceMap;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.util.Utils;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class NearPeopleAdapter extends BaseListAdapter<AVUser> {
	PrettyTime prettyTime;
	AVGeoPoint location;

	public NearPeopleAdapter(Context ctx) {
		super(ctx);
		init();
	}

	private void init() {
		prettyTime = new PrettyTime();
		PreferenceMap preferenceMap = PreferenceMap.getCurUserPreference(ctx);
		location = preferenceMap.getLocation();
	}

	public NearPeopleAdapter(Context ctx, List<AVUser> datas) {
		super(ctx, datas);
		init();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.discover_near_people_item,
					null, false);
		}
		final AVUser user = items.get(position);
		TextView nameView = ViewHolder
				.findViewById(convertView, R.id.name_text);
		TextView distanceView = ViewHolder.findViewById(convertView,
				R.id.distance_text);
		TextView loginTimeView = ViewHolder.findViewById(convertView,
				R.id.login_time_text);
		ImageView avatarView = ViewHolder.findViewById(convertView,
				R.id.avatar_view);
		String avatarUrl = User.getAvatarUrl(user);

		UserService.displayAvatar(avatarUrl, avatarView);

		AVGeoPoint geoPoint = user.getAVGeoPoint(User.LOCATION);
		String currentLat = String.valueOf(location.getLatitude());
		String currentLong = String.valueOf(location.getLongitude());
		if (geoPoint != null && !currentLat.equals("")
				&& !currentLong.equals("")) {
			double distance = DistanceOfTwoPoints(
					Double.parseDouble(currentLat),
					Double.parseDouble(currentLong),
					user.getAVGeoPoint(User.LOCATION).getLatitude(), user
							.getAVGeoPoint(User.LOCATION).getLongitude());
			distanceView.setText(Utils.getFormattedDistance(distance));
		} else {
			distanceView
					.setText(MyApplication.mCtx.getString(R.string.unknown));
		}
		nameView.setText(user.getUsername());
		Date updatedAt = user.getUpdatedAt();
		String prettyTimeStr = this.prettyTime.format(updatedAt);
		loginTimeView.setText(MyApplication.mCtx
				.getString(R.string.recent_login_time) + prettyTimeStr);
		return convertView;
	}

	private static final double EARTH_RADIUS = 6378137;

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * calculate distance btw two points（double) according to latitude
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return distance in unit of meters
	 */
	public static double DistanceOfTwoPoints(double lat1, double lng1,
			double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

}
