package pictisoft.cipherwright.blocks.kubejstable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.server.IntegratedServer;
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
    public static final int WELL_PARAMETER_SYNC = 6;
    public static final int CIPHER_REMOVE_NBT = 7;
    public static final int CIPHER_ADJUSTMENT = 8;
    public static final int CIPHER_CHANGE_TO_TAG = 9;
    public static final int CIPHER_CHANGE_TO_FLUID_TAG = 10;
    public static final int ORIGINAL_RECIPE_CLEAR_ON_SERVER = 20;
    public static final int RECIPE_CLEAR = 40;
    public static final int SET_ITEM_FROM_JEI_DROP = 50;
    public static final int SET_FLUID_FROM_JEI_DROP = 51;
    public static final int PARAMETER_SYNC = 90;
    public static final int RECIPE_TYPE_SCROLLBOX = 99;
    public static final int CHANGE_COMMENT_PARAMETER = 199;
    public static final int CHANGE_WEAKNBT_PARAMETER = 198;
    public static final int CHANGE_FORMATTING_PARAMETER = 197;
    public static final int CHANGE_REMOVERECIPE_PARAMETER = 196;
    public static final int HANDLE_JSON_AS_RECIPE = 599;
    //public static final int CHANGE_SETTING_BASE=170;

    public static final int RUN_COMMAND = 1000;
    public static final int RUN_COMMAND_RELOAD = 0;

    private ArrayList<CipherSlot> _slots = new ArrayList<>();

    // #START_SYNCED VALUES
    private ResourceLocation _originalRecipeID; // SYNCED // The original recipe ID to replace... the id to use for a "remove recipe" code snip
    private ResourceLocation _recipeTypeID; // SYNCED // found cipher
    private Map<String, String> _parameterValues = new LinkedHashMap<>(); // SYNCED // the text boxes containing custom values
    private boolean _includeComments;
    private boolean _includeWeakNBT;
    private boolean _formatCode;
    private boolean _removeRecipe;
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
        // we MUST assume that _recipeTypeID is set to a cipher that exists...
        if (CipherJsonLoader.getCipherByRecipeId(_recipeTypeID) == null)
        {
            return CipherJsonLoader.getDefaultCipher();
        }
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
        if (tag.contains("includeWeakNBT"))
        {
            _includeWeakNBT = tag.getBoolean("includeWeakNBT");
        }
        if (tag.contains("formatCode"))
        {
            _formatCode = tag.getBoolean("formatCode");
        }
        if (tag.contains("removeRecipe"))
        {
            _removeRecipe = tag.getBoolean("removeRecipe");
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
        tag.putBoolean("includeWeakNBT", _includeWeakNBT);
        tag.putBoolean("formatCode", _formatCode);
        tag.putBoolean("removeRecipe", _removeRecipe);
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
                  "type": "bloodmagic:arc",
                  "addedoutput": [
                    {
                      "type": {
                        "item": "minecraft:string"
                      },
                      "chance": 0.5,
                      "mainchance": 0.0
                    },
                    {
                      "type": {
                        "count": 4,
                        "item": "minecraft:netherite_scrap"
                      },
                      "chance": 0.0,
                      "mainchance": 1.0
                    }
                  ],
                  "consumeingredient": false,
                  "input": {
                    "tag": "forge:cobblestone"
                  },
                  "inputsize": 2,
                  "mainoutputchance": 0.0,
                  "output": {
                    "count": 4,
                    "item": "minecraft:gold_ingot"
                  },
                  "tool": {
                    "item": "minecraft:shears"
                  }
                }
                """;
//        return """
//                {
//                  "type": "create:deploying",
//                  "ingredients": [
//                    {
//                      "item": "minecraft:exposed_copper"
//                    },
//                    {
//                      "tag": "minecraft:axes"
//                    }
//                  ],
//                  "keepHeldItem": true,
//                  "results": [
//                    {
//                      "item": "minecraft:copper_block"
//                    }
//                  ]
//                }
//                """;
//        return """
//                {
//                  "type": "botania:runic_altar",
//                  "ingredients": [
//                    {
//                      "tag": "minecraft:logs_that_burn"
//                    },
//                    {
//                      "item": "minecraft:cobblestone"
//                    },
//                    {
//                      "item": "minecraft:stick"
//                    },
//                    {
//                      "item": "minecraft:egg"
//                    }
//                  ],
//                  "mana": 12000,
//                  "output": {
//                    "item": "minecraft:cooked_beef"
//                  }
//                }
//                """;
    }

    String json2()
    {
        var sample1 =
                """
                        {
                          "type": "create:crushing",
                          "ingredients": [
                            {
                              "item": "minecraft:amethyst_block"
                            }
                          ],
                          "processingTime": 150,
                          "results": [
                            {
                              "chance": 0.75,
                              "count": 3,
                              "item": "minecraft:amethyst_shard"
                            },
                            {
                              "chance": 0.5,
                              "item": "minecraft:amethyst_shard"
                            },
                            {
                              "chance": 0.05,
                              "item": "minecraft:diamond"
                            }
                          ]
                        }
                        """;
        var sample2 = """
                {
                  "type": "create:deploying",
                  "ingredients": [
                    {
                      "item": "minecraft:stick"
                    },
                    {
                      "tag": "minecraft:planks"
                    }
                  ],
                  "results": [
                    {
                      "item": "minecraft:furnace"
                    }
                  ]
                }""";
        var sample3 = """
                {
                  "type": "create:sandpaper_polishing",
                  "ingredients": [
                    {
                      "item": "minecraft:redstone"
                    }
                  ],
                  "results": [
                    {
                      "item": "minecraft:diamond"
                    }
                  ]
                }""";
        var sample4 = """
                {
                  "type": "create:compacting",
                  "ingredients": [
                    {
                      "amount": 250,
                      "fluidTag": "cipherwright:dead"
                    }
                  ],
                  "results": [
                    {
                      "item": "minecraft:honey_block"
                    }
                  ]
                }""";
        return sample4;
    }

    String json3()
    {
        var aa = """
                {
                  "type": "create:mechanical_crafting",
                  "acceptMirrored": false,
                  "key": {
                    "A": {
                      "item": "minecraft:cobblestone"
                    },
                    "P": {
                      "tag": "minecraft:planks"
                    },
                    "S": {
                      "tag": "forge:stone"
                    }
                  },
                  "pattern": [
                    " AAA ",
                    "AAPAA",
                    "APSPA",
                    "AAPAA",
                    " AAA "
                  ],
                  "result": {
                    "count": 2,
                    "item": "minecraft:blast_furnace"
                  }
                }
                """;
        var t = """
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
        var tt = """
                {
                  "type": "create:emptying",
                  "ingredients": [
                    {
                      "item": "minecraft:honey_bottle"
                    }
                  ],
                  "results": [
                    {
                      "item": "minecraft:glass_bottle"
                    },
                    {
                      "amount": 250,
                      "fluid": "cipherwright:dead_water_source"
                    }
                  ]
                }
                """;
        var ttt = """
                {
                  "type": "create:filling",
                  "ingredients": [
                    {
                      "item": "minecraft:dirt"
                    },
                    {
                      "amount": 500,
                      "fluid": "minecraft:water",
                      "nbt": {}
                    }
                  ],
                  "results": [
                    {
                      "item": "minecraft:grass_block"
                    }
                  ]
                }""";
        var tttt = """
                {
                  "type": "create:haunting",
                  "ingredients": [
                    {
                      "tag": "forge:gems/lapis"
                    }
                  ],
                  "results": [
                    {
                      "chance": 0.75,
                      "item": "minecraft:prismarine_shard"
                    },
                    {
                      "chance": 0.125,
                      "item": "minecraft:prismarine_crystals"
                    }
                  ]
                }
                """;
        return aa;
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
        if (controlId == CHANGE_WEAKNBT_PARAMETER)
        {
            this._includeWeakNBT = !this._includeWeakNBT;
            updateBlock();
        }
        if (controlId == CHANGE_FORMATTING_PARAMETER)
        {
            this._formatCode = !this._formatCode;
            updateBlock();
        }
        if (controlId == CHANGE_REMOVERECIPE_PARAMETER)
        {
            this._removeRecipe = !this._removeRecipe;
            updateBlock();
        }
        if (controlId == ORIGINAL_RECIPE_CLEAR_ON_SERVER)
        {
            this.setOriginalRecipe(null);
        }
        if (controlId == RECIPE_CLEAR)
        {
            if (value == 91) // test buttons
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
                // actually clear
                Cipher.clearSlots(this.getCipherSlots());
                CipherParameter.clearParameters(_parameterValues, getCipher());
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
        if (controlId == HANDLE_JSON_AS_RECIPE)
        {
            handleRecipe(value, null);
            updateBlock();
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
            if (getLevel().getServer() != null && getLevel().getServer() instanceof IntegratedServer clientserver)
            {
                var jsonRecipeString = RecipeJsonFetcher.getRecipeJson(clientserver, recipeId);
                if (recipe.isPresent() && jsonRecipeString != null)
                {
                    handleRecipe(jsonRecipeString, recipeId);
                }
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

    public void handleRecipe(String jsonRecipeString, @Nullable ResourceLocation recipeId)
    {
        if (this.level != null && !this.level.isClientSide())
        {
            JsonElement jsonRoot = JsonParser.parseString(jsonRecipeString);
            if (jsonRoot.isJsonObject())
            {
                Cipher.clearSlots(this.getCipherSlots());
                CipherParameter.clearParameters(_parameterValues, getCipher());
                JsonObject json = jsonRoot.getAsJsonObject();
                // must have the "type" or it is ignored
                if (json.has("type"))
                {
                    var possibletype = new ResourceLocation(json.get("type").getAsString());
                    var cipher = CipherJsonLoader.getCipherByRecipeId(possibletype);
                    if (cipher != null && !cipher.getInputs().isEmpty())
                    {
                        Cipher.clearSlots(this.getCipherSlots());
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
                _includeComments, _includeWeakNBT, _formatCode, _removeRecipe));
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
                        case WELL_PARAMETER_SYNC:
                            r.setWellParameter(string);
                            updateBlock();
                            break;
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
                        case CIPHER_CHANGE_TO_FLUID_TAG:
                            r.setFluidTagKey(TagKey.create(Registries.FLUID, new ResourceLocation(string)));
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

    public boolean areWeakNBTIncluded()
    {
        return _includeWeakNBT;
    }

    public boolean areCodeFormatted()
    {
        return _formatCode;
    }

    public boolean areRemoveRecipe()
    {
        return _removeRecipe;
    }

}