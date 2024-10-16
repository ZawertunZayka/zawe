package im.expensive.utils.render.font;

public class Fonts {

    public static Font montserrat, consolas, icons, icons2, wasafont, damage, sfui, sfbold, sfMedium;

    public static void register() {
        montserrat = new Font("Montserrat-Regular.ttf.png", "Montserrat-Regular.ttf.json");
        icons = new Font("icons.ttf.png", "icons.ttf.json");
        icons2 = new Font("icons2", "icons2.json");
        wasafont = new Font("wasafont.png", "wasafont.ttf.json");
        consolas = new Font("consolas.ttf.png", "consolas.ttf.json");
        damage = new Font("damage.ttf.png", "damage.ttf.json");
        sfui = new Font("sf_semibold.ttf.png", "sf_semibold.ttf.json");
        sfbold = new Font("sf_bold.ttf.png", "sf_bold.ttf.json");
        sfMedium = new Font("sf_medium.ttf.png", "sf_medium.ttf.json");
    }

}
