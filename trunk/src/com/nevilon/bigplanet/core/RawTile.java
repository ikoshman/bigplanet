package com.nevilon.bigplanet.core;

import java.io.Serializable;


/**
 * Представляет параметры тайла
 * 
 * @author hudvin
 * 
 */
public class RawTile implements Serializable {

	public int x, y, z, s,l;


	public RawTile(int x, int y, int z, int s, int l) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.s = s;
		this.l = l;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		result = prime * result + s;
		result = prime * result + l;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RawTile))
			return false;
		RawTile other = (RawTile) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		if (s != other.s)
			return false;
		if (l != other.l)
			return false;

		return true;
	}

	@Override
	public String toString() {
		String path = s+"/"+z+"/"+x+"/"+y+"/";
		return path;
	}

}
