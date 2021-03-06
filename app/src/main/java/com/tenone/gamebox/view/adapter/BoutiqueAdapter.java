package com.tenone.gamebox.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.john.superadapter.SuperAdapter;
import com.john.superadapter.SuperViewHolder;
import com.tenone.gamebox.R;
import com.tenone.gamebox.mode.mode.BoutiqueGameModel;
import com.tenone.gamebox.view.base.MyApplication;
import com.tenone.gamebox.view.utils.DisplayMetricsUtils;
import com.tenone.gamebox.view.utils.image.ImageLoadUtils;

import java.util.List;

public class BoutiqueAdapter extends SuperAdapter<BoutiqueGameModel> {

	private Context mContext;
	private int landscapeImgWidth, verticalImgWidth, landscapeImgHeight, verticalImgHeight, dp5;


	public BoutiqueAdapter(Context context, List<BoutiqueGameModel> items, int layoutResId) {
		super( context, items, layoutResId );
		this.mContext = context;
		landscapeImgWidth = DisplayMetricsUtils.getScreenWidth( mContext ) - DisplayMetricsUtils.dipTopx( mContext, 20 );
		landscapeImgHeight = landscapeImgWidth * 9 / 16;
		verticalImgWidth = (DisplayMetricsUtils.getScreenWidth( mContext ) - DisplayMetricsUtils.dipTopx( mContext, 30 )) / 3;
		verticalImgHeight = verticalImgWidth * 16 / 9;
		dp5 = DisplayMetricsUtils.dipTopx( mContext, 5 );
	}

	@Override
	public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, BoutiqueGameModel item) {
		ImageView imageView = holder.findViewById( R.id.id_boutique_icon );
		ImageLoadUtils.loadNormalImg( imageView, mContext, item.getGameIcon() );
		holder.setText( R.id.id_boutique_name, item.getGameName() );
		holder.setText( R.id.id_boutique_type, item.getGameType() );
		holder.setText( R.id.id_boutique_size, item.getGameSize() + "M" );
		holder.setText( R.id.id_boutique_intro, item.getGameIntro() );
		holder.setText( R.id.id_boutique_comment, item.getGameCommentCounts() + "\u4e2a\u6e38\u620f\u70b9\u8bc4" );
		ImageView adsIv = holder.findViewById( R.id.id_boutique_img );
		LinearLayout linearLayout = holder.findViewById( R.id.id_boutique_imgRoot );
		String ads = item.getGameAdsImg();
		if (!MyApplication.getHttpUrl().getBaseUrl().equals( ads )) {
			ViewGroup.LayoutParams params = adsIv.getLayoutParams();
			params.width = landscapeImgWidth;
			params.height = landscapeImgHeight;
			adsIv.setLayoutParams( params );
			adsIv.setScaleType( ImageView.ScaleType.CENTER_CROP );
			ImageLoadUtils.loadBannerImg( adsIv, mContext, ads );
			adsIv.setVisibility( View.VISIBLE );
			linearLayout.setVisibility( View.GONE );
		} else {
			adsIv.setVisibility( View.GONE );
			linearLayout.setVisibility( View.VISIBLE );
			String[] imgs = item.getGameImgs();
			ImageView[] iv = {holder.findViewById( R.id.id_boutique_img1 ),
					holder.findViewById( R.id.id_boutique_img2 ), holder.findViewById( R.id.id_boutique_img3 )};
			if (imgs != null) {
				int length = imgs.length;
				for (int i = 0; i < length; i++) {
					String url = imgs[i];
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv[i].getLayoutParams();
					params.width = verticalImgWidth;
					params.height = verticalImgHeight;
					if (i == 1) {
						params.leftMargin = dp5;
						params.rightMargin = dp5;
					}
					iv[i].setLayoutParams( params );
					iv[i].setScaleType( ImageView.ScaleType.CENTER_CROP );
					ImageLoadUtils.loadGameDetailsImg( iv[i], mContext, url );
				}
			}
		}
	}
}

