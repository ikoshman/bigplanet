package com.nevilon.moow.core;

import java.io.Serializable;

/**
 * Представляет параметры тайла
 * 
 * @author hudvin
 * 
 */
public class RawTile implements Serializable{

	public int x, y, z;

	public RawTile(){
		x = 0;
		y = 0;
		z = 16;
	}
	
	public RawTile(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
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
		return true;
	}

	@Override
	public String toString() {
		return x + " : " + y + " : " + z;
	}

}
