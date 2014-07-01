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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the loading of {@link ResourceBundle}s based off the same
 * bundle base name.
 */
public class LocaleManager {

    private static final Logger log = Logger.getLogger(LocaleManager.class.getCanonicalName());
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();
    private final String bundleName;
    private final ManagedControl control;
    private ClassLoader classLoader = LocaleManager.class.getClassLoader();

    /**
     * Create a new instance of the locale manager.
     *
     * @param bundleName the bundle name
     * @param defaultLocale the default locale to use if no the requested locale is not found
     */
    public LocaleManager(String bundleName, Locale defaultLocale) {
        checkNotNull(bundleName);
        checkNotNull(defaultLocale);
        this.control = new ManagedControl(defaultLocale);
        this.bundleName = bundleName;
    }

    /**
     * Get the class loader to use to load bundles.
     *
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Set the class loader to load bundles.
     *
     * @param classLoader the class loader
     */
    public void setClassLoader(ClassLoader classLoader) {
        checkNotNull(classLoader);
        this.classLoader = classLoader;
    }

    @Nullable
    public ResourceBundle getBundleForLocale(Locale locale) {
        // When Bukkit decides to use a newer version of Guava, then we
        // can use Guava's cache features

        ResourceBundle bundle;

        // First try to read from the cache
        try {
            readLock.lock();
            bundle = bundles.get(locale);
            if (bundle != null) {
                return bundle;
            }
        } finally {
            readLock.unlock();
        }

        // Try loading the bundle
        bundle = ResourceBundle.getBundle(bundleName, locale, classLoader, control);

        // Save to cache
        try {
            writeLock.lock();
            bundles.put(locale, bundle);
            return bundle;
        } catch (MissingResourceException e) {
            log.log(Level.WARNING, "Failed to load resource bundle", e); //NON-NLS
            return null;
        } finally {
            writeLock.unlock();
        }
    }

}
