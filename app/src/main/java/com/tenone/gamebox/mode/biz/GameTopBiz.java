package com.tenone.gamebox.mode.biz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.tenone.gamebox.mode.able.GameTopAble;
import com.tenone.gamebox.mode.listener.GameTopListener;
import com.tenone.gamebox.mode.mode.GameModel;
import com.tenone.gamebox.mode.mode.GameStatus;
import com.tenone.gamebox.mode.mode.ResultItem;
import com.tenone.gamebox.view.base.MyApplication;

import java.util.ArrayList;
import java.util.List;

public class GameTopBiz implements GameTopAble {

	@Override
	public void constructArray(Context context, List<ResultItem> resultItems,
			GameTopListener listener) {
		new MyThread(context, resultItems, listener).start();
	}

	class MyThread extends Thread {
		Context context;
		List<ResultItem> resultItems;
		List<GameModel> items = new ArrayList<GameModel>();
		GameTopListener listener;

		public MyThread(Context context, List<ResultItem> resultItems,
				GameTopListener listener) {
			this.context = context;
			this.resultItems = resultItems;
			this.listener = listener;
		}

		@Override
		public void run() {
			for (ResultItem resultItem2 : resultItems) {
				GameModel model = new GameModel();
				model.setType(1);
				model.setGameTag(resultItem2.getString("tag"));
				model.setImgUrl(MyApplication.getHttpUrl().getBaseUrl()
						+ resultItem2.getString("logo"));
				model.setName(resultItem2.getString("gamename"));
				String id = resultItem2.getString("id");
				if (!TextUtils.isEmpty(id)) {
					model.setGameId(Integer.valueOf(id).intValue());
				}

				model.setSize(resultItem2.getString("size"));
				model.setUrl(resultItem2.getString("android_url"));
				model.setVersionsName(resultItem2.getString("version"));
				String down = resultItem2.getString("download");
				int download = 0;
				if (!TextUtils.isEmpty(down)) {
					download = Integer.valueOf(down).intValue();
				}

				if (download > 10000) {
					download = download / 10000;
					model.setTimes(download + "\u4e07+" );
				} else {
					model.setTimes(download + "");
				}
				model.setPackgeName(resultItem2.getString("android_pack"));
				model.setStatus(GameStatus.UNLOAD);
				model.setContent(resultItem2.getString("content"));
				String str = resultItem2.getString("label");
				if (!TextUtils.isEmpty(str)) {
					String[] lableArray = str.split(",");
					model.setLabelArray(lableArray);
				}
				items.add(model);
			}
			handler.post( () -> {
				if (listener != null) {
					listener.updateUI(items);
				}
			} );
			super.run();
		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};

}
