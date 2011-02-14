/*
 * Copyright (C) 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna.basicutilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.UUID;

/**
 * Class which controls the caching process.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class CacheController {

	private static final long MAX_FILESIZE = 4294967295L;
	private static CacheController instance = null;

	HashMap<UUID, CacheObject> cacheMap = new HashMap<UUID, CacheObject>();
	UUID currentFileID = null;

	private CacheController() {

	}

	/**
	 * @return CacheController instance.
	 */
	public static synchronized CacheController getInstance() {
		if (instance == null) {
			instance = new CacheController();
		}
		return instance;
	}

	/**
	 * @return Retrieves the current cache file.
	 */
	private File getCurrentCacheFile() {
		String filename = FileNameGenerator.getCacheDir();
		if (this.currentFileID == null) {
			this.currentFileID = UUID.randomUUID();
		}
		filename += File.separator + this.currentFileID.toString();
		filename += ".cache";
		return new File(filename);
	}

	/**
	 * Generates a new cache file.
	 * 
	 * @return The new cache file
	 */
	private File getNewCacheFile() {
		String filename = FileNameGenerator.getCacheDir();
		this.currentFileID = UUID.randomUUID();
		filename += File.separator + this.currentFileID.toString();
		filename += ".cache";
		return new File(filename);
	}

	/**
	 * Retrieves a cache file from its UUID.
	 * 
	 * @param uuid
	 *            UUID of the cache file.
	 * @return The cache fule.
	 */
	private File getCacheFileByID(UUID uuid) {
		String filename = FileNameGenerator.getCacheDir();
		filename += File.separator + uuid.toString();
		filename += ".cache";
		return new File(filename);
	}

	/**
	 * Caches the given data on the file system.
	 * 
	 * @param data
	 *            The data to be cached.
	 * @return UUID of the cached data.
	 * @throws Exception
	 */
	public synchronized UUID cacheByteStream(byte[] data) throws Exception {
		// Get cache file
		File cacheFile = this.getCurrentCacheFile();
		long newSize = cacheFile.length() + data.length;
		if (newSize >= MAX_FILESIZE) {
			cacheFile = getNewCacheFile();
		}
		CacheObject cacheObj = new CacheObject();
		cacheObj.offset = cacheFile.length();
		cacheObj.size = data.length;
		cacheObj.fileID = this.currentFileID;
		UUID dataID = UUID.randomUUID();
		FileOutputStream fos = new FileOutputStream(cacheFile, true);
		fos.write(data);
		fos.flush();
		fos.close();
		this.cacheMap.put(dataID, cacheObj);
		return dataID;
	}

	/**
	 * Retrieves cached data from file system.
	 * 
	 * @param uuid
	 *            UUID of the cached data.
	 * @return The cached data.
	 * @throws Exception
	 */
	public synchronized byte[] uncacheByteStream(UUID uuid) throws Exception {
		CacheObject cacheObj = this.cacheMap.get(uuid);
		File cacheFile = this.getCacheFileByID(cacheObj.fileID);
		byte[] data = new byte[cacheObj.size];
		RandomAccessFile raf = new RandomAccessFile(cacheFile, "r");
		raf.seek(cacheObj.offset);
		raf.read(data);
		raf.close();
		return data;
	}
}
