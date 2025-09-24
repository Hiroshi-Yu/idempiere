/***********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 **********************************************************************/
package cn.idempiere.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.compiere.model.MSysConfig;
import org.compiere.util.Env;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;

/**
 * Tools for Chinese localization.
 * @author Yang.Yu
 */
public final class LCN {
	// LCN-ICZI84
	public static void importCSVBefore(InputStream filestream,Charset charset) {
		if (isCJKV() && isUsedBOM(charset)) {
			try {
				byte[] fileHeader = {(byte)0xFF,(byte)0xFF,(byte)0xFF};
				filestream.mark(0);
					filestream.read(fileHeader, 0, 3);
				if ( !LCN.isHasBOM(fileHeader, charset) )
					filestream.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	// LCN-ICZI84
	public static void exportCSVBefore(FileOutputStream fileOut,Charset charset) {
		if (isCJKV() && isUsedBOM(charset)) {
			try {
				if (mapUnicodeBOM.containsKey(charset))
					fileOut.write(UnicodeBOM(charset));
				else if (Charsets.UTF_16.equals(charset)) 
					fileOut.write(UnicodeBOM(Charsets.UTF_16LE));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	// ---------------------------------------------------------------------	
	private static boolean isCJKV() {
		return setCJK.contains(Env.getAD_Language(Env.getCtx()));
	}

	private static boolean isUsedBOM(Charset charset) {
		return MSysConfig.getBooleanValue(LCN_HAS_BOM, true, Env.getAD_Client_ID(Env.getCtx()))
				&& mapUnicodeBOM.containsKey(charset);
	}

	private static boolean isHasBOM(byte[] fileHeader,Charset charset) {
		boolean result = false;
		if (mapUnicodeBOM.containsKey(charset))
			result = isMatched(fileHeader,UnicodeBOM(charset));
		else if (Charsets.UTF_16.equals(charset)) 
			result = isMatched(fileHeader,UnicodeBOM(Charsets.UTF_16LE)) || isMatched(fileHeader,UnicodeBOM(Charsets.UTF_16BE));
        return result;
	}

	private static final String LCN_HAS_BOM = "LCN_HAS_BOM";
   
	private static Set<String> setCJK = Sets.newHashSet("zh_CN", "ja_JP", "ko_KR", "vi_VN", "zh_TW", "zh_HK", "zh_SG");
	private static Map<Charset,byte[]> mapUnicodeBOM = Map.of(
			Charsets.UTF_16LE,new byte[]{(byte) 0xFF, (byte) 0xFE},
			Charsets.UTF_16BE,new byte[]{(byte) 0xFE, (byte) 0xFF},
			Charsets.UTF_8,new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}
			//Charsets.UTF32Little = { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 };
			//Charsets.UTF32Big = { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF };
		);

	private static boolean isMatched(byte[] data,byte[] bom) {
        boolean result = true;
        for (int i = 0; i < bom.length; i++)
        {
            if (bom[i] != data[i]) {
                result = false;
                break;
            }
        }
        return result;
	}

	private static byte[] UnicodeBOM(Charset charset) {
		return mapUnicodeBOM.get(charset);
	}
}
