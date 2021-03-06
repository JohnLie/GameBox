package com.tenone.gamebox.view.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.tenone.gamebox.R;
import com.tenone.gamebox.mode.mode.GameStatus;
import com.tenone.gamebox.view.utils.DisplayMetricsUtils;

public class DownloadProgressBar extends View implements Runnable {
	private PorterDuffXfermode xfermode = new PorterDuffXfermode( PorterDuff.Mode.SRC_ATOP );
	private int defaultHeight = 35;
	private int borderWidth;
	private float maxProgress = 100f;
	private Paint textPaint;
	private Paint bgPaint;
	private Paint pgPaint;
	private String progressText;
	private Rect textRect;
	private RectF bgRectf;
	private Bitmap flikerBitmap;
	private float flickerLeft;
	private Bitmap pgBitmap;
	private Canvas pgCanvas;
	private float progress = 0;
	private boolean isFinish;
	private boolean isStop;
	private int loadingColor;
	private int stopColor;
	private int progressColor;
	private int progressTextColor;
	private int textSize;
	private int radius;
	private Thread thread;
	private BitmapShader bitmapShader;

	private int stae = GameStatus.UNLOAD;

	public DownloadProgressBar(Context context) {
		this( context, null );
	}

	public DownloadProgressBar(Context context, @Nullable AttributeSet attrs) {
		this( context, attrs, 0 );
	}

