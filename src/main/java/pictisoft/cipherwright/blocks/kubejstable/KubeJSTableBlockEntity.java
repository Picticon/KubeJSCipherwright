package pictisoft.cipherwright.blocks.kubejstable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import pictisoft.cipherwright.cipher.*;
import pictisoft.cipherwright.network.*;
import pictisoft.cipherwright.registry.BlockEntityRegistry;
import pictisoft.cipherwright.util.Chatter;
import pictisoft.cipherwright.util.ItemAndIngredientHelpers;
import pictisoft.cipherwright.util.RecipeJsonFetcher;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlock.KUBEJS_TABLE_TITLE;

public class KubeJSTableBlockEntity extends BlockEntity implements MenuProvider,
        RecipeMessage.IHandleRecipeMessage, EntityToServerIntIntMessage.IToServerIntIntMessageHandler,
        EntityToServerIntStringMessage.IToServerIntStringMessageHandler,
        EntityToServerIntStringStringMessage.IToServerIntStringStringMessageHandler,
        EntityC2SCipherCommand.IHandleEntityC2SMessage,
        EntityToServerIntIntMessage.IToServerIntIntMessageHandlerWithPlayer
{
    public static final int RECIPE_TYPE_SCROLLBOX = 99;
    public static final int RECIPE_CLEAR = 50;
    public static final int PARAMETER_SYNC = 90;
    public static final int ORIGINAL_RECIPE_CLEAR_ON_SERVER = 51;
    public static final int CIPHER_REMOVE_NBT = 7;
    public static final int CIPHER_ADJUSTMENT = 8;
    public static final int CIPHER_CHANGE_TO_TAG = 9;
    public static final int CHANGE_COMMENT_PARAMETER = 199;
    public static final int RUN_COMMAND = 100;
    public static final int RUN_COMMAND_RELOAD = 0;
    public static final int SET_ITEM_FROM_JEI_DROP = 50;
    public static final int SET_FLUID_FROM_JEI_DROP = 51;
    private static final int MAX_PARAMETERS = 20;
    //    protected final ItemStackHandler itemHandler = createHandler();
//    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    //private boolean _removeCheck; // the user wants to create the remove recipe fragment.

    private ArrayList<CipherSlot> _slots = new ArrayList<>();

    // #START_SYNCED VALUES
    private ResourceLocation _originalRecipeID; // SYNCED // The original recipe ID to replace... the id to use for a "remove recipe" code snip
    private ResourceLocation _recipeTypeID; // SYNCED // found cipher
    private Map<String, String> _parameterValues = new LinkedHashMap<>(); // SYNCED // the text boxes containing custom values
    private boolean _includeComments;
    // #END_SYNCED_VALUES

    public KubeJSTableBlockEntity(BlockPos pPos, BlockState pState)
    {
        super(BlockEntityRegistry.KUBEJS_TABLE_BLOCK_ENTITY.get(), pPos, pState);
        setRecipeTypeAndRebuildCipherSlots(new ResourceLocation("minecraft:crafting_shaped"));
    }

    // remake slots based on the current recipe
    public void rebuildCipherSlots()
    {
        //Chatter.chatDebug(getLevel(), "rebuildCipherSlots(%s)".formatted(_recipeTypeID));
        _slots.clear();
        _parameterValues.clear();
        for (int i = 0; i < getCipher().getSlotCount(); i++)
        {
            CipherSlot slot = new CipherSlot((CipherWell) getCipher().getSlot(i));
            _slots.add(slot);
        }
        for (var p : getCipher().getParameters())
        {
            _parameterValues.put(p.getPath(), p.getDefaultValue());
            //Chatter.chat("Set " + p.getPath() + " to " + p.getDefaultValue());
        }
    }

    public ArrayList<CipherSlot> getCipherSlots()
    {
        return _slots;
    }

    public Cipher getCipher()
    {
        return CipherJsonLoader.getCipherByRecipeId(_recipeTypeID);
    }


    // Change the recipe type layout (cipher) e.g. "minecraft:smelting", "minecraft:crafting_shapeless"
    private void setRecipeTypeAndRebuildCipherSlots(ResourceLocation recipeTypeId)
    {
        //Chatter.chatDebug(getLevel(), "setRecipeTypeAndRebuildCipherSlots(%s)".formatted(recipeTypeId));
        if (recipeTypeId.equals(_recipeTypeID)) return;
        _recipeTypeID = recipeTypeId;
        rebuildCipherSlots();
        updateBlock();
        notifyClients();
    }

    // notify the client this block changed.
    public void updateBlock()
    {
        if (level != null && !level.isClientSide)
        {
            //Chatter.chat("updateBlock()");
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            setChanged();
            //sendSyncMessage();
        }
    }

//    private void sendSyncMessage()
//    {
//        if (this.level != null && this.level instanceof ServerLevel slevel)
//        {
//            // ON-SERVER
//            if (!_recipeTypeID.equals(_lastSentRecipeTypeID))
//            {
//                _lastSentRecipeTypeID = _recipeTypeID;
//                MessageRegistry.sendToClientsNear(this.getBlockPos(), this.level, new ReopenContainerPacket(this.getBlockPos()));
//            }
//            MessageRegistry.sendToClientsNear(getBlockPos(), slevel, new SyncParameterTrigger(this));
//        }
//    }

    public ResourceLocation getOriginalRecipeID()
    {
        return _originalRecipeID;
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        //Chatter.chatDebug(getLevel(), "load(%s)".formatted(_recipeTypeID));
        if (tag.contains("recipeTypeId"))
        {
            // the following will return if already set
            setRecipeTypeAndRebuildCipherSlots(new ResourceLocation(tag.getString("recipeTypeId")));
        }

        _originalRecipeID = null;
        if (tag.contains("originalRecipeId"))
        {
            var r = tag.getString("originalRecipeId");
            if (r.isBlank()) _originalRecipeID = null;
            else _originalRecipeID = new ResourceLocation(r);
        }

        // Load slot data
        ListTag slotsTag = tag.getList("Slots", 10); // 10 = CompoundTag
        for (int i = 0; i < slotsTag.size(); i++)
        {
            CompoundTag slotTag = slotsTag.getCompound(i);
            if (_slots.size() > i)
            {
                _slots.get(i).loadExtra(slotTag);
            }
        }

        // load parameters
        _parameterValues.clear();
        if (tag.contains("cipherParameters"))
        {
            var mapTag = tag.getCompound("cipherParameters");
            for (String key : mapTag.getAllKeys())
            {
                _parameterValues.put(key, mapTag.getString(key));
            }
        }
        if (tag.contains("includeComments"))
        {
            _includeComments = tag.getBoolean("includeComments");
        }
    }

    // This is to save to disk, but is also used for update packets
    @Override
    public void saveAdditional(CompoundTag tag)
    {
        //Chatter.chatDebug(getLevel(), "saveAdditional(%s)".formatted(_recipeTypeID));
        tag.putString("recipeTypeId", _recipeTypeID.toString());
        tag.putString("originalRecipeId", _originalRecipeID == null ? "" : _originalRecipeID.toString());

        // Save slot data
        ListTag slotsTag = new ListTag();
        for (var slot : _slots)
        {
            CompoundTag slotTag = new CompoundTag();
            slot.saveExtra(slotTag);
            slotsTag.add(slotTag);
        }
        tag.put("Slots", slotsTag);

        CompoundTag mapTag = new CompoundTag();
        for (var kvp : _parameterValues.entrySet())
        {
            mapTag.putString(kvp.getKey(), kvp.getValue());
        }
        tag.put("cipherParameters", mapTag);
        tag.putBoolean("includeComments", _includeComments);
    }

    // server::getUpdatePacket() -> server::getUpdateTag() -> client::onDataPacket() -> client::handleUpdateTag

    // required to update client with state
    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        //Chatter.chat("getUpdatePacket " + ((level == null) ? "null" : (level.isClientSide ? "client" : "server")));
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // builds update data packet
    @Override
    public CompoundTag getUpdateTag()
    {
        //Chatter.chat("getUpdateTag " + ((level == null) ? "null" : (level.isClientSide ? "client" : "server")));
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    // (on client side, from server)
    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        //Chatter.chat("handleUpdateTag " + ((level == null) ? "null" : (level.isClientSide ? "client" : "server")));
        load(tag);
    }

    // I think this is when client receives packet
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        //Chatter.chat("onDataPacket " + ((level == null) ? "null" : (level.isClientSide ? "client" : "server")));
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag() == null ? new CompoundTag() : pkt.getTag());
    }

    private String json1()
    {
        return """
                {
                  "type": "botania:runic_altar",
                  "ingredients": [
                    {
                      "tag": "minecraft:logs_that_burn"
                    },
                    {
                      "item": "minecraft:cobblestone"
                    },
                    {
                      "item": "minecraft:stick"
                    },
                    {
                      "item": "minecraft:egg"
                    }
                  ],
                  "mana": 12000,
                  "output": {
                    "item": "minecraft:cooked_beef"
                  }
                }
                """;
    }

    String json2()
    {
        return """
                {'type':'minecraft:crafting_shaped','key':{"A":{"item":"minecraft:diamond_sword","nbt":{"Damage":500}}},'pattern':["AAA","AAA","AAA"],'result':{"item":"minecraft:crimson_fence","count":3}}
                """;
    }

    String json3()
    {
        return """
                {
                   "type": "create:compacting",
                   "ingredients": [
                     {
                       "item": "minecraft:oak_wood"
                     },
                     {
                       "fluid": "cipherwright:dead_water_source",
                       "amount": 1000
                     }
                   ],
                   "results": [
                     {
                       "item": "minecraft:dead_bush",
                       "count": 4
                     },
                     {
                       "fluid": "cipherwright:dead_water_source",
                       "amount": 500
                     }
                   ],
                   "heatRequirement": "heated"
                 }
                
                """;
//        return """
//                {
//                  "type": "botania:elven_trade",
//                  "ingredients": [
//                    {
//                      "item": "minecraft:iron_ingot"
//                    },
//                    {
//                      "item": "minecraft:iron_ingot"
//                    }
//                  ],
//                  "output": [
//                    {
//                      "item": "minecraft:diamond"
//                    }
//                  ]
//                }
//                """;
    }

    @Override
    public void handleFromClientIntInt(int controlId, int value, ServerPlayer player)
    {
        if (controlId == RUN_COMMAND)
        {
            if (value == RUN_COMMAND_RELOAD)
            {
                MinecraftServer server = player.getServer();
                if (server != null)
                {
                    CommandSourceStack source = player.createCommandSourceStack()
                            .withPermission(4) // same as OP permission level
                            .withSuppressedOutput(); // if you don’t want spam in chat

                    server.getCommands().performPrefixedCommand(source, "reload");
                }
            }
        }
    }

    @Override
    public void handleFromClientIntInt(int controlId, int value)
    {
        if (controlId == CHANGE_COMMENT_PARAMETER)
        {
            this._includeComments = !this._includeComments;
            updateBlock();
        }
        if (controlId == ORIGINAL_RECIPE_CLEAR_ON_SERVER)
        {
            this.setOriginalRecipe(null);
        }
        if (controlId == RECIPE_CLEAR)
        {
            if (value == 91)
            {
                handleRecipe(json1(), null);
            } else if (value == 92)
            {
                handleRecipe(json2(), null);
            } else if (value == 93)
            {
                handleRecipe(json3(), null);
            } else
            {

                Cipher.clearSlots(this.getCipherSlots());
            }
            updateBlock();
        }
    }

    // TELL SERVER NEW RECIPE TYPE:
    @Override
    public void handleFromClientIntString(int controlId, String value)
    {
        //Chatter.chatDebug(getLevel(), "handleFromClientIntString(%s)".formatted(value));
        if (controlId == RECIPE_TYPE_SCROLLBOX)
        {
            for (var k : CipherJsonLoader.getCiphers().entrySet())
            {
                if (Objects.equals(value, k.getValue().getRecipeTypeId().toString()))
                {
                    setRecipeTypeAndRebuildCipherSlots(k.getValue().getRecipeTypeId());
                }
            }
        }
    }

    @Override // remove from level
    public void setRemoved()
    {
        super.setRemoved();
        //handler.invalidate();
    }

    // Sent from JEI
    @Override
    public void handleRecipe(ResourceLocation recipeId)
    {
        // CipherJsonLoader.getCipherByRecipeId()

        if (!level.isClientSide) // SERVER
        {
            RecipeManager recipeManager = this.level.getRecipeManager();
            Optional<? extends Recipe<?>> recipe = recipeManager.byKey(recipeId);

            // json is the original recipe json declaration
            var jsonRecipeString = RecipeJsonFetcher.getRecipeJson(getLevel().getServer(), recipeId);
            if (recipe.isPresent() && jsonRecipeString != null)
            {
                handleRecipe(jsonRecipeString, recipeId);
            }
        }
        updateBlock();
    }

    // this sends SERVER->CLIENT
    public void setOriginalRecipe(ResourceLocation recipeId)
    {
        if (this.level != null && !this.level.isClientSide)
        {
            _originalRecipeID = recipeId;
            updateBlock();
        }
    }

    private void handleRecipe(String jsonRecipeString, ResourceLocation recipeId)
    {
        if (this.level != null && !this.level.isClientSide())
        {
            JsonElement jsonRoot = JsonParser.parseString(jsonRecipeString);
            if (jsonRoot.isJsonObject())
            {
                JsonObject json = jsonRoot.getAsJsonObject();
                // must have the "type" or it is ignored
                if (json.has("type"))
                {
                    var possibletype = new ResourceLocation(json.get("type").getAsString());
                    var cipher = CipherJsonLoader.getCipherByRecipeId(possibletype);
                    if (!cipher.getInputs().isEmpty())
                    {
                        setRecipeTypeAndRebuildCipherSlots(cipher.getRecipeTypeId());
                        setOriginalRecipe(recipeId);
                        getCipher().handleRecipe(json, this.getCipherSlots(), _parameterValues); // loads parameter values
                        updateBlock();
                    }
                }
            }
        }
    }


    private void notifyClients()
    {
        if (this.level instanceof ServerLevel serverLevel)
        {
            //Chatter.chatDebug(getLevel(), "notifyClients(%s)");
            for (ServerPlayer player : serverLevel.players())
            {
                if (player.containerMenu instanceof KubeJSTableContainer menu)
                {
                    if (this == menu.getBlockEntity())
                    {

                        CompletableFuture.runAsync(() -> {
                            player.closeContainer();
                            player.nextContainerCounter();
                        }).thenRunAsync(() -> {
                            try
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                Thread.currentThread().interrupt();
                            }
                            player.getServer().execute(() -> {
                                NetworkHooks.openScreen(player, this, this.getBlockPos());
                            });
                        });

//                        MinecraftServer server = player.server;
//                        player.closeContainer();
//                        player.nextContainerCounter();
//                        server.execute(() -> {
//                            NetworkHooks.openScreen(player, this, this.getBlockPos());
//                        });
                    }
                }
            }
        }
    }

    public Map<String, String> getCipherParameters()
    {
        return _parameterValues;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable(KUBEJS_TABLE_TITLE);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer)
    {
        return new KubeJSTableContainer(pContainerId, this.getBlockPos(), pPlayerInventory, pPlayer);
    }

    public void clipboardTemplate(CipherTemplate t)
    {
        // template
        // need to gather the slots and the parameters. Settings?
        // run through snippets, check condition
        // run through fragments, parse
        // send to clipboard

        t.clipboard(new CipherTemplate.DataLoad(getCipher(), _originalRecipeID == null ? null : _originalRecipeID.toString(), _slots, _parameterValues,
                _includeComments));
    }

    @Override
    public void handleIntStringStringFromClient(int controlId, String value1, String value2)
    {
        if (controlId == SET_ITEM_FROM_JEI_DROP)
        {
            for (var slot : getCipherSlots())
            {
                if (slot.getSerialKey().equals(value1))
                {
                    var is = ItemAndIngredientHelpers.jsonToItemStack(value2);
                    slot.setItem(is);
                    updateBlock();
                }
            }
        }
        if (controlId == SET_FLUID_FROM_JEI_DROP)
        {
            for (var slot : getCipherSlots())
            {
                if (slot.getSerialKey().equals(value1))
                {
                    var fluidStack = ItemAndIngredientHelpers.jsonToFluidStack(value2);
                    if (slot.canBeFluid())
                        slot.setFluid(fluidStack);
                    updateBlock();
                }
            }

        }
        if (controlId == PARAMETER_SYNC)
        {
            if (_parameterValues.containsKey(value1))
            {
                _parameterValues.put(value1, value2);
                updateBlock();
            }
        }
    }

    @Override
    public void handleC2SMessageForCipher(String serial, int command, String string)
    {
        for (var r : getCipherSlots())
        {
            if (r.getSerialKey().equals(serial))
            {
                try
                {
                    switch (command)
                    {
                        case CIPHER_ADJUSTMENT:
                            r.adjustCount(Integer.parseInt(string));
                            updateBlock();
                            break;
                        case CIPHER_REMOVE_NBT:
                            r.getItemStack().removeTagKey(string);
                            updateBlock();
                            break;
                        case CIPHER_CHANGE_TO_TAG:
                            r.setTagKey(TagKey.create(Registries.ITEM, new ResourceLocation(string)));
                            updateBlock();
                            break;
                    }
                } catch (Exception ignored)
                {
                    Chatter.chat("Error during cipher network message.");
                }
            }
        }
    }

    public boolean areCommentsIncluded()
    {
        return _includeComments;
    }

}