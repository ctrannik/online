package alex.imhere.service.api;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.service.parser.JsonParser;

public class UserApi extends Api {
	static final String
			API_GetOnlineUsers = "GetOnlineUsers";

	@Inject
	public UserApi(JsonParser parser) {
		super(parser);
	}

	public final ArrayList<DyingUser> getOnlineUsers(@NonNull final DyingUser dyingUser) throws ApiException {
		ArrayList<DyingUser> users = null;
		try {
			String jsonUsers = ParseCloud.callFunction(API_GetOnlineUsers, constructRequestForUser(dyingUser.getUdid()));
			users = parser.fromJson(jsonUsers, new TypeToken<List<DyingUser>>(){}.getType());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ApiException("cannot get online users", e);
		}
		return users;
	}
}