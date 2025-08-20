package net.ltxprogrammer.changed.ability.tree;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Instantiated per player, exists outside TransfurVariantInstance to persist between variants
 */
public class AbilityTreeInstance {
    public static class Account {
        private int points;

        public int getPoints() {
            return points;
        }

        public int getLevels() {
            return points % 10;
        }
    }

    public static class AccountedTree {
        private final AbilityTree tree;
        private final Account account;
        private final Set<ResourceLocation> purchasedNodes = new HashSet<>();

        public AccountedTree(AbilityTree tree, Account account) {
            this.tree = tree;
            this.account = account;
        }
    }

    private final Set<AccountedTree> trees = new HashSet<>();
}
