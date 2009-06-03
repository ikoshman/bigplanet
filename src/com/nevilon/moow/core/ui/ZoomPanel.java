package com.nevilon.moow.core.ui;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

public class ZoomPanel extends RelativeLayout {

	private ZoomControls zoomControls;

	public ZoomPanel(Context context) {
		super(context);
		zoomControls = new ZoomControls(getContext());
		addView(zoomControls);
		setPadding(80, 368, 0, 0);
	}

	/**
	 * Устанавливает кнопку увеличения детализации в активное/неактивное
	 * состояние
	 * 
	 * @param isEnabled
	 */
	public void setIsZoomInEnabled(boolean isEnabled) {
		zoomControls.setIsZoomInEnabled(isEnabled);

	}

	/**
	 * Устанавливает кнопку уменьшения детализации в активное/неактивное
	 * состояние
	 * 
	 * @param isEnabled
	 */
	public void setIsZoomOutEnabled(boolean isEnabled) {
		zoomControls.setIsZoomOutEnabled(isEnabled);
	}
	
	public void setOnZoomInClickListener(OnClickListener onClickListener ){
		zoomControls.setOnZoomInClickListener(onClickListener);
	}
	
	public void setOnZoomOutClickListener(OnClickListener onClickListener ){
		zoomControls.setOnZoomOutClickListener(onClickListener);
	}

}


