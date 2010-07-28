/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;

/**
 * Class which serializes/deserializes objects into/from byte arrays.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class CDKObjectHandler {

	/**
	 * Serializes an object into a byte array.
	 */
	public static byte[] getBytes(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
		bos.close();
		byte[] data = bos.toByteArray();
		return data;
	}

	/**
	 * Serializes an objects into a list of byte arrays.
	 */
	public static List<byte[]> getBytesList(List<Object> objs) throws IOException {
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		for (Object obj : objs) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();
			byte[] data = bos.toByteArray();
			list.add(data);
		}
		return list;
	}

	/**
	 * Serializes an objects into a list of byte arrays.
	 */
	public static List<byte[]> getBytesList(Object[] objs) throws IOException {
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		for (Object obj : objs) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();
			byte[] data = bos.toByteArray();
			list.add(data);
		}
		return list;
	}

	/**
	 * Deserializes a byte array into an object.
	 */
	public static Object getObject(byte[] data) throws Exception {
		Object object = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		object = ois.readObject();
		ois.close();
		bis.close();
		return object;
	}

	public static List<CMLChemFile> getChemFileList(List<byte[]> dataArray) throws Exception {
		ArrayList<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		for (byte[] data : dataArray) {
			Object obj = null;
			try {
				obj = CDKObjectHandler.getObject(data);
			} catch (Exception e) {
				throw new Exception(CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
			if (obj instanceof CMLChemFile) {
				List<CMLChemFile> list = CMLChemFileWrapper.wrapInChemModelList((CMLChemFile) obj);
				chemFileList.addAll(list);
			} else {
				throw new Exception(CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
		}
		return chemFileList;
	}

	public static List<Reaction> getReactionList(List<byte[]> dataArray) throws Exception {
		ArrayList<Reaction> reactionList = new ArrayList<Reaction>();
		for (byte[] data : dataArray) {
			Object obj = null;
			try {
				obj = CDKObjectHandler.getObject(data);
			} catch (Exception e) {
				throw new Exception(CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
			if (obj instanceof Reaction) {
				reactionList.add((Reaction) obj);
			} else {
				throw new Exception(CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
		}
		return reactionList;
	}

}
