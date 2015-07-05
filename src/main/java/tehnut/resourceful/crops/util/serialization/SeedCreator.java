package tehnut.resourceful.crops.util.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.common.registry.GameData;
import org.apache.commons.io.filefilter.FileFilterUtils;
import tehnut.resourceful.crops.ResourcefulCrops;
import tehnut.resourceful.crops.base.Seed;
import tehnut.resourceful.crops.base.SeedReq;
import tehnut.resourceful.crops.base.SeedReqBuilder;
import tehnut.resourceful.crops.registry.SeedRegistry;
import tehnut.resourceful.crops.util.BlockStack;
import tehnut.resourceful.crops.util.helper.JsonHelper;
import tehnut.resourceful.crops.base.SeedBuilder;
import tehnut.resourceful.crops.util.Utils;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SeedCreator {

    public static void registerJsonSeeds(GsonBuilder gsonBuilder, File folder) {
        File[] files = folder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        for (File file : files)
            SeedCreator.createSeedsFromJson(gsonBuilder, file);
    }

    public static void registerJsonSeeds(GsonBuilder gsonBuilder) {
        File folder = new File(ResourcefulCrops.getConfigDir().getPath() + "/seeds");

        File[] files = folder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        for (File file : files)
            SeedCreator.createSeedsFromJson(gsonBuilder, file);
    }

    @SuppressWarnings("unchecked")
    public static List<Seed> createSeedsFromJson(GsonBuilder gsonBuilder, File file) {
        try {
            Gson gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
            return gson.fromJson(new FileReader(file), ArrayList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void createJsonFromSeeds(GsonBuilder gsonBuilder, List list, String fileName) {
        try {
            Gson gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
            String reverse = gson.toJson(list, List.class);
            FileWriter fw = new FileWriter(new File(ResourcefulCrops.getConfigDir().getPath() + "/seeds", fileName + ".json"));
            fw.write("{\n\"seeds\": " + reverse + "\n}");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createJsonFromSeeds(GsonBuilder gsonBuilder, List list) {
        createJsonFromSeeds(gsonBuilder, list, "PrintedSeeds");
    }

    public static void registerCustomSerializers(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(ArrayList.class, new CustomListJson());
        gsonBuilder.registerTypeAdapter(Seed.class, new CustomSeedJson());
        gsonBuilder.registerTypeAdapter(BlockStack.class, new CustomBlockStackJson());
        gsonBuilder.registerTypeAdapter(SeedReq.class, new CustomSeedReqJson());
    }

    public static class CustomSeedJson implements JsonDeserializer<Seed>, JsonSerializer<Seed> {

        @Override
        public Seed deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonHelper helper = new JsonHelper(json);

            String name = helper.getString("name");
            int tier = helper.getNullableInteger("tier", 1);
            int amount = helper.getNullableInteger("amount", 1);
            String input = helper.getString("input");
            String output = helper.getString("output");
            String color = helper.getString("color");
            SeedReq seedReq = new SeedReqBuilder().build();
            if (json.getAsJsonObject().get("seedReq") != null)
                seedReq = context.deserialize(json.getAsJsonObject().get("seedReq"), new TypeToken<SeedReq>() { }.getType());

            SeedBuilder builder = new SeedBuilder();
            builder.setName(name);
            builder.setTier(tier);
            builder.setAmount(amount);
            builder.setInput(input);
            builder.setOutput(Utils.parseItemStack(output, false));
            builder.setColor(Color.decode(color));
            builder.setSeedReq(seedReq);

            return builder.build();
        }

        @Override
        public JsonElement serialize(Seed src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", src.getName());
            jsonObject.addProperty("tier", src.getTier());
            jsonObject.addProperty("amount", src.getAmount());
            jsonObject.addProperty("input", src.getInput());
            jsonObject.addProperty("output", Utils.itemStackToString(src.getOutput()));
            jsonObject.addProperty("color", "#" + Integer.toHexString(src.getColor().getRGB()).substring(2).toUpperCase());
            if (!isSeedReqDefault(src.getSeedReq()))
                jsonObject.add("seedReq", context.serialize(src.getSeedReq()));

            return jsonObject;
        }

        /**
         * Used to determine whether to add the seedReq
         * field to the JSON printing.
         *
         * @param seedReq - SeedReq to check if default
         * @return        - Whether the given SeedReq is default
         */
        private static boolean isSeedReqDefault(SeedReq seedReq) {
            return seedReq.getGrowthReq() == null && seedReq.getLightLevelMin() == 9 && seedReq.getLightLevelMax() == Short.MAX_VALUE;
        }
    }

    public static class CustomBlockStackJson implements JsonDeserializer<BlockStack>, JsonSerializer<BlockStack> {

        @Override
        public BlockStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonHelper helper = new JsonHelper(json);

            String name = helper.getString("name");
            int meta = helper.getNullableInteger("meta", 0);

            return new BlockStack(GameData.getBlockRegistry().containsKey(name) ? GameData.getBlockRegistry().getObject(name).getStateFromMeta(meta) : null);
        }

        @Override
        public JsonElement serialize(BlockStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", GameData.getBlockRegistry().getNameForObject(src.getBlock()).toString());
            jsonObject.addProperty("meta", src.getMeta());

            return jsonObject;
        }
    }

    public static class CustomSeedReqJson implements JsonDeserializer<SeedReq>, JsonSerializer<SeedReq> {

        @Override
        public SeedReq deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonHelper helper = new JsonHelper(json);

            BlockStack blockStack = context.deserialize(json.getAsJsonObject().get("blockStack"), new TypeToken<BlockStack>() { }.getType());
            int lightLevelMin = helper.getNullableInteger("lightLevelMin", 9);
            int lightLevelMax = helper.getNullableInteger("lightLevelMax", Integer.MAX_VALUE);

            SeedReqBuilder builder = new SeedReqBuilder();
            builder.setGrowthReq(blockStack);
            builder.setLightLevelMin(lightLevelMin);
            builder.setLightLevelMax(lightLevelMax);

            return builder.build();
        }

        @Override
        public JsonElement serialize(SeedReq src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (src.getGrowthReq() != null)
                jsonObject.add("blockStack", context.serialize(src.getGrowthReq()));
            if (src.getLightLevelMin() != 9)
                jsonObject.addProperty("lightLevelMin", src.getLightLevelMin());
            if (src.getLightLevelMax() != Integer.MAX_VALUE)
                jsonObject.addProperty("lightLevelMax", src.getLightLevelMax());

            return jsonObject;
        }
    }

    public static class CustomListJson implements JsonDeserializer<List<Seed>>, JsonSerializer<List<Seed>> {

        @Override
        public List<Seed> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<Seed> list = context.deserialize(json.getAsJsonObject().get("seeds"), new TypeToken<List<Seed>>() {
            }.getType());

            for (Seed seed : list)
                SeedRegistry.registerSeed(seed);

            return list;
        }

        @Override
        public JsonElement serialize(List<Seed> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("seeds", context.serialize(SeedRegistry.getSeedList()));

            return jsonObject;
        }
    }
}
