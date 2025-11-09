package io.github.FOUR.game;

import com.badlogic.gdx.graphics.Texture;

public class Item extends Entity{
    /*
    key:
    1 = healing pickup
     */
    public int type, index;

    public Item(float x, float y, int type, Texture texture, int index) {
        super(x, y, texture);
        this.type = type;
        this.index = index;
    }

    public void collision() {
        float dist = (float) Math.sqrt(Math.pow(Main.player.x - x, 2) + Math.pow(Main.player.y - y, 2));
        if (dist < 16)  {
            Main.pickUpSound.play();
            switch (type) {
                case 1:
                    healPlayer(25);
                    break;
            }
            kill();
        }
    }

    private void healPlayer(int amount) {
        Main.player.hp += amount;
    }

    @Override
    public void kill() {
        texture.dispose();
        Main.removeItem(index);
    }
}