	public DownloadProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super( context, attrs, defStyleAttr );
		initAttrs( attrs );
	}

	private void initAttrs(AttributeSet attrs) {
		TypedArray ta = getContext().obtainStyledAttributes( attrs, R.styleable.FlikerProgressBar );
		try {
			textSize = (int) ta.getDimension( R.styleable.FlikerProgressBar_bar_textSize, 12 );
			loadingColor = ta.getColor( R.styleable.FlikerProgressBar_loadingColor, Color.parseColor( "#40c4ff" ) );
			stopColor = ta.getColor( R.styleable.FlikerProgressBar_stopColor, Color.parseColor( "#ff9800" ) );
			radius = (int) ta.getDimension( R.styleable.FlikerProgressBar_bar_radius, 0 );
			borderWidth = (int) ta.getDimension( R.styleable.FlikerProgressBar_bar_borderWidth, 1 );
			progressTextColor = ta.getColor( R.styleable.FlikerProgressBar_progress_text_color,
					Color.parseColor( "#ffffff" ) );
		} finally {
			ta.recycle();
		}
		init();
	}

	private void init() {
		if (getMeasuredWidth() > 0) {
			bgPaint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG );
			bgPaint.setStyle( Paint.Style.STROKE );
			bgPaint.setStrokeWidth( borderWidth );

			pgPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
			pgPaint.setStyle( Paint.Style.FILL );

			textPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
			textPaint.setTextSize( textSize );
			textPaint.setColor( progressTextColor );

			textRect = new Rect();
			bgRectf = new RectF( borderWidth, borderWidth,
					getMeasuredWidth() - borderWidth, getMeasuredHeight() - borderWidth );
			if (isStop) {
				progressColor = stopColor;
			} else {
				progressColor = loadingColor;
			}
			flikerBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.flicker );
			flickerLeft = -flikerBitmap.getWidth();
			initPgBitmap();
		}
	}

	private void initPgBitmap() {
		int width = getMeasuredWidth() - borderWidth;
		int height = getMeasuredHeight() - borderWidth;
		if (width > 0 && height > 0) {
			pgBitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
			pgCanvas = new Canvas( pgBitmap );
			thread = new Thread( this );
			thread.start();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		int widthSpecSize = MeasureSpec.getSize( widthMeasureSpec );
		int heightSpecMode = MeasureSpec.getMode( heightMeasureSpec );
		int heightSpecSize = MeasureSpec.getSize( heightMeasureSpec );
		int height = 0;
		switch (heightSpecMode) {
			case MeasureSpec.AT_MOST:
				height = DisplayMetricsUtils.dipTopx( getContext(), defaultHeight );
				break;
			case MeasureSpec.EXACTLY:
			case MeasureSpec.UNSPECIFIED:
				height = heightSpecSize;
				break;
		}
		setMeasuredDimension( widthSpecSize, height );
		if (pgBitmap == null) {
			init();
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw( canvas );
		drawBackGround( canvas );

		drawProgress( canvas );

		drawProgressText( canvas );

		drawColorProgressText( canvas );
	}

	private void drawBackGround(Canvas canvas) {
		bgPaint.setColor( progressColor );
		canvas.drawRoundRect( bgRectf, radius, radius, bgPaint );
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("WrongConstant")
	private void drawProgress(Canvas canvas) {
		float right = (stae == GameStatus.LOADING || stae == GameStatus.PAUSEING) ?
				(progress / maxProgress) * getMeasuredWidth() : getMeasuredWidth();
		pgPaint.setColor( progressColor );
		pgCanvas.save( Canvas.CLIP_SAVE_FLAG );
		pgCanvas.clipRect( 0, 0, right, getMeasuredHeight() );
		pgCanvas.drawColor( progressColor );
		pgCanvas.restore();
		if (!isStop) {
			pgPaint.setXfermode( xfermode );
			pgCanvas.drawBitmap( flikerBitmap, flickerLeft, 0, pgPaint );
			pgPaint.setXfermode( null );
		}
		bitmapShader = new BitmapShader( pgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP );
		pgPaint.setShader( bitmapShader );
		canvas.drawRoundRect( bgRectf, radius, radius, pgPaint );
	}

	private void drawProgressText(Canvas canvas) {
		textPaint.setColor( (stae == GameStatus.UNLOAD ||
				stae == GameStatus.COMPLETED || stae == GameStatus.INSTALLCOMPLETED
				|| stae == GameStatus.INSTALLING) ? progressTextColor : progressColor );
		progressText = getProgressText();
		textPaint.getTextBounds( progressText, 0, progressText.length(), textRect );
		int tWidth = textRect.width();
		int tHeight = textRect.height();
		float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
		float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
		canvas.drawText( progressText, xCoordinate, yCoordinate, textPaint );
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("WrongConstant")
	private void drawColorProgressText(Canvas canvas) {
		textPaint.setColor( Color.WHITE );
		int tWidth = textRect.width();
		int tHeight = textRect.height();
		float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
		float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
		float progressWidth = (progress / maxProgress) * getMeasuredWidth();
		if (progressWidth > xCoordinate) {
			canvas.save( Canvas.CLIP_SAVE_FLAG );
			float right = Math.min( progressWidth, xCoordinate + tWidth * 1.1f );
			canvas.clipRect( xCoordinate, 0, right, getMeasuredHeight() );
			canvas.drawText( progressText, xCoordinate, yCoordinate, textPaint );
			canvas.restore();
		}
	}

	public void setProgress(float progress) {
		if (!isStop) {
			if (progress < maxProgress) {
				this.progress = progress;
			} else {
				this.progress = maxProgress;
				finishLoad();
			}
			invalidate();
		}
	}

	public void setStop(boolean stop) {
		isStop = stop;
		if (isStop) {
			progressColor = stopColor;
			if (thread != null) {
				thread.interrupt();
			}
			setStae( GameStatus.PAUSEING );
		} else {
			progressColor = loadingColor;
			thread = new Thread( this );
			thread.start();
			setStae( GameStatus.LOADING );
		}
		invalidate();
	}

	public void finishLoad() {
		isFinish = true;
		setStop( true );
		setStae( GameStatus.COMPLETED );
	}

	public void toggle() {
		if (!isFinish) {
			if (isStop) {
				setStop( false );
			} else {
				setStop( true );
			}
		}
	}

	public void setStae(int stae) {
		this.stae = stae;
		isStop = (stae != GameStatus.LOADING);
		invalidate();
	}

	@Override
	public void run() {
		int width = flikerBitmap.getWidth();
		try {
			while (!isStop && !thread.isInterrupted() && stae == GameStatus.LOADING) {
				flickerLeft += DisplayMetricsUtils.dipTopx( getContext(), 5 );
				float progressWidth = (progress / maxProgress) * getMeasuredWidth();
				if (flickerLeft >= progressWidth) {
					flickerLeft = -width;
				}
				postInvalidate();
				Thread.sleep( 20 );
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public void reset() {

		setStop( true );
		progress = 0;
		isFinish = false;
		isStop = false;
		progressColor = loadingColor;
		progressText = "";
		if (flikerBitmap != null) {
			flickerLeft = -flikerBitmap.getWidth();
		}
		initPgBitmap();
	}

	public float getProgress() {
		return progress;
	}

	public boolean isStop() {
		return isStop;
	}

	public boolean isFinish() {
		return isFinish;
	}

	private String getProgressText() {
		String text = "\u7acb\u5373\u4e0b\u8f7d";
		switch (stae) {
			case GameStatus.UNLOAD:
				text = "\u7acb\u5373\u4e0b\u8f7d";
				break;
			case GameStatus.COMPLETED:
				text = "\u5b89\u88c5";
				break;
			case GameStatus.INSTALLING:
				text = "\u5b89\u88c5\u4e2d";
				break;
			case GameStatus.LOADING:
				text = "\u4e0b\u8f7d\u4e2d" + progress + "%";
				break;
			case GameStatus.PAUSEING:
				text = "\u7ee7\u7eed";
				break;
			case GameStatus.INSTALLCOMPLETED:
				text = "\u6253\u5f00";
				break;
		}
		return text;
	}
}
