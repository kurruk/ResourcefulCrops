package tehnut.resourceful.crops.core.data;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import tehnut.resourceful.crops.core.RegistrarResourcefulCrops;
import tehnut.resourceful.crops.item.ItemResourceful;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public final class SeedStack {

    private final ItemResourceful type;
    private final ResourceLocation seed;
    private final int amount;

    public SeedStack(@Nonnull ItemResourceful type, @Nonnull ResourceLocation seed, @Nonnegative int amount) {
        Preconditions.checkNotNull(type, "Item cannot be null");
        Preconditions.checkNotNull(seed, "Seed name cannot be null");
        Preconditions.checkArgument(amount >= 0, "Amount cannot be negative");

        this.type = type;
        this.seed = seed;
        this.amount = amount;
    }

    public SeedStack(ItemResourceful type, ResourceLocation seed) {
        this(type, seed, 1);
    }

    public SeedStack(ItemResourceful type, Seed seed, int amount) {
        this(type, seed.getRegistryName(), amount);
    }

    public SeedStack(ItemResourceful type, Seed seed) {
        this(type, seed, 1);
    }

    public ItemResourceful getType() {
        return type;
    }

    public ResourceLocation getSeedKey() {
        return seed;
    }

    public Seed getSeed() {
        return RegistrarResourcefulCrops.SEEDS.getValue(seed);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeedStack)) return false;

        SeedStack seedStack = (SeedStack) o;

        if (amount != seedStack.amount) return false;
        if (type != null ? !type.equals(seedStack.type) : seedStack.type != null) return false;
        return seed != null ? seed.equals(seedStack.seed) : seedStack.seed == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (seed != null ? seed.hashCode() : 0);
        result = 31 * result + amount;
        return result;
    }
}
