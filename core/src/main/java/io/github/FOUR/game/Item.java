package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;

/**
 * the Item class handles picking up items, drawing them, and performing code based on its type.
 */
public class Item extends Entity{
    /*
    key:
    1 = healing pickup
    2 = torch
     */
    public int type, index;

    /**
     * constructor for the Item class
     *
     * @param x the initial x pos
     * @param y the initial y pos
     * @param type the type of item. see key above
     * @param texture the texture sheet for items (though there's only one right now)
     * @param index the index of the item
     */
    public Item(float x, float y, int type, Texture texture, int index) {
        super(x, y, texture);
        this.type = type;
        this.index = index;
    }

    /**
     * if a player "touches" the item then perform the specified code and "kill" the item.
     */
    public void collision() {
        float dist = (float) Math.sqrt(Math.pow(Main.player.x - x, 2) + Math.pow(Main.player.y - y, 2));
        if (dist < 16)  {
            Main.pickUpSound.play();
            switch (type) {
                case 1:
                    Main.score += 10;
                    healPlayer(25);
                    break;
            }
            kill();
        }

        sprite.setPosition(x-16, y-16);
    }

    private void healPlayer(int amount) {
        Main.player.hp += amount;
        if (Main.player.hp > 100) {
            Main.player.hp = 100;
        }
    }

    @Override
    public void kill() {
        Main.removeItem(index);
    }

}

