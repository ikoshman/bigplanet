package com.nevilon.bigplanet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.nevilon.bigplanet.core.Preferences;
import com.nevilon.bigplanet.core.RawTile;
import com.nevilon.bigplanet.core.providers.MapStrategyFactory;
import com.nevilon.bigplanet.core.tools.savemap.MapSaverUI;
import com.nevilon.bigplanet.core.ui.MapControl;
import com.nevilon.bigplanet.core.ui.OnMapLongClickListener;

public class BigPlanet extends Activity {

	
	
	private Toast textMessage;
	
	/*
	 * Графический движок, реализующий карту
	 */
	private MapControl mapControl;

	/**
	 * Конструктор
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// создание карты
		RawTile savedTile = Preferences.getTile();
		configMapControl(savedTile);
		// использовать ли сеть
		boolean useNet = Preferences.getUseNet();
		mapControl.getPhysicalMap().getTileResolver().setUseNet(useNet);
		// источник карты
		int mapSourceId = Preferences.getSourceId();
		mapControl.getPhysicalMap().getTileResolver().setMapSource(mapSourceId);
		// величина отступа
		Point globalOffset = Preferences.getOffset();
		mapControl.getPhysicalMap().setGlobalOffset(globalOffset);
		mapControl.getPhysicalMap().reloadTiles();
	}

	/**
	 * Обрабатывает поворот телефона
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		System.gc();
		configMapControl(mapControl.getPhysicalMap().getDefaultTile());
	}

	/**
	 * Запоминает текущий тайл и отступ при выгрузке приложения
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(textMessage!=null){
			textMessage.cancel();
		}
		Preferences.putTile(mapControl.getPhysicalMap().getDefaultTile());
		Preferences.putOffset(mapControl.getPhysicalMap().getGlobalOffset());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			/**
			 * если текущий режим SELECT_MODE  - изменить на ZOOM_MODE
			 * если текущий режим ZOOM_MODE - делегировать обработку
			 */
			if(mapControl.getMapMode() ==MapControl.SELECT_MODE){
				mapControl.setMapMode(MapControl.ZOOM_MODE);
				return true;
			} 
		default:
			return super.onKeyDown(keyCode, ev);
		}
	}
	
	
	

	/**
	 * Создает элементы меню
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// add map source menu
		menu.add(0, 0, 0, "Map source");
		// add tools menu
		SubMenu sub = menu.addSubMenu(0, 1, 0, "Tools");
		sub.add(2, 11, 1, "Cache map");
		// add network mode menu
		menu.add(0, 3, 0, "Network mode");
		// add settings menu
		// menu.add(0, 4,0, "Settings");
		// add bookmark menu
		menu.add(0,5,0,"Add bookmark");
		return true;
	}

	/**
	 * Устанавливает статус(активен/неактивен) пунктов меню
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean useNet = Preferences.getUseNet();
		menu.findItem(11).setEnabled(useNet);
		return true;
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println(resultCode);
		if (resultCode == RESULT_OK) {
			mapControl.setMapMode(MapControl.ZOOM_MODE);
		}
	}
	
	/**
	 * Устанавливает размеры карты и др. свойства
	 */
	private void configMapControl(RawTile tile) {
		WindowManager wm = this.getWindowManager();
		Display display = wm.getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();
		if (mapControl == null) {
			mapControl = new MapControl(this, width, height, tile);
			mapControl.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(int x, int y) {
					Intent intent = new Intent();
					intent.setClass(BigPlanet.this, AddGeoBookmark.class);
					startActivityForResult(intent,0);

				}

			});
		} else {
			mapControl.setSize(width, height);
		}
		setContentView(mapControl, new ViewGroup.LayoutParams(width, height));
	}

	/**
	 * Создает радиокнопку с заданными параметрами
	 * 
	 * @param label
	 * @param id
	 * @return
	 */
	private RadioButton buildRadioButton(String label, int id) {
		RadioButton btn = new RadioButton(this);
		btn.setText(label);
		btn.setId(id);
		return btn;
	}

	/**
	 * Обрабатывает нажатие на меню
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 11:
			showMapSaver();
			break;
		case 0:
			selectMapSource();
			break;
		case 3:
			selectNetworkMode();
			break;
		case 4:
			// showSettingsMenu();
			break;
			
		case 5:
			switchToBookmarkMode();
		}
		return false;

	}

	
	private void switchToBookmarkMode(){
		if(mapControl.getMapMode()!=MapControl.SELECT_MODE){
			mapControl.setMapMode(MapControl.SELECT_MODE);
			textMessage = Toast.makeText(this, "Select object",
	                    Toast.LENGTH_LONG);
	        textMessage.show();
		}
	}
	
	
	/**
	 * Отображает диалоги для кеширования карты в заданном радиусе
	 */
	private void showMapSaver() {
		MapSaverUI mapSaverUI = new MapSaverUI(this, mapControl
				.getPhysicalMap().getZoomLevel(), mapControl.getPhysicalMap()
				.getAbsoluteCenter(), mapControl.getPhysicalMap()
				.getTileResolver().getMapSourceId());
		mapSaverUI.show();
	}

	/**
	 * Создает диалог для выбора режима работы(оффлайн, онлайн)
	 */
	private void selectNetworkMode() {
		final Dialog networkModeDialog;
		networkModeDialog = new Dialog(this);
		networkModeDialog.setCanceledOnTouchOutside(true);
		networkModeDialog.setCancelable(true);
		networkModeDialog.setTitle("Select network mode");

		final LinearLayout mainPanel = new LinearLayout(this);
		mainPanel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		mainPanel.setOrientation(LinearLayout.VERTICAL);

		RadioGroup modesRadioGroup = new RadioGroup(this);

		LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
				RadioGroup.LayoutParams.WRAP_CONTENT,
				RadioGroup.LayoutParams.WRAP_CONTENT);

		modesRadioGroup
				.addView(buildRadioButton("Offline", 0), 0, layoutParams);

		modesRadioGroup
				.addView(buildRadioButton("Online ", 1), 0, layoutParams);

		boolean useNet = Preferences.getUseNet();
		int checked = 0;
		if (useNet) {
			checked = 1;
		}
		modesRadioGroup.check(checked);

		modesRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						boolean useNet = checkedId == 1;
						mapControl.getPhysicalMap().getTileResolver()
								.setUseNet(useNet);
						Preferences.putUseNet(useNet);
						networkModeDialog.hide();
					}

				});

		mainPanel.addView(modesRadioGroup);
		networkModeDialog.setContentView(mainPanel);
		networkModeDialog.show();

	}

	/**
	 * Создает диалог для выбора источника карт
	 */
	private void selectMapSource() {
		final Dialog mapSourceDialog;
		mapSourceDialog = new Dialog(this);
		mapSourceDialog.setCanceledOnTouchOutside(true);
		mapSourceDialog.setCancelable(true);
		mapSourceDialog.setTitle("Select map source");

		ScrollView scrollPanel = new ScrollView(this);
		scrollPanel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		final LinearLayout mainPanel = new LinearLayout(this);
		mainPanel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		mainPanel.setOrientation(LinearLayout.VERTICAL);

		RadioGroup sourcesRadioGroup = new RadioGroup(this);

		LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
				RadioGroup.LayoutParams.WRAP_CONTENT,
				RadioGroup.LayoutParams.WRAP_CONTENT);

		for (Integer id : MapStrategyFactory.strategies.keySet()) {
			sourcesRadioGroup.addView(
					buildRadioButton(MapStrategyFactory.strategies.get(id)
							.getDescription(), id), 0, layoutParams);
		}

		sourcesRadioGroup.check(Preferences.getSourceId());

		sourcesRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						Preferences.putSourceId(checkedId);
						mapControl.setMapSource(checkedId);
						mapSourceDialog.hide();
					}

				});

		mainPanel.addView(sourcesRadioGroup);
		scrollPanel.addView(mainPanel);
		mapSourceDialog.setContentView(scrollPanel);
		mapSourceDialog.show();
	}

}