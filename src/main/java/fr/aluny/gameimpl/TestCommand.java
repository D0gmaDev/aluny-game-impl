package fr.aluny.gameimpl;

import de.studiocode.invui.animation.impl.HorizontalSnakeAnimation;
import de.studiocode.invui.gui.GUI;
import de.studiocode.invui.gui.builder.GUIBuilder;
import de.studiocode.invui.gui.builder.guitype.GUIType;
import de.studiocode.invui.gui.impl.PagedGUI;
import de.studiocode.invui.gui.impl.ScrollGUI;
import de.studiocode.invui.item.Item;
import de.studiocode.invui.item.ItemProvider;
import de.studiocode.invui.item.builder.ItemBuilder;
import de.studiocode.invui.item.impl.BaseItem;
import de.studiocode.invui.item.impl.SimpleItem;
import de.studiocode.invui.item.impl.controlitem.PageItem;
import de.studiocode.invui.item.impl.controlitem.ScrollItem;
import de.studiocode.invui.virtualinventory.VirtualInventoryManager;
import de.studiocode.invui.window.impl.single.SimpleWindow;
import fr.aluny.gameapi.command.Command;
import fr.aluny.gameapi.command.CommandInfo;
import fr.aluny.gameapi.command.Default;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

@CommandInfo(name = "testinv", aliases = "go")
public class TestCommand extends Command {

    @Default
    public void defaultContext(Player player, String[] args) {

        Item border = new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("§r"));

// an example list of items to display
        List<SimpleItem> items = Arrays.stream(Material.values())
                .filter(material -> !material.isAir() && material.isItem())
                .map(material -> new SimpleItem(new ItemBuilder(material))).toList();

// create the gui
        GUI gui = new GUIBuilder<>(GUIType.NORMAL)
                .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # # # # # # #")
                .addIngredient('x', VirtualInventoryManager.getInstance().getOrCreate(UUID.fromString("499962c6-945b-432d-8330-e53e0eb5578b"), 14))
                .addIngredient('#', border)
                .build();

        SimpleWindow simpleWindow = new SimpleWindow(player, "§4Cool", gui);
        simpleWindow.show();

        gui.playAnimation(new HorizontalSnakeAnimation(0, true), slotElement -> true);
    }

    public static class CountItem extends BaseItem {

        private int count;

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.DIAMOND).setDisplayName("Count: " + count);
        }

        @Override
        public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                count++; // increment if left click
            } else {
                count--; // else decrement
            }

            notifyWindows(); // this will update the ItemStack that is displayed to the player
        }

    }

    public static class BackItem extends PageItem {

        public BackItem() {
            super(false);
        }

        @Override
        public ItemProvider getItemProvider(PagedGUI gui) {
            ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
            builder.setDisplayName("§7Previous page")
                    .addLoreLines(gui.hasPageBefore()
                            ? "§7Go to page §e" + gui.getCurrentPageIndex() + "§7/§e" + gui.getPageAmount()
                            : "§cYou can't go further back");

            return builder;
        }

    }

    public static class ForwardItem extends PageItem {

        public ForwardItem() {
            super(true);
        }

        @Override
        public ItemProvider getItemProvider(PagedGUI gui) {
            ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
            builder.setDisplayName("§7Next page")
                    .addLoreLines(gui.hasNextPage()
                            ? "§7Go to page §e" + (gui.getCurrentPageIndex() + 2) + "§7/§e" + gui.getPageAmount()
                            : "§cThere are no more pages");

            return builder;
        }

    }

    public static class ScrollDownItem extends ScrollItem {

        public ScrollDownItem() {
            super(1);
        }

        @Override
        public ItemProvider getItemProvider(ScrollGUI gui) {
            ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
            builder.setDisplayName("§7Scroll down");
            if (!gui.canScroll(1))
                builder.addLoreLines("§cYou can't scroll further down");

            return builder;
        }

    }

    public static class ScrollUpItem extends ScrollItem {

        public ScrollUpItem() {
            super(-1);
        }

        @Override
        public ItemProvider getItemProvider(ScrollGUI gui) {
            ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
            builder.setDisplayName("§7Scroll up");
            if (!gui.canScroll(-1))
                builder.addLoreLines("§cYou've reached the top");

            return builder;
        }

    }

}
