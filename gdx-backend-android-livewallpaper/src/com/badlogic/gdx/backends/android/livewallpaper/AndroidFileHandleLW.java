/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 *  Modified by Elijah Cornell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.android.livewallpaper;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public class AndroidFileHandleLW extends FileHandle {
	// The asset manager, or null if this is not an internal file.
	final AssetManager assets;

	AndroidFileHandleLW (AssetManager assets, String fileName, FileType type) {
		super(fileName, type);
		this.assets = assets;
	}

	AndroidFileHandleLW (AssetManager assets, File file, FileType type) {
		super(file, type);
		this.assets = assets;
	}

	public FileHandle child (String name) {
		if (file.getPath().length() == 0) return new AndroidFileHandleLW(assets, new File(name), type);
		return new AndroidFileHandleLW(assets, new File(file, name), type);
	}

	public FileHandle parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new AndroidFileHandleLW(assets, parent, type);
	}

	public InputStream read () {
		if (type == FileType.Internal) {
			try {
				return assets.open(file.getPath());
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading file: " + file + " (" + type + ")", ex);
			}
		}
		return super.read();
	}

	public FileHandle[] list () {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				for (int i = 0, n = handles.length; i < n; i++)
					handles[i] = new AndroidFileHandleLW(assets, new File(file, relativePaths[i]), type);
				return handles;
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list();
	}

	public boolean isDirectory () {
		if (type == FileType.Internal) {
			try {
				return assets.list(file.getPath()).length > 0;
			} catch (IOException ex) {
				return false;
			}
		}
		return super.isDirectory();
	}

	public boolean exists () {
		if (type == FileType.Internal) {
			String fileName = file.getPath();
			try {
				assets.open(fileName).close(); // Check if file exists.
				return true;
			} catch (Exception ex) {
				try {
					return assets.list(fileName).length > 0;
				} catch (Exception ignored) {
					return false;
				}
			}
		}
		return super.exists();
	}
}

