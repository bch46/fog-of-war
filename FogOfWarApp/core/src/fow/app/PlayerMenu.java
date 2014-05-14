package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class PlayerMenu extends Table {

	private final PlayerScreen controller;
	private final Skin skin;

	private TextField menuText;

	public PlayerMenu(final PlayerScreen controller) {
		this.controller = controller;
		skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

		menuText = new TextField("", skin);

		this.setFillParent(true);

		this.add(new Label("Welcome, player!", skin));
		this.add(menuText);
		this.row();

		TextButton submitButton = new TextButton("Submit move", skin);
		submitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				// TODO send move request
			}
		});
		this.add(submitButton);
	}

	public String getIdText() {
		return menuText.getText();
	}

	public void dispose() {
		skin.dispose();
	}

}
