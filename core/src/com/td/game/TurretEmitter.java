package com.td.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TurretEmitter {
    public class TurretTemplate {
        private int id;
        private int level;
        private int costLevel;
        private int imageIndex;
        private int costTurret;
        private int damage;
        private int radius;
        private float fireRate;

        public int getImageIndex() {
            return imageIndex;
        }

        public int getCostTurret() {
            return costTurret;
        }

        public int getDamage() {
            return damage;
        }

        public int getRadius() {
            return radius;
        }

        public float getFireRate() {
            return fireRate;
        }

        public int getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public int getCostLevel() {
            return costLevel;
        }

        // #image_index cost fire_rate damage radius
        public TurretTemplate(String line) {
            String[] tokens = line.split("\\s");
            id = Integer.parseInt(tokens[0]);
            level = Integer.parseInt(tokens[1]);
            costLevel = Integer.parseInt(tokens[2]);
            imageIndex = Integer.parseInt(tokens[3]);
            costTurret = Integer.parseInt(tokens[4]);
            fireRate = Float.parseFloat(tokens[5]);
            damage = Integer.parseInt(tokens[6]);
            radius = Integer.parseInt(tokens[7]);
        }
    }

    private GameScreen gameScreen;
    private TextureAtlas atlas;
    private Map map;
    private Turret[] turrets;
    private TurretTemplate[] templates;

    public TurretEmitter(TextureAtlas atlas, GameScreen gameScreen, Map map) {
        this.loadTurretData();
        this.gameScreen = gameScreen;
        this.map = map;
        this.atlas = atlas;
        this.turrets = new Turret[20];
        TextureRegion[] regions = new TextureRegion(atlas.findRegion("turrets")).split(80, 80)[0];
        for (int i = 0; i < turrets.length; i++) {
            turrets[i] = new Turret(regions, gameScreen, map, 0, 0);
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < turrets.length; i++) {
            if (turrets[i].isActive()) {
                turrets[i].render(batch);
            }
        }
    }

    public void update(float dt) {
        for (int i = 0; i < turrets.length; i++) {
            if (turrets[i].isActive()) {
                turrets[i].update(dt);
            }
        }
    }

    public int getTurretCost(int index) {
        return templates[index].costTurret;
    }

    //-- Вернет стоимость улучшения пуки до следующего уровня
    public int getLevelCost(int id, int level) {
        TurretTemplate template = getTemplate(id, level);
        if (template != null)
            return template.getCostLevel();
        else
            return -1;
    }

    public void setTurret(int id, int cellX, int cellY) {
        if (map.isCellEmpty(cellX, cellY)) {

            //-- поиск шаблона с нужным id
            TurretTemplate template = null;
            for (int i = 0; i < templates.length; i++) {
                if (templates[i].getId() == id && templates[i].getLevel() == 0) {
                    template = templates[i];
                }
            }

            if (template != null) {
                for (int i = 0; i < turrets.length; i++) {
                    if (turrets[i].isActive() && turrets[i].getCellX() == cellX && turrets[i].getCellY() == cellY) {
                        return;
                    }
                }
                for (int i = 0; i < turrets.length; i++) {
                    if (!turrets[i].isActive()) {
                        turrets[i].activate(template, cellX, cellY);
                        break;
                    }
                }
            }
        }
    }

    public void destroyTurret(int cellX, int cellY) {
        for (int i = 0; i < turrets.length; i++) {
            if (turrets[i].isActive() && turrets[i].getCellX() == cellX && turrets[i].getCellY() == cellY) {
                turrets[i].deactivate();
            }
        }
    }

    //-- improve turret
    public void improveTurret(int cellX, int cellY) {
        for (int i = 0; i < turrets.length; i++) {
            if (turrets[i].isActive() && turrets[i].getCellX() == cellX && turrets[i].getCellY() == cellY) {
                TurretTemplate template = getTemplate(turrets[i].getId(), turrets[i].getLevel() + 1);
                if (template != null) {
                    int levelCost = template.getCostLevel(); //-- стоимость обновления
                    PlayerInfo playerInfo = gameScreen.getPlayerInfo();
                    if (levelCost != -1 && playerInfo.isMoneyEnough(levelCost)) {
                        turrets[i].improveTurret(template);
                        playerInfo.decreaseMoney(levelCost);
                    }
                }
            }
        }
    }

    //-- Вернет шаблон по id и level
    private TurretTemplate getTemplate(int id, int level) {
        for (int i = 0; i < templates.length; i++) {
            if (templates[i].getId() == id && templates[i].getLevel() == level) {
                return templates[i];
            }
        }
        return null;
    }

    public void loadTurretData() {
        BufferedReader br = null;
        List<String> lines = new ArrayList<String>();
        try {
            br = Gdx.files.internal("turrets.dat").reader(8192);
            String str;
            while ((str = br.readLine()) != null) {
                lines.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        templates = new TurretTemplate[lines.size() - 1];
        for (int i = 1; i < lines.size(); i++) {
            templates[i - 1] = new TurretTemplate(lines.get(i));
        }
    }
}

