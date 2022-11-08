package fr.aluny.gameimpl.world;

import com.google.common.base.Preconditions;
import fr.aluny.gameapi.service.ServiceManager;
import fr.aluny.gameapi.utils.GameUtils;
import fr.aluny.gameapi.world.LootModifier;
import fr.aluny.gameapi.world.LootModifierService;
import fr.aluny.gameapi.world.ModifierPriority;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class LootModifierServiceImpl implements LootModifierService {

    private final Queue<PriorityObject<LootModifier>> lootModifierList = new PriorityQueue<>(Comparator.comparingInt(PriorityObject::priority));

    private final ServiceManager serviceManager;

    public LootModifierServiceImpl(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void register(ModifierPriority modifierPriority, Material blockType, LootModifier lootModifier) {
        register(modifierPriority, blockType, lootModifier, 0, false);
    }

    public void register(ModifierPriority modifierPriority, Material blockType, LootModifier lootModifier, int expToDrop) {
        register(modifierPriority, blockType, lootModifier, expToDrop, false);
    }

    public void register(ModifierPriority modifierPriority, Material blockType, LootModifier lootModifier, int expToDrop, boolean allowBlockPlaced) {
        Preconditions.checkNotNull(lootModifier);

        this.lootModifierList.add(new PriorityObject<>(blockType, lootModifier, expToDrop, modifierPriority.ordinal(), allowBlockPlaced));
    }

    @Override
    public void applyLootModifiers(BlockBreakEvent event) {
        boolean modified = false;

        List<ItemStack> drops = new ArrayList<>(event.getBlock().getDrops(event.getPlayer().getItemInUse()));

        for (PriorityObject<LootModifier> priorityObject : this.lootModifierList) {
            if (!priorityObject.allowBlockPlaced() && !event.getBlock().getMetadata("dropped").isEmpty())
                continue;

            if (priorityObject.blockType() == null || priorityObject.blockType() == event.getBlock().getType()) {
                modified = true;

                if (priorityObject.expToDrop() > 0)
                    GameUtils.dropExperience(event.getBlock().getLocation().add(0.5, 0.5, 0.5), priorityObject.expToDrop());

                LootModifier lootModifier = priorityObject.object();

                for (ItemStack itemStack : drops) {
                    lootModifier.accept(itemStack);
                }
            }
        }

        if (modified) {
            event.setDropItems(false);

            serviceManager.getRunnableHelper().runLaterSynchronously(() -> {
                GameUtils.dropExperience(event.getBlock().getLocation().add(0.5, 0.5, 0.5), event.getExpToDrop());

                for (ItemStack itemStack : drops)
                    if (itemStack != null && itemStack.getType() != Material.AIR)
                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation().clone().add(0.5, 0.3, 0.5), itemStack);
            }, 3);
        }
    }

    private record PriorityObject<U>(Material blockType, U object, int expToDrop, int priority, boolean allowBlockPlaced) {

    }
}
