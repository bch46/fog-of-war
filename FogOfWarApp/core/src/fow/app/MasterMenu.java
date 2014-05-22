package fow.app;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import fow.common.VisibilityLayer;

public class MasterMenu extends Table {

    private final MasterScreen controller;
    private final Skin skin;

    private final Button approveButton;
    private final Button denyButton;

    private final Label numPlayers;
    private final Label pendingRequests;
    private final SelectBox<Integer> selectPlayer;

    public MasterMenu(final MasterScreen controller) {
        this.controller = controller;
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        this.add(new Label("Welcome, Dungeon Master!", skin)).colspan(3);
        this.row();

        numPlayers = new Label("Currently there are no players", skin);
        pendingRequests = new Label(" and no pending requests", skin);
        this.add(numPlayers);
        this.add(pendingRequests);
        this.row();

        selectPlayer = new SelectBox<Integer>(skin);
        selectPlayer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MasterMenu.this.controller.setSelectedPlayer(selectPlayer.getSelected());
            }
        });
        this.add(selectPlayer);

        approveButton = new TextButton("Approve move", skin);
        approveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MasterMenu.this.controller.onMoveRequestDecision(true);
            }
        });
        this.add(approveButton);

        denyButton = new TextButton("Deny move", skin);
        denyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MasterMenu.this.controller.onMoveRequestDecision(false);
            }
        });
        this.add(denyButton);
    }

    public void dispose() {
        skin.dispose();
    }

    public void updateVisibilities(HashMap<Integer, VisibilityLayer> visibilities) {
        int n = visibilities.size();
        Integer[] ids = visibilities.keySet().toArray(new Integer[n]);
        selectPlayer.setItems(ids);
        
        if (n == 0) {
            numPlayers.setText("There are currently no players");
        } else if (n == 1) {
            numPlayers.setText("There is 1 player");
        } else {
            numPlayers.setText("There are " + n + " players");
        }
    }

    public void updateNumPendingRequests(int numRequests) {
        if (numRequests == 0) {
            pendingRequests.setText(" and no requests");
        } else if (numRequests == 1) {
            pendingRequests.setText(" and 1 request");
        } else {
            pendingRequests.setText(" and " + numRequests + " requests");
        }
    }

    public void setButtonsVisible(boolean visible) {
        approveButton.setVisible(visible);
        denyButton.setVisible(visible);
    }
    
    public void selectFirstPlayer() {
        selectPlayer.setSelectedIndex(0);
        selectPlayer.fire(new ChangeListener.ChangeEvent());
    }

}
