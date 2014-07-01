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

package com.sk89q.worldedit.util.i18n;

import java.util.Locale;
import java.util.ResourceBundle.Control;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An extension of {@link Control} that returns a pre-set fallback
 * locale (rather than {@link Locale#getDefault()}).
 */
class ManagedControl extends Control {

    private final Locale defaultLocale;

    /**
     * Create a new instance.
     *
     * @param defaultLocale the default locale to return
     */
    ManagedControl(Locale defaultLocale) {
        checkNotNull(defaultLocale);
        this.defaultLocale = defaultLocale;
    }

    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
        checkNotNull(baseName);
        return locale.equals(defaultLocale) ? null : defaultLocale;
    }
}
