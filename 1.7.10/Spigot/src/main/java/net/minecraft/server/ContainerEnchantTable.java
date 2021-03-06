package net.minecraft.server;

import java.util.List;
// CraftBukkit start
import java.util.Map;
import java.util.Random;

import org.bukkit.craftbukkit.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
// CraftBukkit end
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class ContainerEnchantTable extends Container {

	// CraftBukkit - make type specific (changed from IInventory)
	public ContainerEnchantTableInventory enchantSlots = new ContainerEnchantTableInventory(this, "Enchant", true, 1);
	private World world;
	private int x;
	private int y;
	private int z;
	private Random l = new Random();
	public long f;
	public int[] costs = new int[3];
	// CraftBukkit start
	private CraftInventoryView bukkitEntity = null;
	private Player player;

	// CraftBukkit end

	public ContainerEnchantTable(PlayerInventory playerinventory, World world, int i, int j, int k) {
		this.world = world;
		x = i;
		y = j;
		z = k;
		this.a(new SlotEnchant(this, enchantSlots, 0, 25, 47));

		int l;

		for (l = 0; l < 3; ++l) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.a(new Slot(playerinventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
			}
		}

		for (l = 0; l < 9; ++l) {
			this.a(new Slot(playerinventory, l, 8 + l * 18, 142));
		}

		// CraftBukkit start
		player = (Player) playerinventory.player.getBukkitEntity();
		enchantSlots.player = player;
		// CraftBukkit end
	}

	@Override
	public void addSlotListener(ICrafting icrafting) {
		super.addSlotListener(icrafting);
		icrafting.setContainerData(this, 0, costs[0]);
		icrafting.setContainerData(this, 1, costs[1]);
		icrafting.setContainerData(this, 2, costs[2]);
	}

	@Override
	public void b() {
		super.b();

		for (int i = 0; i < listeners.size(); ++i) {
			ICrafting icrafting = (ICrafting) listeners.get(i);

			icrafting.setContainerData(this, 0, costs[0]);
			icrafting.setContainerData(this, 1, costs[1]);
			icrafting.setContainerData(this, 2, costs[2]);
		}
	}

	@Override
	public void a(IInventory iinventory) {
		if (iinventory == enchantSlots) {
			ItemStack itemstack = iinventory.getItem(0);
			int i;

			if (itemstack != null) { // CraftBukkit - relax condition
				f = l.nextLong();
				if (!world.isStatic) {
					i = 0;

					int j;

					for (j = -1; j <= 1; ++j) {
						for (int k = -1; k <= 1; ++k) {
							if ((j != 0 || k != 0) && world.isEmpty(x + k, y, z + j) && world.isEmpty(x + k, y + 1, z + j)) {
								if (world.getType(x + k * 2, y, z + j * 2) == Blocks.BOOKSHELF) {
									++i;
								}

								if (world.getType(x + k * 2, y + 1, z + j * 2) == Blocks.BOOKSHELF) {
									++i;
								}

								if (k != 0 && j != 0) {
									if (world.getType(x + k * 2, y, z + j) == Blocks.BOOKSHELF) {
										++i;
									}

									if (world.getType(x + k * 2, y + 1, z + j) == Blocks.BOOKSHELF) {
										++i;
									}

									if (world.getType(x + k, y, z + j * 2) == Blocks.BOOKSHELF) {
										++i;
									}

									if (world.getType(x + k, y + 1, z + j * 2) == Blocks.BOOKSHELF) {
										++i;
									}
								}
							}
						}
					}

					for (j = 0; j < 3; ++j) {
						costs[j] = EnchantmentManager.a(l, j, i, itemstack);
					}

					// CraftBukkit start
					CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
					PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(player, getBukkitView(), world.getWorld().getBlockAt(x, y, z), item, costs, i);
					event.setCancelled(!itemstack.x());
					world.getServer().getPluginManager().callEvent(event);

					if (event.isCancelled()) {
						for (i = 0; i < 3; ++i) {
							costs[i] = 0;
						}
						return;
					}
					// CraftBukkit end

					this.b();
				}
			} else {
				for (i = 0; i < 3; ++i) {
					costs[i] = 0;
				}
			}
		}
	}

	@Override
	public boolean a(EntityHuman entityhuman, int i) {
		ItemStack itemstack = enchantSlots.getItem(0);

		if (costs[i] > 0 && itemstack != null && (entityhuman.expLevel >= costs[i] || entityhuman.abilities.canInstantlyBuild)) {
			if (!world.isStatic) {
				List list = EnchantmentManager.b(l, itemstack, costs[i]);
				// CraftBukkit start - Provide an empty enchantment list
				if (list == null) {
					list = new java.util.ArrayList<EnchantmentInstance>();
				}
				// CraftBukkit end

				boolean flag = itemstack.getItem() == Items.BOOK;

				if (list != null) {
					// CraftBukkit start
					Map<org.bukkit.enchantments.Enchantment, Integer> enchants = new java.util.HashMap<org.bukkit.enchantments.Enchantment, Integer>();
					for (Object obj : list) {
						EnchantmentInstance instance = (EnchantmentInstance) obj;
						enchants.put(org.bukkit.enchantments.Enchantment.getById(instance.enchantment.id), instance.level);
					}
					CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);

					EnchantItemEvent event = new EnchantItemEvent((Player) entityhuman.getBukkitEntity(), getBukkitView(), world.getWorld().getBlockAt(x, y, z), item, costs[i], enchants, i);
					world.getServer().getPluginManager().callEvent(event);

					int level = event.getExpLevelCost();
					if (event.isCancelled() || level > entityhuman.expLevel && !entityhuman.abilities.canInstantlyBuild || event.getEnchantsToAdd().isEmpty())
						return false;

					if (flag) {
						itemstack.setItem(Items.ENCHANTED_BOOK);
					}

					for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
						try {
							if (flag) {
								int enchantId = entry.getKey().getId();
								if (Enchantment.byId[enchantId] == null) {
									continue;
								}

								EnchantmentInstance enchantment = new EnchantmentInstance(enchantId, entry.getValue());
								Items.ENCHANTED_BOOK.a(itemstack, enchantment);
							} else {
								item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
							}
						} catch (IllegalArgumentException e) {
							/* Just swallow invalid enchantments */
						}
					}

					entityhuman.levelDown(-level);
					// CraftBukkit end

					this.a(enchantSlots);
				}
			}

			return true;
		} else
			return false;
	}

	@Override
	public void b(EntityHuman entityhuman) {
		super.b(entityhuman);
		if (!world.isStatic) {
			ItemStack itemstack = enchantSlots.splitWithoutUpdate(0);

			if (itemstack != null) {
				entityhuman.drop(itemstack, false);
			}
		}
	}

	@Override
	public boolean a(EntityHuman entityhuman) {
		if (!checkReachable)
			return true; // CraftBukkit
		return world.getType(x, y, z) != Blocks.ENCHANTMENT_TABLE ? false : entityhuman.e(x + 0.5D, y + 0.5D, z + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack b(EntityHuman entityhuman, int i) {
		ItemStack itemstack = null;
		Slot slot = (Slot) c.get(i);

		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();

			itemstack = itemstack1.cloneItemStack();
			if (i == 0) {
				if (!this.a(itemstack1, 1, 37, true))
					return null;
			} else {
				if (((Slot) c.get(0)).hasItem() || !((Slot) c.get(0)).isAllowed(itemstack1))
					return null;

				if (itemstack1.hasTag() && itemstack1.count == 1) {
					((Slot) c.get(0)).set(itemstack1.cloneItemStack());
					itemstack1.count = 0;
				} else if (itemstack1.count >= 1) {
					// Spigot start
					ItemStack clone = itemstack1.cloneItemStack();
					clone.count = 1;
					((Slot) c.get(0)).set(clone);
					// Spigot end
					--itemstack1.count;
				}
			}

			if (itemstack1.count == 0) {
				slot.set((ItemStack) null);
			} else {
				slot.f();
			}

			if (itemstack1.count == itemstack.count)
				return null;

			slot.a(entityhuman, itemstack1);
		}

		return itemstack;
	}

	// CraftBukkit start
	@Override
	public CraftInventoryView getBukkitView() {
		if (bukkitEntity != null)
			return bukkitEntity;

		CraftInventoryEnchanting inventory = new CraftInventoryEnchanting(enchantSlots);
		bukkitEntity = new CraftInventoryView(player, inventory, this);
		return bukkitEntity;
	}
	// CraftBukkit end
}
