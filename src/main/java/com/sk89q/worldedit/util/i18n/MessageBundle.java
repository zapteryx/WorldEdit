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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class that interacts with a {@code LocaleManager} to localize strings.
 *
 * @see LocaleManager stores current locales
 */
public class MessageBundle {

    private static final Logger log = Logger.getLogger(MessageBundle.class.getCanonicalName());
    private final String baseName;
    private final ClassLoader classLoader;

    /**
     * Create a new instance, using the class loader that was loaded the given
     * class.
     *
     * @param baseName the bundle base name
     * @param clazz the class with the desired class loader
     */
    public MessageBundle(String baseName, Class<?> clazz) {
        this(baseName, clazz.getClassLoader());
    }

    /**
     * Create a new instance.
     *
     * @param baseName the bundle base name
     * @param classLoader the class loader
     */
    public MessageBundle(String baseName, ClassLoader classLoader) {
        checkNotNull(baseName);
        checkNotNull(classLoader);
        this.baseName = baseName;
        this.classLoader = classLoader;
    }

    /**
     * Translate a string.
     *
     * <p>If the string is not available, then ${key} will be returned.</p>
     *
     * @param key the key
     * @return the translated string
     */
    public String _(String key) {
        ResourceBundle bundle = LocaleManager.getBundleForThread(baseName, classLoader);
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                log.log(Level.WARNING, "Failed to find message", e); //NON-NLS
            }
        }

        return "${" + key + "}";
    }

    /**
     * Format a translated string.
     *
     * <p>If the string is not available, then ${key}:args will be returned.</p>
     *
     * @param key the key
     * @param args arguments
     * @return a translated string
     */
    public String _(String key, Object... args) {
        ResourceBundle bundle = LocaleManager.getBundleForThread(baseName, classLoader);
        if (bundle != null) {
            try {
                MessageFormat formatter = new MessageFormat(_(key));
                formatter.setLocale(bundle.getLocale());
                return formatter.format(args);
            } catch (MissingResourceException e) {
                log.log(Level.WARNING, "Failed to find message", e); //NON-NLS
            }
        }

        return "${" + key + "}:" + Arrays.toString(args);
    }


}
