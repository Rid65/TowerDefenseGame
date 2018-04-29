package com.td.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.td.game.gui.UpperPanel;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font24;
    private Map map;
    private TurretEmitter turretEmitter;
    private MonsterEmitter monsterEmitter;
    private ParticleEmitter particleEmitter;
    private TextureAtlas atlas;
    private TextureRegion selectedCellTexture;
    private Stage stage;
    private Group groupTurretAction;
    private Group groupTurretSelection;
    private PlayerInfo playerInfo;
    private UpperPanel upperPanel;
    private Camera camera;

    private Vector2 mousePosition;

    private int selectedCellX, selectedCellY;

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public ParticleEmitter getParticleEmitter() {
        return particleEmitter;
    }

    public MonsterEmitter getMonsterEmitter() {
        return monsterEmitter;
    }

    public GameScreen(SpriteBatch batch, Camera camera) {
        this.batch = batch;
        this.camera = camera;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        atlas = Assets.getInstance().getAtlas();
        selectedCellTexture = atlas.findRegion("cursor");
        map = new Map(atlas);
        font24 = Assets.getInstance().getAssetManager().get("zorque24.ttf", BitmapFont.class);
        turretEmitter = new TurretEmitter(atlas, this, map);
        monsterEmitter = new MonsterEmitter(atlas, map, 60);
        particleEmitter = new ParticleEmitter(atlas.findRegion("star16"));
        mousePosition = new Vector2(0, 0);
        playerInfo = new PlayerInfo(100, 32);
        createGUI();
    }

    public void createGUI() {
        stage = new Stage(ScreenManager.getInstance().getViewport(), batch);

        InputProcessor myProc = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                selectedCellX = (int) (mousePosition.x / 80);
                selectedCellY = (int) (mousePosition.y / 80);
                return true;
            }
        };

        InputMultiplexer im = new InputMultiplexer(stage, myProc);
        Gdx.input.setInputProcessor(im);

        Skin skin = new Skin();
        skin.addRegions(Assets.getInstance().getAtlas());

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();

        textButtonStyle.up = skin.getDrawable("shortButton");
        textButtonStyle.font = font24;
        skin.add("simpleSkin", textButtonStyle);

        groupTurretAction = new Group();
        groupTurretAction.setPosition(50, 600);

        Button btnSetTurret = new TextButton("Set", skin, "simpleSkin");
        Button btnUpgradeTurret = new TextButton("Upg", skin, "simpleSkin");
        Button btnDestroyTurret = new TextButton("Dst", skin, "simpleSkin");
        btnSetTurret.setPosition(10, 10);
        btnUpgradeTurret.setPosition(110, 10);
        btnDestroyTurret.setPosition(210, 10);
        groupTurretAction.addActor(btnSetTurret);
        groupTurretAction.addActor(btnUpgradeTurret);
        groupTurretAction.addActor(btnDestroyTurret);


        groupTurretSelection = new Group();
        groupTurretSelection.setVisible(false);
        groupTurretSelection.setPosition(50, 500);
        Button btnSetTurret1 = new TextButton("T1", skin, "simpleSkin");
        Button btnSetTurret2 = new TextButton("T2", skin, "simpleSkin");
        btnSetTurret1.setPosition(10, 10);
        btnSetTurret2.setPosition(110, 10);
        groupTurretSelection.addActor(btnSetTurret1);
        groupTurretSelection.addActor(btnSetTurret2);

        btnSetTurret1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setTurret(0);
            }
        });
        btnSetTurret2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setTurret(1);
            }
        });

        stage.addActor(groupTurretSelection);
        stage.addActor(groupTurretAction);

        upperPanel = new UpperPanel(playerInfo, stage, 0, 720 - 60);

        btnSetTurret.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                groupTurretSelection.setVisible(!groupTurretSelection.isVisible());
            }
        });
        btnUpgradeTurret.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                turretEmitter.improveTurret(selectedCellX, selectedCellY);
            }
        });
        skin.dispose();
    }

    public void setTurret(int index) {
        if (playerInfo.isMoneyEnough(turretEmitter.getTurretCost(index))) {
            playerInfo.decreaseMoney(turretEmitter.getTurretCost(index));
            turretEmitter.setTurret(index, selectedCellX, selectedCellY);
        }
        groupTurretSelection.setVisible(false);
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.position.set(640 + 160, 360, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        map.render(batch);
        turretEmitter.render(batch);
        monsterEmitter.render(batch, font24);
        particleEmitter.render(batch);
        batch.setColor(1, 1, 0, 0.5f);
        batch.draw(selectedCellTexture, selectedCellX * 80, selectedCellY * 80);
        batch.setColor(1, 1, 1, 1);
        batch.end();
        camera.position.set(640, 360, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        stage.draw();

    }

    public void update(float dt) {
        camera.position.set(640 + 160, 360, 0);
        camera.update();
        ScreenManager.getInstance().getViewport().apply();
        mousePosition.set(Gdx.input.getX(), Gdx.input.getY());
        ScreenManager.getInstance().getViewport().unproject(mousePosition);
        monsterEmitter.update(dt);
        turretEmitter.update(dt);
        particleEmitter.update(dt);
        particleEmitter.checkPool();
        checkMonstersAtHome();
        upperPanel.update();
        stage.act(dt);
    }

    public void checkMonstersAtHome() {
        for (int i = 0; i < monsterEmitter.getMonsters().length; i++) {
            Monster m = monsterEmitter.getMonsters()[i];
            if (m.isActive()) {
                if (map.isHome(m.getCellX(), m.getCellY())) {
                    m.deactivate();
                    playerInfo.decreaseHp(1);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        ScreenManager.getInstance().resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
