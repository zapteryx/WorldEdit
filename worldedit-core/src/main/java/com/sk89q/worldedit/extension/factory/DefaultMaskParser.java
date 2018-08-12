/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BiomeMask2D;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.ExpressionMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.NoiseFilter;
import com.sk89q.worldedit.function.mask.OffsetMask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.session.request.RequestSelection;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.Biomes;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultMaskParser extends FaweParser<Mask> {
    private final Dispatcher dispatcher;
    private final Pattern INTERSECTION_PATTERN = Pattern.compile("[&|;]+(?![^\\[]*\\])");

    public DefaultMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
        this.dispatcher = new SimpleDispatcher();
        this.register(new MaskCommands(worldEdit));
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void register(Object clazz) {
        ParametricBuilder builder = new ParametricBuilder();
        builder.setAuthorizer(new ActorAuthorizer());
        builder.addBinding(new WorldEditBinding(worldEdit));
        builder.registerMethodsAsCommands(dispatcher, clazz);
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        if (input.isEmpty()) return null;
        Extent extent = Request.request().getExtent();
        if (extent == null) extent = context.getExtent();
//        List<Mask> intersection = new ArrayList<>();
//        List<Mask> union = new ArrayList<>();
        List<List<Mask>> masks = new ArrayList<>();
        masks.add(new ArrayList<>());

        final CommandLocals locals = new CommandLocals();
        Actor actor = context != null ? context.getActor() : null;
        if (actor != null) {
            locals.put(Actor.class, actor);
        }
        //
        try {
            List<Map.Entry<ParseEntry, List<String>>> parsed = parse(input);
            for (Map.Entry<ParseEntry, List<String>> entry : parsed) {
                ParseEntry pe = entry.getKey();
                String command = pe.input;
                Mask mask = null;
                if (command.isEmpty()) {
                    mask = parseFromInput(StringMan.join(entry.getValue(), ','), context);
                } else if (dispatcher.get(command) == null) {
                    // Legacy patterns
                    char char0 = command.charAt(0);
                    boolean charMask = input.length() > 1 && input.charAt(1) != '[';
                    if (charMask && input.charAt(0) == '=') {
                        return parseFromInput(char0 + "[" + input.substring(1) + "]", context);
                    }
                    if (mask == null) {
                        // Legacy syntax
                        if (charMask) {
                            switch (char0) {
                                case '\\': //
                                case '/': //
                                case '{': //
                                case '$': //
                                case '%': {
                                    command = command.substring(1);
                                    String value = command + ((entry.getValue().isEmpty()) ? "" : "[" + StringMan.join(entry.getValue(), "][") + "]");
                                    if (value.contains(":")) {
                                        if (value.charAt(0) == ':') value.replaceFirst(":", "");
                                        value = value.replaceAll(":", "][");
                                    }
                                    mask = parseFromInput(char0 + "[" + value + "]", context);
                                    break;
                                }
                                case '|':
                                case '~':
                                case '<':
                                case '>':
                                case '!':
                                    input = input.substring(input.indexOf(char0) + 1);
                                    mask = parseFromInput(char0 + "[" + input + "]", context);
                                    if (actor != null) {
                                        BBC.COMMAND_CLARIFYING_BRACKET.send(actor, char0 + "[" + input + "]");
                                    }
                                    return mask;
                            }
                        }
                        if (mask == null) {
                            if (command.startsWith("[")) {
                                int end = command.lastIndexOf(']');
                                mask = parseFromInput(command.substring(1, end == -1 ? command.length() : end), context);
                            } else {
                                List<String> entries = entry.getValue();
                                BlockMaskBuilder builder = new BlockMaskBuilder().addRegex(pe.full);
                                if (builder.isEmpty()) {
                                    try {
                                        context.setPreferringWildcard(true);
                                        context.setRestricted(false);
                                        BlockStateHolder block = worldEdit.getBlockFactory().parseFromInput(pe.full, context);
                                        builder.add(block);
                                    } catch (NoMatchException e) {
                                        throw new NoMatchException(e.getMessage() + " See: //masks");
                                    }
                                }
                                mask = builder.build(extent);
                            }
                        }
                    }
                } else {
                    List<String> args = entry.getValue();
                    if (!args.isEmpty()) {
                        command += " " + StringMan.join(args, " ");
                    }
                    mask = (Mask) dispatcher.call(command, locals, new String[0]);
                }
                if (pe.and) {
                    masks.add(new ArrayList<>());
                }
                masks.get(masks.size() - 1).add(mask);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new InputParseException(e.getMessage(), e);
        }
        List<Mask> maskUnions = new ArrayList<>();
        for (List<Mask> maskList : masks) {
            if (maskList.size() == 1) {
                maskUnions.add(maskList.get(0));
            } else if (maskList.size() != 0) {
                maskUnions.add(new MaskUnion(maskList));
            }
        }
        if (maskUnions.size() == 1) {
            return maskUnions.get(0);
        } else if (maskUnions.size() != 0) {
            return new MaskIntersection(maskUnions);
        } else {
            return null;
        }
    }


}
