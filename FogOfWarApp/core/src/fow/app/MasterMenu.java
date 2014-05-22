package fow.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MasterMenu extends Table {

	private final PlayerScreen controller;
	private final Skin skin;

	public MasterMenu(final PlayerScreen controller) {
		this.controller = controller;
		skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

		this.add(new Label("Welcome, player!", skin));
		this.row();

		final TextButton submitButton = new TextButton("Submit move", skin);
		submitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				MasterMenu.this.controller.sendMoveRequest();
			}
		});
        this.add(submitButton);
	}
	
	public void dispose() {
		skin.dispose();
	}

}
