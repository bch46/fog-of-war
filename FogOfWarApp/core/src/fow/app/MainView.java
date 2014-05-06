package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MainView extends Table {

	private final MainScreen controller;
	private final Skin skin;

	private TextField idText;
	private TextField ipText;

	public MainView(final MainScreen controller) {
		this.controller = controller;
		skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

		idText = new TextField("", skin);
		ipText = new TextField("", skin);

		this.setFillParent(true);
		this.center();

		this.add(new Label("Account ID:", skin));
		this.add(idText);
		this.row();
		this.add(new Label("IP Address:", skin));
		this.add(ipText);

		TextButton connectButton = new TextButton("Connect", skin);
		connectButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				MainView.this.controller.onConnectPressed();
			}
		});

		this.row();
		this.add(connectButton);
	}

	public String getIdText() {
		return idText.getText();
	}

	public String getIPText() {
		return ipText.getText();
	}

	public void dispose() {
		skin.dispose();
	}

}
